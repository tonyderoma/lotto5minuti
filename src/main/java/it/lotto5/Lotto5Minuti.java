package it.lotto5;

import it.eng.pilot.Color;
import it.eng.pilot.PDate;
import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;
import it.lotto5.dto.*;
import org.apache.log4j.BasicConfigurator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Lotto5Minuti extends PilotSupport {
    public static final String FREQUENZE = "frequenze.txt";
    public final PDate inizioElaborazione = now();
    public static final String FREQUENZE_PRECEDENTI = "frequenzePrecedenti.txt";
    public static final String AMPIEZZA_FREQUENZE = "ampiezzaFrequenze.txt";
    public static final String AMPIEZZA_FREQUENZE_PRECEDENTI = "ampiezzaFrequenzePrecedenti.txt";


    public static final String REPORT = "REPORT.TXT";

    public static final Integer maxNumeriSviluppati = 12;
    public static final String FREQ = "freq";
    public static final String AMPIEZZA = "ampiezza";

    public static final String NUMERO = "numero";

    private final PList<Integer> L56 = pl(5, 6);
    private final PList<Integer> L46 = pl(4, 5, 6);
    private final PList<Integer> L36 = pl(3, 4, 5, 6);
    private final PList<Integer> L26 = pl(2, 3, 4, 5, 6);


    private final PList<Integer> L25 = pl(2, 3, 4, 5);
    private final PList<Integer> L35 = pl(3, 4, 5);
    private final PList<Integer> L24 = pl(2, 3, 4);

    public static boolean limitaSviluppati = true;
    public Integer bilancioFinale = 0;
    public Integer vincitaFinale = 0;

    public Integer spesaFinale = 0;
    private Integer ultimeEstrazioni = 1;//limito la verifica alle ultime 10 estrazioni più recenti
    //private  PList<Integer> giocata = pl(Lotto5Minuti.generaGiocata(7,1,10));
    private PList<Integer> giocata = pl();

    private PList<PList<Integer>> giocateMultiple = pl();

    private PList<Giocata> giocate = pl();

    private String oggi = now().toStringFormat("yyyy-MM-dd");
    private String ieri = ieri().toStringFormat("yyyy-MM-dd");
    private String altroIeri = giorniFa(2).toStringFormat("yyyy-MM-dd");
    private String unaSettimanaFa = settimaneFa(1).toStringFormat("yyyy-MM-dd");
    private String dueSettimaneFa = settimaneFa(2).toStringFormat("yyyy-MM-dd");
    private String treSettimaneFa = settimaneFa(3).toStringFormat("yyyy-MM-dd");
    private String unMeseFa = mesiFa(1).toStringFormat("yyyy-MM-dd");
    private String giornoDaScaricare = null;
    private Boolean oro = false;
    private Boolean doppioOro = false;
    private final Boolean extra = false;

    PList<Estrazione5Minuti> estrazioni = pl();


    private static final String FILE = "lotto5minuti.txt";
    private static final String URL = "https://www.lottologia.com/10elotto5minuti/archivio-estrazioni/?as=TXT&date=";
    private static final int BUFFER_SIZE = 4096;
    private Map<Integer, PList<Vincita>> vincite = new HashMap<Integer, PList<Vincita>>();

    public static void main(String[] args) throws Exception {
        Lotto5Minuti l = new Lotto5Minuti();
        BasicConfigurator.configure();
        l.autorun();
        //l.run();
        //l.downloadEstrazioni(2);
    }

    private void run() throws Exception {
        loadGiocate();
        download();
        init();
        loadEstrazioni();
        elaboraFrequenze();
        execute();
        stampaCadenze(4);
        stampaConsecutivi(6);
    }


    private void autorun() throws Exception {
        svuotaFile(AMPIEZZA_FREQUENZE, AMPIEZZA_FREQUENZE_PRECEDENTI, FREQUENZE, FREQUENZE_PRECEDENTI);
        Integer oraStop = 24;
        Integer minutiStop = 00;
        Integer oraStart = 03;
        Integer minutiStart = 0;
        PDate stop = now().ora(oraStop).minuti(minutiStop);
        PDate start = now().ora(oraStart).minuti(minutiStart);
        vincitaFinale = 0;
        spesaFinale = 0;
        bilancioFinale = 0;
        while (true) {
            if (now().isBefore(start)) {
                attendiMinuti(5);
                continue;
            }
            if (now().isAfter(stop)) break;
            attendiSecondi(10);
            if (now().getMinuti() % 5 == 0) {
                log(lfn(2));
                log("Scattati i 5 minuti ", now().getOraCompleta(), " procedo con l'elaborazione!!!!!");
                attendiSecondi(10);
                run();
                giocateMultiple = pl();
                giocata = pl();
                giocate = pl();
                loadGiocate();
                attendiMinuti(1);
            }
        }
        PList<String> out = pl();
        out.add(str("VINCITA:", money(bd(vincitaFinale))));
        out.add(str("SPESA:", money(bd(spesaFinale))));
        out.add(str("BILANCIO:", money(bd(bilancioFinale))));
        appendFile(REPORT, out);
    }

    private PList<Integer> trovaAmpiezzeEstratte(PList<Integer> freqs, PList<Ampiezza> ampiezze) {
        PList<Integer> ampiezzeEstratte = pl();
        freqs.forEach(f -> {
            try {
                ampiezzeEstratte.add(ampiezze.eq(FREQ, f).findOne().getAmpiezza());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return ampiezzeEstratte.sort();
    }


    private void elaboraFrequenze() throws Exception {
        if (estrazioni.size() <= 1) return;
        salvaFrequenze();
        PList<Frequenza> fre = leggiFrequenze(FREQUENZE);
        PList<Frequenza> frequenzePrecedenti = leggiFrequenze(FREQUENZE_PRECEDENTI);
        PList<Ampiezza> ampiezze = leggiAmpiezze(AMPIEZZA_FREQUENZE);
        PList<Ampiezza> ampiezzePrecedenti = leggiAmpiezze(AMPIEZZA_FREQUENZE_PRECEDENTI);

        PList<Report> report = pl();
        Parametri p = new Parametri(report, frequenzePrecedenti, ampiezzePrecedenti);
        PList<Integer> ultimaEstrazione = estrazioni.getFirstElement().getEstrazione();
        PList<Integer> penultimaEstrazione = estrazioni.get(1).getEstrazione();
        PList<Integer> inComune = estrazioni.getFirstElement().getEstrazione().intersection(penultimaEstrazione);
        log(inComune.size(), "NUMERI IN COMUNE CON L'ESTRAZIONE PRECEDENTE", inComune.concatenaDash());
        PList<Integer> frequenzeEstratte = calcolaFrequenzeEstratte(fre, ultimaEstrazione);
        PList<Integer> frequenzeEstrattePrecedenti = calcolaFrequenzeEstratte(frequenzePrecedenti, penultimaEstrazione);

        log("Frequenze estratte precedenti:", tab(), frequenzeEstrattePrecedenti.concatenaDash());
        //log("Ampiezze  estratte precedenti:", tab(), ampiezzePrecedenti.in(FREQ, frequenzeEstrattePrecedenti.distinct()).find().sort(AMPIEZZA).narrow(AMPIEZZA).concatenaDash());
        log("Ampiezze  estratte precedenti:", tab(), trovaAmpiezzeEstratte(frequenzeEstrattePrecedenti, ampiezzePrecedenti).concatenaDash());

        log("Frequenze estratte attuali:   ", tab(), frequenzeEstratte.concatenaDash());
        log("Ampiezze  estratte attuali:   ", tab(), trovaAmpiezzeEstratte(frequenzeEstratte, ampiezze).concatenaDash());

        log("INTERVALLO DI FREQUENZE:      ", quadra(), fre.min(FREQ).getFreq(), comma(), fre.max(FREQ).getFreq(), quadraClose());
        //PList<Integer> frequenzeBuone = ampiezze.gt("quantiIntercettati", 0).find().sort("freq").narrow("freq");
        //PList<Integer> frequenzeBuoneSelezionate = ampiezze.between("quantiIntercettati", 1, 5).between("ampiezza", ampiezzaMinima, ampiezzaMassima).find().sort("freq").narrow("freq");
        System.out.println(color("Estrazione " + estrazioni.getFirstElement().getDataString(), Color.BIANCO, true, true, false, false) + tab() + color(estrazioni.getFirstElement().getEstrazione().concatenaDash(), Color.VIOLA, true, true, false, false));
        //modoGiocoAmpiezzeBasse(ampiezze, report, fre);
        //modoGiocoAmpiezzeTra(2, 3, ampiezze, report, fre, false);
        // modoGiocoFrequenzePuntuali(pl(33, 25, 26), report, fre, false);

        //modoGiocoFrequenzeCasuali(3, fre, report, false);
        //modoGiocoFrequenzeCasuali(3, fre, report, false);
        // modoGiocoTipoFrequenze(ampiezze, report, fre);


        //modoGiocoAmpiezzeBasse(L25, p);
        modoGiocoAmpiezzeBasse(L25, p, true);
        /*modoGiocoAmpiezzePuntuali(L25, pl(2), p, false, true);
        modoGiocoAmpiezzePuntuali(L25, pl(3), p, false, true);*/


        //modoGiocoAmpiezzePuntuali(L25, pl(7), p, false, true);
        //modoGiocoFrequenzeRandomDaAmpiezze(p, 6);
        //modoGiocoFrequenzeRandomDaAmpiezzeTra(p, 4, 6);
        //modoGiocoFrequenzeSingoleDaAmpiezzePuntuali(p, 2, false);
        modoGiocoFrequenzeSingoleDaAmpiezzePuntuali(p, 3, true);
        modoGiocoFrequenzeSingoleDaAmpiezzePuntuali(p, 4, true);
        modoGiocoFrequenzeSingoleDaAmpiezzePuntuali(p, 5, true);
        //modoGiocoVerticaliDaAlte(p, 7);
        /*modoGiocoNumericoRandom(p, 5);
        modoGiocoNumericoRandom(p, 6);
        modoGiocoNumericoRandom(p, 4);
        modoGiocoNumericoRandom(p, 7);
        modoGiocoNumericoRandom(p, 8);
        modoGiocoNumericoRandom(p, 3);
        modoGiocoNumericoRandom(p, 2);*/
        //modoGiocoAmpiezzePuntuali(L25, pl(4), p, false, true);
        //modoGiocoAmpiezzePuntuali(L25, pl(5), p, false, true);
        //modoGiocoPosizionale(frequenzeEstrattePrecedenti, report, fre, false, true);
        //PList<Integer> frequenzeResidue = ampiezze.in(FREQ, calcolaFrequenzeResidue(report, fre)).between(AMPIEZZA, 3, 5).find().narrowDistinct(FREQ);
        //modoGiocoFrequenzePuntuali(frequenzeResidue, report, fre, false, true);
        //modoGiocoExtraRandom(5, report);
        //modoGiocoAmpiezzeAlte(L25, p);
        //giocaResidui(p, 8);
        //giocaResidui(p, 10);
        //giocaResidui(p, 6);
        //modoGiocoCadenze(pl(1, 3, 5, 7, 9), p);
        printReport(p);
    }


    private PList<Integer> calcolaFrequenzeEstratte(PList<Frequenza> fre, PList<Integer> estrazione) throws Exception {
        PList<Integer> frequenzeEstratte = pl();
        if (notNull(fre))
            for (Integer i : estrazione) {
                Frequenza f = fre.eq(NUMERO, i).findOne();
                if (Null(f)) frequenzeEstratte.add(0);
                else
                    frequenzeEstratte.add(f.getFreq());
            }
        return frequenzeEstratte.sort();
    }

    //Modo gioco che considera i numeri corrispondenti all'intervallo di ampiezze indicato
    private void modoGiocoAmpiezzeTra(PList<Integer> lunghezzeAmmesse, Integer minAmp, Integer maxAmp, Parametri p, boolean togliNumeriEstrazionePrecedente) throws Exception {
        PList<Integer> numeri = getNumeriAmpiezzeTra(minAmp, maxAmp, p, togliNumeriEstrazionePrecedente);
        giocaNumeri(TipoGiocata.AMPIEZZE_TRA, numeri, lunghezzeAmmesse, 3);
    }

    private void modoGiocoAmpiezzeTra(PList<Integer> lunghezzeAmmesse, Integer minAmp, Integer maxAmp, Parametri p, boolean togliNumeriEstrazionePrecedente, boolean pariDispari) throws Exception {
        if (!pariDispari)
            modoGiocoAmpiezzeTra(lunghezzeAmmesse, minAmp, maxAmp, p, togliNumeriEstrazionePrecedente);
        else {
            limitaSviluppati = false;
            PList<Integer> numeri = getNumeriAmpiezzeTra(minAmp, maxAmp, p, togliNumeriEstrazionePrecedente);
            giocaNumeriPariDispari(TipoGiocata.AMPIEZZE_TRA, numeri, lunghezzeAmmesse, 1);
            limitaSviluppati = true;
        }
    }

    //gioca numeri estratti da frequenze scelte a caso
    private void modoGiocoFrequenzeCasuali(PList<Integer> lunghezzeAmmesse, Integer quanteFrequenzeCasuali, Parametri p, boolean togliNumeriEstrazionePrecedente) throws Exception {
        PList<Integer> frequenze = p.getFrequenze().narrow(FREQ);
        modoGiocoFrequenzePuntuali(lunghezzeAmmesse, frequenze.random(quanteFrequenzeCasuali), p, togliNumeriEstrazionePrecedente);
    }

    private void modoGiocoFrequenzeTra(PList<Integer> lunghezzeAmmesse, Integer minFreq, Integer maxFreq, Parametri p, boolean togliNumeriEstrazionePrecedente) throws Exception {
        PList<Integer> numeri = getNumeriFrequenzeTra(minFreq, maxFreq, p, togliNumeriEstrazionePrecedente);
        giocaNumeri(TipoGiocata.FREQUENZE_TRA, numeri, lunghezzeAmmesse, 3);
    }

    private void modoGiocoFrequenzeTra(PList<Integer> lunghezzeAmmesse, Integer minFreq, Integer maxFreq, Parametri p, boolean togliNumeriEstrazionePrecedente, boolean pariDispari) throws Exception {
        if (!pariDispari)
            modoGiocoFrequenzeTra(lunghezzeAmmesse, minFreq, maxFreq, p, togliNumeriEstrazionePrecedente);
        else {
            limitaSviluppati = false;
            PList<Integer> numeri = getNumeriFrequenzeTra(minFreq, maxFreq, p, togliNumeriEstrazionePrecedente);
            giocaNumeriPariDispari(TipoGiocata.FREQUENZE_TRA, numeri, lunghezzeAmmesse, 1);
            limitaSviluppati = true;
        }

    }


    private PList<Integer> calcolaFrequenzeResidue(PList<Report> report, PList<Frequenza> fre) throws Exception {
        PList<Integer> frequenzeGiocate = pl();
        report.forEach(r -> frequenzeGiocate.addAll(r.getFrequenze()));
        return fre.notIn(FREQ, frequenzeGiocate.distinct()).find().sortDesc(FREQ).narrowDistinct(FREQ);
    }

    private void modoGiocoFrequenzePuntuali(PList<Integer> lunghezzeAmmesse, PList<Integer> freqs, Parametri p, boolean togliNumeriEstrazionePrecedente) throws Exception {
        PList<Integer> numeri = getNumeriFrequenzePuntuali(freqs, p, togliNumeriEstrazionePrecedente);
        giocaNumeri(TipoGiocata.FREQUENZE_PUNTUALI, numeri, lunghezzeAmmesse, 3);
    }

    private void modoGiocoFrequenzePuntuali(PList<Integer> lunghezzeAmmesse, PList<Integer> freqs, Parametri p, boolean togliNumeriEstrazionePrecedente, boolean pariDispari) throws Exception {
        if (!pariDispari)
            modoGiocoFrequenzePuntuali(lunghezzeAmmesse, freqs, p, togliNumeriEstrazionePrecedente);
        else {
            limitaSviluppati = false;
            PList<Integer> numeri = getNumeriFrequenzePuntuali(freqs, p, togliNumeriEstrazionePrecedente);
            limitaSviluppati = true;
            giocaNumeriPariDispari(TipoGiocata.FREQUENZE_PUNTUALI, numeri, lunghezzeAmmesse, 1);
        }
    }


    private void modoGiocoAmpiezzePuntuali(PList<Integer> lunghezzeAmmesse, PList<Integer> amps, Parametri p, boolean togliNumeriEstrazionePrecedente) throws Exception {
        PList<Integer> numeri = getNumeriAmpiezzePuntuali(amps, p, togliNumeriEstrazionePrecedente);
        giocaNumeri(TipoGiocata.AMPIEZZE_PUNTUALI, numeri, lunghezzeAmmesse, 3);
    }

    private void modoGiocoAmpiezzePuntuali(PList<Integer> lunghezzeAmmesse, PList<Integer> amps, Parametri p, boolean togliNumeriEstrazionePrecedente, boolean pariDispari) throws Exception {
        if (!pariDispari)
            modoGiocoAmpiezzePuntuali(lunghezzeAmmesse, amps, p, togliNumeriEstrazionePrecedente);
        else {
            limitaSviluppati = false;
            PList<Integer> numeri = getNumeriAmpiezzePuntuali(amps, p, togliNumeriEstrazionePrecedente);
            giocaNumeriPariDispari(TipoGiocata.AMPIEZZE_PUNTUALI, numeri, lunghezzeAmmesse, 1);
            limitaSviluppati = true;
        }
    }


    //Modo gioco che considera i primi numeri al massimo a partire dalle ampiezze 1
    private void modoGiocoAmpiezzeBasse(PList<Integer> lunghezzeAmmesse, Parametri p) throws Exception {
        PList<Integer> numeri = getNumeriDaAmpiezzeBasse(6, p, false);
        Integer quanteGiocate = giocaNumeri(TipoGiocata.AMPIEZZE_BASSE, numeri, lunghezzeAmmesse, 3);
        p.getReport().getLastElement().setCosto(quanteGiocate);
    }

    private void modoGiocoAmpiezzeAlte(PList<Integer> lunghezzeAmmesse, Parametri p) throws Exception {
        TipoGiocata g = TipoGiocata.AMPIEZZE_ALTE;
        PList<Integer> freqs = p.getAmpiezze().gt(AMPIEZZA, 6).find().random(2).narrowDistinct(FREQ);
        PList<Integer> numeri = p.getFrequenze().in(FREQ, freqs).find().narrowDistinct(NUMERO);
        p.getReport().add(new Report(g.getTipo(), freqs, numeri, trovaNumeriEstratti(numeri)));
        Integer quanteGiocate = giocaNumeriPariDispari(g, numeri, lunghezzeAmmesse, 1);
        p.getReport().getLastElement().setCosto(quanteGiocate);
    }


    private void modoGiocoFrequenzeRandomDaAmpiezze(Parametri p, Integer ampiezzaMaggioreDi) throws Exception {
        PList<Integer> freqs = p.getAmpiezze().gt(AMPIEZZA, ampiezzaMaggioreDi).find().narrowDistinct(FREQ);
        limitaSviluppati = false;
        PList<Integer> numeri = getNumeriFrequenzePuntuali(freqs.random(2), p, false);
        limitaSviluppati = true;
        Integer quanteGiocate = giocaNumeriPariDispari(TipoGiocata.FREQUENZE_PUNTUALI, numeri, L25, 1);
        p.getReport().getLastElement().setCosto(quanteGiocate);
    }


    private void modoGiocoFrequenzeSingoleDaAmpiezzePuntuali(Parametri p, Integer ampiezza, boolean giocaVerticali) throws Exception {
        PList<Integer> freqs = p.getAmpiezze().eq(AMPIEZZA, ampiezza).find().narrowDistinct(FREQ);
        limitaSviluppati = false;
        PList<Integer> vert = pl();
        giocaVerticali = tutte(giocaVerticali, freqs.size() > 1);
        for (Integer f : freqs) {
            PList<Integer> numeri = getNumeriFrequenzePuntuali(pl(f), p, false);
            if (is(ampiezza, 2, 3)) {
                Integer quanteGiocate = giocaNumeri(TipoGiocata.FREQUENZE_PUNTUALI, numeri, L25, 1);
                p.getReport().getLastElement().setCosto(quanteGiocate);
            } else {
                Integer quanteGiocate = giocaNumeriPariDispari(TipoGiocata.FREQUENZE_PUNTUALI, numeri, L25, 1);
                p.getReport().getLastElement().setCosto(quanteGiocate);
            }
            if (giocaVerticali && is(ampiezza, 3, 4, 5)) {
                PList<Integer> svil = p.getReport().getLastElement().getSviluppati();
                vert.add(svil.get(1));
                vert.add(svil.get(svil.size() - 2));
            }
            //giocaNumeriPariDispari(TipoGiocata.FREQUENZE_PUNTUALI, numeri);
        }
        vert = vert.distinct();
        if (giocaVerticali && is(ampiezza, 3, 4, 5)) {
            p.getReport().add(new Report(TipoGiocata.VERTICALI.getTipo(), freqs, vert, trovaNumeriEstratti(vert)));
            Integer quanteGiocate = giocaNumeriPariDispari(TipoGiocata.VERTICALI, vert, L25, 1);
            p.getReport().getLastElement().setCosto(quanteGiocate);
        }
        limitaSviluppati = true;
    }


    private void modoGiocoVerticaliDaAlte(Parametri p, Integer ampiezza) throws Exception {
        PList<Integer> freqs = p.getAmpiezze().gte(AMPIEZZA, ampiezza).find().narrowDistinct(FREQ);
        PList<Integer> vert = pl();
        for (Integer f : freqs) {
            PList<Integer> svil = p.getFrequenze().eq(FREQ, f).find().narrowDistinct(NUMERO);
            vert.add(svil.get(1));
            vert.add(svil.get(3));
            vert.add(svil.get(svil.size() - 2));
            vert.add(svil.get(svil.size() - 3));
        }
        vert = vert.distinct();
        p.getReport().add(new Report(TipoGiocata.VERTICALI.getTipo(), freqs, vert, trovaNumeriEstratti(vert)));
        Integer quanteGiocate = giocaNumeriPariDispari(TipoGiocata.VERTICALI, vert, L25, 1);
        p.getReport().getLastElement().setCosto(quanteGiocate);
    }

    private void modoGiocoNumericoRandom(Parametri p, Integer quanti) throws Exception {
        PList<Integer> numeri = pl();
        for (int i = 1; i <= quanti; i++) {
            Integer n = estrazioni.randomOne().getEstrazione().randomOne();
            if (numeri.contains(n)) {
                i--;
                continue;
            }
            numeri.add(n);
        }
        p.getReport().add(new Report(TipoGiocata.NUMERICO_RANDOM.getTipo(), pl(), numeri, trovaNumeriEstratti(numeri)));

        if (numeri.pari().size() >= 2) {
            addGiocata(TipoGiocata.NUMERICO_RANDOM.getTipo(), numeri.pari());
            giocateMultiple.add(numeri.pari());
        }
        if (numeri.dispari().size() >= 2) {
            addGiocata(TipoGiocata.NUMERICO_RANDOM.getTipo(), numeri.dispari());
            giocateMultiple.add(numeri.dispari());
        }

        addGiocata(TipoGiocata.NUMERICO_RANDOM.getTipo(), numeri);
        giocateMultiple.add(numeri);
    }

    private void modoGiocoFrequenzeRandomDaAmpiezzeTra(Parametri p, Integer ampiezzaMin, Integer ampiezzaMax) throws Exception {
        PList<Integer> freqs = p.getAmpiezze().between(AMPIEZZA, ampiezzaMin, ampiezzaMax).find().narrowDistinct(FREQ);
        limitaSviluppati = false;
        PList<Integer> numeri = getNumeriFrequenzePuntuali(freqs.random(3), p, false);
        limitaSviluppati = true;
        giocaNumeriPariDispari(TipoGiocata.FREQUENZE_PUNTUALI, numeri, L25, 1);
    }

    private void modoGiocoCadenze(PList<Integer> cadenzeAmmesse, Parametri p) throws Exception {
        TipoGiocata g = TipoGiocata.CADENZE;
        cadenzeAmmesse.forEach(c -> {
            PList<Integer> cadenze = generaCadenze(c, p.getTotaleSviluppati());
            p.getReport().add(new Report(g.getTipo() + c, cadenze, pl(intersection(cadenze, estrazioni.getFirstElement().getEstrazione()))));
            Integer quanteGiocate = giocaNumeri(g, cadenze, L24, 1);
            p.getReport().getLastElement().setCosto(quanteGiocate);
        });
    }

    private PList<Integer> generaCadenze(Integer n, PList<Integer> sviluppati) {
        PList<Integer> cad = pl();
        sviluppati.forEach(i -> {
            if (isCadenza(i, n)) cad.add(i);
        });

        return cad;
    }


    private boolean isCadenza(Integer n, Integer cadenza) {
        return zero((n - cadenza) % 10);
    }

    private void modoGiocoAmpiezzeBasse(PList<Integer> lunghezzeAmmesse, Parametri p, boolean pariDispari) throws Exception {
        if (!pariDispari) modoGiocoAmpiezzeBasse(lunghezzeAmmesse, p);
        else {
            PList<Integer> numeri = getNumeriDaAmpiezzeBasse(6, p, false);
            Integer quanteGiocate = giocaNumeriPariDispari(TipoGiocata.AMPIEZZE_BASSE, numeri, lunghezzeAmmesse, 1);
            p.getReport().getLastElement().setCosto(quanteGiocate);
        }
    }


    //Modalità di gioco frequenze prolifiche e non
    private void modoGiocoTipoFrequenze(PList<Integer> lunghezzeAmmesse, Parametri p) throws Exception {
        PList<Integer> frequenzeNonBuoneSelezionate = p.getAmpiezze().eq("quantiIntercettati", 0).gt(AMPIEZZA, 2).find().sort(FREQ).narrow(FREQ);
        PList<Integer> frequenzeProlifiche = p.getAmpiezze().sortDesc("quantiIntercettati").narrow(FREQ);
        PList<Integer> numeriDaFrequenzeProlifiche = getNumeriFrequenzePuntuali(frequenzeProlifiche.cutToFirst(3), p, false);
        PList<Integer> numeriDaFrequenzeNonBuone = getNumeriFrequenzePuntuali(frequenzeNonBuoneSelezionate, p, false);
        giocaNumeri(TipoGiocata.TIPO_FREQUENZE, numeriDaFrequenzeProlifiche, lunghezzeAmmesse, 3);
        giocaNumeri(TipoGiocata.TIPO_FREQUENZE, numeriDaFrequenzeNonBuone, lunghezzeAmmesse, 3);
    }

    //Modalità di gioco per posizione nell'array di frequenze estratte precedente bottom, medium , top
    private void modoGiocoPosizionale(PList<Integer> frequenzeEstrattePrecedenti, Parametri p, boolean togliNumeriEstrazionePrecedente, boolean pariDispari) throws Exception {
        Integer quanteFrequenzeDistinte = frequenzeEstrattePrecedenti.size();
        Integer posizioneMedia = quanteFrequenzeDistinte / 2 - 1;
        Integer low = posizioneMedia - 1;
        Integer high = posizioneMedia + 2;
        PList<Integer> intervalloFrequenze = pl(frequenzeEstrattePrecedenti.subList(low, high));
        modoGiocoFrequenzePuntuali(L36, intervalloFrequenze, p, togliNumeriEstrazionePrecedente, pariDispari);


        low = 1;
        high = 3;
        intervalloFrequenze = pl(frequenzeEstrattePrecedenti.subList(low, high));
        modoGiocoFrequenzePuntuali(L36, intervalloFrequenze, p, togliNumeriEstrazionePrecedente, pariDispari);


        low = quanteFrequenzeDistinte - 5;
        high = quanteFrequenzeDistinte - 2;
        intervalloFrequenze = pl(frequenzeEstrattePrecedenti.subList(low, high));
        modoGiocoFrequenzePuntuali(L36, intervalloFrequenze, p, togliNumeriEstrazionePrecedente, pariDispari);
    }

    private void printReport(Parametri p) {
        log(lf());
        p.getReport().forEach(System.out::println);
        PList<Integer> totaleIntercettati = p.getTotaleIntercettati();
        PList<Integer> totaleSviluppati = p.getTotaleSviluppati();
        String s = str("Totale: ", giallo(str(totaleIntercettati.size(), slash(), totaleSviluppati.size())), tab(), "Intercettati:", verde(totaleIntercettati.sort().concatenaDash()), "  Sviluppati:", bianco(totaleSviluppati.sort().concatenaDash()));
        System.out.println(s);
        log(lf());
    }


    private void giocaResidui(Parametri p, Integer quantiResidui) throws Exception {
        PList<Integer> totaleSviluppati = p.getTotaleSviluppati();
        PList<Integer> fib = getFibonacci();
        while (totaleSviluppati.size() > quantiResidui) {
            totaleSviluppati = totaleSviluppati.sottraiList(estrazioni.randomOne().getEstrazione().random(7));
        }
        p.getReport().add(new Report(TipoGiocata.RESIDUI.getTipo(), totaleSviluppati, trovaNumeriEstratti(totaleSviluppati)));
        giocaNumeriPariDispari(TipoGiocata.RESIDUI, totaleSviluppati, L25, 1);
        //giocaNumeri(totaleSviluppati, L25, 1);
    }

    public PList<Integer> trovaNumeriEstratti(PList<Integer> numeriSviluppati) throws Exception {
        return estrazioni.getFirstElement().getEstrazione().intersection(numeriSviluppati).sort();
    }

    private Integer giocaNumeri(TipoGiocata tipoGiocata, PList<Integer> numeri, PList<Integer> lunghezzeGiocate, int quantePerLunghezza) {
        Integer quanteGiocate = 0;
        if (numeri.size() < lunghezzeGiocate.min()) {
            return 0;
        }
        for (int i = 1; i <= quantePerLunghezza; i++) {
            quanteGiocate = quanteGiocate + lunghezzeGiocate.size();
            lunghezzeGiocate.forEach(l -> {
                addGiocata(tipoGiocata.getTipo(), numeri.random(l));
                giocateMultiple.add(numeri.random(l));

            });
        }
        return quanteGiocate;
    }

    private Integer giocaNumeri(TipoGiocata tipoGiocata, PList<Integer> numeri) {
        Integer quanteGiocate = 0;
        if (numeri.size() < 2) {
            return 0;
        }
        addGiocata(tipoGiocata.getTipo(), numeri);
        giocateMultiple.add(numeri);
        return 1;
    }


    private void addGiocata(String tipoGiocata, PList<Integer> numeri) {
        Giocata g = new Giocata(tipoGiocata);
        g.addGiocata(numeri);
        giocate.add(g);
    }


    private void modoGiocoExtraRandom(Integer quanteGiocate, PList<Report> report) throws Exception {
        PList<Integer> ultima = estrazioni.getFirstElement().getEstrazione();
        for (int i = 1; i <= quanteGiocate; i++) {
            PList<Integer> es1 = pl(estrazioni.get(ultima.randomOne()).getExtra().subList(0, 2));
            PList<Integer> es2 = pl(estrazioni.get(ultima.randomOne()).getExtra().subList(3, 5));
            PList<Integer> es3 = pl(estrazioni.get(ultima.randomOne()).getExtra().subList(6, 8));
            PList<Integer> totale = pl(es1.aggiungiList(es2, es3));
            report.add(new Report(TipoGiocata.EXTRA_RANDOM.getTipo(), totale, trovaNumeriEstratti(totale)));
            giocaNumeri(TipoGiocata.EXTRA_RANDOM, totale.distinct(), pl(3, 4, 5, 6), 1);
            giocaNumeriPariDispari(TipoGiocata.EXTRA_RANDOM, totale.distinct(), pl(3, 4), 1);
        }
    }


    private Integer giocaNumeriPariDispari(TipoGiocata tipoGiocata, PList<Integer> numeri, PList<Integer> lunghezzeGiocate, int quantePerLunghezza) {
        Integer quanteGiocate = 0;
        quanteGiocate += giocaNumeri(tipoGiocata, numeri.pari(), lunghezzeGiocate, quantePerLunghezza);
        quanteGiocate += giocaNumeri(tipoGiocata, numeri.dispari(), lunghezzeGiocate, quantePerLunghezza);
        return quanteGiocate;
    }

    private Integer giocaNumeriPariDispari(TipoGiocata tipoGiocata, PList<Integer> numeri) {
        Integer quanteGiocate = 0;
        quanteGiocate += giocaNumeri(tipoGiocata, numeri.pari());
        quanteGiocate += giocaNumeri(tipoGiocata, numeri.dispari());
        return quanteGiocate;
    }


    private PList<Integer> getNumeriDaAmpiezzeBasse(Integer quantiNumeriAlMassimo, Parametri p, boolean togliNumeriUltimaEstrazione) throws Exception {
        int contaNumeri = 0;
        PList<Integer> frequenzeTrovate = pl();
        for (Ampiezza a : p.getAmpiezze().sort(AMPIEZZA, FREQ)) {
            contaNumeri += a.getAmpiezza();
            if (contaNumeri > quantiNumeriAlMassimo) break;
            else {
                if (!frequenzeTrovate.contains(a.getFreq()))
                    frequenzeTrovate.add(a.getFreq());
            }
        }
        PList<Integer> numeriSviluppati = p.getFrequenze().in(FREQ, frequenzeTrovate).find().narrowDistinct(NUMERO);
        if (limitaSviluppati)
            while (numeriSviluppati.size() > maxNumeriSviluppati && frequenzeTrovate.size() > 0) {
                frequenzeTrovate = frequenzeTrovate.dropLast();
                numeriSviluppati = p.getFrequenze().in(FREQ, frequenzeTrovate).find().narrowDistinct(NUMERO);
            }
        if (togliNumeriUltimaEstrazione) {
            numeriSviluppati = numeriSviluppati.sottraiList(estrazioni.get(1).getEstrazione());
        }
        p.getReport().add(new Report(TipoGiocata.AMPIEZZE_BASSE.getTipo(), frequenzeTrovate, numeriSviluppati, trovaNumeriEstratti(numeriSviluppati)));
        return numeriSviluppati;
    }


    private PList<Integer> getNumeriAmpiezzeTra(int minAmpiezza, int maxAmpiezza, Parametri p, boolean togliNumeriUltimaEstrazione) throws Exception {
        PList<Integer> amps = p.getAmpiezze().between(AMPIEZZA, minAmpiezza, maxAmpiezza).find().narrowDistinct(AMPIEZZA);
        return getNumeriAmpiezzePuntuali(amps, p, togliNumeriUltimaEstrazione);
    }


    private PList<Integer> getNumeriFrequenzeTra(int minFreq, int maxFreq, Parametri p, boolean togliNumeriUltimaEstrazione) throws Exception {
        return getNumeriFrequenzePuntuali(p.getFrequenze().between(FREQ, minFreq, maxFreq).find().narrowDistinct(FREQ), p, togliNumeriUltimaEstrazione);
    }


    private PList<Integer> getNumeriAmpiezzePuntuali(PList<Integer> amps, Parametri p, boolean togliNumeriUltimaEstrazione) throws Exception {
        if (Null(amps)) return pl();
        PList<Integer> frequenze = p.getAmpiezze().in(AMPIEZZA, amps).find().narrowDistinct(FREQ);
        if (Null(frequenze)) return pl();
        PList<Integer> numeri = p.getFrequenze().in(FREQ, frequenze).find().narrowDistinct(NUMERO);
        if (limitaSviluppati)
            while (numeri.size() > maxNumeriSviluppati && frequenze.size() > 0) {
                frequenze = frequenze.dropLast();
                numeri = p.getFrequenze().in(FREQ, frequenze).find().narrowDistinct(NUMERO);
            }
        if (togliNumeriUltimaEstrazione) {
            numeri = numeri.sottraiList(estrazioni.get(1).getEstrazione());
        }
        p.getReport().add(new Report(str(TipoGiocata.AMPIEZZE_PUNTUALI.getTipo(), amps.concatenaDash()), frequenze, numeri, trovaNumeriEstratti(numeri)));
        return numeri;
    }


    private PList<Integer> getNumeriFrequenzePuntuali(PList<Integer> freqs, Parametri p, boolean togliNumeriUltimaEstrazione) throws Exception {
        if (Null(freqs)) return pl();
        PList<Integer> numeriSviluppati = p.getFrequenze().in(FREQ, freqs).find().narrowDistinct(NUMERO);
        if (limitaSviluppati)
            while (numeriSviluppati.size() > maxNumeriSviluppati && freqs.size() > 0) {
                freqs = freqs.dropLast();
                numeriSviluppati = p.getFrequenze().in(FREQ, freqs).find().narrowDistinct(NUMERO);
            }
        if (togliNumeriUltimaEstrazione) {
            numeriSviluppati = numeriSviluppati.sottraiList(estrazioni.get(1).getEstrazione());
        }
        p.getReport().add(new Report(str(TipoGiocata.FREQUENZE_PUNTUALI.getTipo(), freqs.concatenaDash()), freqs, numeriSviluppati, trovaNumeriEstratti(numeriSviluppati)));
        return numeriSviluppati;
    }


    private void beep(int val) throws LineUnavailableException {
        float sampleRate = 2000.0F;
        byte[] buf = new byte[1];
        AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
        SourceDataLine line = AudioSystem.getSourceDataLine(af);
        line.open(af);
        line.start();
        for (int i = 0; i < 1000; i++) {
            double angle = i * 2.0 * Math.PI * 640.0 / sampleRate;
            buf[0] = (byte) (Math.sin(angle) * val);
            line.write(buf, 0, 1);
        }
        line.drain();
        line.close();
    }


    public PList<Integer> getFrequenzePuntuali(PList<Integer> frequenzeEstrattePrecedenti, PList<Integer> posizioni) {
        PList<Integer> frequenzePuntuali = pl();
        for (int i = 0; i < frequenzeEstrattePrecedenti.size(); i++) {
            if (posizioni.contains(i)) frequenzePuntuali.add(frequenzeEstrattePrecedenti.get(Integer.valueOf(i)));
        }
        return frequenzePuntuali;
    }

    private PList<Integer> tuttiNumeriGiocati() {
        PList<Integer> giocati = pl();
        for (PList<Integer> giocata : safe(giocateMultiple)) {
            giocati.addAll(giocata);
        }
        return giocati.sort().distinct();
    }


    private void generaGiocatePariDispari(PList<Integer> numeriSottofrequenze, Integer quanteGiocate, PList<Integer> lunghezzeGiocateAmmesse) {
        if (numeriSottofrequenze.size() >= 7 && numeriSottofrequenze.size() <= 10)
            giocateMultiple.add(numeriSottofrequenze);
        PList<Integer> pari = numeriSottofrequenze.pari();
        PList<Integer> dispari = numeriSottofrequenze.dispari();

        if (pari.size() >= 8 && pari.size() <= 10)
            giocateMultiple.add(pari);
        if (dispari.size() >= 8 && dispari.size() <= 10)
            giocateMultiple.add(dispari);


        int quantePari = quanteGiocate / 2;
        if (quantePari == 0) quantePari = 1;
        int quanteDispari = quanteGiocate - quantePari;
        if (pari.size() >= 3)
            for (int i = 1; i <= quantePari; i++) {
                giocateMultiple.add(pari.random(lunghezzeGiocateAmmesse.randomOne()));
            }
        if (dispari.size() >= 3)
            for (int i = 1; i <= quanteDispari; i++) {
                giocateMultiple.add(dispari.random(lunghezzeGiocateAmmesse.randomOne()));
            }

        if (pari.size() >= 15)
            for (int i = 1; i <= quantePari; i++) {
                giocateMultiple.add(pari.random(pl(7, 8, 9, 10).randomOne()));
            }
        if (dispari.size() >= 15)
            for (int i = 1; i <= quanteDispari; i++) {
                giocateMultiple.add(dispari.random(pl(7, 8, 9, 10).randomOne()));
            }
        generaGiocatePariDispariDinamiche(numeriSottofrequenze);
    }


    private void generaGiocatePariDispariDinamiche(PList<Integer> numeri) {
        PList<Integer> pari = numeri.pari();
        PList<Integer> dispari = numeri.dispari();

        if (pari.size() <= 8 && pari.size() >= 3) {
            giocateMultiple.add(pari);
        }
        if (dispari.size() <= 8 && dispari.size() >= 3) {
            giocateMultiple.add(dispari);
        }

    }


    private void generaGiocate(PList<Integer> numeriSottofrequenze, Integer quanteGiocate, PList<Integer> lunghezzeGiocateAmmesse) {
        for (int i = 1; i <= quanteGiocate; i++) {
            giocateMultiple.add(numeriSottofrequenze.random(lunghezzeGiocateAmmesse.randomOne()));
        }
    }


    private void loadGiocate() {
        if (oro && doppioOro) oro = false;
        String sep = space();
        for (String l : safe(readFile("giocate.txt"))) {
            sep = space();
            if (l.indexOf(tab()) > -1) {
                sep = tab();
            }
            if (l.indexOf(dash()) > -1) {
                sep = dash();
            }
            if (l.indexOf(comma()) > -1) {
                sep = comma();
            }
            giocateMultiple.add(split(l, sep).toListInteger());
        }
        if (notNull(giocata))
            giocateMultiple.add(giocata);
    }


    private void init() {
        PList<Vincita> uno = pl(new Vincita(0, 0), new Vincita(1, 3, 63, 0, 4));
        PList<Vincita> due = pl(new Vincita(0, 0), new Vincita(1, 0, 25, 0, 1), new Vincita(2, 14, 70, 250, 16));
        PList<Vincita> tre = pl(new Vincita(0, 0), new Vincita(1, 0, 15, 0, 0), new Vincita(2, 2, 25, 75, 4), new Vincita(3, 45, 130, 300, 100));
        PList<Vincita> quattro = pl(new Vincita(0, 0), new Vincita(1, 0, 10, 0, 0), new Vincita(2, 1, 15, 40, 2), new Vincita(3, 10, 40, 80, 25), new Vincita(4, 90, 300, 800, 225));
        PList<Vincita> cinque = pl(new Vincita(0, 0), new Vincita(1, 0, 10, 0, 0), new Vincita(2, 1, 14, 25, 2), new Vincita(3, 4, 20, 40, 10), new Vincita(4, 15, 30, 100, 30), new Vincita(5, 140,
                300, 1000, 300));
        PList<Vincita> sei = pl(new Vincita(0, 0), new Vincita(1, 0, 10, 0, 0), new Vincita(2, 0, 10, 20, 1), new Vincita(3, 2, 10, 25, 7), new Vincita(4, 10, 20, 50, 20), new Vincita(5, 100, 200,
                500, 200), new Vincita(6, 1000, 2500, 6000, 2000));
        PList<Vincita> sette = pl(new Vincita(0, 1), new Vincita(1, 0, 10, 0, 0), new Vincita(2, 0, 7, 15, 0), new Vincita(3, 0, 7, 25, 5), new Vincita(4, 4, 10, 40, 15), new Vincita(5, 40, 75, 150,
                75), new Vincita(6, 400, 750, 1500, 750), new Vincita(7, 1600, 5000, 15000, 5000));
        PList<Vincita> otto = pl(new Vincita(0, 1), new Vincita(1, 0, 8, 0, 0), new Vincita(2, 0, 3, 10, 0), new Vincita(3, 0, 5, 15, 3), new Vincita(4, 0, 10, 25, 10),
                new Vincita(5, 20, 45, 100, 45), new Vincita(6, 200, 450, 1000, 450), new Vincita(7, 800, 2000, 5000, 2000), new Vincita(8, 10000, 30000, 100000, 20000));
        PList<Vincita> nove = pl(new Vincita(0, 2, 0, 0, 1), new Vincita(1, 0, 5, 0, 0), new Vincita(2, 0, 3, 10, 0), new Vincita(3, 0, 5, 15, 0), new Vincita(4, 0, 10, 25, 10), new Vincita(5, 10,
                25, 50, 20), new Vincita(6, 40, 80, 200, 75), new Vincita(7, 400, 800, 2000, 500), new Vincita(8, 2000, 5000, 20000, 5000), new Vincita(9, 100000, 250000, 500000, 250000));
        PList<Vincita> dieci = pl(new Vincita(0, 2, 0, 0, 1), new Vincita(1, 0, 10, 0, 0), new Vincita(2, 0, 3, 10, 0), new Vincita(3, 0, 3, 15, 0), new Vincita(4, 0, 5, 20, 6), new Vincita(5, 5, 20,
                30, 20), new Vincita(6, 15, 25, 70, 35), new Vincita(7, 150, 250, 500, 250), new Vincita(8, 1000, 2500, 5000, 2000), new Vincita(9, 20000, 50000, 100000, 40000), new Vincita(10,
                1000000, 2500000, 5000000, 2000000));
        vincite.put(1, uno);
        vincite.put(2, due);
        vincite.put(3, tre);
        vincite.put(4, quattro);
        vincite.put(5, cinque);
        vincite.put(6, sei);
        vincite.put(7, sette);
        vincite.put(8, otto);
        vincite.put(9, nove);
        vincite.put(10, dieci);
    }

    private Integer calcolaVincita(Estrazione5Minuti estr) throws Exception {
        PList<Vincita> vv = vincite.get(safe(estr.getGiocata()).size());
        if (Null(vv)) return 0;
        Vincita v = vincite.get(safe(estr.getGiocata()).size()).eq("trovati", estr.getQuantiTrovati()).findOne();
        Integer quota = v.getQuota();
        if (estr.presoOro())
            quota = v.getQuotaOro();
        if (estr.presoDoppioOro())
            quota = v.getQuotaDoppioOro();
        estr.setVincitaNormale(quota);
        if (estr.isGiocataExtra()) {
            v = vincite.get(estr.getGiocata().size()).eq("trovati", estr.getQuantiTrovatiExtra()).findOne();
            quota += v.getQuotaExtra();
            estr.setVincitaExtra(v.getQuotaExtra());
        }
        return quota;
    }


    private void downloadEstrazioni(Integer quantiGiorniFa) throws Exception {
        PDate da = now().giorniFa(quantiGiorniFa);
        PDate finoA = now().ieri();
        for (PDate d = da; d.isNotAfter(finoA); d = d.domani()) {
            download(d);
        }
    }

    private void download() throws Exception {
        String giorno = Null(giornoDaScaricare) ? now().toStringFormat("yyyy-MM-dd") : giornoDaScaricare;
        String fileURL = str(URL, giorno);
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(FILE);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }


    private void download(PDate giorno) throws Exception {
        URL url = new URL(str(URL, giorno.toStringFormat("yyyy-MM-dd")));
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(str("estrazioni/", giorno.toStringFormat("dd-MM-YYYY"), dot(), "txt"));
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }

    private void loadEstrazioni() {
        estrazioni = pl();
        PList<String> cont = readFile(FILE);
        cont = pl(cont.subList(3, cont.size() - 3));
        for (String item : cont) {
            Estrazione5Minuti es = new Estrazione5Minuti(item);
            es.setOroGiocato(oro);
            es.setDoppioOroGiocato(doppioOro);
            es.setGiocataExtra(extra);
            estrazioni.add(es);
        }
    }

    private void execute() throws Exception {
        boolean almenoUnaGiocata = false;
        for (Giocata g : safe(giocate)) {
            for (PList<Integer> giocata : safe(g.getGiocate())) {
                if (notNull(giocata)) {
                    almenoUnaGiocata = true;
                    break;
                    //log("GIOCATI ", giocata.size(), " NUMERI:", giocata.concatenaDash());
                }
            }
            if (almenoUnaGiocata) break;
        }

        if (!almenoUnaGiocata) {
            log("---------Nessuna schedina giocata da verificare!---------");
            return;
        }
        Integer oriPresi = 0;
        Integer doppioOriPresi = 0;
        if (notNull(ultimeEstrazioni)) estrazioni = estrazioni.cutToFirst(ultimeEstrazioni);
        for (Estrazione5Minuti es : safe(estrazioni)) {
            for (Giocata g : safe(giocate))
                for (PList<Integer> giocata : safe(g.getGiocate())) {
                    es.setTipoGiocata(g.getTipo());
                    es.setGiocata(giocata);
                    es.setOroGiocato(oro);
                    es.setDoppioOroGiocato(doppioOro);
                    es.setGiocataExtra(extra);
                    es.addSpesa();
                    if (es.presoOro())
                        oriPresi++;
                    if (es.presoDoppioOro())
                        doppioOriPresi++;
                    es.addVincita(calcolaVincita(es));
                    es.impostaTrovati();
                    es.impostaMsgOri();
                }


        }
        // estrazioni.sortDesc("ampiezza", "quantiTrovatiExtra");
        estrazioni.sortDesc("vincita");
        Integer vincite = estrazioni.gt("vincita", 0).find().size();
        log("-------------- VINCENTI ", vincite, slash(), estrazioni.size(), "   ", percentuale(bd(vincite), bd(estrazioni.size())), "%");
        for (Estrazione5Minuti e : estrazioni) {
            if (e.getVincita() > 0) {
                log("VINCITA:", verde(moneyEuro(bd(e.getVincita()))));
                log(e.getMsgTrovatiVincenti(), lf());
                //log(e);
            }
        }
        String output = str(estrazioni.getFirstElement().getDataString(), "   ");
        //log(getTitle("REPORT FINALE", 80, "*"));
        //  log("Max Trovati", estrazioni.max("maxTrovati"));
        // log("Max Trovati Extra", estrazioni.max("maxTrovatiExtra"));
        Integer vincitaTotale = estrazioni.sommatoria("vincita", Integer.class);
        Integer spesaTotale = estrazioni.sommatoria("spesaTotale", Integer.class);
        vincitaFinale += vincitaTotale;
        spesaFinale += spesaTotale;
        bilancioFinale = vincitaFinale - spesaFinale;
        output = str(output, "VINCITA: ", money(bd(vincitaTotale)), tab(), "SPESA: ", money(bd(spesaTotale)), tab(), "BILANCIO:", money(bd(vincitaTotale - spesaTotale)));
        appendFile(REPORT, pl(output));
        Integer bl = vincitaTotale - spesaTotale;
        log(lf(), tabn(5), "VINCITA TOTALE:", verde(moneyEuro(bd(vincitaTotale))), lf(), tabn(5), "SPESA TOTALE:", moneyEuro(bd(spesaTotale)), lf(), tabn(5), "BILANCIO:", bl >= 0 ? verde(moneyEuro(bd(bl))) : rosso(moneyEuro(bd(bl))));
        if (almenoUna(oro, doppioOro))
            log(str(lf(), tabn(5), "ORO preso:", oriPresi, " volte"));
        if (doppioOro)
            log(str(lf(), tabn(5), "DOPPIO ORO preso:", doppioOriPresi, " volte"));
        log("-------------- VINCENTI ", vincite, slash(), estrazioni.size(), "   ", percentuale(bd(vincite), bd(estrazioni.size())), "%");
        int bilancio = vincitaTotale - spesaTotale;
        if (bilancio > 50 && bilancio <= 100) beep(97);
        if (bilancio > 100) beep(300);
        log("BILANCIO COMPLESSIVO ", estrazioni.getFirstElement().getDataString(), "   VINTI: ", moneyEuro(bd(vincitaFinale)), "  SPESI: ", moneyEuro(bd(spesaFinale)), "   BILANCIO:", bilancioFinale >= 0 ? verde(moneyEuro(bd(bilancioFinale))) : rosso(moneyEuro(bd(bilancioFinale))), tab(), "Tempo trascorso:", color(elapsedTime(inizioElaborazione), Color.BIANCO, true, true, false, false));
    }


    private String verde(String s) {
        return color(s, Color.VERDE, true, true, false, false);
    }

    private String rosso(String s) {
        return color(s, Color.ROSSO, true, true, false, false);
    }

    private String giallo(String s) {
        return color(s, Color.GIALLO, true, true, false, false);
    }

    private String bianco(String s) {
        return color(s, Color.BIANCO_CORNICE_VUOTO, true, true, false, false);
    }

    private PList<PList<Integer>> getEstrazioniFibonacci() {
        PList<Integer> fb = getFibonacci();
        PList<PList<Integer>> rows = pl();
        for (Integer i : fb) {
            rows.add(estrazioni.get(i).getEstrazione());
        }
        return rows;
    }

    private PList<Integer> getGiocataFibonacci(Integer l) {
        PList<Integer> giocataFibonacci = pl();
        PList<Integer> fb = getFibonacci();
        PList<PList<Integer>> estrazioniFibonacci = getEstrazioniFibonacci();
        int i = 0;
        for (PList<Integer> n : estrazioniFibonacci) {
            if (giocataFibonacci.size() == l.intValue()) break;
            int fibo = fb.get(i);
            int numero = 0;
            if (n.size() < fibo) {
                Integer quot = fibo / n.size();
                Integer quanti = n.size() * quot;
                Integer resto = fibo - quanti;
                numero = n.get(resto);
            } else {
                numero = n.get(fibo);
            }
            if (!giocataFibonacci.contains(numero))
                giocataFibonacci.add(numero);
            i++;
        }
        return giocataFibonacci;
    }

    private PList<Integer> getFibonacci() {
        PList<Integer> fb = pl();
        int a = 0, b = 1;
        //fb.add(1);
        for (int i = 2; i <= 13; ++i) {
            int c = a + b;
            fb.add(c);
            a = b;
            b = c;
        }
        return fb;
    }

    private PList<Frequenza> calcolaFrequenze(Integer partiDa) throws Exception {
        Map<Integer, Integer> frequenze = new HashMap<>();
        for (int i = partiDa; i < estrazioni.size(); i++) {
            Estrazione5Minuti e = estrazioni.get(i);
            for (Integer n : safe(e.getEstrazione())) {
                if (Null(frequenze.get(n))) {
                    frequenze.put(n, 1);
                } else {
                    frequenze.put(n, frequenze.get(n) + 1);
                }
            }
        }
        PList<Frequenza> frequencies = pl();
        for (Map.Entry<Integer, Integer> entry : frequenze.entrySet()) {
            frequencies.add(new Frequenza(entry.getKey(), entry.getValue()));
        }
        return frequencies.sortDesc(FREQ);
    }

    private void salvaFrequenze() throws Exception {
        PList<Frequenza> freqsPrecedenti = calcolaFrequenze(1);
        PList<Frequenza> freqs = calcolaFrequenze(0);

        writeFile(AMPIEZZA_FREQUENZE, calcolaAmpiezze(freqs));
        writeFile(AMPIEZZA_FREQUENZE_PRECEDENTI, calcolaAmpiezze(freqsPrecedenti));
        writeFile(FREQUENZE, freqs);
        writeFile(FREQUENZE_PRECEDENTI, freqsPrecedenti);
    }

    private PList<Ampiezza> calcolaAmpiezze(PList<Frequenza> freqs) throws Exception {
        PList<Ampiezza> ampiezze = pl();

        Map<Integer, PList<Frequenza>> mappa = freqs.groupBy(FREQ);
        for (Map.Entry<Integer, PList<Frequenza>> entry : mappa.entrySet()) {
            ampiezze.add(new Ampiezza(entry.getKey(), entry.getValue().size()));
        }


        freqs.forEach((f) -> {
            if (estrazioni.getFirstElement().getEstrazione().contains(f.getNumero())) {
                try {
                    ampiezze.eq(FREQ, f.getFreq()).findOne().addNumeroIntercettato(f.getNumero());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
        return ampiezze.sortDesc(AMPIEZZA, FREQ);
    }


    private PList<Frequenza> leggiFrequenze(String file) {
        PList<String> cont = readFile(file);
        PList<Frequenza> freq = pl();
        for (String s : safe(cont)) {
            PList<String> arr = split(s, arrow());
            String frq = arr.getLastElement();
            Integer numero = getInteger(arr.getFirstElement());
            Integer fr = getInteger(frq);
            freq.add(new Frequenza(numero, fr));
        }
        return freq;
    }

    private PList<Ampiezza> leggiAmpiezze(String file) {
        PList<String> cont = readFile(file);
        PList<Ampiezza> ampiezze = pl();
        for (String s : safe(cont)) {
            PList<String> arr = split(s, arrow());
            Integer frq = getInteger(substring(arr.getFirstElement(), "Frequenza ", false, false, null, false, false));
            Integer ampiezza = getInteger(substring(arr.getLastElement(), null, false, false, tab(), false, true));
            Integer quantiIntercettati = getInteger(substring(arr.getLastElement(), tab(), false, false, space(), false, false));
            //Integer quantiIntercettatiFuturi = getInteger(substring(arr.getLastElement(), Ampiezza.FUTURI, false, true, colon(), false, true));
            ampiezze.add(new Ampiezza(frq, ampiezza, quantiIntercettati));
        }
        return ampiezze;
    }


    private void stampaCadenze(Integer almeno) {
        for (Estrazione5Minuti e : safe(estrazioni)) {
            String cad = e.getCadenze(almeno);
            if (Null(cad)) continue;
            log(cad);
        }
    }

    private void stampaConsecutivi(Integer almeno) {
        for (Estrazione5Minuti e : safe(estrazioni)) {
            PList<PList<Integer>> consecutivi = e.getConsecutivi(almeno);
            if (Null(consecutivi)) continue;
            for (PList<Integer> cons : consecutivi)
                log(e.getDataString(), "Consecutivi: ", color(cons.concatenaDash(), Color.VIOLA_CORNICE, true, true, false, false));
        }
    }

}

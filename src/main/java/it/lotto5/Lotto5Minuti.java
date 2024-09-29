package it.lotto5;

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
    public static final String AMPIEZZA_FREQUENZE = "ampiezzaFrequenze.txt";
    public static final String REPORT = "REPORT.TXT";

    public static final Integer maxNumeriSviluppati = 20;
    public Integer bilancioFinale = 0;
    public Integer vincitaFinale = 0;

    public Integer spesaFinale = 0;
    private Integer ultimeEstrazioni = 1;//limito la verifica alle ultime 10 estrazioni più recenti
    //private  PList<Integer> giocata = pl(Lotto5Minuti.generaGiocata(7,1,10));
    private PList<Integer> giocata = pl();

    private PList<PList<Integer>> giocateMultiple = pl();

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
    Map<Integer, Integer> frequenze = new HashMap<>();


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
        stampaCadenze();
        stampaConsecutivi();
    }


    private void autorun() throws Exception {
        Integer oraStop = 24;
        Integer minutiStop = 00;
        Integer oraStart = 2;
        Integer minutiStart = 00;
        PDate start = now().ora(oraStart).minuti(minutiStart);
        vincitaFinale = 0;
        spesaFinale = 0;
        bilancioFinale = 0;
        while (true) {
            if (now().isBefore(start)) {
                Thread.sleep(60000 * 5);
                continue;
            }
            if (now().isAfter(now().ora(oraStop).minuti(minutiStop))) break;
            Thread.sleep(10000);
            if (now().getMinuti() % 5 == 0) {
                log("Scattati i 5 minuti ", now().getOraCompleta(), " procedo con l'elaborazione!!!!!");
                Thread.sleep(10000);
                run();
                frequenze = new HashMap<>();
                giocateMultiple = pl();
                giocata = pl();
                loadGiocate();
                Thread.sleep(60000);
            }
        }
        PList<String> out = pl();
        out.add(str("VINCITA:", money(bd(vincitaFinale))));
        out.add(str("SPESA:", money(bd(spesaFinale))));
        out.add(str("BILANCIO:", money(bd(bilancioFinale))));
        appendFile(REPORT, out);
    }


    private void elaboraFrequenze() throws Exception {
        if (estrazioni.size() <= 1) return;
        PList<Frequenza> fre = leggiFrequenze();
        PList<Ampiezza> ampiezze = leggiAmpiezze();
        if (Null(fre)) {
            salvaFrequenze();
            fre = leggiFrequenze();
            ampiezze = leggiAmpiezze();
        }
        PList<Report> report = pl();
        PList<Integer> ultimaEstrazione = estrazioni.getFirstElement().getEstrazione();
        PList<Integer> penultimaEstrazione = estrazioni.get(1).getEstrazione();
        PList<Integer> inComune = estrazioni.getFirstElement().getEstrazione().intersection(penultimaEstrazione);
        log(inComune.size(), "NUMERI IN COMUNE CON L'ESTRAZIONE PRECEDENTE", inComune.concatenaDash());
        PList<Integer> frequenzeEstratte = pl();
        PList<Integer> frequenzeEstrattePrecedenti = pl();
        if (notNull(fre))
            for (Integer i : ultimaEstrazione) {
                Frequenza f = fre.eq("numero", i).findOne();
                if (Null(f)) frequenzeEstratte.add(0);
                else
                    frequenzeEstratte.add(f.getFreq());
            }
        for (Integer i : penultimaEstrazione) {
            Frequenza f = fre.eq("numero", i).findOne();
            if (Null(f)) frequenzeEstrattePrecedenti.add(0);
            else
                frequenzeEstrattePrecedenti.add(f.getFreq());
        }

        frequenzeEstrattePrecedenti = frequenzeEstrattePrecedenti.distinct().sort();
        Integer quanteFrequenzeDistinte = frequenzeEstrattePrecedenti.size();
        Integer posizioneMedia = frequenzeEstrattePrecedenti.size() / 2 - 1;
        frequenzeEstratte = frequenzeEstratte.sort();

        log("Frequenze estratte distinte precedenti", frequenzeEstrattePrecedenti.sort().concatenaDash());
        log("Frequenze estratte attuali   ", frequenzeEstratte.sort().concatenaDash());
        log("Ampiezze frequenze estratte:", ampiezze.in("freq", frequenzeEstratte.distinct()).find().sort("ampiezza").narrow("ampiezza").concatenaDash());
        log("INTERVALLO DI FREQUENZE:  ", quadra(), fre.min("freq").getFreq(), comma(), fre.max("freq").getFreq(), quadraClose());
        PList<Integer> numeriDaAmpiezzeBasse = getNumeriDaAmpiezzeBasse(6, ampiezze, fre, report, false);
        PList<Integer> ampiezzePuntuali = pl();
        PList<Integer> frequenzeBuone = ampiezze.gt("quantiIntercettati", 0).find().sort("freq").narrow("freq");
        Integer ampiezzaMinima = 3;
        Integer ampiezzaMassima = 4;
        PList<Integer> frequenzeBuoneSelezionate = ampiezze.between("quantiIntercettati", 1, 5).between("ampiezza", ampiezzaMinima, ampiezzaMassima).find().sort("freq").narrow("freq");
        PList<Integer> frequenzeNonBuoneSelezionate = ampiezze.eq("quantiIntercettati", 0).gt("ampiezza", 2).find().sort("freq").narrow("freq");
        PList<Integer> frequenzeProlifiche = ampiezze.sortDesc("quantiIntercettati").narrow("freq");
        //ampiezzePuntuali.add(4);
        //ampiezzePuntuali.add(3);
        //ampiezzePuntuali.add(2);
        //PList<Integer> numeriDaAmpiezzePuntuali = getNumeriAmpiezzaPuntuale(ampiezze, fre, ampiezzePuntuali, report, false);


        //PList<Integer> numeriDaFrequenzeBuone = getNumeriFrequenzePuntuali(fre, frequenzeBuoneSelezionate, report, true);
        PList<Integer> numeriDaFrequenzeProlifiche = getNumeriFrequenzePuntuali(fre, frequenzeProlifiche.cutToFirst(3), report, false);
        PList<Integer> numeriDaFrequenzeNonBuone = getNumeriFrequenzePuntuali(fre, frequenzeNonBuoneSelezionate, report, false);
        giocaNumeri(numeriDaAmpiezzeBasse, pl(3, 4, 5, 6), 3);
        //giocaNumeri(numeriDaAmpiezzePuntuali, pl(5, 6, 7), 3);
        giocaNumeri(numeriDaFrequenzeProlifiche, pl(3, 4, 5, 6), 3);
        giocaNumeri(numeriDaFrequenzeNonBuone, pl(3, 4, 5, 6), 3);

        report.forEach(System.out::println);
        Integer intercettati = 0;
        Integer sviluppati = 0;
        PList<Integer> totaleIntercettati = pl();
        PList<Integer> totaleSviluppati = pl();
        for (Report r : report) {
            intercettati += r.getIntercettati().size();
            totaleIntercettati.addAll(r.getIntercettati());
            totaleSviluppati.addAll((r.getSviluppati()));
            sviluppati += r.getSviluppati().size();
        }
        System.out.println(str(intercettati, slash(), sviluppati, tab(), "Intercettati:", totaleIntercettati.sort().concatenaDash(), "  Sviluppati:", totaleSviluppati.sort().concatenaDash()));
        /*
        Integer low = frequenzeEstrattePrecedenti.get(Integer.valueOf(posizioneMedia - 1));
        Integer high = frequenzeEstrattePrecedenti.get(Integer.valueOf(posizioneMedia + 2));
        PList<Integer> numeriSottofrequenze = beccatiSottoFrequenze(fre, low, high);
        generaGiocatePariDispari(numeriSottofrequenze, 5, pl(3, 4, 5, 6));
        log(lfn(2));
        low = frequenzeEstrattePrecedenti.get(Integer.valueOf(1));
        high = frequenzeEstrattePrecedenti.get(Integer.valueOf(3));
        numeriSottofrequenze = beccatiSottoFrequenze(fre, low, high);
        generaGiocatePariDispari(numeriSottofrequenze, 5, pl(3, 4, 5, 6));
        log(lfn(2));
        low = frequenzeEstrattePrecedenti.get(Integer.valueOf(quanteFrequenzeDistinte - 5));
        high = frequenzeEstrattePrecedenti.get(Integer.valueOf(quanteFrequenzeDistinte - 2));
        numeriSottofrequenze = beccatiSottoFrequenze(fre, low, high);
        generaGiocatePariDispari(numeriSottofrequenze, 5, pl(3, 4, 5, 6));
        log(lfn(2));
        numeriSottofrequenze = beccatiFrequenzePuntuali(fre, getFrequenzePuntuali(frequenzeEstrattePrecedenti, pl(0, 4, quanteFrequenzeDistinte - 3)));
        generaGiocatePariDispari(numeriSottofrequenze, 5, pl(3, 4, 5));
        log(lfn(2));*/
        // generaGiocatePariDispari(numeriFrequenzeBasse, 5, pl(3, 4, 5));
        salvaFrequenze();
    }


    public PList<Integer> trovaNumeriEstratti(PList<Integer> numeriSviluppati) throws Exception {
        return estrazioni.getFirstElement().getEstrazione().intersection(numeriSviluppati).sort();
    }

    private void giocaNumeri(PList<Integer> numeri, PList<Integer> lunghezzeGiocate, int quantePerLunghezza) {
        if (numeri.size() >= (maxNumeriSviluppati - 5) && numeri.size() <= maxNumeriSviluppati) {
            lunghezzeGiocate = pl(7, 8, 9, 10);
        }
        if (numeri.size() >= 3)
            for (int i = 1; i <= quantePerLunghezza; i++) {
                for (Integer quanti : lunghezzeGiocate)
                    giocateMultiple.add(numeri.random(quanti));
            }
    }

    private PList<Integer> getNumeriDaAmpiezzeBasse(Integer quantiNumeriAlMassimo, PList<Ampiezza> ampiezze, PList<Frequenza> fre, PList<Report> report, boolean togliNumeriUltimaEstrazione) throws Exception {
        int contaNumeri = 0;
        PList<Integer> frequenzeTrovate = pl();
        for (Ampiezza a : ampiezze.sort("ampiezza", "freq")) {
            contaNumeri += a.getAmpiezza();
            if (contaNumeri > quantiNumeriAlMassimo) break;
            else {
                if (!frequenzeTrovate.contains(a.getFreq()))
                    frequenzeTrovate.add(a.getFreq());
            }
        }
        PList<Integer> numeriSviluppati = fre.in("freq", frequenzeTrovate).find().narrowDistinct("numero");
        numeriSviluppati = limitaNumeriSviluppati(numeriSviluppati, frequenzeTrovate, fre);
        if (togliNumeriUltimaEstrazione) {
            numeriSviluppati = numeriSviluppati.sottraiList(estrazioni.get(1).getEstrazione());
        }
        report.add(new Report(frequenzeTrovate, numeriSviluppati, trovaNumeriEstratti(numeriSviluppati)));
        return numeriSviluppati;
    }


    private PList<Integer> getNumeriAmpiezzaTra(int minAmpiezza, int maxAmpiezza, PList<Ampiezza> ampiezze, PList<Frequenza> fre, PList<Report> report, boolean togliNumeriUltimaEstrazione) throws Exception {
        PList<Integer> frequenze = ampiezze.between("ampiezza", minAmpiezza, maxAmpiezza).find().narrowDistinct("freq");
        PList<Integer> numeri = fre.in("freq", frequenze).find().narrowDistinct("numero");
        numeri = limitaNumeriSviluppati(numeri, frequenze, fre);
        if (togliNumeriUltimaEstrazione) {
            numeri = numeri.sottraiList(estrazioni.get(1).getEstrazione());
        }
        report.add(new Report(frequenze, numeri, trovaNumeriEstratti(numeri)));
        return numeri;
    }

    private PList<Integer> getNumeriAmpiezzaPuntuale(PList<Ampiezza> ampiezze, PList<Frequenza> fre, PList<Integer> amps, PList<Report> report, boolean togliNumeriUltimaEstrazione) throws Exception {
        if (Null(amps)) return pl();
        PList<Integer> frequenze = ampiezze.in("ampiezza", amps).find().narrowDistinct("freq");
        if (Null(frequenze)) return pl();
        PList<Integer> numeri = fre.in("freq", frequenze).find().narrowDistinct("numero");
        numeri = limitaNumeriSviluppati(numeri, frequenze, fre);
        if (togliNumeriUltimaEstrazione) {
            numeri = numeri.sottraiList(estrazioni.get(1).getEstrazione());
        }
        report.add(new Report(frequenze, numeri, trovaNumeriEstratti(numeri)));
        return numeri;
    }

    private PList<Integer> limitaNumeriSviluppati(PList<Integer> numeriSviluppati, PList<Integer> freqs, PList<Frequenza> fre) throws Exception {
        while (numeriSviluppati.size() > maxNumeriSviluppati) {
            freqs = freqs.dropLast();
            numeriSviluppati = fre.in("freq", freqs).find().narrowDistinct("numero");
        }
        return numeriSviluppati;
    }

    private PList<Integer> getNumeriFrequenzePuntuali(PList<Frequenza> fre, PList<Integer> freqs, PList<Report> report, boolean togliNumeriUltimaEstrazione) throws Exception {
        if (Null(freqs)) return pl();
        PList<Integer> numeriSviluppati = fre.in("freq", freqs).find().narrowDistinct("numero");
        numeriSviluppati = limitaNumeriSviluppati(numeriSviluppati, freqs, fre);
        if (togliNumeriUltimaEstrazione) {
            numeriSviluppati = numeriSviluppati.sottraiList(estrazioni.get(1).getEstrazione());
        }
        report.add(new Report(freqs, numeriSviluppati, trovaNumeriEstratti(numeriSviluppati)));
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
        PList<Integer> pari = pari(numeriSottofrequenze);
        PList<Integer> dispari = dispari(numeriSottofrequenze);

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
        PList<Integer> pari = pari(numeri);
        PList<Integer> dispari = dispari(numeri);

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

    private PList<Integer> beccatiSottoFrequenze(PList<Frequenza> fre, Integer minFreq, Integer maxFreq) throws Exception {
        PList<Integer> sottoFrequenze = fre.between("freq", minFreq, maxFreq).find().narrow("numero");
      /*  if (estrazioni.size() > 1) {
            sottoFrequenze = sottoFrequenze.sottraiList(estrazioni.get(1).getEstrazione());
        }*/
        log("Quanti numeri sviluppati per le sottofrequenze: [", minFreq, comma(), maxFreq, quadraClose(), sottoFrequenze.size(), quadra(), pari(sottoFrequenze).size(), " pari e ", dispari(sottoFrequenze).size(), " dispari ]", sottoFrequenze.sort().concatenaDash());
        PList<Integer> beccati = estrazioni.getFirstElement().getEstrazione().intersection(sottoFrequenze);
        log("Beccati ", beccati.size(), " numeri ", beccati.concatenaDash(), "  dalle sottofrequenze");
        log("Beccati ", pari(beccati).size(), " pari ", pari(beccati).concatenaDash(), "  dalle sottofrequenze");
        log("Beccati ", dispari(beccati).size(), " dispari ", dispari(beccati).concatenaDash(), "  dalle sottofrequenze");
        return sottoFrequenze;
    }

    private PList<Integer> beccatiFrequenzePuntuali(PList<Frequenza> fre, PList<Integer> frequenze) throws Exception {
        PList<Integer> sottoFrequenze = fre.in("freq", frequenze).find().narrow("numero");
      /*  if (estrazioni.size() > 1) {
            sottoFrequenze = sottoFrequenze.sottraiList(estrazioni.get(1).getEstrazione());
        }*/
        log("Quanti numeri sviluppati per le frequenze puntuali: [", frequenze.distinct().concatenaDash(), quadraClose(), sottoFrequenze.size(), quadra(), pari(sottoFrequenze).size(), " pari e ", dispari(sottoFrequenze).size(), " dispari ]", sottoFrequenze.sort().concatenaDash());
        PList<Integer> beccati = estrazioni.getFirstElement().getEstrazione().intersection(sottoFrequenze);
        log("Beccati ", beccati.size(), " numeri ", beccati.concatenaDash(), "  dalle sottofrequenze");
        log("Beccati ", pari(beccati).size(), " pari ", pari(beccati).concatenaDash(), "  dalle sottofrequenze");
        log("Beccati ", dispari(beccati).size(), " dispari ", dispari(beccati).concatenaDash(), "  dalle sottofrequenze");
        return sottoFrequenze;
    }

    private PList<Integer> pari(PList<Integer> l) {
        PList<Integer> pari = pl();
        l.forEach(i -> {
            if (i % 2 == 0) pari.add(i);
        });
        return pari;
    }


    private PList<Integer> dispari(PList<Integer> l) {
        PList<Integer> dispari = pl();
        l.forEach(i -> {
            if (i % 2 != 0) dispari.add(i);
        });
        return dispari;
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
        for (PList<Integer> giocata : giocateMultiple) {
            if (notNull(giocata)) {
                almenoUnaGiocata = true;
                log("GIOCATI ", giocata.size(), " NUMERI:", giocata.concatenaDash());
            }
        }
        if (!almenoUnaGiocata) {
            log("---------Nessuna schedina giocata da verificare!---------");
            return;
        }
        Integer oriPresi = 0;
        Integer doppioOriPresi = 0;
        if (notNull(ultimeEstrazioni)) estrazioni = estrazioni.cutToFirst(ultimeEstrazioni);
        for (Estrazione5Minuti es : safe(estrazioni)) {
            for (PList<Integer> giocata : safe(giocateMultiple)) {
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
            if (e.getVincita() > 0)
                log(e);
        }
        String output = str(estrazioni.getFirstElement().getDataString(), "   ");
        log(getTitle("REPORT FINALE", 80, "*"));
        //  log("Max Trovati", estrazioni.max("maxTrovati"));
        // log("Max Trovati Extra", estrazioni.max("maxTrovatiExtra"));
        Integer vincitaTotale = estrazioni.sommatoria("vincita", Integer.class);
        Integer spesaTotale = estrazioni.sommatoria("spesaTotale", Integer.class);
        vincitaFinale += vincitaTotale;
        spesaFinale += spesaTotale;
        bilancioFinale = vincitaFinale - spesaFinale;
        output = str(output, "VINCITA: ", money(bd(vincitaTotale)), tab(), "SPESA: ", money(bd(spesaTotale)), tab(), "BILANCIO:", money(bd(vincitaTotale - spesaTotale)));
        appendFile(REPORT, pl(output));
        log(str(lf(), tabn(5), "VINCITA TOTALE:", money(bd(vincitaTotale)), lf(), tabn(5), "SPESA TOTALE:", money(bd(spesaTotale)), lf(), tabn(5), "BILANCIO:", money(bd(vincitaTotale - spesaTotale))));
        if (almenoUna(oro, doppioOro))
            log(str(lf(), tabn(5), "ORO preso:", oriPresi, " volte"));
        if (doppioOro)
            log(str(lf(), tabn(5), "DOPPIO ORO preso:", doppioOriPresi, " volte"));
        log("-------------- VINCENTI ", vincite, slash(), estrazioni.size(), "   ", percentuale(bd(vincite), bd(estrazioni.size())), "%");
        int bilancio = vincitaTotale - spesaTotale;
        if (bilancio > 50 && bilancio <= 100) beep(97);
        if (bilancio > 100) beep(300);
        log("BILANCIO COMPLESSIVO ", estrazioni.getFirstElement().getDataString(), "   VINTI: ", money(bd(vincitaFinale)), "  SPESI: ", money(bd(spesaFinale)), "   BILANCIO:", money(bd(bilancioFinale)));
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

    private PList<Frequenza> calcolaFrequenze() throws Exception {
        for (Estrazione5Minuti e : safe(estrazioni)) {
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
        frequencies = frequencies.sortDesc("freq");
        return frequencies;
    }

    private void salvaFrequenze() throws Exception {
        PList<Frequenza> freqs = calcolaFrequenze();
        PList<Ampiezza> ampiezze = pl();
        Map<Integer, PList<Frequenza>> mappa = freqs.groupBy("freq");
        for (Map.Entry<Integer, PList<Frequenza>> entry : mappa.entrySet()) {
            ampiezze.add(new Ampiezza(entry.getKey(), entry.getValue().size()));
        }
        for (Ampiezza a : ampiezze) {
            for (Map.Entry<Integer, Integer> entry : frequenze.entrySet()) {
                if (is(a.getFreq(), entry.getValue())) {
                    if (estrazioni.getFirstElement().getEstrazione().contains(entry.getKey()))
                        a.addNumeroIntercettato((entry.getKey()));
                }
            }
        }
        writeFile(AMPIEZZA_FREQUENZE, ampiezze.sortDesc("ampiezza", "freq"));
        writeFile(FREQUENZE, freqs);
    }

    private PList<Frequenza> leggiFrequenze() {
        PList<String> cont = readFile(FREQUENZE);
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

    private PList<Ampiezza> leggiAmpiezze() {
        PList<String> cont = readFile(AMPIEZZA_FREQUENZE);
        PList<Ampiezza> ampiezze = pl();
        for (String s : safe(cont)) {
            PList<String> arr = split(s, arrow());
            Integer frq = getInteger(substring(arr.getFirstElement(), "Frequenza ", false, false, null, false, false));
            Integer ampiezza = getInteger(substring(arr.getLastElement(), null, false, false, tab(), false, true));
            Integer quantiIntercettati = getInteger(substring(arr.getLastElement(), tab(), false, false, space(), false, false));
            ampiezze.add(new Ampiezza(frq, ampiezza, quantiIntercettati));
        }
        return ampiezze;
    }


    private void stampaCadenze() {
        for (Estrazione5Minuti e : safe(estrazioni)) {
            String cad = e.getCadenze(6);
            if (Null(cad)) continue;
            log(cad);
        }
    }

    private void stampaConsecutivi() {
        for (Estrazione5Minuti e : safe(estrazioni)) {
            PList<PList<Integer>> consecutivi = e.getConsecutivi(6);
            if (Null(consecutivi)) continue;
            for (PList<Integer> cons : consecutivi)
                log(e.getDataString(), "Consecutivi: ", cons.concatenaDash());
        }
    }

}

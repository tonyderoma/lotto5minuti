package it.lotto5;

import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;
import it.lotto5.dto.Estrazione5Minuti;
import it.lotto5.dto.Frequenza;
import it.lotto5.dto.Vincita;
import org.apache.log4j.BasicConfigurator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lotto5Minuti extends PilotSupport {
    public static final String FREQUENZE = "frequenze.txt";
    public static final String REPORT = "REPORT.TXT";
    public Integer bilancioFinale = 0;
    public Integer vincitaFinale = 0;

    public Integer spesaFinale = 0;
    private Integer ultimeEstrazioni = 1;//limito la verifica alle ultime 10 estrazioni più recenti
    //private  PList<Integer> giocata = pl(Lotto5Minuti.generaGiocata(7,1,10));
    private PList<Integer> giocata = pl();

    private PList<PList<Integer>> giocateMultiple = pl();

    private String oggi = pd().toStringFormat("yyyy-MM-dd");
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
        //l.autorun();
        l.run();
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
        Integer minutiStop = 0;
        vincitaFinale = 0;
        spesaFinale = 0;
        bilancioFinale = 0;
        while (true) {
            if (pd().isAfter(pd().ora(oraStop).minuti(minutiStop))) break;
            Thread.sleep(10000);
            if (pd().getMinuti() % 5 == 0) {
                log("Scattati i 5 minuti ", pd().getOraCompleta(), " procedo con l'elaborazione!!!!!");
                Thread.sleep(10000);
                run();
                frequenze = new HashMap<>();
                giocateMultiple = pl();
                giocata = pl();
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
        PList<Frequenza> fre = leggiFrequenze();
        PList<Integer> ultimaEstrazione = estrazioni.getFirstElement().getEstrazione();
        PList<Integer> penultimaEstrazione = estrazioni.get(1).getEstrazione();
        if (Null(penultimaEstrazione)) return;
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

        Integer posizioneMedia = 9;
        frequenzeEstrattePrecedenti = frequenzeEstrattePrecedenti.sort();
        frequenzeEstratte = frequenzeEstratte.sort();
        log("Frequenze estratte precedenti", frequenzeEstrattePrecedenti.concatenaDash());
        log("Frequenze estratte attuali   ", frequenzeEstratte.concatenaDash());
        log("MIN FREQ:", fre.min("freq"), "   MAX FREQ:", fre.max("freq"));
        Integer low = frequenzeEstrattePrecedenti.get(posizioneMedia - 2);
        Integer high = frequenzeEstrattePrecedenti.get(posizioneMedia + 2);
        PList<Integer> numeriIntercettati = beccatiSottoFrequenze(fre, low, high);
        generaGiocatePariDispari(numeriIntercettati, 5, pl(3, 4, 5, 6));
        low = frequenzeEstrattePrecedenti.get(1);
        high = frequenzeEstrattePrecedenti.get(6);
        numeriIntercettati = beccatiSottoFrequenze(fre, low, high);
        generaGiocatePariDispari(numeriIntercettati, 5, pl(3, 4, 5, 6));
        low = frequenzeEstrattePrecedenti.get(13);
        high = frequenzeEstrattePrecedenti.get(18);
        numeriIntercettati = beccatiSottoFrequenze(fre, low, high);
        generaGiocatePariDispari(numeriIntercettati, 5, pl(7, 8));
        numeriIntercettati = beccatiFrequenzePuntuali(fre, getFrequenzePuntuali(frequenzeEstrattePrecedenti, pl(5, 6, 7, 15, 16)));
        generaGiocatePariDispari(numeriIntercettati, 5, pl(7, 8));
        salvaFrequenze();
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
            if (posizioni.contains(i + 1)) frequenzePuntuali.add(frequenzeEstrattePrecedenti.get(i));
        }
        return frequenzePuntuali;
    }

    private Integer scegliNumeroCasuale(PList<Integer> numeri) {
        Integer max = numeri.size() - 1;
        return numeri.get(generaNumeroCasuale(0, max));
    }

    private void generaGiocatePariDispari(PList<Integer> numeriIntercettati, Integer quanteGiocate, PList<Integer> lunghezzeGiocateAmmesse) {
        for (int i = 1; i <= quanteGiocate; i++) {
            PList<Integer> sf = p.prob50() ? pari(numeriIntercettati) : dispari(numeriIntercettati);
            giocateMultiple.add(generaGiocataDa(sf, scegliNumeroCasuale(lunghezzeGiocateAmmesse)));
        }
    }

    private void generaGiocate(PList<Integer> numeriIntercettati, Integer quanteGiocate, PList<Integer> lunghezzeGiocateAmmesse) {
        for (int i = 1; i <= quanteGiocate; i++) {
            giocateMultiple.add(generaGiocataDa(numeriIntercettati, scegliNumeroCasuale(lunghezzeGiocateAmmesse)));
        }
    }

    private PList<Integer> beccatiSottoFrequenze(PList<Frequenza> fre, Integer minFreq, Integer maxFreq) throws Exception {
        PList<Integer> sottoFrequenze = fre.between("freq", minFreq, maxFreq).find().narrow("numero");
        log("Quanti numeri sviluppati per le sottofrequenze: [", minFreq, ",", maxFreq, "] ", sottoFrequenze.size(), " [", pari(sottoFrequenze).size(), " pari e ", dispari(sottoFrequenze).size(), " dispari ]", sottoFrequenze.sort().concatenaDash());
        PList<Integer> beccati = estrazioni.getFirstElement().getEstrazione().intersection(sottoFrequenze);
        log("Beccati ", beccati.size(), " numeri ", beccati.concatenaDash(), "  dalle sottofrequenze");
        log("Beccati ", pari(beccati).size(), " pari ", pari(beccati).concatenaDash(), "  dalle sottofrequenze");
        log("Beccati ", dispari(beccati).size(), " dispari ", dispari(beccati).concatenaDash(), "  dalle sottofrequenze");
        return sottoFrequenze;
    }

    private PList<Integer> beccatiFrequenzePuntuali(PList<Frequenza> fre, PList<Integer> frequenze) throws Exception {
        PList<Integer> sottoFrequenze = fre.in("freq", frequenze).find().narrow("numero");
        log("Quanti numeri sviluppati per le frequenze puntuali: [", frequenze.concatenaDash(), "] ", sottoFrequenze.size(), " [", pari(sottoFrequenze).size(), " pari e ", dispari(sottoFrequenze).size(), " dispari ]", sottoFrequenze.sort().concatenaDash());
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

    private PList<Integer> generaGiocataDa(PList<Integer> lista, Integer quantiNumeri) {
        if (lista.size() < quantiNumeri) quantiNumeri = lista.size();
        PList<Integer> giocata = pl();
        for (int i = 1; i <= quantiNumeri; i++) {
            int pos = generaNumeroCasuale(0, lista.size() - 1);
            int num = lista.get(pos);
            if (giocata.contains(num)) {
                i--;
                continue;
            }
            giocata.add(lista.get(pos));
        }
        return giocata.sort();
    }

    private void loadGiocate() {
        if (oro && doppioOro) oro = false;
        String sep = " ";
        for (String l : safe(readFile("giocate.txt"))) {
            sep = " ";
            if (l.indexOf(tab()) > -1) {
                sep = tab();
            }
            if (l.indexOf("-") > -1) {
                sep = "-";
            }
            if (l.indexOf(",") > -1) {
                sep = ",";
            }
            giocateMultiple.add(split(l, sep).toListInteger());
        }
        if (notNull(giocata))
            giocateMultiple.add(giocata);
    }

    private static List<Integer> generaGiocata(Integer lunghezza, Integer min, Integer max) {
        List<Integer> giocata = new ArrayList<>();
        for (int i = 1; i <= lunghezza; i++) {
            Integer numero = generaNumeroCasuale(min, max);
            if (giocata.contains(numero)) {
                i--;
                continue;
            }
            giocata.add(numero);
        }
        return giocata;
    }

    private static Integer generaNumeroCasuale(Integer min, Integer max) {
        Integer d = Double.valueOf(Math.floor(Math.random() * (max - min + 1)) + min).intValue();
        return d;
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
        if (estr.isGiocataExtra()) {
            v = vincite.get(estr.getGiocata().size()).eq("trovati", estr.getQuantiTrovatiExtra()).findOne();
            quota += v.getQuotaExtra();
        }
        return quota;
    }


    private void download() throws Exception {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.3");
        String giorno = Null(giornoDaScaricare) ? pd().toStringFormat("yyyy-MM-dd") : giornoDaScaricare;
        String fileURL = str(URL, giorno);

/*
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
        ts.load(new FileInputStream("C:/testi5/lotto5"), "anto71ok".toCharArray());
        tmf.init(ts);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
*/
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
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
                log("GIOCATI NUMERI:", giocata.concatenaDash());
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
        // estrazioni.sortDesc("quantiTrovati", "quantiTrovatiExtra");
        estrazioni.sortDesc("vincita");
        Integer vincite = estrazioni.gt("vincita", 0).find().size();
        log("-------------- VINCENTI ", vincite, "/", estrazioni.size(), "   ", percentuale(bd(vincite), bd(estrazioni.size())), "%");
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
        log("-------------- VINCENTI ", vincite, "/", estrazioni.size(), "   ", percentuale(bd(vincite), bd(estrazioni.size())), "%");
        int bilancio = vincitaTotale - spesaTotale;
        if (bilancio > 50 && bilancio <= 100) beep(97);
        if (bilancio > 100) beep(300);
        log("BILANCIO COMPLESSIVO", "   VINTI: ", money(bd(vincitaFinale)), "  SPESI: ", money(bd(spesaFinale)), "   BILANCIO:", money(bd(bilancioFinale)));
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
        PList<String> cont = calcolaFrequenze().narrow("freqString");
        writeFile(FREQUENZE, cont);
    }

    private PList<Frequenza> leggiFrequenze() {
        PList<String> cont = readFile(FREQUENZE);
        PList<Frequenza> freq = pl();
        for (String s : safe(cont)) {
            PList<String> arr = split(s, "-->");
            String frq = arr.getLastElement();
            Integer numero = getInteger(arr.getFirstElement());
            Integer fr = getInteger(frq);
            freq.add(new Frequenza(numero, fr));
        }
        return freq;
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

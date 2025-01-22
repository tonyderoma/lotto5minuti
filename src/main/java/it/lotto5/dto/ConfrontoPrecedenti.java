package it.lotto5.dto;

import it.eng.pilot.Color;
import it.eng.pilot.PDate;
import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ConfrontoPrecedenti extends PilotSupport {
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    private static final String URL = "https://www.lottologia.com/10elotto5minuti/archivio-estrazioni/?as=TXT&date=";
    private static final int BUFFER_SIZE = 4096;

    public static final String CONFRONTO_PRECEDENTI = "confrontoPrecedenti.txt";
    PList<Estrazione5Minuti> estrazioni = pl();

    private static final String FILE = "lotto5minuti.txt";
    private Integer giorniPassati = 10;
    private Integer almeno = 9;

    public static void main(String[] args) throws Exception {
        ConfrontoPrecedenti cp = new ConfrontoPrecedenti();
        cp.stampaEstrazioni(15);
    }

    public ConfrontoPrecedenti() {
    }

    public ConfrontoPrecedenti(Integer giorniPassati, Integer almeno) {
        this.giorniPassati = giorniPassati;
        this.almeno = almeno;
    }

    public void autorun() throws Exception {
        while (true) {
            attendiSecondi(10);
            if (now().getMinuti() % 5 == 0) {
                log(lfn(2));
                log("Scattati i 5 minuti ", now().getOraCompleta(), " procedo con la verifica dei numeri in comune con le estrazioni passate!!!!!");
                attendiSecondi(10);
                run();
                attendiMinuti(1);
            }
        }
    }


    public void run() throws Exception {
        download(now());
        loadEstrazioni();
        verifica(giorniPassati, almeno);
    }


    public void run_(PList<Estrazione5Minuti> estrazioni) throws Exception {
        this.estrazioni = estrazioni;
        verifica(giorniPassati, almeno);
    }


    private void analizza() throws Exception {

        for (String s : readFile(CONFRONTO_PRECEDENTI)) {


        }

    }


    private void stampaEstrazioni(int g) throws Exception {
        PList<String> valori = pl();
        PList<PList<Integer>> storico = pl();
        for (int i = g; i >= 1; i--) {
            PDate data = giorniFa(i);
            download(data);
            PList<Estrazione5Minuti> estrazioni_ = loadEstrazioniPassate(data);
            for (Estrazione5Minuti e : estrazioni_.inverti()) {
                storico.add(e.getEstrazione());
                valori.add(e.getEstrazione().concatena(space()));
            }
        }
        download(now());
        for (Estrazione5Minuti e : loadEstrazioni().inverti()) {
            storico.add(e.getEstrazione());
            valori.add(e.getEstrazione().concatena(space()));
        }
        Map<Integer, Integer> freqs = new HashMap<>();
        for (PList<Integer> lista : storico) {
            for (Integer i : lista) {
                if (freqs.containsKey(i)) {
                    freqs.put(i, freqs.get(i) + 1);
                } else {
                    freqs.put(i, 1);
                }
            }
        }
        PList<Frequenza> ff = pl();
        for (Map.Entry<Integer, Integer> entry : freqs.entrySet()) {
            Frequenza f = new Frequenza(entry.getKey(), entry.getValue());
            ff.add(f);
        }

        PList<Integer> numeri = ff.sortDesc("freq").narrow("numero");
        PList<Integer> posizioni = pl();
        for (Integer i : estrazioni.getLastElement().getEstrazione()) {
            posizioni.add(numeri.indexOf(i) + 1);
        }
        log("Estratti", biancoGrassetto(estrazioni.getLastElement().getEstrazione().concatenaDash()));
        //log("Posizioni classifica storico frequenze", posizioni.concatenaDash());
        log("Posizioni ordinate classifica storico frequenze", biancoGrassetto(posizioni.sort().concatenaDash()));
        writeFile("storicoFrequenze.txt", ff.sortDesc("freq"));
        writeFile("storicoEstrazioni.txt", valori);
    }


    private void verifica(int g, int quanti) throws Exception {
        PList<Presi> presi = pl();

        PList<Integer> ultima = estrazioni.getFirstNotNullElement().getEstrazione();
        log("VERIFICO", estrazioni.getFirstElement().getNumero(), dash(), estrazioni.getFirstElement().getDataString(), tab(), biancoGrassetto(ultima.concatenaDash()));
        for (int i = 1; i <= g; i++) {
            PDate data = giorniFa(i);
            download(data);
            PList<Estrazione5Minuti> estrazioni_ = loadEstrazioniPassate(data);
            for (Estrazione5Minuti e : estrazioni_) {
                PList<Integer> inComune = ultima.intersection(e.getEstrazione());
                int q = inComune.size();
                if (q >= quanti) {
                    String s = getNumeriColorati(e.getEstrazione(), inComune);
                    String descr = str(e.getNumero(), tab(), e.getDataString(), arrow(), s);
                    int giorni = now().daysBetween(data);
                    String message = str(quadra(), strSepPipe(q, giorni, e.getNumero()), quadraClose());
                    presi.add(new Presi(estrazioni.getFirstElement().getNumero(), giorni, q, descr, message, e.getNumero()));
                }
            }
        }
        if (notNull(presi)) {
            String m = str(presi.getFirstElement().getNumero().toString(), arrow());
            for (Presi pr : presi.sort("giorni").sortDesc("quanti").cutToFirst(5)) {
                m = strSepSpace(m, pr.getMessage());
            }
            log(m);
            appendFile(CONFRONTO_PRECEDENTI, pl(m));
        }
    }

    private String getNumeriColorati(PList<Integer> numeri, PList<Integer> interc) throws Exception {
        numeri.cleanNull();
        String out = " ";
        String num = "";
        for (Integer n : numeri) {
            num = interc.contains(n) ? rosso(n.toString()) : biancoGrassetto(n.toString());
            out = str(out, num, dash());
        }
        return out.substring(0, out.length() - 1);
    }


    private PList<Estrazione5Minuti> loadEstrazioni() {
        estrazioni = pl();
        PList<String> cont = readFile(FILE);
        if (Null(cont)) return pl();
        cont = pl(cont.subList(3, cont.size() - 3));
        cont.forEach((s -> {
            estrazioni.add(new Estrazione5Minuti((s)));
        }));
        PList<String> valori = pl();
        for (Estrazione5Minuti es : estrazioni) {
            valori.add(es.getEstrazione().concatena(space()));
        }
        writeFile("numeriEstratti.txt", valori);
        return estrazioni;
    }

    private PList<Estrazione5Minuti> loadEstrazioniPassate(PDate d) {
        PList<Estrazione5Minuti> estrazioni = pl();
        PList<String> cont = readFile(str("estrazioni/", d.toStringFormat("dd-MM-YYYY"), dot(), "txt"));
        if (Null(cont)) return pl();
        cont = pl(cont.subList(3, cont.size() - 3));
        cont.forEach((s -> {
            estrazioni.add(new Estrazione5Minuti((s)));
        }));
        return estrazioni;
    }

    private void download(PDate d) throws Exception {
        if (!d.isToday()) {
            if (fileExists(str("estrazioni/", d.toStringFormat("dd-MM-YYYY"), dot(), "txt"))) return;
        }
        java.net.URL url = new URL(str(URL, d.toStringFormat(YYYY_MM_DD)));
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            // opens an output stream to save into file
            FileOutputStream outputStream = null;
            if (d.isToday()) {
                outputStream = new FileOutputStream(FILE);
            } else {
                outputStream = new FileOutputStream(str("estrazioni/", d.toStringFormat("dd-MM-YYYY"), dot(), "txt"));
            }
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


    private String verde(String s) {
        return color(s, Color.VERDE, true, true, false, false);
    }

    private String rosso(String s) {
        return color(s, Color.ROSSO, true, true, false, false);
    }

    private String biancoGrassetto(String s) {
        return color(s, Color.BIANCO, true, true, false, false);
    }

}

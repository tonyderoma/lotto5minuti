package it.lotto5;

import it.eng.pilot.PDate;
import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;
import it.lotto5.dto.Estrazione5Minuti;
import it.lotto5.dto.Frequenza;
import org.apache.log4j.BasicConfigurator;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class StudioFrequenze extends PilotSupport {
    private static final String URL = "https://www.lottologia.com/10elotto5minuti/archivio-estrazioni/?as=TXT&date=";
    private static final int BUFFER_SIZE = 4096;
    PList<Estrazione5Minuti> estrazioni = pl();

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        StudioFrequenze sf = new StudioFrequenze();
        //sf.calcolaFrequenzeFinoA((198,"14-09-2024"));
        //sf.run1(180, "23-09-2024");
        //sf.run1(180, "24-09-2024");
        sf.inComune();

    }


    private void inComune() throws Exception {
        //download(now());
        PList<Estrazione5Minuti> estrazioni = loadEstrazioni("29-09-2024");
        PList<Integer> ultima = estrazioni.get(0).getEstrazione();
        PList<Integer> es1 = pl(estrazioni.randomOne().getEstrazione().subList(0, 3));
        PList<Integer> es2 = pl(estrazioni.randomOne().getEstrazione().subList(3, 7));
        PList<Integer> es3 = pl(estrazioni.randomOne().getEstrazione().subList(7, 11));
        PList<Integer> es4 = pl(estrazioni.randomOne().getEstrazione().subList(11, 15));
        PList<Integer> es5 = pl(estrazioni.randomOne().getEstrazione().subList(15, 20));
        PList<Integer> totale = es1.aggiungiList(es2, es3, es4, es5);
        log(totale.concatenaDash());
        PList<Integer> inComune = ultima.intersection(totale);
        log("In comune", inComune.size(), " numeri:", inComune.sort().concatenaDash());

      /*  for (int i = 1; i <= 30; i++) {
            PList<Integer> numeri = estrazioni.randomOne().getEstrazione();
            log(numeri.size());
            log("In comune ", ultima.intersection(numeri));
        }*/
        /*PList<Integer> numbers = pl();
        for (int j = 1; j < 130; j++) {
            numbers.clear();
            for (int i = 0; i < 8; i++) {
                numbers.add(estrazioni.randomOne().getEstrazione().get(i));
            }
            PList<Integer> intercettati = ultima.intersection(numbers);
            log("In comune", intercettati.size(), tab(), intercettati.concatenaDash());
        }
*/

    }

    private void run1(Integer numeroEstrazione, String giorno) throws Exception {
        PList<Integer> frequenzeEstratte = pl();
        if (numeroEstrazione < 1 || numeroEstrazione > 288) return;
        PList<Estrazione5Minuti> estrazioni = loadEstrazioni(giorno);
        PList<Frequenza> fre = calcolaFrequenzeFinoA(numeroEstrazione, giorno);
        if (notNull(fre))
            for (Integer i : estrazioni.eq("numero", numeroEstrazione).findOne().getEstrazione()) {
                Frequenza f = fre.eq("numero", i).findOne();
                if (Null(f)) frequenzeEstratte.add(0);
                else
                    frequenzeEstratte.add(f.getFreq());
            }
        log("Frequenze estratte per estrazione", numeroEstrazione, colon(), space(), frequenzeEstratte.distinct().sort().concatenaDash());
    }

    private void run() throws Exception {
        PList<Frequenza> frequenze1 = calcolaFrequenzeFinoA(172, "22-09-2024");
        PList<Frequenza> frequenze2 = calcolaFrequenzeFinoA(172, "23-09-2024");
        log("Frequenze 1: ", frequenze1.narrowDistinct("freq").sort().concatenaDash());
        log("Frequenze 2: ", frequenze2.narrowDistinct("freq").sort().concatenaDash());
    }

    private PList<Frequenza> calcolaFrequenzeFinoA(Integer numeroEstrazione, String giorno) throws Exception {
        if (numeroEstrazione < 1 || numeroEstrazione > 288) return pl();
        PList<Estrazione5Minuti> estrazioni = loadEstrazioni(giorno);
        PList<Estrazione5Minuti> precedenti = estrazioni.lt("numero", numeroEstrazione).find();
        return calcolaFrequenze(precedenti);
    }

    private PList<Frequenza> calcolaFrequenze(PList<Estrazione5Minuti> estrazioni) throws Exception {
        Map<Integer, Integer> frequenze = new HashMap<>();
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

    private PList<Estrazione5Minuti> loadEstrazioni(String giorno) {
        PList<Estrazione5Minuti> estrazioni = pl();
        PList<String> cont = readFile(str("estrazioni/", giorno, dot(), "txt"));
        if (Null(cont)) return pl();
        cont = pl(cont.subList(3, cont.size() - 3));
        cont.forEach((s -> {
            estrazioni.add(new Estrazione5Minuti((s)));
        }));
        return estrazioni;
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
}

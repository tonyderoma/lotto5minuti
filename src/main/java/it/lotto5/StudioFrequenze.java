package it.lotto5;

import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;
import it.lotto5.dto.Estrazione5Minuti;
import it.lotto5.dto.Frequenza;
import org.apache.log4j.BasicConfigurator;

import java.util.HashMap;
import java.util.Map;

public class StudioFrequenze extends PilotSupport {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        StudioFrequenze sf = new StudioFrequenze();
        //sf.calcolaFrequenzeFinoA((198,"14-09-2024"));
        sf.run1(180, "23-09-2024");
        sf.run1(180, "24-09-2024");
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
}

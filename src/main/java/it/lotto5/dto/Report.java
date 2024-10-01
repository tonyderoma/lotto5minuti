package it.lotto5.dto;

import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;

public class Report extends PilotSupport {
    PList<Integer> frequenze = pl();
    PList<Integer> sviluppati = pl();
    PList<Integer> intercettati = pl();

    public Report(PList<Integer> frequenze, PList<Integer> sviluppati, PList<Integer> intercettati) {
        this.frequenze = frequenze;
        this.sviluppati = sviluppati;
        this.intercettati = intercettati;
    }

    public Report(PList<Integer> sviluppati, PList<Integer> intercettati) {
        this.sviluppati = sviluppati;
        this.intercettati = intercettati;
    }

    public PList<Integer> getFrequenze() {
        return frequenze;
    }

    public void setFrequenze(PList<Integer> frequenze) {
        this.frequenze = frequenze;
    }

    public String toString() {
        String rapporti = str(intercettati.size(), slash(), sviluppati.size(), tab(), " [Pari: ", intercettati.pari().size(), slash(), sviluppati.pari().size(), quadraClose(), space(2), " [Dispari: ", intercettati.dispari().size(), slash(), sviluppati.dispari().size(), quadraClose());
        return str(rapporti, "   Frequenze:", quadra(), frequenze.sort().concatenaDash(), quadraClose(), "   Sviluppati ", sviluppati.size(), " numeri: ", sviluppati.sort().concatenaDash(), "  Intercettati ", intercettati.size(), " numeri: ", intercettati.sort().concatenaDash());
    }

    public PList<Integer> getSviluppati() {
        return sviluppati;
    }

    public void setSviluppati(PList<Integer> sviluppati) {
        this.sviluppati = sviluppati;
    }

    public PList<Integer> getIntercettati() {
        return intercettati;
    }

    public void setIntercettati(PList<Integer> intercettati) {
        this.intercettati = intercettati;
    }

}

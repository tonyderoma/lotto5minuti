package it.lotto5.dto;

import it.eng.pilot.Color;
import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;

public class Report extends PilotSupport {
    PList<Integer> frequenze = pl();
    PList<Integer> sviluppati = pl();
    PList<Integer> intercettati = pl();

    private String tipoGioco;

    public Report(PList<Integer> frequenze, PList<Integer> sviluppati, PList<Integer> intercettati) {
        this.frequenze = frequenze;
        this.sviluppati = sviluppati;
        this.intercettati = intercettati;
    }

    public Report(String tipoGioco, PList<Integer> frequenze, PList<Integer> sviluppati, PList<Integer> intercettati) {
        this.tipoGioco = tipoGioco;
        this.frequenze = frequenze;
        this.sviluppati = sviluppati;
        this.intercettati = intercettati;
    }

    public Report(PList<Integer> sviluppati, PList<Integer> intercettati) {
        this.sviluppati = sviluppati;
        this.intercettati = intercettati;
    }

    public Report(String tipoGioco, PList<Integer> sviluppati, PList<Integer> intercettati) {
        this.tipoGioco = tipoGioco;
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
        return str(getTipoGioco(), tab(), color(rapporti, Color.ROSSO, true, true, false, false), "   Frequenze:", quadra(), color(frequenze.sort().concatenaDash(), Color.VERDE, true, true, false, false), quadraClose(), "   Sviluppati ", sviluppati.size(), " numeri: ", color(sviluppati.sort().concatenaDash(), Color.BIANCO_CORNICE_VUOTO, true, true, false, false), "  Intercettati ", intercettati.size(), " numeri: ", color(intercettati.sort().concatenaDash(), Color.VERDE, true, true, false, false));
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


    public String getTipoGioco() {
        return color(tipoGioco, Color.BIANCO, false, true, false, false);
    }

    public void setTipoGioco(String tipoGioco) {
        this.tipoGioco = tipoGioco;
    }
}

package it.lotto5.dto;

import it.eng.pilot.Color;
import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;

public class Report extends PilotSupport {
    PList<Integer> frequenze = pl();
    PList<Integer> sviluppati = pl();
    PList<Integer> intercettati = pl();

    private Integer costo;

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
        String rapporto = str(intercettati.size(), slash(), sviluppati.size());
        String rapportoPari = str(" [Pari: ", intercettati.pari().size(), slash(), sviluppati.pari().size(), quadraClose());
        String rapportoDispari = str(" [Dispari: ", intercettati.dispari().size(), slash(), sviluppati.dispari().size(), quadraClose());
        String rapporti = str(impostaColore(intercettati.size(), rapporto), impostaColore(intercettati.pari().size(), rapportoPari), space(2), impostaColore(intercettati.dispari().size(), rapportoDispari));
        return str(impostaColore(intercettati.size(), getTipoGioco()), tab(), giallo(moneyEuro(bd(getCosto()))), tab(), rapporti, "   Frequenze:", quadra(), color(frequenze.sort().concatenaDash(), Color.VERDE, true, true, false, false), quadraClose(), "   Sviluppati ", sviluppati.size(), " numeri: ", color(sviluppati.sort().concatenaDash(), Color.BIANCO_CORNICE_VUOTO, true, true, false, false), "  Intercettati ", intercettati.size(), " numeri: ", color(intercettati.sort().concatenaDash(), Color.VERDE, true, true, false, false));
    }


    private String impostaColore(Integer n, String s) {
        return n >= 2 ? verde(s) : rosso(s);
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

    private String verde(String s) {
        return color(s, Color.VERDE, true, true, false, false);
    }

    private String giallo(String s) {
        return color(s, Color.GIALLO, true, true, false, false);
    }

    private String rosso(String s) {
        return color(s, Color.ROSSO, true, true, false, false);
    }

    private String bianco(String s) {
        return color(s, Color.BIANCO_CORNICE_VUOTO, true, true, false, false);
    }


    public Integer getCosto() {
        return costo;
    }

    public void setCosto(Integer costo) {
        this.costo = costo;
    }
}

package it.lotto5.dto;

import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;

public class Ampiezza extends PilotSupport {

    public static final String FUTURI = "Futuri: ";
    private Integer freq;
    private Integer ampiezza;

    private PList<Integer> numeriIntercettati = pl();

    private PList<Integer> numeriIntercettatiFuturi = pl();

    private Integer quantiIntercettati = 0;

    private Integer quantiIntercettatiFuturi = 0;


    public Ampiezza(Integer freq, Integer ampiezza) {
        this.freq = freq;
        this.ampiezza = ampiezza;
    }

    public Ampiezza(Integer freq, Integer ampiezza, Integer quantiIntercettati) {
        this.freq = freq;
        this.ampiezza = ampiezza;
        this.quantiIntercettati = quantiIntercettati;
    }

    public Ampiezza(Integer freq, Integer ampiezza, Integer quantiIntercettati, Integer quantiIntercettatiFuturi) {
        this.freq = freq;
        this.ampiezza = ampiezza;
        this.quantiIntercettati = quantiIntercettati;
        this.quantiIntercettatiFuturi = quantiIntercettatiFuturi;
    }


    public Integer getFreq() {
        return freq;
    }

    public void setFreq(Integer freq) {
        this.freq = freq;
    }

    public Integer getAmpiezza() {
        return ampiezza;
    }

    public void setAmpiezza(Integer ampiezza) {
        this.ampiezza = ampiezza;
    }

    public String toString() {
        String intercFuturi = "";
        if (notNull(getNumeriIntercettatiFuturi())) {
            Integer l = getNumeriIntercettati().concatenaDash().length();
            intercFuturi = str(intercFuturi, space(15 - l), FUTURI, getNumeriIntercettatiFuturi().size(), colon(), space(4), getNumeriIntercettatiFuturi().concatenaDash());
        }
        return str("Frequenza ", getFreq(), arrow(), getAmpiezza(), tab(), getNumeriIntercettati().size(), " numeri intercettati: ", getNumeriIntercettati().concatenaDash(), intercFuturi);
    }

    public void addNumeroIntercettato(Integer numero) {
        if (!getNumeriIntercettati().contains(numero))
            getNumeriIntercettati().add(numero);
    }

    public void addNumeroIntercettatoFuturo(Integer numero) {
        if (!getNumeriIntercettatiFuturi().contains(numero))
            getNumeriIntercettatiFuturi().add(numero);
    }


    public PList<Integer> getNumeriIntercettati() {
        return numeriIntercettati;
    }

    public void setNumeriIntercettati(PList<Integer> numeriIntercettati) {
        this.numeriIntercettati = numeriIntercettati;
    }

    public Integer getQuantiIntercettati() {
        return quantiIntercettati;
    }

    public void setQuantiIntercettati(Integer quantiIntercettati) {
        this.quantiIntercettati = quantiIntercettati;
    }

    public PList<Integer> getNumeriIntercettatiFuturi() {
        return numeriIntercettatiFuturi;
    }

    public void setNumeriIntercettatiFuturi(PList<Integer> numeriIntercettatiFuturi) {
        this.numeriIntercettatiFuturi = numeriIntercettatiFuturi;
    }

    public Integer getQuantiIntercettatiFuturi() {
        return quantiIntercettatiFuturi;
    }

    public void setQuantiIntercettatiFuturi(Integer quantiIntercettatiFuturi) {
        this.quantiIntercettatiFuturi = quantiIntercettatiFuturi;
    }
}

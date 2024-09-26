package it.lotto5.dto;

public class Ampiezza {

    private Integer freq;
    private Integer quantiNumeri;


    public Ampiezza(Integer freq, Integer numeri) {
        this.freq = freq;
        this.quantiNumeri = numeri;
    }

    public Integer getFreq() {
        return freq;
    }

    public void setFreq(Integer freq) {
        this.freq = freq;
    }

    public Integer getQuantiNumeri() {
        return quantiNumeri;
    }

    public void setQuantiNumeri(Integer quantiNumeri) {
        this.quantiNumeri = quantiNumeri;
    }

    public String toString() {
        return "Frequenza " + getFreq() + "-->" + getQuantiNumeri() + " numeri";
    }
}

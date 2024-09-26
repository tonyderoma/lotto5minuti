package it.lotto5.dto;

public class Ampiezza {

    private Integer freq;
    private Integer ampiezza;


    public Ampiezza(Integer freq, Integer ampiezza) {
        this.freq = freq;
        this.ampiezza = ampiezza;
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
        return "Frequenza " + getFreq() + "-->" + getAmpiezza() + " numeri";
    }
}

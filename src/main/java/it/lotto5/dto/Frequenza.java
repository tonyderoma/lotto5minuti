package it.lotto5.dto;

public class Frequenza {

    private Integer numero;
    private Integer freq;

    public Frequenza(Integer numero, Integer freq) {
        this.numero = numero;
        this.freq = freq;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getFreq() {
        return freq;
    }

    public void setFreq(Integer freq) {
        this.freq = freq;
    }

    public String toString() {
        return getNumero() + "-->" + getFreq();
    }

}

package it.lotto5.dto;

import it.eng.pilot.PilotSupport;

public class Presi extends PilotSupport {


    private Integer numero;

    private Integer numero2;
    private Integer quanti;

    private Integer giorni;
    private String descr;

    private String message;


    public Presi(Integer numero, Integer giorni, Integer quanti, String descr, String message, Integer numero2) {
        this.numero = numero;
        this.giorni = giorni;
        this.quanti = quanti;
        this.descr = descr;
        this.message = message;
        this.numero2 = numero2;
    }

    public Integer getQuanti() {
        return quanti;
    }

    public void setQuanti(Integer quanti) {
        this.quanti = quanti;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String toString() {
        return str(quanti, tab(), descr);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getGiorni() {
        return giorni;
    }

    public void setGiorni(Integer giorni) {
        this.giorni = giorni;
    }

    public Integer getNumero2() {
        return numero2;
    }

    public void setNumero2(Integer numero2) {
        this.numero2 = numero2;
    }
}

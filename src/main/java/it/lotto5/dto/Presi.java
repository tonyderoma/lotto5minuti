package it.lotto5.dto;

import it.eng.pilot.PilotSupport;

public class Presi extends PilotSupport {


    private Integer numero;
    private Integer quanti;
    private String descr;

    private String message;


    public Presi(Integer numero, Integer quanti, String descr, String message) {
        this.numero = numero;
        this.quanti = quanti;
        this.descr = descr;
        this.message = message;
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
}

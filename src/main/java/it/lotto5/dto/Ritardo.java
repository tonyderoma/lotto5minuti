package it.lotto5.dto;

import it.eng.pilot.PilotSupport;

public class Ritardo extends PilotSupport {

    private Integer numero;
    private Integer ritardo;

    private String data;

    public Ritardo(Integer numero, Integer ritardo, String data) {
        this.numero = numero;
        this.ritardo = ritardo;
        this.data = data;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getRitardo() {
        return ritardo;
    }

    public void setRitardo(Integer ritardo) {
        this.ritardo = ritardo;
    }

    public String toString() {
        return getNumero().toString().concat(arrow()).concat(getRitardo().toString()).concat(" estrazioni dalle ").concat(pd(getData()).getOraCompleta()).concat(" da ben...").concat(elapsedTime(toDate(getData()), now()));
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

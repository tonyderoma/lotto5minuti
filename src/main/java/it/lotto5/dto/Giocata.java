package it.lotto5.dto;

import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;

public class Giocata extends PilotSupport {

    private String tipo;
    private PList<PList<Integer>> giocate = pl();

    private Integer vincita = 0;
    private Integer spesa = 0;


    public Giocata(String tipo, PList<PList<Integer>> giocate) {
        this.tipo = tipo;
        this.giocate = giocate;
    }

    public Giocata(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public PList<PList<Integer>> getGiocate() {
        return giocate;
    }

    public void setGiocate(PList<PList<Integer>> giocate) {
        this.giocate = giocate;
    }

    public void addGiocata(PList<Integer> giocata) {
        giocate.add(giocata);
    }

    public Integer getVincita() {
        return vincita;
    }

    public void setVincita(Integer vincita) {
        this.vincita = vincita;
    }

    public Integer getSpesa() {
        return spesa;
    }

    public void setSpesa(Integer spesa) {
        this.spesa = spesa;
    }
}

package it.lotto5.dto;

import it.eng.pilot.Color;
import it.eng.pilot.PilotSupport;

public class ConfrontoEstrazioni extends PilotSupport {


    private Estrazione5Minuti estrazioneBase;
    private Integer inComune;

    private Estrazione5Minuti estrazione;

    public ConfrontoEstrazioni() {
    }

    private String verde(String s) {
        return color(s, Color.VERDE, true, true, false, false);
    }

    private String giallo(String s) {
        return color(s, Color.GIALLO, true, true, false, false);
    }

    public Integer getInComune() {
        return inComune;
    }

    public void setInComune(Integer inComune) {
        this.inComune = inComune;
    }

    public Estrazione5Minuti getEstrazione() {
        return estrazione;
    }

    public void setEstrazione(Estrazione5Minuti estrazione) {
        this.estrazione = estrazione;
    }

    public String toString() {
        return str(giallo(str("" + getEstrazioneBase().getNumero(), tab(), getEstrazioneBase().getDataString())), arrow(), " in comune ", verde("" + inComune), " numeri con l'estrazione N° ", verde(str(getEstrazione().getNumero(), tab(), getEstrazione().getDataString())));
    }

    public Estrazione5Minuti getEstrazioneBase() {
        return estrazioneBase;
    }

    public void setEstrazioneBase(Estrazione5Minuti estrazioneBase) {
        this.estrazioneBase = estrazioneBase;
    }

    public String getNumeroOra() {

        return str(space(2), quadra(), space(2 - (getInComune() + "").length()), getInComune(), dash(), getEstrazione().getNumero(), str("°", space(3 - (getEstrazione().getNumero() + "").length())), quadraClose(), space(2));
    }


}

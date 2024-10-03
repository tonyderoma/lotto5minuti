package it.lotto5.dto;

import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;

public class Parametri extends PilotSupport {

    private PList<Report> report = pl();

    private PList<Frequenza> frequenze = pl();

    private PList<Ampiezza> ampiezze = pl();

    public Parametri(PList<Report> report, PList<Frequenza> frequenze, PList<Ampiezza> ampiezze) {
        this.report = report;
        this.frequenze = frequenze;
        this.ampiezze = ampiezze;
    }

    public PList<Report> getReport() {
        return report;
    }

    public void setReport(PList<Report> report) {
        this.report = report;
    }

    public PList<Frequenza> getFrequenze() {
        return frequenze;
    }

    public void setFrequenze(PList<Frequenza> frequenze) {
        this.frequenze = frequenze;
    }

    public PList<Ampiezza> getAmpiezze() {
        return ampiezze;
    }

    public void setAmpiezze(PList<Ampiezza> ampiezze) {
        this.ampiezze = ampiezze;
    }
}

package it.lotto5.dto;

import it.eng.pilot.PilotSupport;

public class Vincita extends PilotSupport {

    private Integer quota = 0;
    private Integer trovati = 0;
    private Integer quotaOro = 0;
    private Integer quotaDoppioOro = 0;
    private Integer quotaExtra = 0;

    public Vincita(Integer trovati, Integer quota, Integer quotaOro) {
        super();
        this.trovati = trovati;
        this.quota = quota;
        this.quotaOro = quotaOro;
    }

    public Vincita(Integer trovati, Integer quota, Integer quotaOro, Integer quotaDoppioOro) {
        super();
        this.trovati = trovati;
        this.quota = quota;
        this.quotaOro = quotaOro;
        this.quotaDoppioOro = quotaDoppioOro;
    }

    public Vincita(Integer trovati, Integer quota, Integer quotaOro, Integer quotaDoppioOro, Integer quotaExtra) {
        super();
        this.trovati = trovati;
        this.quota = quota;
        this.quotaOro = quotaOro;
        this.quotaDoppioOro = quotaDoppioOro;
        this.quotaExtra = quotaExtra;
    }

    public Vincita(Integer trovati, Integer quota) {
        super();
        this.trovati = trovati;
        this.quota = quota;
    }

    public Integer getTrovati() {
        return trovati;
    }

    public void setTrovati(Integer trovati) {
        this.trovati = trovati;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Integer getQuotaOro() {
        return quotaOro;
    }

    public void setQuotaOro(Integer quotaOro) {
        this.quotaOro = quotaOro;
    }

    public Integer getQuotaDoppioOro() {
        return quotaDoppioOro;
    }



    public Integer getQuotaExtra() {
        return quotaExtra;
    }

    public void setQuotaExtra(Integer quotaExtra) {
        this.quotaExtra = quotaExtra;
    }


}

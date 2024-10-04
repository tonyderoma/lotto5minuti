package it.lotto5.dto;

import it.eng.pilot.Color;
import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;

import java.util.Date;


public class Estrazione5Minuti extends PilotSupport {


    private int numero;
    private Date data;
    private PList<Integer> estrazione = pl();
    private PList<Integer> extra = pl();
    private Integer oro;
    private Integer doppioOro;
    private PList<Integer> giocata = pl();
    private Integer vincita = 0;

    private Integer vincitaNormale = 0;

    private Integer vincitaExtra = 0;
    private Integer spesa = 1;

    private Integer spesaTotale = 0;
    private boolean oroGiocato;
    private boolean doppioOroGiocato;
    private boolean giocataExtra;

    private String msgTrovati = "Trovati: ";

    private String msgTrovatiVincenti = "";
    private String msgTrovatiExtra = "Trovati Extra: ";

    private String msgOro = "PRESO ORO ";
    private String msgDoppioOro = "PRESO DOPPIO ORO ";

    public Estrazione5Minuti(String row) {
        String nums = substring(row, tab(), false, false, tab2(), false, true);
        String ori = substring(row, tab2(), false, true, null, false, false);
        PList<String> listaOri = split(ori, tab());
        String oro = listaOri.getFirstElement();
        setOro(getInteger(oro));
        String doppioOro = listaOri.get(1);
        setDoppioOro(getInteger(doppioOro));
        PList<Integer> extra = split(listaOri.getLastElement(), dot()).toListInteger();
        setExtra(extra);
        PList<String> elems = split(row, tab());
        PList<String> data = split(elems.getFirstElement(), slash());
        String d = data.getFirstElement();
        d = d.replace(dash(), slash());
        PList<String> last = split(data.getLastElement(), space());
        setNumero(getInteger(last.getFirstElement()));
        String ora = last.getLastElement().replace(dot(), colon());
        PList<String> dataElems = split(d, slash());
        String ita = strSep(slash(), dataElems.get(2), dataElems.get(1), dataElems.get(0));
        d = str(ita, space(), ora, ":00");
        setData(pd(d));
        setEstrazione(split(nums, tab()).toListInteger());
        msgOro += str(getOro(), colon());
        msgDoppioOro += str(getDoppioOro(), colon());
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public PList<Integer> getEstrazione() {
        return estrazione;
    }

    public void setEstrazione(PList<Integer> estrazione) {
        this.estrazione = estrazione;
    }

    public PList<Integer> getExtra() {
        return extra;
    }

    public void setExtra(PList<Integer> extra) {
        this.extra = extra;
    }

    public Integer getOro() {
        return oro;
    }

    public void setOro(Integer oro) {
        this.oro = oro;
    }

    public Integer getDoppioOro() {
        return doppioOro;
    }

    public void setDoppioOro(Integer doppioOro) {
        this.doppioOro = doppioOro;
    }

    public String getDataString() {
        return dateToStringhhmmss(getData());
    }

    public Integer getQuantiTrovati() {
        return intersection(getEstrazione(), getGiocata()).size();
    }

    private String ok(String s) {
        return color(s, Color.VERDE_CORNICE, true, true, false, false);
    }

    private String nok(String s) {
        return color(s, Color.BIANCO, false, false, false, true);
    }


    private String getColored(PList<Integer> interc) {
        String s = "";
        for (Integer i : getGiocata()) {
            s += str(interc.indexOf(i) > -1 ? ok(i.toString()) : nok(i.toString()), dash());
        }
        return s.substring(0, s.length() - 1);
    }

    private void addTrovati() {
        PList<Integer> intercettati = pl(intersection(getEstrazione(), getGiocata()));
        Integer tr = getQuantiTrovati();
        msgTrovati += str(tr, slash(), safe(getGiocata()).size(), getVincitaNormale() > 0 ? " [" + money(bd(getVincitaNormale())) + space() + getGiocata().concatenaDash() + "]  " : "  ");
        if (getVincitaNormale() > 0) {
            msgTrovatiVincenti += str(lf(), tabn(6), tr, slash(), safe(getGiocata()).size(), tab(), quadra(), moneyEuro(bd(getVincitaNormale())), space(3), getColored(intercettati), quadraClose());
        }
    }

    public void impostaTrovati() {
        addTrovati();
        addTrovatiExtra();
    }

    public String getMsgTrovati() {
        return msgTrovati;
    }

    private void addTrovatiExtra() {
        Integer tr = getQuantiTrovatiExtra();
        msgTrovatiExtra += str(tr, slash(), safe(getGiocata()).size(), getVincitaExtra() > 0 ? " [" + money(bd(getVincitaExtra())) + space() + getGiocata().concatenaDash() + "]  " : "  ");
    }


    private void addMessaggioOro() {
        msgOro += str(sn(presoOro()), space());
    }

    private void addMessaggioDoppioOro() {
        msgDoppioOro += str(sn(presoDoppioOro()), space());
    }

    public void impostaMsgOri() {
        addMessaggioOro();
        addMessaggioDoppioOro();
    }


    public String getMsgOro() {
        return msgOro;
    }


    public String getMsgDoppioOro() {
        return msgDoppioOro;
    }


    public String getMsgTrovatiExtra() {
        return msgTrovatiExtra;
    }

    public PList<Integer> getGiocata() {
        return giocata;
    }

    public void setGiocata(PList<Integer> giocata) {
        this.giocata = giocata;
    }

    public Integer getQuantiTrovatiExtra() {
        return intersection(getExtra(), getGiocata()).size();
    }

    public String toString() {
        String s = "";
        if (getVincita() > 0)
            s = str("VINCITA!!!!!...", money((bd(getVincita()))));
        return str(lf(), s, "    ", getDataString(), "   ", getMsgTrovati(), getMsgTrovatiExtra(),
                getMsgOro(), "    ", getMsgDoppioOro(), lf2());
    }

    public Integer getVincita() {
        return vincita;
    }


    public void addVincita(Integer vincita) {
        this.vincita += vincita;
    }

    public Integer getSpesa() {
        if (Null(getGiocata())) return 0;
        if (giocataExtra)
            spesa = 2;
        if (isDoppioOroGiocato())
            return spesa * 3;
        return isOroGiocato() ? spesa * 2 : spesa;
    }

    public void addSpesa() {
        this.spesaTotale += getSpesa();
    }

    public Integer getSpesaTotale() {
        return spesaTotale;
    }


    public boolean presoOro() {
        boolean ret = false;
        PList<Integer> inters = pl(intersection(getEstrazione(), getGiocata()));
        if (isOroGiocato() && !isDoppioOroGiocato()) {
            ret = inters.contains(getOro());
        } else if (isDoppioOroGiocato()) {
            ret = almenoUna(inters.contains(getOro()), inters.contains(getDoppioOro()));
        }
        return ret;
    }

    public boolean presoDoppioOro() {
        PList<Integer> inter = pl(intersection(getEstrazione(), getGiocata()));
        return isDoppioOroGiocato() ? inter.contains(getOro()) && inter.contains(getDoppioOro()) : false;
    }

    public boolean isOroGiocato() {
        return oroGiocato;
    }

    public void setOroGiocato(boolean oroGiocato) {
        this.oroGiocato = oroGiocato;
    }

    public boolean isDoppioOroGiocato() {
        return doppioOroGiocato;
    }

    public void setDoppioOroGiocato(boolean doppioOroGiocato) {
        this.doppioOroGiocato = doppioOroGiocato;
    }

    public boolean isGiocataExtra() {
        return giocataExtra;
    }

    public void setGiocataExtra(boolean giocataExtra) {
        this.giocataExtra = giocataExtra;
    }


    private boolean isCadenza(Integer n, Integer cadenza) {
        return zero((n - cadenza) % 10);
    }

    public String getCadenze() {
        return getCadenze(5);
    }

    public String getCadenze(Integer almeno) {
        String cad = "";
        PList<Integer> sottoEstrazione = pl();
        for (int i = 0; i <= 9; i++) {
            sottoEstrazione = pl();
            for (Integer numero : safe(estrazione)) {
                if (isCadenza(numero, i)) {
                    sottoEstrazione.add(numero);
                }
            }
            if (sottoEstrazione.size() >= almeno) {
                cad = str(cad, getDataString(), "   Cadenza ", i, ":   ", color(sottoEstrazione.concatenaDash(), Color.VIOLA_CORNICE, true, true, false, false), lf());
            }
        }
        return cad;
    }

    public PList<PList<Integer>> getConsecutivi(Integer n) {
        PList<PList<Integer>> consecutivi = pl();
        PList<Integer> successivi = pl();
        for (int i = 0; i < estrazione.size(); i++) {
            if (!successivi.contains(estrazione.get(i)))
                successivi.add(estrazione.get(i));
            if (i == estrazione.size() - 1) continue;
            if (estrazione.get(i).equals(estrazione.get(i + 1) - 1)) {
                successivi.add(estrazione.get(i + 1));
            } else {
                if (successivi.size() >= n) {
                    consecutivi.add(successivi);
                    successivi = pl();
                } else {
                    successivi = pl();
                }
            }

        }
        return consecutivi;
    }

    public Integer getVincitaNormale() {
        return vincitaNormale;
    }

    public void setVincitaNormale(Integer vincitaNormale) {
        this.vincitaNormale = vincitaNormale;
    }

    public Integer getVincitaExtra() {
        return vincitaExtra;
    }

    public void setVincitaExtra(Integer vincitaExtra) {
        this.vincitaExtra = vincitaExtra;
    }

    public String getMsgTrovatiVincenti() {
        return msgTrovatiVincenti;
    }

    public void setMsgTrovatiVincenti(String msgTrovatiVincenti) {
        this.msgTrovatiVincenti = msgTrovatiVincenti;
    }
}

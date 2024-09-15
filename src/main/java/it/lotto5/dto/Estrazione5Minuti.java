package it.lotto5.dto;

import it.eng.pilot.PilotSupport;
import it.eng.pilot.PList;

import java.util.Date;


public class Estrazione5Minuti extends PilotSupport{


    private int numero;
    private Date data;
    private PList<Integer> estrazione = pl();
    private PList<Integer> extra = pl();
    private Integer oro;
    private Integer doppioOro;
    private PList<Integer> giocata = pl();
    private Integer vincita=0;
    private Integer spesa = 1;

    private Integer spesaTotale = 0;
    private boolean oroGiocato;
    private boolean doppioOroGiocato;
    private boolean giocataExtra;

    private String msgTrovati="Trovati: ";
    private String msgTrovatiExtra="Trovati Extra: ";

    private String msgOro="PRESO ORO ";
    private String msgDoppioOro="PRESO DOPPIO ORO ";

    private PList<Integer> trovati=pl();
    private PList<Integer> trovatiExtra=pl();

    private Integer maxTrovati;
    private Integer maxTrovatiExtra;

    public Estrazione5Minuti(String row) {
        String nums = substring(row, tab(), false, false, tab2(), false, true);
        String ori = substring(row, tab2(), false, true, null, false, false);
        PList<String> listaOri = split(ori, tab());
        String oro = listaOri.getFirstElement();
        setOro(getInteger(oro));
        String doppioOro = listaOri.get(1);
        setDoppioOro(getInteger(doppioOro));
        PList<Integer> extra = split(listaOri.getLastElement(), ".").toListInteger();
        setExtra(extra);
        PList<String> elems = split(row, tab());
        PList<String> data = split(elems.getFirstElement(), "/");
        String d = data.getFirstElement();
        d = d.replace("-", "/");
        PList<String> last = split(data.getLastElement(), " ");
        setNumero(getInteger(last.getFirstElement()));
        String ora = last.getLastElement().replace(".", ":");
        PList<String> dataElems = split(d, "/");
        String ita = strSep("/", dataElems.get(2), dataElems.get(1), dataElems.get(0));
        d = str(ita, " ", ora, ":00");
        setData(pd(d));
        setEstrazione(split(nums, tab()).toListInteger());
        msgOro+=getOro()+":";
        msgDoppioOro+=getDoppioOro()+":";
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

    private void addTrovati(){
        Integer tr=getQuantiTrovati();
        trovati.add(tr);
        msgTrovati+=str(tr,"/",getGiocata().size(),"  ");
    }

    public void impostaTrovati(){
        addTrovati();
        addTrovatiExtra();
    }

    public String getMsgTrovati(){
        return msgTrovati;
    }

    private void addTrovatiExtra(){
        Integer tr=getQuantiTrovatiExtra();
        trovatiExtra.add(tr);
        msgTrovatiExtra+=str(tr,"/",getGiocata().size(),"  ");
    }



    private void addMessaggioOro(){
        msgOro+=str(sn(presoOro())," ");
    }
    private void addMessaggioDoppioOro(){
        msgDoppioOro+=str(sn(presoDoppioOro())," ");
    }

    public void impostaMsgOri(){
        addMessaggioOro();
        addMessaggioDoppioOro();
    }


    public String getMsgOro(){
        return msgOro;
    }


    public String getMsgDoppioOro(){
        return msgDoppioOro;
    }


    public String getMsgTrovatiExtra(){
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
        return str(lf(), s, "    ", getDataString(),"   ", getMsgTrovati(), getMsgTrovatiExtra(),
               getMsgOro() ,"    ",getMsgDoppioOro(), lf2());
    }

    public Integer getVincita() {
        return vincita;
    }


    public void addVincita(Integer vincita) {
        this.vincita+= vincita;
    }

    public Integer getSpesa() {
        if (giocataExtra)
            spesa = 2;
        if (isDoppioOroGiocato())
            return spesa * 3;
        return isOroGiocato() ? spesa * 2 : spesa;
    }
    public void addSpesa() {
        this.spesaTotale+= getSpesa();
    }

    public Integer getSpesaTotale() {
        return spesaTotale;
    }

    public void setSpesa(Integer spesa) {
        this.spesa = spesa;
    }

    public boolean presoOro() {
        boolean ret=false;
        PList<Integer> inters=pl(intersection(getEstrazione(), getGiocata()));
        if (isOroGiocato() && !isDoppioOroGiocato()){
            ret=inters.contains(getOro());
        }else if (isOroGiocato()&&isDoppioOroGiocato()){
            ret=almenoUna(inters.contains(getOro()),inters.contains(getDoppioOro()));
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

    public Integer getMaxTrovati() {
        return trovati.max();
    }

    public Integer getMaxTrovatiExtra() {
        return trovatiExtra.max();
    }
}

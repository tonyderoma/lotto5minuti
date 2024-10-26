package it.lotto5.dto;

public class CoordFreq {

    private Point p;
    private Integer freq;

    public CoordFreq(Point p, Integer freq) {
        this.p = p;
        this.freq = freq;
    }

    public Point getP() {
        return p;
    }

    public void setP(Point p) {
        this.p = p;
    }

    public Integer getFreq() {
        return freq;
    }

    public void setFreq(Integer freq) {
        this.freq = freq;
    }

    public String toString() {
        return getP().getCoords() + "-->" + getFreq();
    }
}

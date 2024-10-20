package it.lotto5.dto;

import it.eng.pilot.PilotSupport;

public class Point extends PilotSupport {


    private Integer n;
    private Integer x;
    private Integer y;

    public Point(Integer n, Integer x, Integer y) {
        this.n = n;
        this.x = x;
        this.y = y;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public String toString() {
        return str(tab(), getN(), space(), quadra(), getX(), comma(), getY(), quadraClose(), tab());
    }
}

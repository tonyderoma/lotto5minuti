package it.lotto5.dto;

import it.eng.pilot.PilotSupport;

import java.util.Objects;

public class Point extends PilotSupport {


    private Integer x;
    private Integer y;

    private Integer n;

    private Integer freq = 0;


    public Point(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Point(Integer x, Integer y, Integer freq) {
        this.x = x;
        this.y = y;
        this.freq = freq;
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


    public String getCoords() {
        return str(quadra(), getX(), dash(), getY(), quadraClose());
    }

    public String toString() {
        return str(tab(), getN(), space(), quadra(), getX(), comma(), getY(), quadraClose(), graffa(), getFreq(), graffaClose(), tab());
    }

    public Integer getFreq() {
        return freq;
    }

    public void setFreq(Integer freq) {
        this.freq = freq;
    }


    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Objects.equals(getX(), point.getX()) && Objects.equals(getY(), point.getY());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY());
    }
}

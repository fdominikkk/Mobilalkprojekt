package com.example.koncertjegy;

public class Koncert {
    private String id;
    private String nev;
    private String helyszin;
    private String datum;
    private double jegyAra;
    private int elerhetoJegyek;
    private String leiras;
    private String kepUrl;
    private double latitude;
    private double longitude;

    public Koncert() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNev() {
        return nev;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

    public String getHelyszin() {
        return helyszin;
    }

    public void setHelyszin(String helyszin) {
        this.helyszin = helyszin;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public double getJegyAra() {
        return jegyAra;
    }

    public void setJegyAra(double jegyAra) {
        this.jegyAra = jegyAra;
    }

    public int getElerhetoJegyek() {
        return elerhetoJegyek;
    }

    public void setElerhetoJegyek(int elerhetoJegyek) {
        this.elerhetoJegyek = elerhetoJegyek;
    }

    public String getLeiras() {
        return leiras;
    }

    public void setLeiras(String leiras) {
        this.leiras = leiras;
    }

    public String getKepUrl() {
        return kepUrl;
    }

    public void setKepUrl(String kepUrl) {
        this.kepUrl = kepUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
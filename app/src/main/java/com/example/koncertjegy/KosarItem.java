package com.example.koncertjegy;

public class KosarItem {
    private String id;
    private String userId;
    private String koncertId;
    private String koncertNev;
    private String koncertDatum;
    private double jegyAra;

    public KosarItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKoncertId() {
        return koncertId;
    }

    public void setKoncertId(String koncertId) {
        this.koncertId = koncertId;
    }

    public String getKoncertNev() {
        return koncertNev;
    }

    public void setKoncertNev(String koncertNev) {
        this.koncertNev = koncertNev;
    }

    public String getKoncertDatum() {
        return koncertDatum;
    }

    public void setKoncertDatum(String koncertDatum) {
        this.koncertDatum = koncertDatum;
    }

    public double getJegyAra() {
        return jegyAra;
    }

    public void setJegyAra(double jegyAra) {
        this.jegyAra = jegyAra;
    }
}
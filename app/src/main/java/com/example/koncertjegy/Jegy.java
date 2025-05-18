package com.example.koncertjegy;

public class Jegy {
    private String id;
    private String koncertId;
    private String vasarloId;
    private String jegyTipus;
    private double jegyAra;
    private String vasarlasDatuma;

    public Jegy() {}

    public Jegy(String id, String koncertId, String vasarloId, String jegyTipus, double jegyAra, String vasarlasDatuma) {
        this.id = id;
        this.koncertId = koncertId;
        this.vasarloId = vasarloId;
        this.jegyTipus = jegyTipus;
        this.jegyAra = jegyAra;
        this.vasarlasDatuma = vasarlasDatuma;
    }

    // Getterek és setterek
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getKoncertId() { return koncertId; }
    public void setKoncertId(String koncertId) { this.koncertId = koncertId; }
    public String getVasarloId() { return vasarloId; }
    public void setVasarloId(String vasarloId) { this.vasarloId = vasarloId; }
    public String getJegyTipus() { return jegyTipus; }
    public void setJegyTipus(String jegyTipus) { this.jegyTipus = jegyTipus; }
    public double getJegyAra() { return jegyAra; }
    public void setJegyAra(double jegyAra) { this.jegyAra = jegyAra; }
    public String getVasarlasDatuma() { return vasarlasDatuma; }
    public void setVasarlasDatuma(String vasarlasDatuma) { this.vasarlasDatuma = vasarlasDatuma; }
}
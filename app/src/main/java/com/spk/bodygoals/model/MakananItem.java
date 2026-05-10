package com.spk.bodygoals.model;

public class MakananItem {
    public int id;
    public int kategoriId;
    public String nama;
    public double kkal;
    public double protein;
    public double karbohidrat;
    public double lemak;
    public double serat;
    public double gula;
    public double natrium;

    public MakananItem(
            int id,
            int kategoriId,
            String nama,
            double kkal,
            double protein,
            double karbohidrat,
            double lemak,
            double serat,
            double gula,
            double natrium
    ) {
        this.id = id;
        this.kategoriId = kategoriId;
        this.nama = nama;
        this.kkal = kkal;
        this.protein = protein;
        this.karbohidrat = karbohidrat;
        this.lemak = lemak;
        this.serat = serat;
        this.gula = gula;
        this.natrium = natrium;
    }

    @Override
    public String toString() {
        return nama;
    }
}
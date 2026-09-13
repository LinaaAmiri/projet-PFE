package com.example.demo.model;

public class RapportRequest {

    private String typeRapport; // VENTES | COMMANDES | SYNTHESE | CLASSEMENTS
    private Integer annee;
    private Integer mois;

    public String getTypeRapport() { return typeRapport; }
    public void setTypeRapport(String typeRapport) { this.typeRapport = typeRapport; }
    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }
    public Integer getMois() { return mois; }
    public void setMois(Integer mois) { this.mois = mois; }
}
package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RapportResponse {

    private String titre;

    @JsonProperty("sousTitre")
    private String sousTitre;

    private String periode;
    private String role;
    private List<Section> sections;
    private String conclusion;
    private String erreur;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section {
        private String titreSection;
        private String typeGraphique;
        private List<Map<String, Object>> donnees;
        private String analyse;
        private String recommandation;  // ← AJOUT

        public String getTitreSection() { return titreSection; }
        public void setTitreSection(String t) { this.titreSection = t; }
        public String getTypeGraphique() { return typeGraphique; }
        public void setTypeGraphique(String t) { this.typeGraphique = t; }
        public List<Map<String, Object>> getDonnees() { return donnees; }
        public void setDonnees(List<Map<String, Object>> d) { this.donnees = d; }
        public String getAnalyse() { return analyse; }
        public void setAnalyse(String a) { this.analyse = a; }
        public String getRecommandation() { return recommandation; }  // ← AJOUT
        public void setRecommandation(String r) { this.recommandation = r; }  // ← AJOUT
    }

    public String getTitre() { return titre; }
    public void setTitre(String t) { this.titre = t; }
    public String getSousTitre() { return sousTitre; }
    public void setSousTitre(String s) { this.sousTitre = s; }
    public String getPeriode() { return periode; }
    public void setPeriode(String p) { this.periode = p; }
    public String getRole() { return role; }
    public void setRole(String r) { this.role = r; }
    public List<Section> getSections() { return sections; }
    public void setSections(List<Section> s) { this.sections = s; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String c) { this.conclusion = c; }
    public String getErreur() { return erreur; }
    public void setErreur(String e) { this.erreur = e; }
}
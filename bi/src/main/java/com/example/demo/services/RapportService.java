package com.example.demo.services;

import com.example.demo.model.RapportRequest;
import com.example.demo.model.RapportResponse;
import com.example.demo.repository.RapportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class RapportService {

    private final RapportRepository repo;
    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${claude.api.key}")
    private String claudeApiKey;

    @Value("${claude.api.model}")
    private String claudeModel;

    public RapportService(RapportRepository repo, WebClient.Builder builder) {
        this.repo = repo;
        this.webClient = builder.baseUrl("https://api.anthropic.com").build();
    }

    public RapportResponse generer(RapportRequest req) {
        try {
            int annee = req.getAnnee() != null ? req.getAnnee() : 2024;
            String periode = "Année " + annee;

            Map<String, Object> kpis = collecterKPIs(req.getTypeRapport(), annee);
            String prompt = construirePrompt(req.getTypeRapport(), periode, kpis);
            return appelerClaude(prompt, req.getTypeRapport(), periode);

        } catch (Exception e) {
            RapportResponse err = new RapportResponse();
            err.setErreur("Erreur generation rapport : " + e.getMessage());
            return err;
        }
    }

    private Map<String, Object> collecterKPIs(String type, int annee) {
        Map<String, Object> data = new LinkedHashMap<>();
        switch (type) {
            case "VENTES" -> {
                data.put("kpis_globaux",      repo.getCATotal(annee));
                data.put("taux_reussite",     repo.getTauxReussiteCA(annee));
                data.put("ca_par_livreur",    repo.getCAParLivreur(annee));
                data.put("ca_par_nature",     repo.getCAParNatureClient(annee));
                data.put("ca_par_region",     repo.getCAParRegion(annee));
                data.put("ca_par_client",     repo.getCAParClient(annee));
                data.put("quantite_par_mois", repo.getQuantiteParMois(annee));
                data.put("croissance",        repo.getTauxCroissanceParMois(annee));
                data.put("ca_par_article",    repo.getCAParArticle(annee));
                data.put("ca_par_categorie",  repo.getCAParCategorie(annee));
            }
            case "COMMANDES" -> {
                data.put("kpis_commandes",        repo.getKPIsCommandes(annee));
                data.put("par_statut",            repo.getCommandesParStatut(annee));
                data.put("par_statut_et_livreur", repo.getCommandesParStatutEtLivreur(annee));
                data.put("par_region",            repo.getMontantCommandesParRegion(annee));
                data.put("validees_par_mois",     repo.getMontantCommandesValideesParMois(annee));
                data.put("annulees_par_client",   repo.getCommandesAnnuleesParClient(annee));
                data.put("par_client_montant",    repo.getCommandesParClientEtMontant(annee));
            }
            case "SYNTHESE" -> {
                data.put("kpis_synthese",       repo.getKPIsSynthese(annee));
                data.put("evolution_mensuelle", repo.getEvolutionMensuelle(annee));
                data.put("ca_par_region",       repo.getCAParRegion(annee));
                data.put("croissance",          repo.getTauxCroissanceParMois(annee));
            }
            case "CLASSEMENTS" -> {
                data.put("top10_clients",    repo.getTop10ClientsFideles(annee));
                data.put("top10_articles",   repo.getTop10ArticlesQuantite(annee));
                data.put("top5_regions",     repo.getTop5Regions(annee));
                data.put("livreurs",         repo.getLivreursPerformants(annee));
                data.put("ca_par_categorie", repo.getCAParCategorie(annee));
            }
        }
        return data;
    }

    private String construirePrompt(String type, String periode,
                                    Map<String, Object> kpis) {
        String config = switch (type) {
            case "VENTES" -> """
                Titre : Rapport de Performance des Ventes
                Role : Responsable Vente
                Graphiques attendus :
                - donut : Chiffre d affaires par livreur
                - horizontal_bar : CA par nature de client
                - pie : CA par region
                - horizontal_bar : CA par client (top 10)
                - area : Quantite vendue par mois
                - area : Taux de croissance par mois
                - table : CA par article
                - table : CA par categorie
                """;
            case "COMMANDES" -> """
                Titre : Rapport de Suivi Commande
                Role : Responsable Commande
                Graphiques attendus :
                - pie : Nombre de commandes par statut
                - stacked_bar : Commandes par statut et par livreur
                - pie : Montant total par region
                - area : Montant commandes validees par mois
                - bar : Commandes annulees par client
                - table : Commandes par client et montant valide
                """;
            case "SYNTHESE" -> """
                Titre : Rapport de Synthese Commerciale
                Role : Responsable General Vente et Commande
                Graphiques attendus :
                - card : KPIs globaux (CA total, nb ventes, montant valide, nb commandes)
                - bar : Nombre de commandes et montant valide par mois
                - area : Taux de conversion par mois
                - line : Evolution mensuelle ventes vs commandes
                """;
            case "CLASSEMENTS" -> """
                Titre : Rapport des Performances Cles
                Role : Owner / Direction
                Graphiques attendus :
                - horizontal_bar : Top 10 clients les plus fideles
                - pie : Top 10 articles par quantites vendues
                - treemap : Top 5 regions les plus actives
                - horizontal_bar : Livreurs les plus performants
                - table : CA par categorie d article
                """;
            default -> "Rapport general";
        };

        try {
            String kpisJson = mapper.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(kpis);
            return """
                Tu es un expert en analyse decisionnelle. \
                Genere un rapport narratif professionnel en francais.

                CONFIGURATION :
                %s

                PERIODE ANALYSEE : %s

                DONNEES DU DATA WAREHOUSE :
                %s

                INSTRUCTIONS :
                Pour chaque graphique liste dans la configuration, genere une section avec :
                1. Le titre exact du graphique
                2. Le type de graphique
                3. Les donnees structurees extraites des donnees DW fournies
                4. Une analyse narrative de 3 phrases professionnelles
                5. Une recommandation claire et actionnable : \
                   dis exactement a l utilisateur CE QU IL DOIT FAIRE \
                   ou NE PAS FAIRE en se basant sur les donnees. \
                   Commence par un verbe d action (Ex: Renforcez, Reduisez, \
                   Evitez, Priorisez, Concentrez-vous sur...). \
                   2 a 3 phrases maximum.

                Termine par une conclusion generale de synthese de 3 phrases.

                RETOURNE UNIQUEMENT du JSON valide avec cette structure exacte :
                {
                  "titre": "...",
                  "sousTitre": "...",
                  "sections": [
                    {
                      "titreSection": "...",
                      "typeGraphique": "...",
                      "donnees": [...],
                      "analyse": "...",
                      "recommandation": "..."
                    }
                  ],
                  "conclusion": "..."
                }
                """.formatted(config, periode, kpisJson);
        } catch (Exception e) {
            return "Erreur construction prompt";
        }
    }

    private RapportResponse appelerClaude(String prompt,
                                          String type,
                                          String periode) {
        Map<String, Object> body = Map.of(
            "model", claudeModel,
            "max_tokens", 4000,
            "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            String response = webClient.post()
                .uri("/v1/messages")
                .header("x-api-key", claudeApiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode root = mapper.readTree(response);
            String text = root.path("content").get(0).path("text").asText();
            text = text.replaceAll("```json", "").replaceAll("```", "").trim();

            RapportResponse rapport = mapper.treeToValue(
                mapper.readTree(text), RapportResponse.class);
            rapport.setPeriode(periode);
            rapport.setRole(getRoleLabel(type));
            return rapport;

        } catch (Exception e) {
            RapportResponse err = new RapportResponse();
            err.setErreur("Erreur appel Claude : " + e.getMessage());
            return err;
        }
    }

    private String getRoleLabel(String type) {
        return switch (type) {
            case "VENTES"      -> "Responsable Vente";
            case "COMMANDES"   -> "Responsable Commande";
            case "SYNTHESE"    -> "Responsable General V&C";
            case "CLASSEMENTS" -> "Owner / Direction";
            default            -> "Utilisateur";
        };
    }
}
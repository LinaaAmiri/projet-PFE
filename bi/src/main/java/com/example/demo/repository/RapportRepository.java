package com.example.demo.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class RapportRepository {

    private final JdbcTemplate dw;

    public RapportRepository(@Qualifier("dwJdbcTemplate") JdbcTemplate dw) {
        this.dw = dw;
    }

    // ── VENTES ────────────────────────────────────────────────────

    public Map<String, Object> getTauxReussiteCA(int annee) {
        String sql = """
            SELECT
              CASE WHEN SUM(FC.Montant) = 0 THEN 0
                   ELSE ROUND(CAST(SUM(FV.Montant) AS FLOAT)
                        / SUM(FC.Montant) * 100, 2)
              END AS Taux_Reussite_CA
            FROM FAIT_VENTE FV
            CROSS JOIN (SELECT SUM(Montant) AS Montant FROM FAIT_COMMANDE) FC
            INNER JOIN DIM_DATE DD ON FV.CleDate = DD.CleDate
            WHERE DD.Annee = ?
            """;
        return dw.queryForMap(sql, annee);
    }

    public Map<String, Object> getCATotal(int annee) {
        String sql = """
            SELECT ROUND(SUM(FV.Montant), 2)    AS CA_Total,
                   SUM(FV.Quantite)              AS Quantite_Totale,
                   COUNT(DISTINCT FV.IdVente)    AS Nombre_Ventes
            FROM FAIT_VENTE FV
            INNER JOIN DIM_DATE DD ON FV.CleDate = DD.CleDate
            WHERE DD.Annee = ?
            """;
        return dw.queryForMap(sql, annee);
    }

    public List<Map<String, Object>> getCAParLivreur(int annee) {
        String sql = """
            SELECT DL.NomCompletLivreur,
                   ROUND(SUM(FV.Montant), 2) AS CA
            FROM FAIT_VENTE FV
            INNER JOIN DIM_LIVREUR DL ON FV.CleLivreur = DL.CleLivreur
            INNER JOIN DIM_DATE DD    ON FV.CleDate    = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DL.NomCompletLivreur
            ORDER BY CA DESC
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getCAParNatureClient(int annee) {
        String sql = """
            SELECT DC.NatureClient,
                   ROUND(SUM(FV.Montant), 2) AS CA
            FROM FAIT_VENTE FV
            INNER JOIN DIM_CLIENT DC ON FV.CleClient = DC.CleClient
            INNER JOIN DIM_DATE DD   ON FV.CleDate   = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DC.NatureClient
            ORDER BY CA DESC
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getCAParRegion(int annee) {
        String sql = """
            SELECT DR.NomRegion,
                   ROUND(SUM(FV.Montant), 2) AS CA
            FROM FAIT_VENTE FV
            INNER JOIN DIM_REGION DR ON FV.CleRegion = DR.CleRegion
            INNER JOIN DIM_DATE DD   ON FV.CleDate   = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DR.NomRegion
            ORDER BY CA DESC
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getCAParClient(int annee) {
        String sql = """
            SELECT DC.NomCompletClient,
                   ROUND(SUM(FV.Montant), 2) AS CA
            FROM FAIT_VENTE FV
            INNER JOIN DIM_CLIENT DC ON FV.CleClient = DC.CleClient
            INNER JOIN DIM_DATE DD   ON FV.CleDate   = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DC.NomCompletClient
            ORDER BY CA DESC
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getQuantiteParMois(int annee) {
        String sql = """
            SELECT DD.Mois,
                   SUM(FV.Quantite) AS Quantite_Vendue
            FROM FAIT_VENTE FV
            INNER JOIN DIM_DATE DD ON FV.CleDate = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DD.Mois
            ORDER BY DD.Mois
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getTauxCroissanceParMois(int annee) {
        String sql = """
            WITH CA_Mois AS (
              SELECT DD.Mois, DD.Annee, SUM(FV.Montant) AS CA
              FROM FAIT_VENTE FV
              INNER JOIN DIM_DATE DD ON FV.CleDate = DD.CleDate
              WHERE DD.Annee IN (?, ?)
              GROUP BY DD.Annee, DD.Mois
            )
            SELECT N.Mois,
                   N.CA AS CA_N,
                   P.CA AS CA_N1,
                   CASE WHEN P.CA = 0 OR P.CA IS NULL THEN 0
                        ELSE ROUND((N.CA - P.CA) / P.CA * 100, 2)
                   END AS Taux_Croissance
            FROM CA_Mois N
            LEFT JOIN CA_Mois P
              ON N.Mois = P.Mois AND P.Annee = N.Annee - 1
            WHERE N.Annee = ?
            ORDER BY N.Mois
            """;
        return dw.queryForList(sql, annee, annee - 1, annee);
    }

    public List<Map<String, Object>> getCAParArticle(int annee) {
        String sql = """
            SELECT DA.NomArticle,
                   ROUND(SUM(FV.Montant), 2) AS CA
            FROM FAIT_VENTE FV
            INNER JOIN DIM_ARTICLE DA ON FV.CleArticle = DA.CleArticle
            INNER JOIN DIM_DATE DD    ON FV.CleDate    = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DA.NomArticle
            ORDER BY CA DESC
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getCAParCategorie(int annee) {
        String sql = """
            SELECT DA.CategorieArticle,
                   ROUND(SUM(FV.Montant), 2) AS CA
            FROM FAIT_VENTE FV
            INNER JOIN DIM_ARTICLE DA ON FV.CleArticle = DA.CleArticle
            INNER JOIN DIM_DATE DD    ON FV.CleDate    = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DA.CategorieArticle
            ORDER BY CA DESC
            """;
        return dw.queryForList(sql, annee);
    }

    // ── COMMANDES ─────────────────────────────────────────────────

    public Map<String, Object> getKPIsCommandes(int annee) {
        String sql = """
            SELECT
              COUNT(*)                                                    AS Nombre_Commandes,
              ROUND(SUM(Montant), 2)                                      AS Montant_Total,
              ROUND(SUM(CASE WHEN Statut='VALIDEE' THEN Montant ELSE 0 END), 2) AS Montant_Valide,
              ROUND(CAST(SUM(CASE WHEN Statut='REFUSEE' THEN 1 ELSE 0 END)
                    AS FLOAT) / COUNT(*) * 100, 2)                        AS Taux_Annulation
            FROM FAIT_COMMANDE FC
            INNER JOIN DIM_DATE DD ON FC.CleDate = DD.CleDate
            WHERE DD.Annee = ?
            """;
        return dw.queryForMap(sql, annee);
    }

    public List<Map<String, Object>> getCommandesParStatut(int annee) {
        String sql = """
            SELECT FC.Statut, COUNT(*) AS Nombre
            FROM FAIT_COMMANDE FC
            INNER JOIN DIM_DATE DD ON FC.CleDate = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY FC.Statut
            ORDER BY Nombre DESC
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getCommandesParStatutEtLivreur(int annee) {
        String sql = """
            SELECT DL.NomCompletLivreur, FC.Statut, COUNT(*) AS Nombre
            FROM FAIT_COMMANDE FC
            INNER JOIN DIM_LIVREUR DL ON FC.CleLivreur = DL.CleLivreur
            INNER JOIN DIM_DATE DD    ON FC.CleDate    = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DL.NomCompletLivreur, FC.Statut
            ORDER BY DL.NomCompletLivreur, FC.Statut
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getMontantCommandesParRegion(int annee) {
        String sql = """
            SELECT DR.NomRegion,
                   ROUND(SUM(FC.Montant), 2) AS Montant_Total
            FROM FAIT_COMMANDE FC
            INNER JOIN DIM_REGION DR ON FC.CleRegion = DR.CleRegion
            INNER JOIN DIM_DATE DD   ON FC.CleDate   = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DR.NomRegion
            ORDER BY Montant_Total DESC
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getMontantCommandesValideesParMois(int annee) {
        String sql = """
            SELECT DD.Mois,
                   ROUND(SUM(FC.Montant), 2) AS Montant_Valide
            FROM FAIT_COMMANDE FC
            INNER JOIN DIM_DATE DD ON FC.CleDate = DD.CleDate
            WHERE DD.Annee = ? AND FC.Statut = 'VALIDEE'
            GROUP BY DD.Mois
            ORDER BY DD.Mois
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getCommandesAnnuleesParClient(int annee) {
        String sql = """
            SELECT DC.NomCompletClient,
                   COUNT(*) AS Nb_Annulees
            FROM FAIT_COMMANDE FC
            INNER JOIN DIM_CLIENT DC ON FC.CleClient = DC.CleClient
            INNER JOIN DIM_DATE DD   ON FC.CleDate   = DD.CleDate
            WHERE DD.Annee = ? AND FC.Statut = 'REFUSEE'
            GROUP BY DC.NomCompletClient
            ORDER BY Nb_Annulees DESC
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getCommandesParClientEtMontant(int annee) {
        String sql = """
            SELECT DC.NomCompletClient,
                   COUNT(*) AS Nb_Commandes,
                   ROUND(SUM(CASE WHEN FC.Statut='VALIDEE'
                             THEN FC.Montant ELSE 0 END), 2) AS Montant_Valide
            FROM FAIT_COMMANDE FC
            INNER JOIN DIM_CLIENT DC ON FC.CleClient = DC.CleClient
            INNER JOIN DIM_DATE DD   ON FC.CleDate   = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DC.NomCompletClient
            ORDER BY Nb_Commandes DESC
            """;
        return dw.queryForList(sql, annee);
    }

    // ── SYNTHESE ──────────────────────────────────────────────────

    public Map<String, Object> getKPIsSynthese(int annee) {
        String sql = """
            SELECT
              (SELECT ROUND(SUM(Montant),2) FROM FAIT_VENTE FV
               JOIN DIM_DATE D ON FV.CleDate=D.CleDate
               WHERE D.Annee=?)                                     AS CA_Total,
              (SELECT COUNT(DISTINCT IdVente) FROM FAIT_VENTE FV
               JOIN DIM_DATE D ON FV.CleDate=D.CleDate
               WHERE D.Annee=?)                                     AS Nb_Ventes,
              (SELECT ROUND(SUM(Montant),2) FROM FAIT_COMMANDE FC
               JOIN DIM_DATE D ON FC.CleDate=D.CleDate
               WHERE D.Annee=? AND Statut='VALIDEE')               AS Montant_Valide,
              (SELECT COUNT(*) FROM FAIT_COMMANDE FC
               JOIN DIM_DATE D ON FC.CleDate=D.CleDate
               WHERE D.Annee=?)                                     AS Nb_Commandes
            """;
        return dw.queryForMap(sql, annee, annee, annee, annee);
    }

    public List<Map<String, Object>> getEvolutionMensuelle(int annee) {
        String sql = """
            SELECT DD.Mois,
              (SELECT COUNT(DISTINCT FV2.IdVente) FROM FAIT_VENTE FV2
               JOIN DIM_DATE D2 ON FV2.CleDate=D2.CleDate
               WHERE D2.Annee=DD.Annee AND D2.Mois=DD.Mois) AS Nb_Ventes,
              (SELECT COUNT(*) FROM FAIT_COMMANDE FC2
               JOIN DIM_DATE D3 ON FC2.CleDate=D3.CleDate
               WHERE D3.Annee=DD.Annee AND D3.Mois=DD.Mois) AS Nb_Commandes
            FROM DIM_DATE DD
            WHERE DD.Annee = ?
            GROUP BY DD.Annee, DD.Mois
            ORDER BY DD.Mois
            """;
        return dw.queryForList(sql, annee);
    }

    // ── CLASSEMENTS ───────────────────────────────────────────────

    public List<Map<String, Object>> getTop10ClientsFideles(int annee) {
        String sql = """
            SELECT TOP 10 DC.NomCompletClient,
                   COUNT(DISTINCT FC.IdCommande) AS Nb_Commandes
            FROM FAIT_COMMANDE FC
            INNER JOIN DIM_CLIENT DC ON FC.CleClient = DC.CleClient
            INNER JOIN DIM_DATE DD   ON FC.CleDate   = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DC.NomCompletClient
            ORDER BY Nb_Commandes DESC
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getTop10ArticlesQuantite(int annee) {
        String sql = """
            SELECT TOP 10 DA.NomArticle,
                   SUM(FV.Quantite) AS Quantite_Vendue
            FROM FAIT_VENTE FV
            INNER JOIN DIM_ARTICLE DA ON FV.CleArticle = DA.CleArticle
            INNER JOIN DIM_DATE DD    ON FV.CleDate    = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DA.NomArticle
            ORDER BY Quantite_Vendue DESC
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getTop5Regions(int annee) {
        String sql = """
            SELECT TOP 5 DR.NomRegion,
                   ROUND(SUM(FV.Montant), 2) AS CA
            FROM FAIT_VENTE FV
            INNER JOIN DIM_REGION DR ON FV.CleRegion = DR.CleRegion
            INNER JOIN DIM_DATE DD   ON FV.CleDate   = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DR.NomRegion
            ORDER BY CA DESC
            """;
        return dw.queryForList(sql, annee);
    }

    public List<Map<String, Object>> getLivreursPerformants(int annee) {
        String sql = """
            SELECT DL.NomCompletLivreur,
                   ROUND(SUM(FV.Montant), 2) AS CA
            FROM FAIT_VENTE FV
            INNER JOIN DIM_LIVREUR DL ON FV.CleLivreur = DL.CleLivreur
            INNER JOIN DIM_DATE DD    ON FV.CleDate    = DD.CleDate
            WHERE DD.Annee = ?
            GROUP BY DL.NomCompletLivreur
            ORDER BY CA DESC
            """;
        return dw.queryForList(sql, annee);
    }
}
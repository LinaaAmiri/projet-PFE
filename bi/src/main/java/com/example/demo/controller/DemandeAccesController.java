package com.example.demo.controller;
import org.springframework.web.bind.annotation.*;
import com.example.demo.services.DemandeAccesService;
import com.example.demo.model.DemandeAcces;
import java.util.List;

@RestController
@RequestMapping("/api/demandes")
@CrossOrigin(origins = "http://localhost:4200")
public class DemandeAccesController {

    private final DemandeAccesService service;

    public DemandeAccesController(DemandeAccesService service) {
        this.service = service;
    }

    // Utilisateur envoie une demande
    @PostMapping("/demander")
    public String demander(@RequestParam Long userId,
                           @RequestParam String rapport) {
        return service.creerDemande(userId, rapport);
    }

    // Admin voir toutes les demandes
    @GetMapping("/toutes")
    public List<DemandeAcces> toutes() {
        return service.getToutesDemandes();
    }

    // Admin accepte
    @PutMapping("/{id}/accepter")
    public String accepter(@PathVariable Long id) {
        return service.traiterDemande(id, "ACCEPTE");
    }

    // Admin bloque
    @PutMapping("/{id}/bloquer")
    public String bloquer(@PathVariable Long id) {
        return service.traiterDemande(id, "BLOQUE");
    }

    // Admin arrête accès existant
    @PutMapping("/{id}/arreter")
    public String arreter(@PathVariable Long id) {
        return service.traiterDemande(id, "ARRETE");
    }
    @GetMapping("/mes-demandes/{userId}")
    public List<DemandeAcces> getMesDemandes(@PathVariable Long userId) {
        // Cette méthode doit être créée dans ton DemandeAccesService
        return service.getDemandesParUtilisateur(userId); 
    }
}

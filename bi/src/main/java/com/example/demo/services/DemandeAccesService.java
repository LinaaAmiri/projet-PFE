package com.example.demo.services;
import com.example.demo.model.DemandeAcces;
import com.example.demo.model.User;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.repository.DemandeAccesRepository;
import com.example.demo.repository.UserRepository;

@Service
public class DemandeAccesService {

    private final DemandeAccesRepository demandeRepo;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;

    public DemandeAccesService(DemandeAccesRepository demandeRepo,
                                UserRepository userRepo,
                                JavaMailSender mailSender) {
        this.demandeRepo = demandeRepo;
        this.userRepo = userRepo;
        this.mailSender = mailSender;
    }

    // Créer une demande + notifier admin
    public String creerDemande(Long userId, String rapport) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DemandeAcces demande = new DemandeAcces();
        demande.setUser(user);
        demande.setRapportDemande(rapport);
        demande.setStatut("EN_ATTENTE");
        demande.setDateDemande(LocalDateTime.now());
        demandeRepo.save(demande);

        // Email à l'admin
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("lamirilinaa@gmail.com"); // email admin
        msg.setSubject("Nouvelle demande d'accès");
        msg.setText("L'utilisateur " + user.getUsername() +
                    " (" + user.getRole() + ") demande accès au rapport: " + rapport +
                    "\nDate: " + demande.getDateDemande() +
                    "\nConnectez-vous pour traiter la demande.");
        mailSender.send(msg);

        return "Demande envoyée";
    }

    // Traiter une demande (accepter/bloquer/arreter)
    public String traiterDemande(Long id, String statut) {
        DemandeAcces demande = demandeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande not found"));
        demande.setStatut(statut);
        demandeRepo.save(demande);

        // Email à l'utilisateur
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(demande.getUser().getEmail());
        msg.setSubject("Réponse à votre demande d'accès");
        msg.setText("Votre demande pour le rapport " + demande.getRapportDemande() +
                    " a été : " + statut);
        mailSender.send(msg);

        return "Demande mise à jour : " + statut;
    }

    public List<DemandeAcces> getToutesDemandes() {
        return demandeRepo.findAll();
    }
    public List<DemandeAcces> getDemandesParUtilisateur(Long userId) {
        return demandeRepo.findByUserId(userId);
    }
}

package com.example.demo.services;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository repo;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository repo, JwtService jwtService, JavaMailSender mailSender) {
        this.repo = repo;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
    }

    // 🔐 LOGIN
    public Map<String, String> login(String username, String password) {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        String token = jwtService.generateToken(user.getUsername());
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("userId", String.valueOf(user.getId())); // ✅ AJOUTÉ
        return response;
    }

    // 📧 FORGOT PASSWORD
    public String forgotPassword(String email) {
        User user = repo.findByEmail(email).orElse(null);
        if (user == null) return "EMAIL_NOT_FOUND";
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        repo.save(user);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset Password");
        message.setText("http://localhost:4200/reset-password?token=" + token);
        mailSender.send(message);
        return "EMAIL_SENT";
    }

    // 🔑 RESET PASSWORD
    public String resetPassword(String token, String password) {
        User user = repo.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null);
        repo.save(user);
        return "Password updated";
    }

    // 🆕 REGISTER (auto-inscription sans rôle)
    public String register(String username, String email, String password) {
        if (repo.existsByEmail(email)) return "Email already exists";
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("EMPLOYE");
        user.setStatut("ACTIF");
        repo.save(user);
        return "User created successfully";
    }

    // ✅ NOUVEAU — Admin ajoute un utilisateur AVEC rôle
    public String ajouterUserAvecRole(String username, String email, 
                                       String password, String role) {
        if (repo.existsByEmail(email)) return "Email already exists";
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);       // ✅ rôle bien sauvegardé
        user.setStatut("ACTIF");  // ✅ statut actif par défaut
        repo.save(user);
        return "User created successfully";
    }

    // Supprimer utilisateur
    public String supprimerUser(Long id) {
        if (!repo.existsById(id)) return "Utilisateur introuvable";
        repo.deleteById(id);
        return "Utilisateur supprimé";
    }

    // Voir tous les utilisateurs
    public List<User> getAllUsers() {
        return repo.findAll();
    }
}
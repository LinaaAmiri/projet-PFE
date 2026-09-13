package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.services.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    // 🔐 LOGIN
    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String username,
                                      @RequestParam String password) {
        return service.login(username, password);
    }

    // 📧 FORGOT PASSWORD
    @PostMapping("/forgot-password")
    public String forgot(@RequestParam String email) {

        System.out.println("🔥 CONTROLLER HIT");
        System.out.println("EMAIL = " + email);

        String result = service.forgotPassword(email);

        System.out.println("🔥 RESULT = " + result);

        return result;
    }

    // 🔑 RESET PASSWORD
    @PostMapping("/reset-password")
    public String reset(@RequestParam String token,
                        @RequestParam String password) {
        return service.resetPassword(token, password);
    }
    
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return service.register(user.getUsername(), user.getEmail(), user.getPassword());
    }
    
    
    
 // Admin ajoute un utilisateur
    @PostMapping("/admin/ajouter")
    public String ajouterUser(@RequestBody User user) {
        return service.ajouterUserAvecRole(
            user.getUsername(),
            user.getEmail(),
            user.getPassword(),
            user.getRole()
        );
    }

    // Admin supprime un utilisateur
    @DeleteMapping("/admin/supprimer/{id}")
    public String supprimerUser(@PathVariable Long id) {
        return service.supprimerUser(id);
    }

    // Admin voir tous les utilisateurs
    @GetMapping("/admin/utilisateurs")
    public List<User> tousLesUsers() {
        return service.getAllUsers();}
}
package com.example.demo.repository;
import com.example.demo.model.User;
import java.util.Optional;
import com.example.demo.model.DemandeAcces;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandeAccesRepository extends JpaRepository<DemandeAcces, Long> {
    List<DemandeAcces> findByStatut(String statut);
    List<DemandeAcces> findByUserId(Long userId);
}
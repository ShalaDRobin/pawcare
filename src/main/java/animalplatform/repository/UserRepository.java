package animalplatform.repository;

import animalplatform.model.User;
import animalplatform.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Trouver un utilisateur par email
    Optional<User> findByEmail(String email);

    // Vérifier si un email existe
    boolean existsByEmail(String email);

    // Trouver les utilisateurs par rôle
    List<User> findByRole(Role role);

    // Trouver les utilisateurs par ville
    List<User> findByCity(String city);

    // Recherche d'utilisateurs
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);

    // Compter les utilisateurs par rôle
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);

    // ✅ CORRECTION: Trouver les utilisateurs actifs (qui ont posté récemment)
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.adoptionPosts p " +
            "WHERE p.createdAt >= CURRENT_DATE - 30 DAY " +  // ← CORRECTION: ajout de "DAY"
            "ORDER BY p.createdAt DESC")
    List<User> findActiveUsers();

    // Statistiques par ville
    @Query("SELECT u.city, COUNT(u) FROM User u " +
            "WHERE u.city IS NOT NULL " +
            "GROUP BY u.city " +
            "ORDER BY COUNT(u) DESC")
    List<Object[]> countUsersByCity();
}
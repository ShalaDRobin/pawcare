package animalplatform.repository;

import animalplatform.model.AdoptionPost;
import animalplatform.model.PostStatus;
import animalplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdoptionPostRepository extends JpaRepository<AdoptionPost, Long> {

    // ============================================
    // MÉTHODES DE BASE
    // ============================================

    // Trouver les annonces par utilisateur
    List<AdoptionPost> findByUserOrderByCreatedAtDesc(User user);

    // Trouver les annonces par statut (avec pagination)
    Page<AdoptionPost> findByStatus(PostStatus status, Pageable pageable);

    // Trouver les annonces par type d'animal et statut
    Page<AdoptionPost> findByAnimalTypeAndStatus(String animalType, PostStatus status, Pageable pageable);

    // Trouver les dernières annonces (6 dernières)
    List<AdoptionPost> findTop6ByStatusOrderByCreatedAtDesc(PostStatus status);

    // ============================================
    // MÉTHODES DE FILTRAGE AVANCÉ
    // ============================================

    // Trouver les annonces par localisation
    @Query("SELECT a FROM AdoptionPost a WHERE " +
            "LOWER(a.location) LIKE LOWER(CONCAT('%', :location, '%')) " +
            "AND a.status = :status " +
            "ORDER BY a.createdAt DESC")
    Page<AdoptionPost> findByLocationContaining(@Param("location") String location,
                                                @Param("status") PostStatus status,
                                                Pageable pageable);

    // Recherche avancée avec filtres multiples (type, localisation, urgent)
    @Query("SELECT a FROM AdoptionPost a WHERE " +
            "(:animalType IS NULL OR a.animalType = :animalType) AND " +
            "(:location IS NULL OR LOWER(a.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:urgent IS NULL OR a.isUrgent = :urgent) AND " +
            "a.status = 'AVAILABLE' " +
            "ORDER BY a.createdAt DESC")
    Page<AdoptionPost> findWithFilters(@Param("animalType") String animalType,
                                       @Param("location") String location,
                                       @Param("urgent") Boolean urgent,
                                       Pageable pageable);

    // Trouver les annonces urgentes
    List<AdoptionPost> findByIsUrgentTrueAndStatusOrderByCreatedAtDesc(PostStatus status);

    // ============================================
    // MÉTHODES UTILITAIRES
    // ============================================

    // Incrémenter le compteur de vues
    @Modifying
    @Transactional
    @Query("UPDATE AdoptionPost a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // Compter les annonces par type d'animal
    @Query("SELECT a.animalType, COUNT(a) FROM AdoptionPost a " +
            "WHERE a.status = 'AVAILABLE' " +
            "GROUP BY a.animalType")
    List<Object[]> countByAnimalType();

    // Trouver des annonces similaires (même type ou même localisation)
    @Query("SELECT a FROM AdoptionPost a WHERE " +
            "(a.animalType = :animalType OR LOWER(a.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND a.id != :postId " +
            "AND a.status = 'AVAILABLE' " +
            "ORDER BY a.createdAt DESC")
    List<AdoptionPost> findSimilarPosts(@Param("animalType") String animalType,
                                        @Param("location") String location,
                                        @Param("postId") Long postId,
                                        Pageable pageable);

    // Statistiques mensuelles
    @Query("SELECT FUNCTION('MONTH', a.createdAt), COUNT(a) FROM AdoptionPost a " +
            "WHERE a.createdAt >= :startDate " +
            "GROUP BY FUNCTION('MONTH', a.createdAt) " +
            "ORDER BY FUNCTION('MONTH', a.createdAt)")
    List<Object[]> getMonthlyStats(@Param("startDate") LocalDateTime startDate);
}
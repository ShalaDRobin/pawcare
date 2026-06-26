package animalplatform.repository;

import animalplatform.model.Sensibilisation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SensibilisationRepository extends JpaRepository<Sensibilisation, Long> {

    // ✅ Trouver les articles publiés
    List<Sensibilisation> findByIsPublishedTrueOrderByPublishedAtDesc();

    // ✅ Trouver par catégorie
    List<Sensibilisation> findByCategoryAndIsPublishedTrueOrderByPublishedAtDesc(String category);

    // ✅ Trouver les articles populaires (lus) avec pagination
    @Query("SELECT s FROM Sensibilisation s WHERE s.isPublished = true ORDER BY s.readCount DESC")
    List<Sensibilisation> findTopByIsPublishedTrueOrderByReadCountDesc(Pageable pageable);

    // ✅ Trouver les articles récents avec pagination
    @Query("SELECT s FROM Sensibilisation s WHERE s.isPublished = true ORDER BY s.publishedAt DESC")
    List<Sensibilisation> findTopByIsPublishedTrueOrderByPublishedAtDesc(Pageable pageable);

    // ✅ Top 6 populaires (pour l'affichage)
    List<Sensibilisation> findTop6ByIsPublishedTrueOrderByReadCountDesc();

    // ✅ Recherche d'articles
    @Query("SELECT s FROM Sensibilisation s WHERE " +
            "s.isPublished = true AND (" +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.content) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.summary) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY s.publishedAt DESC")
    List<Sensibilisation> searchArticles(@Param("search") String search);

    // ✅ Trouver par tag
    @Query("SELECT s FROM Sensibilisation s JOIN s.tags t " +
            "WHERE t = :tag AND s.isPublished = true " +
            "ORDER BY s.publishedAt DESC")
    List<Sensibilisation> findByTag(@Param("tag") String tag);

    // ✅ Trouver par utilisateur
    List<Sensibilisation> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ✅ Incrémenter le compteur de lectures
    @Modifying
    @Transactional
    @Query("UPDATE Sensibilisation s SET s.readCount = s.readCount + 1 WHERE s.id = :id")
    void incrementReadCount(@Param("id") Long id);

    // ✅ Incrémenter les likes
    @Modifying
    @Transactional
    @Query("UPDATE Sensibilisation s SET s.likeCount = s.likeCount + 1 WHERE s.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    // ✅ Obtenir toutes les catégories distinctes
    @Query("SELECT DISTINCT s.category FROM Sensibilisation s WHERE s.isPublished = true")
    List<String> findAllCategories();

    // ✅ Obtenir tous les tags distincts
    @Query("SELECT DISTINCT t FROM Sensibilisation s JOIN s.tags t")
    List<String> findAllTags();

    // ✅ Articles recommandés (basés sur la même catégorie)
    @Query("SELECT s FROM Sensibilisation s WHERE " +
            "s.category = :category AND s.id != :articleId AND s.isPublished = true " +
            "ORDER BY s.readCount DESC")
    List<Sensibilisation> findRelatedArticles(@Param("category") String category,
                                              @Param("articleId") Long articleId,
                                              Pageable pageable);

    // ✅ Statistiques par catégorie
    @Query("SELECT s.category, COUNT(s), SUM(s.readCount) FROM Sensibilisation s " +
            "WHERE s.isPublished = true " +
            "GROUP BY s.category")
    List<Object[]> getCategoryStats();

    // ✅ Compter les articles par catégorie
    @Query("SELECT s.category, COUNT(s) FROM Sensibilisation s " +
            "WHERE s.isPublished = true " +
            "GROUP BY s.category")
    List<Object[]> countByCategory();
}
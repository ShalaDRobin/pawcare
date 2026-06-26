package animalplatform.repository;

import animalplatform.model.ForumCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForumCategoryRepository extends JpaRepository<ForumCategory, Long> {

    // Trouver par nom
    Optional<ForumCategory> findByName(String name);

    // Vérifier si une catégorie existe
    boolean existsByName(String name);

    // ✅ Toutes les catégories triées par ordre
    @Query("SELECT c FROM ForumCategory c ORDER BY c.orderIndex ASC")
    List<ForumCategory> findAllByOrderByOrderIndexAsc();

    // ✅ Mettre à jour le nombre de posts - CORRIGÉ
    @Modifying
    @Transactional
    @Query("UPDATE ForumCategory c SET c.postCount = " +
            "(SELECT COUNT(p) FROM ForumPost p WHERE p.category.id = :categoryId) " +
            "WHERE c.id = :categoryId")
    void updatePostCount(@Param("categoryId") Long categoryId);

    // Catégories les plus actives
    @Query("SELECT c FROM ForumCategory c ORDER BY c.postCount DESC")
    List<ForumCategory> findMostActiveCategories();

    // Recherche de catégories
    @Query("SELECT c FROM ForumCategory c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<ForumCategory> searchCategories(@Param("search") String search);

    // ✅ Statistiques des catégories - CORRIGÉ
    @Query("SELECT c.name, c.postCount, " +
            "(SELECT COUNT(p) FROM ForumPost p WHERE p.category.id = c.id AND p.createdAt >= :startDate) as weeklyPosts " +
            "FROM ForumCategory c " +
            "ORDER BY c.postCount DESC")
    List<Object[]> getCategoryStats(@Param("startDate") LocalDateTime startDate);

    // ✅ Récupérer une catégorie avec son nombre de posts (jointure)
    @Query("SELECT c, COUNT(p) FROM ForumCategory c " +
            "LEFT JOIN c.posts p " +
            "WHERE c.id = :id " +
            "GROUP BY c")
    List<Object[]> findCategoryWithPostCount(@Param("id") Long id);
}
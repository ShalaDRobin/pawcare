package animalplatform.repository;

import animalplatform.model.ForumPost;
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
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    // ✅ Trouver par catégorie (avec pagination)
    Page<ForumPost> findByCategoryIdOrderByIsPinnedDescCreatedAtDesc(Long categoryId, Pageable pageable);

    // ✅ Trouver par utilisateur - CORRIGÉ avec @Param
    @Query("SELECT p FROM ForumPost p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    Page<ForumPost> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // ✅ Trouver les posts récents
    List<ForumPost> findTop10ByOrderByCreatedAtDesc();

    // ✅ Trouver les posts populaires (plus de vues)
    List<ForumPost> findTop5ByOrderByViewCountDesc();

    // ✅ Trouver les posts avec le plus de commentaires
    @Query("SELECT p FROM ForumPost p ORDER BY p.commentCount DESC")
    List<ForumPost> findMostCommentedPosts(Pageable pageable);

    // ✅ Recherche dans les posts
    @Query("SELECT p FROM ForumPost p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "ORDER BY p.createdAt DESC")
    Page<ForumPost> searchPosts(@Param("search") String search, Pageable pageable);

    // ✅ Compter les posts par catégorie
    @Query("SELECT COUNT(p) FROM ForumPost p WHERE p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    // ✅ Incrémenter les vues
    @Modifying
    @Transactional
    @Query("UPDATE ForumPost p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // ✅ Incrémenter les likes
    @Modifying
    @Transactional
    @Query("UPDATE ForumPost p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    // ✅ Mettre à jour la dernière activité
    @Modifying
    @Transactional
    @Query("UPDATE ForumPost p SET p.lastActivityAt = CURRENT_TIMESTAMP, " +
            "p.lastActivityBy = :username WHERE p.id = :id")
    void updateLastActivity(@Param("id") Long id, @Param("username") String username);

    // ✅ Trouver les posts épinglés
    List<ForumPost> findByIsPinnedTrueOrderByCreatedAtDesc();

    // ✅ Obtenir l'activité récente
    @Query("SELECT p FROM ForumPost p WHERE p.lastActivityAt >= :date " +
            "ORDER BY p.lastActivityAt DESC")
    List<ForumPost> findRecentActivity(@Param("date") LocalDateTime date);

    // ✅ Statistiques par jour
    @Query("SELECT DATE(p.createdAt), COUNT(p) FROM ForumPost p " +
            "WHERE p.createdAt >= :startDate " +
            "GROUP BY DATE(p.createdAt) " +
            "ORDER BY DATE(p.createdAt)")
    List<Object[]> getDailyStats(@Param("startDate") LocalDateTime startDate);

    // ✅ Trouver les posts d'un utilisateur avec pagination
    @Query("SELECT p FROM ForumPost p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    Page<ForumPost> findPostsByUser(@Param("userId") Long userId, Pageable pageable);

    // ✅ Trouver les posts non verrouillés - CORRIGÉ
    @Query("SELECT p FROM ForumPost p WHERE p.isLocked = false " +
            "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<ForumPost> findUnlockedPosts(Pageable pageable);

    // ✅ Incrémenter le compteur de commentaires
    @Modifying
    @Transactional
    @Query("UPDATE ForumPost p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    // ✅ Décrémenter le compteur de commentaires
    @Modifying
    @Transactional
    @Query("UPDATE ForumPost p SET p.commentCount = p.commentCount - 1 WHERE p.id = :id AND p.commentCount > 0")
    void decrementCommentCount(@Param("id") Long id);

    // ✅ AJOUT : Compter les posts d'un utilisateur
    @Query("SELECT COUNT(p) FROM ForumPost p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
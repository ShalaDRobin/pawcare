package animalplatform.repository;

import animalplatform.model.ForumComment;
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
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {

    // ✅ Trouver les commentaires d'un post (avec tri par date)
    List<ForumComment> findByForumPostIdOrderByCreatedAtAsc(Long postId);

    // ✅ Trouver les commentaires d'un post avec leurs réponses (eager loading)
    @Query("SELECT c FROM ForumComment c " +
            "LEFT JOIN FETCH c.user " +
            "LEFT JOIN FETCH c.parentComment " +
            "WHERE c.forumPost.id = :postId " +
            "ORDER BY c.createdAt ASC")
    List<ForumComment> findByForumPostIdWithUsers(@Param("postId") Long postId);

    // ✅ Trouver les commentaires d'un utilisateur
    List<ForumComment> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ✅ Trouver les réponses à un commentaire
    List<ForumComment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    // ✅ Compter les commentaires d'un post
    long countByForumPostId(Long postId);

    // ✅ Incrémenter les likes
    @Modifying
    @Transactional
    @Query("UPDATE ForumComment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    // ✅ Derniers commentaires d'un utilisateur
    @Query("SELECT c FROM ForumComment c WHERE c.user.id = :userId " +
            "ORDER BY c.createdAt DESC")
    List<ForumComment> findRecentByUser(@Param("userId") Long userId);

    // ✅ Activité récente sur les commentaires
    @Query("SELECT c FROM ForumComment c WHERE c.createdAt >= :date " +
            "ORDER BY c.createdAt DESC")
    List<ForumComment> findRecentComments(@Param("date") LocalDateTime date);

    // ✅ Supprimer tous les commentaires d'un post
    @Modifying
    @Transactional
    @Query("DELETE FROM ForumComment c WHERE c.forumPost.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    // ✅ Compter les commentaires par utilisateur
    @Query("SELECT COUNT(c) FROM ForumComment c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // ✅ Vérifier si un commentaire a des réponses
    @Query("SELECT COUNT(c) > 0 FROM ForumComment c WHERE c.parentComment.id = :commentId")
    boolean hasReplies(@Param("commentId") Long commentId);

    // ✅ Trouver les commentaires d'un post avec pagination
    @Query("SELECT c FROM ForumComment c WHERE c.forumPost.id = :postId " +
            "AND c.parentComment IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<ForumComment> findTopLevelComments(@Param("postId") Long postId, Pageable pageable);
}
package animalplatform.repository;

import animalplatform.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Trouver les notifications d'un utilisateur
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Trouver les notifications non lues
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // ✅ NOUVEAU: Trouver les notifications par type
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);

    // Compter les notifications non lues
    long countByUserIdAndIsReadFalse(Long userId);

    // Marquer comme lue
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.user.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    // Marquer toutes comme lues
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    // ✅ NOUVEAU: Supprimer les notifications lues d'un utilisateur
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId AND n.isRead = true")
    int deleteByUserIdAndIsReadTrue(@Param("userId") Long userId);

    // Supprimer les anciennes notifications (plus de 30 jours)
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
    int deleteOldNotifications(@Param("date") LocalDateTime date);

    // Vérifier si une notification existe déjà
    boolean existsByUserIdAndReferenceIdAndType(Long userId, String referenceId, String type);

    // Statistiques des notifications par type
    @Query("SELECT n.type, COUNT(n) FROM Notification n " +
            "WHERE n.createdAt >= :startDate " +
            "GROUP BY n.type")
    List<Object[]> getNotificationStats(@Param("startDate") LocalDateTime startDate);
}
package animalplatform.repository;

import animalplatform.model.AdoptionRequest;
import animalplatform.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {

    // Trouver les demandes pour une annonce
    List<AdoptionRequest> findByAdoptionPostIdOrderByCreatedAtDesc(Long postId);

    // ✅ Trouver les demandes pour une annonce avec un statut spécifique
    List<AdoptionRequest> findByAdoptionPostIdAndStatus(Long postId, RequestStatus status);

    // Trouver les demandes faites par un utilisateur
    List<AdoptionRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    // Trouver les demandes reçues par un propriétaire
    List<AdoptionRequest> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    // Trouver par statut
    List<AdoptionRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    // ✅ CORRIGÉ: Utilise RequestStatus au lieu de String
    boolean existsByAdoptionPostIdAndRequesterIdAndStatusNot(Long postId, Long requesterId, RequestStatus status);

    // Vérifier si une demande existe déjà avec un statut spécifique
    boolean existsByAdoptionPostIdAndRequesterIdAndStatus(Long postId, Long requesterId, RequestStatus status);

    // Compter les demandes pour une annonce avec un statut spécifique
    long countByAdoptionPostIdAndStatus(Long postId, RequestStatus status);

    // Mettre à jour le statut
    @Modifying
    @Transactional
    @Query("UPDATE AdoptionRequest r SET r.status = :status, r.respondedAt = CURRENT_TIMESTAMP " +
            "WHERE r.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") RequestStatus status);

    // Compter les demandes en attente pour un propriétaire
    long countByOwnerIdAndStatus(Long ownerId, RequestStatus status);

    // Trouver les demandes avec leurs relations chargées
    @Query("SELECT r FROM AdoptionRequest r " +
            "JOIN FETCH r.adoptionPost " +
            "JOIN FETCH r.requester " +
            "JOIN FETCH r.owner " +
            "WHERE r.id = :id")
    AdoptionRequest findByIdWithDetails(@Param("id") Long id);

    // Trouver toutes les demandes pour une annonce avec leurs relations
    @Query("SELECT r FROM AdoptionRequest r " +
            "JOIN FETCH r.requester " +
            "WHERE r.adoptionPost.id = :postId " +
            "ORDER BY r.createdAt DESC")
    List<AdoptionRequest> findByAdoptionPostIdWithDetails(@Param("postId") Long postId);

    // Statistiques des demandes par mois
    @Query("SELECT FUNCTION('MONTH', r.createdAt), COUNT(r) FROM AdoptionRequest r " +
            "WHERE r.createdAt >= :startDate " +
            "GROUP BY FUNCTION('MONTH', r.createdAt) " +
            "ORDER BY FUNCTION('MONTH', r.createdAt)")
    List<Object[]> getMonthlyRequestStats(@Param("startDate") LocalDateTime startDate);

    // Taux de conversion (demandes acceptées)
    @Query("SELECT " +
            "COUNT(CASE WHEN r.status = 'ACCEPTED' THEN 1 END) * 100.0 / COUNT(*) " +
            "FROM AdoptionRequest r")
    Double getAcceptanceRate();

    // Taux de conversion par annonce
    @Query("SELECT " +
            "COUNT(CASE WHEN r.status = 'ACCEPTED' THEN 1 END) * 100.0 / COUNT(*) " +
            "FROM AdoptionRequest r " +
            "WHERE r.adoptionPost.id = :postId")
    Double getAcceptanceRateForPost(@Param("postId") Long postId);

    // Trouver les demandes en attente
    List<AdoptionRequest> findByStatus(RequestStatus status);

    // Supprimer les anciennes demandes (plus de 90 jours)
    @Modifying
    @Transactional
    @Query("DELETE FROM AdoptionRequest r WHERE r.createdAt < :date AND r.status != 'PENDING'")
    int deleteOldRequests(@Param("date") LocalDateTime date);
}
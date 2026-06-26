package animalplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "adoption_requests", indexes = {
        @Index(name = "idx_adoption_post_id", columnList = "adoption_post_id"),
        @Index(name = "idx_requester_id", columnList = "requester_id"),
        @Index(name = "idx_owner_id", columnList = "owner_id"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adoption_post_id", nullable = false)
    private AdoptionPost adoptionPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester; // Personne qui veut adopter

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // Propriétaire de l'animal

    @Column(length = 1000)
    private String message; // Message de motivation

    @Column(name = "phone_number", length = 20)
    private String phoneNumber; // Téléphone de contact

    @Column(name = "living_situation", length = 100)
    private String livingSituation; // Situation de vie (maison, appartement, jardin, etc.)

    @Column(name = "has_other_pets")
    private Boolean hasOtherPets = false; // A d'autres animaux

    @Column(name = "other_pets_details", length = 500)
    private String otherPetsDetails; // Détails sur les autres animaux

    @Column(name = "has_children")
    private Boolean hasChildren = false; // A des enfants

    @Column(length = 500)
    private String experience; // Expérience avec les animaux

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING; // PENDING, ACCEPTED, REJECTED, CANCELLED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt; // Date de réponse (acceptation/rejet)

    // ✅ Ajout d'une réponse du propriétaire (optionnel)
    @Column(name = "owner_response", length = 1000)
    private String ownerResponse; // Message du propriétaire lors de la réponse

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status != RequestStatus.PENDING && respondedAt == null) {
            respondedAt = LocalDateTime.now();
        }
    }

    // ✅ Méthodes utilitaires
    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == RequestStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return status == RequestStatus.REJECTED;
    }

    public boolean isCancelled() {
        return status == RequestStatus.CANCELLED;
    }

    // ✅ Méthode pour accepter la demande
    public void accept(String ownerMessage) {
        this.status = RequestStatus.ACCEPTED;
        this.ownerResponse = ownerMessage;
        this.respondedAt = LocalDateTime.now();
        this.adoptionPost.setStatus(PostStatus.ADOPTED);
    }

    // ✅ Méthode pour refuser la demande
    public void reject(String ownerMessage) {
        this.status = RequestStatus.REJECTED;
        this.ownerResponse = ownerMessage;
        this.respondedAt = LocalDateTime.now();
    }

    // ✅ Méthode pour annuler la demande (par le demandeur)
    public void cancel() {
        this.status = RequestStatus.CANCELLED;
    }
}


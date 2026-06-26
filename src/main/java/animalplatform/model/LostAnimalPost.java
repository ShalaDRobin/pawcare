package animalplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lost_animal_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostAnimalPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String animalType;

    private String breed;

    private String name; // Nom de l'animal

    private Integer age;

    private String gender;

    private String color;

    private String distinctFeatures; // Signes distinctifs

    @Column(nullable = false)
    private String lastSeenLocation;

    @Column(nullable = false)
    private LocalDate lastSeenDate;

    private String imageUrl; // Image principale

    // ✅ CORRIGÉ: Initialisation par défaut
    @ElementCollection
    @CollectionTable(name = "lost_animal_images", joinColumns = @JoinColumn(name = "lost_animal_post_id"))
    @Column(name = "image_url")
    private List<String> additionalImages = new ArrayList<>(); // ← Initialisé

    private Boolean hasChip = false; // Puce électronique

    private String chipNumber; // Numéro de puce

    @Column(nullable = false)
    private String ownerPhone;

    private String ownerEmail;

    @Enumerated(EnumType.STRING)
    private LostAnimalStatus status = LostAnimalStatus.LOST; // LOST, FOUND, RETURNED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Propriétaire qui a perdu l'animal

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Integer viewCount = 0;

    private Integer shareCount = 0; // Nombre de partages

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (viewCount == null) viewCount = 0;
        if (shareCount == null) shareCount = 0;
        if (additionalImages == null) additionalImages = new ArrayList<>();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ✅ Méthodes utilitaires
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    public String getFormattedLastSeenDate() {
        if (lastSeenDate == null) return "";
        return lastSeenDate.toString();
    }
}
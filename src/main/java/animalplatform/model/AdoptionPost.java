package animalplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "adoption_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String animalType; // Chien, Chat, Lapin, Oiseau, Autre

    private String breed; // Race

    private Integer age; // Âge en années/mois

    private String gender; // Mâle, Femelle

    private String size; // Petit, Moyen, Grand

    private String color; // Couleur(s)

    @Column(nullable = false)
    private String location; // Ville/Quartier

    private Boolean vaccinated = false;

    private Boolean sterilized = false;

    private Boolean dewormed = false; // Vermifugé

    private String healthStatus; // État de santé

    private String personality; // Caractère (calme, joueur, etc.)

    private String imageUrl; // URL de l'image principale

    @ElementCollection
    @CollectionTable(name = "adoption_post_images", joinColumns = @JoinColumn(name = "adoption_post_id"))
    @Column(name = "image_url")
    private List<String> additionalImages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private PostStatus status = PostStatus.AVAILABLE; // AVAILABLE, ADOPTED, PENDING

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Propriétaire de l'annonce

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Integer viewCount = 0; // Nombre de vues

    private Boolean isUrgent = false; // Adoption urgente


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
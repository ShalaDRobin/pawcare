package animalplatform.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sensibilisation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sensibilisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(nullable = false)
    private String category; // Soins, Abandon, Stérilisation, Droits, Rue, Vaccins

    private String summary; // Résumé court

    private String imageUrl;

    private String author; // Auteur de l'article

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Utilisateur qui a créé l'article

    private Integer readCount = 0; // Nombre de lectures

    private Integer likeCount = 0;

    private Integer shareCount = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Boolean isPublished = true;

    @ElementCollection
    @CollectionTable(name = "sensibilisation_tags", joinColumns = @JoinColumn(name = "sensibilisation_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (publishedAt == null && isPublished) {
            publishedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

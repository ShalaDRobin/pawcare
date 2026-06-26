package animalplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "forum_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String icon;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @OneToMany(mappedBy = "category")
    private List<ForumPost> posts;

    @Column(name = "post_count")
    private Integer postCount = 0;  // ✅ Initialisé à 0

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (orderIndex == null) {
            orderIndex = 0;
        }
        if (postCount == null) {
            postCount = 0;  // ✅ FORCER L'INITIALISATION À 0
        }
    }

    // ✅ Méthodes utilitaires avec gestion du null
    public void incrementPostCount() {
        if (this.postCount == null) {
            this.postCount = 0;
        }
        this.postCount++;
    }

    public void decrementPostCount() {
        if (this.postCount == null) {
            this.postCount = 0;
        }
        if (this.postCount > 0) {
            this.postCount--;
        }
    }

    // ✅ Méthode utilitaire pour obtenir le postCount en toute sécurité
    public int getPostCountSafe() {
        return postCount != null ? postCount : 0;
    }
}
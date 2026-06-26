package animalplatform.dto;

import animalplatform.model.Sensibilisation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensibilisationDTO {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    private String title;

    @NotBlank(message = "Le contenu est obligatoire")
    @Size(max = 5000, message = "Le contenu ne doit pas dépasser 5000 caractères")
    private String content;

    @NotBlank(message = "La catégorie est obligatoire")
    private String category; // Soins, Abandon, Stérilisation, Droits, Rue, Vaccins

    @Size(max = 500, message = "Le résumé ne doit pas dépasser 500 caractères")
    private String summary; // Résumé court

    private String imageUrl; // URL de l'image

    private String author; // Auteur de l'article

    // Informations sur l'utilisateur
    private Long userId;
    private String userFirstName;
    private String userLastName;

    private Integer readCount = 0;
    private Integer likeCount = 0;
    private Integer shareCount = 0;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean isPublished = true;

    private List<String> tags = new ArrayList<>();

    // Pour les uploads d'images
    private List<MultipartFile> images;

    // ✅ Constructeur à partir de l'entité
    public static SensibilisationDTO fromEntity(Sensibilisation sensibilisation) {
        SensibilisationDTO dto = new SensibilisationDTO();

        dto.setId(sensibilisation.getId());
        dto.setTitle(sensibilisation.getTitle());
        dto.setContent(sensibilisation.getContent());
        dto.setCategory(sensibilisation.getCategory());
        dto.setSummary(sensibilisation.getSummary());
        dto.setImageUrl(sensibilisation.getImageUrl());
        dto.setAuthor(sensibilisation.getAuthor());
        dto.setReadCount(sensibilisation.getReadCount());
        dto.setLikeCount(sensibilisation.getLikeCount());
        dto.setShareCount(sensibilisation.getShareCount());
        dto.setPublishedAt(sensibilisation.getPublishedAt());
        dto.setCreatedAt(sensibilisation.getCreatedAt());
        dto.setUpdatedAt(sensibilisation.getUpdatedAt());
        dto.setIsPublished(sensibilisation.getIsPublished());

        if (sensibilisation.getTags() != null) {
            dto.setTags(new ArrayList<>(sensibilisation.getTags()));
        }

        if (sensibilisation.getUser() != null) {
            dto.setUserId(sensibilisation.getUser().getId());
            dto.setUserFirstName(sensibilisation.getUser().getFirstName());
            dto.setUserLastName(sensibilisation.getUser().getLastName());
        }

        return dto;
    }

    // ✅ Méthode pour obtenir le nom complet de l'auteur
    public String getAuthorFullName() {
        if (userFirstName != null && userLastName != null) {
            return userFirstName + " " + userLastName;
        }
        return author != null ? author : "Auteur inconnu";
    }

    // ✅ Vérifier si l'article est récent (moins de 7 jours)
    public boolean isRecent() {
        if (createdAt == null) return false;
        return createdAt.isAfter(LocalDateTime.now().minusDays(7));
    }

    // ✅ Obtenir le temps écoulé depuis la publication
    public String getTimeAgo() {
        if (publishedAt == null) return "";
        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(publishedAt, now);

        long seconds = duration.getSeconds();
        if (seconds < 60) return "Il y a " + seconds + " secondes";
        if (seconds < 3600) return "Il y a " + (seconds / 60) + " minutes";
        if (seconds < 86400) return "Il y a " + (seconds / 3600) + " heures";
        if (seconds < 2592000) return "Il y a " + (seconds / 86400) + " jours";
        return "Il y a " + (seconds / 2592000) + " mois";
    }
}

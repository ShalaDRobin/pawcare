package animalplatform.dto;

import animalplatform.model.ForumPost;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumPostDTO {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 200, message = "Le titre doit contenir entre 5 et 200 caractères")
    private String title;

    @NotBlank(message = "Le contenu est obligatoire")
    @Size(min = 10, max = 5000, message = "Le contenu doit contenir entre 10 et 5000 caractères")
    private String content;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;

    private String categoryName; // Pour l'affichage
    private String categoryIcon; // Icône de la catégorie

    private Long userId;
    private String authorName;
    private String authorAvatar;

    private Integer commentCount = 0;
    private Integer viewCount = 0;
    private Integer likeCount = 0;

    private Boolean isPinned = false;
    private Boolean isLocked = false;

    private String lastActivityBy;
    private String lastActivityAt;

    private String createdAt;
    private String updatedAt;

    // ✅ Liste des commentaires (pour la page détail)
    private List<ForumCommentDTO> comments;

    // ✅ Constructeur à partir de l'entité
    public static ForumPostDTO fromEntity(ForumPost post) {
        return fromEntity(post, false);
    }

    public static ForumPostDTO fromEntity(ForumPost post, boolean withComments) {
        ForumPostDTO dto = new ForumPostDTO();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCommentCount(post.getCommentCount());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setIsPinned(post.getIsPinned());
        dto.setIsLocked(post.getIsLocked());

        // Catégorie
        if (post.getCategory() != null) {
            dto.setCategoryId(post.getCategory().getId());
            dto.setCategoryName(post.getCategory().getName());
            dto.setCategoryIcon(post.getCategory().getIcon());
        }

        // Utilisateur
        if (post.getUser() != null) {
            dto.setUserId(post.getUser().getId());
            dto.setAuthorName(post.getUser().getFirstName() + " " + post.getUser().getLastName());
        }

        // Dates
        if (post.getCreatedAt() != null) {
            dto.setCreatedAt(post.getCreatedAt().format(formatter));
        }
        if (post.getUpdatedAt() != null) {
            dto.setUpdatedAt(post.getUpdatedAt().format(formatter));
        }
        if (post.getLastActivityAt() != null) {
            dto.setLastActivityAt(post.getLastActivityAt().format(formatter));
        }

        dto.setLastActivityBy(post.getLastActivityBy());

        // ✅ Commentaires (si demandé)
        if (withComments && post.getComments() != null) {
            List<ForumCommentDTO> commentDTOs = post.getComments().stream()
                    .filter(comment -> comment.getParentComment() == null) // Seulement les commentaires parents
                    .map(comment -> ForumCommentDTO.fromEntity(comment, true))
                    .collect(Collectors.toList());
            dto.setComments(commentDTOs);
        }

        return dto;
    }

    // ✅ Méthode utilitaire
    public boolean isEditableBy(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    public String getTimeAgo() {
        if (createdAt == null) return "";
        // Calcul simplifié - à améliorer
        return createdAt;
    }
}
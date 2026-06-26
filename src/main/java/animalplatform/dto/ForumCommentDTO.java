package animalplatform.dto;

import animalplatform.model.ForumComment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumCommentDTO {

    private Long id;

    @NotBlank(message = "Le commentaire ne peut pas être vide")
    @Size(min = 2, max = 2000, message = "Le commentaire doit contenir entre 2 et 2000 caractères")
    private String content;

    private Long postId;
    private Long parentCommentId;

    private Long userId;
    private String authorName;
    private String authorAvatar;

    private Integer likeCount = 0;
    private Boolean isEdited = false;

    private String createdAt;
    private String updatedAt;

    // ✅ Réponses à ce commentaire
    private List<ForumCommentDTO> replies = new ArrayList<>();

    // ✅ Constructeur à partir de l'entité
    public static ForumCommentDTO fromEntity(ForumComment comment) {
        return fromEntity(comment, false);
    }

    public static ForumCommentDTO fromEntity(ForumComment comment, boolean withReplies) {
        ForumCommentDTO dto = new ForumCommentDTO();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setLikeCount(comment.getLikeCount());
        dto.setIsEdited(comment.getIsEdited());

        if (comment.getForumPost() != null) {
            dto.setPostId(comment.getForumPost().getId());
        }

        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }

        if (comment.getUser() != null) {
            dto.setUserId(comment.getUser().getId());
            dto.setAuthorName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName());
        }

        if (comment.getCreatedAt() != null) {
            dto.setCreatedAt(comment.getCreatedAt().format(formatter));
        }
        if (comment.getUpdatedAt() != null) {
            dto.setUpdatedAt(comment.getUpdatedAt().format(formatter));
        }

        // ✅ Récupérer les réponses
        if (withReplies && comment.getParentComment() == null) {
            // C'est un commentaire parent, on récupère ses réponses
            // Les réponses sont dans la base, on les charge via le repository
            // Cette partie sera gérée dans le service
        }

        return dto;
    }

    // ✅ Méthode utilitaire
    public boolean isEditableBy(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    public String getTimeAgo() {
        if (createdAt == null) return "";
        return createdAt;
    }
}
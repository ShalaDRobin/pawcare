package animalplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;

    private String title;

    private String message;

    private String type; // ADOPTION_REQUEST, LOST_ANIMAL, FORUM_REPLY, etc.

    private String referenceId;

    private String referenceType;

    private Boolean isRead = false;

    private String createdAt;

    private String timeAgo; // "Il y a 2 heures", etc.

    // Constructeur à partir de l'entité Notification
    public static NotificationDTO fromEntity(animalplatform.model.Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType() != null ? notification.getType().toString() : "SYSTEM");
        dto.setReferenceId(notification.getReferenceId());
        dto.setReferenceType(notification.getReferenceType());
        dto.setIsRead(notification.getIsRead());

        // Formatage de la date
        if (notification.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            dto.setCreatedAt(notification.getCreatedAt().format(formatter));
            dto.setTimeAgo(calculateTimeAgo(notification.getCreatedAt()));
        }

        return dto;
    }

    // Calcul du temps écoulé (pour affichage "Il y a X minutes")
    private static String calculateTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(dateTime, now);

        long seconds = duration.getSeconds();

        if (seconds < 60) return "Il y a " + seconds + " secondes";
        if (seconds < 3600) return "Il y a " + (seconds / 60) + " minutes";
        if (seconds < 86400) return "Il y a " + (seconds / 3600) + " heures";
        if (seconds < 2592000) return "Il y a " + (seconds / 86400) + " jours";

        return "Il y a " + (seconds / 2592000) + " mois";
    }
}

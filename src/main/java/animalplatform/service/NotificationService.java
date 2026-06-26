package animalplatform.service;

import animalplatform.model.*;
import animalplatform.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    // ✅ NOTIFICATION - Animal trouvé
    @Transactional
    public void notifyAnimalFound(LostAnimalPost post) {
        Notification notification = new Notification();
        notification.setUser(post.getUser());
        notification.setTitle("🐾 Bonne nouvelle ! Animal trouvé");
        notification.setMessage("Quelqu'un a signalé que " +
                (post.getName() != null ? post.getName() : "votre animal") +
                " a été trouvé ! Consultez les détails pour contacter la personne.");
        notification.setType(NotificationType.LOST_ANIMAL_FOUND);
        notification.setReferenceId(post.getId().toString());
        notification.setReferenceType("LostAnimalPost");

        notificationRepository.save(notification);

        // Envoyer par email - avec try/catch pour éviter le rollback
        try {
            emailService.sendLostAnimalFoundEmail(post);
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email (ignorée): " + e.getMessage());
        }
    }

    // ✅ NOTIFICATION - Utilisateurs dans une zone
    @Transactional
    public void notifyUsersInArea(LostAnimalPost post) {
        Notification notification = new Notification();
        notification.setUser(post.getUser());
        notification.setTitle("📢 Signalement publié");
        notification.setMessage("Votre signalement pour " +
                (post.getName() != null ? post.getName() : "l'animal perdu") +
                " a été publié avec succès.");
        notification.setType(NotificationType.LOST_ANIMAL_POSTED);
        notification.setReferenceId(post.getId().toString());
        notification.setReferenceType("LostAnimalPost");

        notificationRepository.save(notification);
    }

    // ✅ NOTIFICATION - Demande d'adoption - CORRIGÉ
    @Transactional
    public void sendAdoptionRequestNotification(User owner, AdoptionRequest request) {
        // Créer la notification (toujours sauvegardée)
        Notification notification = new Notification();
        notification.setUser(owner);
        notification.setTitle("📨 Nouvelle demande d'adoption");
        notification.setMessage(request.getRequester().getFirstName() + " " +
                request.getRequester().getLastName() + " souhaite adopter " +
                request.getAdoptionPost().getTitle());
        notification.setType(NotificationType.ADOPTION_REQUEST);
        notification.setReferenceId(request.getId().toString());
        notification.setReferenceType("AdoptionRequest");

        notificationRepository.save(notification);

        // ✅ TRY/CATCH pour éviter le rollback en cas d'erreur email
        try {
            emailService.sendAdoptionRequestEmail(owner, request);
            System.out.println("📧 Email de demande envoyé à: " + owner.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email (ignorée, transaction continue): " + e.getMessage());
            // Ne pas relancer l'exception - la transaction continue
        }
    }

    // ✅ NOTIFICATION - Réponse à une demande - CORRIGÉ
    @Transactional
    public void sendAdoptionResponseNotification(User requester, AdoptionRequest request) {
        Notification notification = new Notification();
        notification.setUser(requester);
        notification.setTitle("📬 Réponse à votre demande d'adoption");
        notification.setMessage("Votre demande pour " + request.getAdoptionPost().getTitle() +
                " a été " + (request.getStatus() == RequestStatus.ACCEPTED ? "acceptée ✅" : "refusée ❌"));
        notification.setType(NotificationType.ADOPTION_RESPONSE);
        notification.setReferenceId(request.getId().toString());
        notification.setReferenceType("AdoptionRequest");

        notificationRepository.save(notification);

        // ✅ TRY/CATCH pour éviter le rollback en cas d'erreur email
        try {
            emailService.sendAdoptionResponseEmail(requester, request);
            System.out.println("📧 Email de réponse envoyé à: " + requester.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email (ignorée, transaction continue): " + e.getMessage());
            // Ne pas relancer l'exception - la transaction continue
        }
    }

    // ✅ NOTIFICATION - Réponse forum
    @Transactional
    public void sendForumReplyNotification(User postAuthor, ForumComment comment) {
        Notification notification = new Notification();
        notification.setUser(postAuthor);
        notification.setTitle("💬 Nouvelle réponse sur votre post");
        notification.setMessage(comment.getUser().getFirstName() + " " +
                comment.getUser().getLastName() + " a répondu à votre discussion");
        notification.setType(NotificationType.FORUM_REPLY);
        notification.setReferenceId(comment.getForumPost().getId().toString());
        notification.setReferenceType("ForumPost");

        notificationRepository.save(notification);
    }

    // ✅ NOTIFICATION - Rappel animal perdu
    @Transactional
    public void sendLongLostAlert(LostAnimalPost post) {
        String animalName = post.getName() != null ? post.getName() : "votre animal";

        Notification notification = new Notification();
        notification.setUser(post.getUser());
        notification.setTitle("⏰ Rappel - Animal perdu depuis 30 jours");
        notification.setMessage("Cela fait 30 jours que " + animalName + " a disparu. " +
                "Pensez à mettre à jour l'annonce si besoin.");
        notification.setType(NotificationType.LOST_ANIMAL_REMINDER);
        notification.setReferenceId(post.getId().toString());
        notification.setReferenceType("LostAnimalPost");

        notificationRepository.save(notification);
    }

    // ✅ NOTIFICATION - Message de contact
    @Transactional
    public void sendContactMessage(User owner, User contact, String message, LostAnimalPost post) {
        String animalName = post.getName() != null ? post.getName() : "l'animal perdu";

        Notification notification = new Notification();
        notification.setUser(owner);
        notification.setTitle("📱 Nouveau message à propos de " + animalName);
        notification.setMessage(contact.getFirstName() + " " + contact.getLastName() +
                " vous a contacté : \"" + truncate(message, 50) + "...\"");
        notification.setType(NotificationType.CONTACT_MESSAGE);
        notification.setReferenceId(post.getId().toString());
        notification.setReferenceType("LostAnimalPost");

        notificationRepository.save(notification);

        // ✅ TRY/CATCH pour éviter le rollback
        try {
            emailService.sendContactEmail(owner, contact, message, post);
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email contact (ignorée): " + e.getMessage());
        }
    }

    // ✅ Méthodes de lecture
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // ✅ NOUVELLE MÉTHODE: Récupérer les notifications par type
    public List<Notification> getNotificationsByType(Long userId, String type) {
        return notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
    }

    // ✅ NOUVELLE MÉTHODE: Récupérer une notification par ID
    public Notification getNotificationById(Long id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        return notification.orElse(null);
    }

    // ✅ MODIFIÉ: Marquer comme lue (retourne void mais utilise le repository)
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    // ✅ MODIFIÉ: Marquer toutes comme lues (retourne int)
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    // ✅ NOUVELLE MÉTHODE: Supprimer les notifications lues
    @Transactional
    public int deleteReadNotifications(Long userId) {
        return notificationRepository.deleteByUserIdAndIsReadTrue(userId);
    }

    // ✅ Supprimer une notification
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));

        if (notification.getUser().getId().equals(userId)) {
            notificationRepository.delete(notification);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength);
    }
}
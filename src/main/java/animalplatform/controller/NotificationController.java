package animalplatform.controller;

import animalplatform.dto.NotificationDTO;
import animalplatform.model.Notification;
import animalplatform.model.User;
import animalplatform.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Voir toutes les notifications
    @GetMapping
    public String viewNotifications(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        List<Notification> notifications = notificationService.getUserNotifications(currentUser.getId());
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser.getId()));

        return "pawcare/components/notifications";
    }

    // ✅ API: Obtenir toutes les notifications (format JSON)
    @GetMapping("/api/all")
    @ResponseBody
    public List<NotificationDTO> getAllNotifications(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return List.of();
        }

        List<Notification> notifications = notificationService.getUserNotifications(currentUser.getId());
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Obtenir les notifications non lues (pour le badge)
    @GetMapping("/unread")
    @ResponseBody
    public List<Notification> getUnreadNotifications(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return List.of();
        }
        return notificationService.getUnreadNotifications(currentUser.getId());
    }

    // ✅ API: Obtenir les notifications non lues (format DTO)
    @GetMapping("/api/unread")
    @ResponseBody
    public List<NotificationDTO> getUnreadNotificationsDTO(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return List.of();
        }

        List<Notification> notifications = notificationService.getUnreadNotifications(currentUser.getId());
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ API: Obtenir le nombre de notifications non lues
    @GetMapping("/api/unread-count")
    @ResponseBody
    public long getUnreadCount(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return 0;
        }
        return notificationService.getUnreadCount(currentUser.getId());
    }

    // ✅ API: Obtenir les notifications de demandes d'adoption
    @GetMapping("/api/adoption-requests")
    @ResponseBody
    public List<NotificationDTO> getAdoptionRequestNotifications(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return List.of();
        }

        List<Notification> notifications = notificationService.getNotificationsByType(
                currentUser.getId(),
                "ADOPTION_REQUEST"
        );

        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ API: Obtenir les réponses aux demandes d'adoption
    @GetMapping("/api/adoption-responses")
    @ResponseBody
    public List<NotificationDTO> getAdoptionResponseNotifications(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return List.of();
        }

        List<Notification> notifications = notificationService.getNotificationsByType(
                currentUser.getId(),
                "ADOPTION_RESPONSE"
        );

        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Marquer comme lue
    @PostMapping("/{id}/read")
    @ResponseBody
    public String markAsRead(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "error: Non authentifié";
        }

        try {
            notificationService.markAsRead(id, currentUser.getId());
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // ✅ API: Marquer comme lue (format JSON)
    @PostMapping("/api/{id}/read")
    @ResponseBody
    public NotificationDTO markAsReadJson(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return null;
        }

        try {
            notificationService.markAsRead(id, currentUser.getId());
            Notification notification = notificationService.getNotificationById(id);
            return notification != null ? NotificationDTO.fromEntity(notification) : null;
        } catch (Exception e) {
            return null;
        }
    }

    // Marquer toutes comme lues
    @PostMapping("/read-all")
    @ResponseBody
    public String markAllAsRead(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "error: Non authentifié";
        }

        try {
            notificationService.markAllAsRead(currentUser.getId());
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // ✅ API: Marquer toutes comme lues (format JSON)
    @PostMapping("/api/read-all")
    @ResponseBody
    public int markAllAsReadJson(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return 0;
        }

        return notificationService.markAllAsRead(currentUser.getId());
    }

    // Supprimer une notification
    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteNotification(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "error: Non authentifié";
        }

        try {
            notificationService.deleteNotification(id, currentUser.getId());
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // ✅ API: Supprimer une notification (format JSON)
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public boolean deleteNotificationJson(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return false;
        }

        try {
            notificationService.deleteNotification(id, currentUser.getId());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ API: Supprimer toutes les notifications lues
    @DeleteMapping("/api/delete-read")
    @ResponseBody
    public int deleteReadNotifications(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return 0;
        }

        return notificationService.deleteReadNotifications(currentUser.getId());
    }
}

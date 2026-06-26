package animalplatform.controller;

import animalplatform.dto.LostAnimalPostDTO;
import animalplatform.model.LostAnimalPost;
import animalplatform.model.User;
import animalplatform.service.LostAnimalService;
import animalplatform.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/perdus")
public class LostAnimalController {

    @Autowired
    private LostAnimalService lostAnimalService;

    @Autowired
    private NotificationService notificationService;

    // Liste des animaux perdus
    @GetMapping
    public String listLostAnimals(Model model) {
        List<LostAnimalPost> lostAnimals = lostAnimalService.getAllActive();
        model.addAttribute("lostAnimals", lostAnimals);
        return "pawcare/components/perdus-section :: lostList";
    }

    // ✅ CORRIGÉ: Voir un signalement spécifique - avec isOwner
    @GetMapping("/{id}")
    public String viewLostAnimal(@PathVariable Long id, Model model, HttpSession session) {
        LostAnimalPost post = lostAnimalService.getPostById(id);
        lostAnimalService.incrementViewCount(id);
        model.addAttribute("post", post);
        model.addAttribute("isOwner", isOwner(post, session));  // ← AJOUTÉ
        return "pawcare/pages/lost-details";
    }

    // Formulaire de signalement
    @GetMapping("/report")
    public String showReportForm(Model model, HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("lostAnimalPost", new LostAnimalPostDTO());
        return "pawcare/components/lost-report";
    }

    // ✅ CORRIGÉ: Signaler un animal perdu (avec image)
    @PostMapping("/report")
    public String reportLostAnimal(
            @Valid @ModelAttribute LostAnimalPostDTO postDTO,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Veuillez vous connecter");
            return "redirect:/auth/login";
        }

        try {
            System.out.println("📝 Signalement d'animal perdu:");
            System.out.println("   Titre: " + postDTO.getTitle());
            System.out.println("   Type: " + postDTO.getAnimalType());
            System.out.println("   Lieu: " + postDTO.getLastSeenLocation());
            System.out.println("   Images reçues: " + (images != null ? images.size() : 0));

            LostAnimalPost post = lostAnimalService.createPost(postDTO, currentUser, images);

            // Notifier les utilisateurs dans la zone
            notificationService.notifyUsersInArea(post);

            redirectAttributes.addFlashAttribute("success", "✅ Signalement publié avec succès !");
            return "redirect:/perdus/" + post.getId();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du signalement: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/perdus/report";
        }
    }

    // ✅ NOUVEAU: Afficher le formulaire d'édition
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        LostAnimalPost post = lostAnimalService.getPostById(id);

        if (!canEdit(post, session)) {
            redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à modifier ce signalement");
            return "redirect:/perdus/" + id;
        }

        model.addAttribute("post", post);
        return "pawcare/components/lost-edit";
    }

    // ✅ NOUVEAU: Traiter la modification
    @PostMapping("/{id}/edit")
    public String updateLostAnimal(
            @PathVariable Long id,
            @Valid @ModelAttribute LostAnimalPostDTO postDTO,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        LostAnimalPost existingPost = lostAnimalService.getPostById(id);

        if (!canEdit(existingPost, session)) {
            redirectAttributes.addFlashAttribute("error", "Non autorisé");
            return "redirect:/perdus/" + id;
        }

        try {
            User currentUser = (User) session.getAttribute("user");
            lostAnimalService.updatePost(id, postDTO, currentUser, images);
            redirectAttributes.addFlashAttribute("success", "✅ Signalement mis à jour avec succès !");
            return "redirect:/perdus/" + id;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la modification: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/perdus/" + id + "/edit";
        }
    }

    // ✅ NOUVEAU: Changer le statut
    @PostMapping("/{id}/status")
    @ResponseBody
    public Map<String, Object> changeStatus(@PathVariable Long id,
                                            @RequestParam String status,
                                            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        LostAnimalPost post = lostAnimalService.getPostById(id);

        if (!canEdit(post, session)) {
            response.put("success", false);
            response.put("message", "Non autorisé");
            return response;
        }

        try {
            lostAnimalService.changeStatus(id, status);
            response.put("success", true);
            response.put("message", "Statut mis à jour");
            response.put("newStatus", status);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ✅ API: Récupérer tous les signalements (JSON)
    @GetMapping("/api/all")
    @ResponseBody
    public List<LostAnimalPostDTO> getAllLostAnimalsApi() {
        List<LostAnimalPost> posts = lostAnimalService.getAllActive();
        return posts.stream()
                .map(LostAnimalPostDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    // ✅ API: Récupérer un signalement par ID (JSON)
    @GetMapping("/api/{id}")
    @ResponseBody
    public LostAnimalPostDTO getLostAnimalApi(@PathVariable Long id) {
        LostAnimalPost post = lostAnimalService.getPostById(id);
        return LostAnimalPostDTO.fromEntity(post);
    }

    // Marquer comme trouvé (ancien endpoint, gardé pour compatibilité)
    @PostMapping("/{id}/found")
    @ResponseBody
    public Map<String, Object> markAsFound(@PathVariable Long id, HttpSession session) {
        return changeStatus(id, "FOUND", session);
    }

    // Supprimer le signalement
    @PostMapping("/{id}/delete")
    @ResponseBody
    public Map<String, Object> deletePost(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        LostAnimalPost post = lostAnimalService.getPostById(id);

        if (!canEdit(post, session)) {
            response.put("success", false);
            response.put("message", "Non autorisé");
            return response;
        }

        try {
            lostAnimalService.deletePost(id);
            response.put("success", true);
            response.put("message", "Signalement supprimé avec succès");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Contacter le propriétaire
    @PostMapping("/{id}/contact")
    @ResponseBody
    public Map<String, Object> contactOwner(@PathVariable Long id,
                                            @RequestParam String message,
                                            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            LostAnimalPost post = lostAnimalService.getPostById(id);
            notificationService.sendContactMessage(post.getUser(), currentUser, message, post);
            response.put("success", true);
            response.put("message", "Message envoyé au propriétaire");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Recherche par zone
    @GetMapping("/search")
    @ResponseBody
    public List<LostAnimalPostDTO> searchByLocation(@RequestParam String location) {
        List<LostAnimalPost> posts = lostAnimalService.findByLocation(location);
        return posts.stream()
                .map(LostAnimalPostDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    // ✅ Vérifier si l'utilisateur est le propriétaire
    private boolean isOwner(LostAnimalPost post, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && post.getUser().getId().equals(user.getId());
    }

    // ✅ Vérifier si l'utilisateur peut modifier (propriétaire ou admin)
    private boolean canEdit(LostAnimalPost post, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && (post.getUser().getId().equals(user.getId()) ||
                user.getRole().toString().equals("ADMIN"));
    }

    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute("user") != null;
    }
}
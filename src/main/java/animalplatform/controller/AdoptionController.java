package animalplatform.controller;

import animalplatform.dto.AdoptionPostDTO;
import animalplatform.dto.AdoptionRequestDTO;
import animalplatform.model.AdoptionPost;
import animalplatform.model.User;
import animalplatform.model.AdoptionRequest;
import animalplatform.service.AdoptionService;
import animalplatform.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/adoption")
public class AdoptionController {

    @Autowired
    private AdoptionService adoptionService;

    @Autowired
    private NotificationService notificationService;

    // ✅ ENDPOINT DE DEBUG
    @GetMapping("/debug")
    @ResponseBody
    public String debug() {
        return "✅ Adoption controller is working!";
    }

    // Liste des annonces d'adoption avec filtres
    @GetMapping
    public String listAdoptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String animalType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean urgent,
            Model model) {

        System.out.println("📋 GET /adoption - Affichage de la liste des annonces");

        Page<AdoptionPost> adoptionPage = adoptionService.getFilteredPosts(
                animalType, location, urgent, PageRequest.of(page, size));

        model.addAttribute("adoptions", adoptionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", adoptionPage.getTotalPages());
        model.addAttribute("totalItems", adoptionPage.getTotalElements());

        return "pawcare/components/adoption-section :: adoptionList";
    }

    // Voir une annonce spécifique
    @GetMapping("/{id}")
    public String viewAdoption(@PathVariable Long id, Model model, HttpSession session) {
        System.out.println("👁️ GET /adoption/" + id + " - Affichage des détails");

        AdoptionPost post = adoptionService.getPostById(id);
        adoptionService.incrementViewCount(id);

        model.addAttribute("post", post);
        model.addAttribute("isOwner", isOwner(post, session));
        model.addAttribute("similarPosts", adoptionService.getSimilarPosts(post, 4));

        return "pawcare/components/adoption-details";
    }

    // ✅ Afficher le formulaire de demande d'adoption
    @GetMapping("/{id}/request-form")
    public String showRequestForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        System.out.println("📝 GET /adoption/" + id + "/request-form - Formulaire de demande d'adoption");

        if (!isAuthenticated(session)) {
            redirectAttributes.addFlashAttribute("error", "Veuillez vous connecter pour faire une demande");
            return "redirect:/auth/login";
        }

        AdoptionPost post = adoptionService.getPostById(id);

        // Vérifier que l'utilisateur n'est pas le propriétaire
        if (isOwner(post, session)) {
            redirectAttributes.addFlashAttribute("error", "Vous ne pouvez pas adopter votre propre animal");
            return "redirect:/adoption/" + id;
        }

        // Vérifier que l'animal est encore disponible
        if (post.getStatus() != null && post.getStatus().toString().equals("ADOPTED")) {
            redirectAttributes.addFlashAttribute("error", "Cet animal a déjà été adopté");
            return "redirect:/adoption/" + id;
        }

        model.addAttribute("post", post);
        model.addAttribute("request", new AdoptionRequest());
        return "pawcare/components/adoption-request-form";
    }

    // ✅ Traitement de la demande d'adoption
    @PostMapping("/{id}/request")
    public String requestAdoption(
            @PathVariable Long id,
            @ModelAttribute AdoptionRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        System.out.println("📨 POST /adoption/" + id + "/request - Demande d'adoption");

        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Veuillez vous connecter");
            return "redirect:/auth/login";
        }

        // Vérifier que l'utilisateur n'est pas le propriétaire
        AdoptionPost post = adoptionService.getPostById(id);
        if (post.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Vous ne pouvez pas adopter votre propre animal");
            return "redirect:/adoption/" + id;
        }

        // Vérifier que l'animal est disponible
        if (post.getStatus() != null && post.getStatus().toString().equals("ADOPTED")) {
            redirectAttributes.addFlashAttribute("error", "Cet animal a déjà été adopté");
            return "redirect:/adoption/" + id;
        }

        try {
            // Associer le demandeur et l'annonce
            request.setRequester(currentUser);
            request.setAdoptionPost(post);
            request.setOwner(post.getUser());

            AdoptionRequest adoptionRequest = adoptionService.createRequest(id, currentUser, request);
            notificationService.sendAdoptionRequestNotification(
                    adoptionRequest.getOwner(),
                    adoptionRequest
            );
            redirectAttributes.addFlashAttribute("success", "Demande envoyée avec succès !");
            return "redirect:/adoption/" + id;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la demande: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/adoption/" + id + "/request-form";
        }
    }

    // Formulaire de création d'annonce
    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {
        System.out.println("📝 GET /adoption/create - Formulaire de création");

        if (!isAuthenticated(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("adoptionPost", new AdoptionPostDTO());
        return "pawcare/components/adoption-create";
    }

    // Créer une annonce
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createAdoption(
            @Valid @ModelAttribute AdoptionPostDTO postDTO,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        System.out.println("=== CRÉATION D'ANNONCE ===");
        System.out.println("Titre: " + postDTO.getTitle());
        System.out.println("Type: " + postDTO.getAnimalType());

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            AdoptionPost post = adoptionService.createPost(postDTO, currentUser, images);
            response.put("success", true);
            response.put("message", "Annonce créée avec succès");
            response.put("postId", post.getId());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    // Modifier une annonce
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        AdoptionPost post = adoptionService.getPostById(id);
        if (!canEdit(post, session)) {
            return "redirect:/adoption/" + id;
        }
        model.addAttribute("post", post);
        return "pawcare/components/adoption-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateAdoption(
            @PathVariable Long id,
            @Valid @ModelAttribute AdoptionPostDTO postDTO,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!canEdit(adoptionService.getPostById(id), session)) {
            return "redirect:/adoption/" + id;
        }

        try {
            adoptionService.updatePost(id, postDTO);
            redirectAttributes.addFlashAttribute("success", "Annonce mise à jour!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/adoption/" + id;
    }

    // Supprimer une annonce
    @PostMapping("/{id}/delete")
    @ResponseBody
    public Map<String, Object> deleteAdoption(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        AdoptionPost post = adoptionService.getPostById(id);

        if (!canEdit(post, session)) {
            response.put("success", false);
            response.put("message", "Non autorisé");
            return response;
        }

        try {
            adoptionService.deletePost(id);
            response.put("success", true);
            response.put("message", "Annonce supprimée avec succès");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Marquer comme adopté
    @PostMapping("/{id}/mark-adopted")
    @ResponseBody
    public Map<String, Object> markAsAdopted(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        AdoptionPost post = adoptionService.getPostById(id);

        if (!canEdit(post, session)) {
            response.put("success", false);
            response.put("message", "Non autorisé");
            return response;
        }

        try {
            adoptionService.markAsAdopted(id);
            response.put("success", true);
            response.put("message", "Animal marqué comme adopté");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Filtrer les annonces
    @GetMapping("/filter")
    @ResponseBody
    public List<AdoptionPostDTO> filterAdoptions(
            @RequestParam(required = false) String animalType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean urgent,
            HttpSession session) {

        List<AdoptionPost> results;

        if (urgent != null && urgent) {
            results = adoptionService.getUrgentPosts();
        } else if (animalType != null && !animalType.isEmpty()) {
            results = adoptionService.filterByAnimalType(animalType);
        } else {
            results = adoptionService.getAllAvailablePosts();
        }

        return results.stream()
                .map(AdoptionPostDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Récupérer tous les posts disponibles
    @GetMapping("/all")
    @ResponseBody
    public List<AdoptionPostDTO> getAllAvailable(HttpSession session) {
        List<AdoptionPost> posts = adoptionService.getAllAvailablePosts();
        return posts.stream()
                .map(AdoptionPostDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ============================================
    // GESTION DES DEMANDES D'ADOPTION (POUR LE PROPRIÉTAIRE)
    // ============================================

    // ✅ Voir les demandes reçues pour mes annonces (page HTML)
    @GetMapping("/my-received-requests")
    public String showReceivedRequests(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        List<AdoptionRequest> requests = adoptionService.getRequestsByOwner(currentUser.getId());
        model.addAttribute("requests", requests);
        return "pawcare/pages/received-requests";
    }

    // ✅ API: Voir mes demandes envoyées (JSON)
    @GetMapping("/my-requests")
    @ResponseBody
    public List<AdoptionRequestDTO> getMyRequests(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return List.of();
        }

        List<AdoptionRequest> requests = adoptionService.getRequestsByRequester(currentUser.getId());
        return requests.stream()
                .map(AdoptionRequestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ API: Voir les demandes reçues (JSON)
    @GetMapping("/received-requests")
    @ResponseBody
    public List<AdoptionRequestDTO> getReceivedRequests(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return List.of();
        }

        List<AdoptionRequest> requests = adoptionService.getRequestsByOwner(currentUser.getId());
        return requests.stream()
                .map(AdoptionRequestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ API: Accepter une demande
    @PostMapping("/request/{requestId}/accept")
    @ResponseBody
    public Map<String, Object> acceptRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String message,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            adoptionService.acceptRequest(requestId, currentUser, message);
            response.put("success", true);
            response.put("message", "Demande acceptée avec succès");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ✅ API: Refuser une demande
    @PostMapping("/request/{requestId}/reject")
    @ResponseBody
    public Map<String, Object> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String message,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            adoptionService.rejectRequest(requestId, currentUser, message);
            response.put("success", true);
            response.put("message", "Demande refusée");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ✅ API: Annuler une demande (par le demandeur)
    @PostMapping("/request/{requestId}/cancel")
    @ResponseBody
    public Map<String, Object> cancelRequest(
            @PathVariable Long requestId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            adoptionService.cancelRequest(requestId, currentUser);
            response.put("success", true);
            response.put("message", "Demande annulée");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    private boolean isOwner(AdoptionPost post, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && post.getUser().getId().equals(user.getId());
    }

    private boolean canEdit(AdoptionPost post, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && (post.getUser().getId().equals(user.getId()) ||
                user.getRole().toString().equals("ADMIN"));
    }

    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute("user") != null;
    }
}
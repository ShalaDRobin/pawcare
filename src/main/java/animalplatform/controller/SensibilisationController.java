package animalplatform.controller;

import animalplatform.dto.SensibilisationDTO;
import animalplatform.model.Sensibilisation;
import animalplatform.model.User;
import animalplatform.service.SensibilisationService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sensibilisation")
public class SensibilisationController {

    @Autowired
    private SensibilisationService sensibilisationService;

    // ✅ Liste complète des articles (du plus récent au plus ancien)
    @GetMapping
    public String listArticles(
            @RequestParam(required = false) String category,
            Model model) {

        List<Sensibilisation> articles;
        if (category != null && !category.isEmpty()) {
            articles = sensibilisationService.getByCategory(category);
            model.addAttribute("currentCategory", category);
        } else {
            articles = sensibilisationService.getAllPublished();
            model.addAttribute("currentCategory", null);
        }

        model.addAttribute("articles", articles);
        model.addAttribute("categories", sensibilisationService.getAllCategories());
        return "pawcare/pages/sensibilisation-list";
    }

    // ✅ Voir un article
    @GetMapping("/{id}")
    public String viewArticle(@PathVariable Long id, Model model, HttpSession session) {
        Sensibilisation article = sensibilisationService.getArticleById(id);
        sensibilisationService.incrementReadCount(id);

        model.addAttribute("article", article);
        model.addAttribute("isOwner", isOwner(article, session));
        model.addAttribute("relatedArticles",
                sensibilisationService.getRelatedArticles(article, 3));

        return "pawcare/components/sensibilisation-details";
    }

    // ✅ Formulaire de création d'article
    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("article", new SensibilisationDTO());
        return "pawcare/components/sensibilisation-create";
    }

    // ✅ Créer un article
    @PostMapping("/create")
    public String createArticle(
            @Valid @ModelAttribute SensibilisationDTO articleDTO,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Veuillez vous connecter");
            return "redirect:/auth/login";
        }

        try {
            Sensibilisation article = sensibilisationService.createArticle(articleDTO, currentUser, images);
            redirectAttributes.addFlashAttribute("success", "✅ Article publié avec succès !");
            return "redirect:/sensibilisation/" + article.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/sensibilisation/create";
        }
    }

    // ✅ Formulaire de modification
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Sensibilisation article = sensibilisationService.getArticleById(id);

        if (!canEdit(article, session)) {
            redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à modifier cet article");
            return "redirect:/sensibilisation/" + id;
        }

        model.addAttribute("article", article);
        return "pawcare/components/sensibilisation-edit";
    }

    // ✅ Modifier un article
    @PostMapping("/{id}/edit")
    public String updateArticle(
            @PathVariable Long id,
            @Valid @ModelAttribute SensibilisationDTO articleDTO,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Veuillez vous connecter");
            return "redirect:/auth/login";
        }

        try {
            sensibilisationService.updateArticle(id, articleDTO, currentUser, images);
            redirectAttributes.addFlashAttribute("success", "✅ Article mis à jour avec succès !");
            return "redirect:/sensibilisation/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/sensibilisation/" + id + "/edit";
        }
    }

    // ✅ Supprimer un article
    @PostMapping("/{id}/delete")
    @ResponseBody
    public Map<String, Object> deleteArticle(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            sensibilisationService.deleteArticle(id, currentUser);
            response.put("success", true);
            response.put("message", "Article supprimé avec succès");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ✅ Ajouter un like
    @PostMapping("/{id}/like")
    @ResponseBody
    public Map<String, Object> likeArticle(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            sensibilisationService.incrementLikeCount(id);
            response.put("success", true);
            response.put("message", "Like ajouté !");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ✅ Recherche d'articles (API JSON)
    @GetMapping("/api/search")
    @ResponseBody
    public List<SensibilisationDTO> searchArticles(@RequestParam String query) {
        List<Sensibilisation> articles = sensibilisationService.searchByKeyword(query);
        return articles.stream()
                .map(SensibilisationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ Articles par catégorie (API JSON)
    @GetMapping("/api/category/{category}")
    @ResponseBody
    public List<SensibilisationDTO> getByCategoryApi(@PathVariable String category) {
        List<Sensibilisation> articles = sensibilisationService.getByCategory(category);
        return articles.stream()
                .map(SensibilisationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ Articles par tag
    @GetMapping("/tag/{tag}")
    public String getByTag(@PathVariable String tag, Model model) {
        model.addAttribute("articles", sensibilisationService.getByTag(tag));
        model.addAttribute("currentTag", tag);
        model.addAttribute("categories", sensibilisationService.getAllCategories());
        return "pawcare/pages/sensibilisation-list";
    }

    // ✅ Tous les articles (JSON)
    @GetMapping("/api/all")
    @ResponseBody
    public List<SensibilisationDTO> getAllArticlesApi() {
        List<Sensibilisation> articles = sensibilisationService.getAllPublished();
        return articles.stream()
                .map(SensibilisationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ Changer le statut de publication
    @PostMapping("/{id}/toggle-publish")
    @ResponseBody
    public Map<String, Object> togglePublish(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            sensibilisationService.togglePublishStatus(id, currentUser);
            response.put("success", true);
            response.put("message", "Statut modifié !");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    private boolean isOwner(Sensibilisation article, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && article.getUser() != null &&
                article.getUser().getId().equals(user.getId());
    }

    private boolean canEdit(Sensibilisation article, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && article.getUser() != null &&
                (article.getUser().getId().equals(user.getId()) ||
                        user.getRole().toString().equals("ADMIN"));
    }

    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute("user") != null;
    }
}
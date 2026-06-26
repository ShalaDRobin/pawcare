package animalplatform.controller;

import animalplatform.dto.ForumCommentDTO;
import animalplatform.dto.ForumPostDTO;
import animalplatform.model.ForumCategory;
import animalplatform.model.ForumPost;
import animalplatform.model.ForumComment;
import animalplatform.model.User;
import animalplatform.service.ForumService;
import animalplatform.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private NotificationService notificationService;

    // ============================================
    // PAGE PRINCIPALE DU FORUM - REDIRECTION VERS L'ACCUEIL
    // ============================================

    @GetMapping
    public String forumIndex() {
        return "redirect:/";
    }

    // ============================================
    // TEST
    // ============================================

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "✅ Forum controller is working!";
    }

    @PostMapping("/test-post")
    @ResponseBody
    public String testPost() {
        System.out.println("🔴🔴🔴 POST TEST RECU ! 🔴🔴🔴");
        return "✅ POST test OK!";
    }

    // ============================================
    // CATÉGORIES
    // ============================================

    @GetMapping("/categorie/{categoryId}")
    public String viewCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        ForumCategory category = forumService.getCategoryById(categoryId);
        Page<ForumPost> posts = forumService.getPostsByCategory(
                categoryId, PageRequest.of(page, 20));

        model.addAttribute("category", category);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", posts.getTotalPages());
        model.addAttribute("totalPosts", posts.getTotalElements());
        // ✅ Toutes les catégories pour la sidebar
        model.addAttribute("allCategories", forumService.getAllCategories());

        return "pawcare/pages/forum-category";
    }

    // ============================================
    // POSTS
    // ============================================

    @GetMapping("/post/{postId}")
    public String viewPost(@PathVariable Long postId, Model model, HttpSession session) {
        ForumPost post = forumService.getPostById(postId);
        forumService.incrementViewCount(postId);

        List<ForumComment> comments = forumService.getCommentsByPost(postId);

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("isOwner", isOwner(post, session));
        model.addAttribute("isAdmin", isAdmin(session));

        return "pawcare/pages/forum-post";
    }

    // ✅ Formulaire de création
    @GetMapping("/creer")
    public String showCreatePostForm(Model model, HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/auth/login";
        }

        model.addAttribute("categories", forumService.getAllCategories());
        model.addAttribute("forumPost", new ForumPostDTO());
        return "pawcare/components/forum-create";
    }

    // ✅ Créer un post - AVEC LOGS
    @PostMapping("/creer")
    public String createPost(
            @Valid @ModelAttribute ForumPostDTO postDTO,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        System.out.println("🔴🔴🔴 POST /forum/creer RECU ! 🔴🔴🔴");
        System.out.println("Titre: " + postDTO.getTitle());
        System.out.println("Catégorie ID: " + postDTO.getCategoryId());
        System.out.println("Contenu longueur: " + (postDTO.getContent() != null ? postDTO.getContent().length() : 0));

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            System.out.println("❌ Utilisateur non connecté");
            return "redirect:/auth/login";
        }

        System.out.println("Utilisateur: " + currentUser.getEmail());

        try {
            ForumPost post = forumService.createPost(postDTO, currentUser);
            System.out.println("✅ Post créé avec ID: " + post.getId());
            redirectAttributes.addFlashAttribute("success", "✅ Post créé avec succès!");
            return "redirect:/forum/post/" + post.getId();
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "❌ Erreur: " + e.getMessage());
            return "redirect:/forum/creer";
        }
    }

    // ✅ Formulaire de modification d'un post
    @GetMapping("/post/{postId}/modifier")
    public String showEditPostForm(@PathVariable Long postId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        ForumPost post = forumService.getPostById(postId);
        if (!forumService.canEditPost(postId, currentUser)) {
            return "redirect:/forum/post/" + postId;
        }

        ForumPostDTO postDTO = ForumPostDTO.fromEntity(post);
        model.addAttribute("post", post);
        model.addAttribute("postDTO", postDTO);
        model.addAttribute("categories", forumService.getAllCategories());
        return "pawcare/components/forum-edit";
    }

    // ✅ Modifier un post
    @PostMapping("/post/{postId}/modifier")
    public String updatePost(
            @PathVariable Long postId,
            @Valid @ModelAttribute ForumPostDTO postDTO,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            forumService.updatePost(postId, postDTO, currentUser);
            redirectAttributes.addFlashAttribute("success", "✅ Post modifié avec succès!");
            return "redirect:/forum/post/" + postId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur: " + e.getMessage());
            return "redirect:/forum/post/" + postId + "/modifier";
        }
    }

    // ✅ Supprimer un post
    @PostMapping("/post/{postId}/supprimer")
    @ResponseBody
    public Map<String, Object> deletePost(@PathVariable Long postId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            forumService.deletePost(postId, currentUser);
            response.put("success", true);
            response.put("message", "Post supprimé avec succès");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ============================================
    // COMMENTAIRES
    // ============================================

    @GetMapping("/post/{postId}/comments")
    @ResponseBody
    public Map<String, Object> getComments(@PathVariable Long postId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ForumComment> comments = forumService.getCommentsByPost(postId);
            List<ForumCommentDTO> commentDTOs = comments.stream()
                    .map(ForumCommentDTO::fromEntity)
                    .collect(Collectors.toList());
            response.put("success", true);
            response.put("comments", commentDTOs);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/post/{postId}/commenter")
    @ResponseBody
    public Map<String, Object> addComment(
            @PathVariable Long postId,
            @RequestParam String content,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            ForumComment comment = forumService.addComment(postId, currentUser, content);

            ForumPost post = forumService.getPostById(postId);
            if (!post.getUser().getId().equals(currentUser.getId())) {
                notificationService.sendForumReplyNotification(post.getUser(), comment);
            }

            response.put("success", true);
            response.put("message", "Commentaire ajouté avec succès");
            response.put("comment", ForumCommentDTO.fromEntity(comment));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/post/{postId}/comment/{commentId}/repondre")
    @ResponseBody
    public Map<String, Object> replyToComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam String content,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            ForumComment reply = forumService.replyToComment(postId, commentId, currentUser, content);
            response.put("success", true);
            response.put("message", "Réponse ajoutée avec succès");
            response.put("reply", ForumCommentDTO.fromEntity(reply));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/comment/{commentId}/modifier")
    @ResponseBody
    public Map<String, Object> updateComment(
            @PathVariable Long commentId,
            @RequestParam String content,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            ForumComment updatedComment = forumService.updateComment(commentId, content, currentUser);
            response.put("success", true);
            response.put("message", "Commentaire modifié avec succès");
            response.put("comment", ForumCommentDTO.fromEntity(updatedComment));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/comment/{commentId}/supprimer")
    @ResponseBody
    public Map<String, Object> deleteComment(@PathVariable Long commentId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            forumService.deleteComment(commentId, currentUser);
            response.put("success", true);
            response.put("message", "Commentaire supprimé avec succès");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ============================================
    // LIKES
    // ============================================

    @PostMapping("/post/{postId}/liker")
    @ResponseBody
    public Map<String, Object> likePost(@PathVariable Long postId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            forumService.likePost(postId, currentUser);
            response.put("success", true);
            response.put("message", "Like ajouté");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/comment/{commentId}/liker")
    @ResponseBody
    public Map<String, Object> likeComment(@PathVariable Long commentId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Veuillez vous connecter");
            return response;
        }

        try {
            forumService.likeComment(commentId, currentUser);
            response.put("success", true);
            response.put("message", "Like ajouté");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ============================================
    // ADMIN
    // ============================================

    @PostMapping("/post/{postId}/epingler")
    @ResponseBody
    public Map<String, Object> pinPost(@PathVariable Long postId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "Non autorisé");
            return response;
        }

        try {
            forumService.pinPost(postId);
            response.put("success", true);
            response.put("message", "Post épinglé/désépinglé");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/post/{postId}/verrouiller")
    @ResponseBody
    public Map<String, Object> lockPost(@PathVariable Long postId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "Non autorisé");
            return response;
        }

        try {
            forumService.lockPost(postId);
            response.put("success", true);
            response.put("message", "Post verrouillé/déverrouillé");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ============================================
    // RECHERCHE
    // ============================================

    @GetMapping("/rechercher")
    @ResponseBody
    public List<ForumPostDTO> searchForum(@RequestParam String query) {
        List<ForumPost> posts = forumService.searchPosts(query);
        return posts.stream()
                .map(ForumPostDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ============================================
    // MÉTHODES PRIVÉES
    // ============================================

    private boolean isOwner(ForumPost post, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && post.getUser().getId().equals(user.getId());
    }

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.getRole().toString().equals("ADMIN");
    }

    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute("user") != null;
    }
}
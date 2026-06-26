package animalplatform.controller;

import animalplatform.model.AdoptionPost;
import animalplatform.model.LostAnimalPost;
import animalplatform.model.ForumPost;
import animalplatform.model.ForumCategory;
import animalplatform.model.Sensibilisation;
import animalplatform.service.AdoptionService;
import animalplatform.service.LostAnimalService;
import animalplatform.service.ForumService;
import animalplatform.service.SensibilisationService;
import animalplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private AdoptionService adoptionService;

    @Autowired
    private LostAnimalService lostAnimalService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private SensibilisationService sensibilisationService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(Model model) {
        // Dernières annonces d'adoption (6 dernières)
        List<AdoptionPost> recentAdoptions = adoptionService.getRecentPosts(6);
        model.addAttribute("recentAdoptions", recentAdoptions);

        // Derniers animaux perdus (4 derniers pour la grille)
        List<LostAnimalPost> recentLost = lostAnimalService.getRecentPosts(4);
        model.addAttribute("recentLost", recentLost);

        // Derniers posts du forum
        List<ForumPost> recentForumPosts = forumService.getRecentPosts(4);
        model.addAttribute("recentForumPosts", recentForumPosts);

        // ✅ Catégories du forum pour la sidebar
        List<ForumCategory> categories = forumService.getAllCategories();
        model.addAttribute("categories", categories);

        // Articles de sensibilisation (3 derniers)
        List<Sensibilisation> sensibilisationArticles = sensibilisationService.getRecentArticles(3);
        model.addAttribute("sensibilisationArticles", sensibilisationArticles);

        // Statistiques pour le hero
        model.addAttribute("totalAdoptions", adoptionService.countTotal());
        model.addAttribute("totalFound", lostAnimalService.countFound());
        model.addAttribute("totalAssociations", userService.countAssociations());

        return "pawcare/index";
    }

    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getStats() {
        return Map.of(
                "adoptions", adoptionService.countTotal(),
                "found", lostAnimalService.countFound(),
                "associations", userService.countAssociations()
        );
    }

    @GetMapping("/test-lost")
    @ResponseBody
    public String testLost() {
        return "Lost animals service is working!";
    }
}

package animalplatform.service;

import animalplatform.dto.SensibilisationDTO;
import animalplatform.model.Sensibilisation;
import animalplatform.model.User;
import animalplatform.repository.SensibilisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SensibilisationService {

    @Autowired
    private SensibilisationRepository sensibilisationRepository;

    private final String UPLOAD_DIR = "uploads/sensibilisation/";

    // ✅ Obtenir tous les articles publiés
    public List<Sensibilisation> getAllPublished() {
        return sensibilisationRepository.findByIsPublishedTrueOrderByPublishedAtDesc();
    }

    // ✅ Obtenir les 3 derniers articles
    public List<Sensibilisation> getRecentArticles(int limit) {
        return sensibilisationRepository.findTopByIsPublishedTrueOrderByPublishedAtDesc(PageRequest.of(0, limit));
    }

    // ✅ Obtenir les articles par catégorie
    public List<Sensibilisation> getByCategory(String category) {
        return sensibilisationRepository.findByCategoryAndIsPublishedTrueOrderByPublishedAtDesc(category);
    }

    // ✅ Obtenir un article par ID
    public Sensibilisation getArticleById(Long id) {
        return sensibilisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
    }

    // ✅ Incrémenter le compteur de lectures
    @Transactional
    public void incrementReadCount(Long id) {
        sensibilisationRepository.incrementReadCount(id);
    }

    // ✅ Ajouter un like
    @Transactional
    public void addLike(Long id, User user) {
        sensibilisationRepository.incrementLikeCount(id);
    }

    // ✅ Rechercher des articles
    public List<Sensibilisation> searchArticles(String query) {
        return sensibilisationRepository.searchArticles(query);
    }

    // ✅ Obtenir les articles par tag
    public List<Sensibilisation> getByTag(String tag) {
        return sensibilisationRepository.findByTag(tag);
    }

    // ✅ Obtenir toutes les catégories
    public List<String> getAllCategories() {
        return sensibilisationRepository.findAllCategories();
    }

    // ✅ Obtenir les articles populaires
    public List<Sensibilisation> getPopularArticles(int limit) {
        return sensibilisationRepository.findTop6ByIsPublishedTrueOrderByReadCountDesc()
                .stream().limit(limit).toList();
    }

    // ✅ Obtenir les articles recommandés
    public List<Sensibilisation> getRelatedArticles(Sensibilisation article, int limit) {
        return sensibilisationRepository.findRelatedArticles(
                article.getCategory(),
                article.getId(),
                PageRequest.of(0, limit)
        );
    }

    // ✅ Créer un nouvel article (avec image)
    @Transactional
    public Sensibilisation createArticle(SensibilisationDTO dto, User author, List<MultipartFile> images) {
        Sensibilisation article = new Sensibilisation();
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setCategory(dto.getCategory());
        article.setSummary(dto.getSummary());
        article.setAuthor(dto.getAuthor() != null ? dto.getAuthor() : author.getFirstName() + " " + author.getLastName());
        article.setUser(author);
        article.setIsPublished(dto.getIsPublished() != null ? dto.getIsPublished() : true);
        article.setTags(dto.getTags());

        if (images != null && !images.isEmpty() && images.get(0) != null && !images.get(0).isEmpty()) {
            try {
                String imageUrl = saveImage(images.get(0));
                article.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de l'upload de l'image", e);
            }
        }

        if (article.getIsPublished()) {
            article.setPublishedAt(LocalDateTime.now());
        }

        return sensibilisationRepository.save(article);
    }

    // ✅ Sauvegarder une image
    private String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path path = uploadPath.resolve(fileName);
        Files.write(path, file.getBytes());

        return "/uploads/sensibilisation/" + fileName;
    }

    // ✅ Mettre à jour un article
    @Transactional
    public Sensibilisation updateArticle(Long id, SensibilisationDTO dto, User user, List<MultipartFile> images) {
        Sensibilisation article = getArticleById(id);

        if (!article.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cet article");
        }

        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setCategory(dto.getCategory());
        article.setSummary(dto.getSummary());
        article.setAuthor(dto.getAuthor() != null ? dto.getAuthor() : user.getFirstName() + " " + user.getLastName());
        article.setTags(dto.getTags());

        if (dto.getIsPublished() != null) {
            article.setIsPublished(dto.getIsPublished());
            if (dto.getIsPublished() && article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }
        }

        if (images != null && !images.isEmpty() && images.get(0) != null && !images.get(0).isEmpty()) {
            try {
                String imageUrl = saveImage(images.get(0));
                article.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de l'upload de l'image", e);
            }
        }

        return sensibilisationRepository.save(article);
    }

    // ✅ Supprimer un article
    @Transactional
    public void deleteArticle(Long id, User user) {
        Sensibilisation article = getArticleById(id);

        if (!article.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cet article");
        }

        sensibilisationRepository.deleteById(id);
    }

    // ✅ Changer le statut de publication
    @Transactional
    public void togglePublishStatus(Long id, User user) {
        Sensibilisation article = getArticleById(id);

        if (!article.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cet article");
        }

        article.setIsPublished(!article.getIsPublished());
        if (article.getIsPublished()) {
            article.setPublishedAt(LocalDateTime.now());
        }
        sensibilisationRepository.save(article);
    }

    // ✅ Incrémenter le compteur de likes
    @Transactional
    public void incrementLikeCount(Long id) {
        sensibilisationRepository.incrementLikeCount(id);
    }

    // ✅ Recherche par mot-clé
    public List<Sensibilisation> searchByKeyword(String keyword) {
        return sensibilisationRepository.searchArticles(keyword);
    }

    // ✅ Obtenir les articles d'un utilisateur
    public List<Sensibilisation> getArticlesByUser(Long userId) {
        return sensibilisationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ✅ Compter les articles par catégorie
    public List<Object[]> getStatsByCategory() {
        return sensibilisationRepository.countByCategory();
    }

    // ✅ Obtenir les statistiques par catégorie
    public List<Object[]> getCategoryStats() {
        return sensibilisationRepository.getCategoryStats();
    }

    // ✅ Obtenir tous les tags
    public List<String> getAllTags() {
        return sensibilisationRepository.findAllTags();
    }
}
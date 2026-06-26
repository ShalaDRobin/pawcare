package animalplatform.service;

import animalplatform.dto.LostAnimalPostDTO;
import animalplatform.model.LostAnimalPost;
import animalplatform.model.LostAnimalStatus;
import animalplatform.model.User;
import animalplatform.repository.LostAnimalPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LostAnimalService {

    @Autowired
    private LostAnimalPostRepository lostAnimalPostRepository;

    @Autowired
    private NotificationService notificationService;

    private final String UPLOAD_DIR = "uploads/lost/";

    // Créer un signalement avec images principales et supplémentaires
    @Transactional
    public LostAnimalPost createPost(LostAnimalPostDTO postDTO, User user, List<MultipartFile> images) {
        LostAnimalPost post = new LostAnimalPost();
        post.setTitle(postDTO.getTitle());
        post.setDescription(postDTO.getDescription());
        post.setAnimalType(postDTO.getAnimalType());
        post.setBreed(postDTO.getBreed());
        post.setName(postDTO.getName());
        post.setAge(postDTO.getAge());
        post.setGender(postDTO.getGender());
        post.setColor(postDTO.getColor());
        post.setDistinctFeatures(postDTO.getDistinctFeatures());
        post.setLastSeenLocation(postDTO.getLastSeenLocation());
        post.setLastSeenDate(postDTO.getLastSeenDate());
        post.setHasChip(postDTO.getHasChip());
        post.setChipNumber(postDTO.getChipNumber());
        post.setOwnerPhone(postDTO.getOwnerPhone());
        post.setOwnerEmail(postDTO.getOwnerEmail());
        post.setUser(user);
        post.setStatus(LostAnimalStatus.LOST);

        // Gérer TOUTES les images (principale + supplémentaires)
        List<String> allImageUrls = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            try {
                List<MultipartFile> validImages = images.stream()
                        .filter(img -> img != null && !img.isEmpty())
                        .collect(Collectors.toList());

                System.out.println("📸 Images reçues pour animal perdu: " + images.size() + ", Images valides: " + validImages.size());

                for (int i = 0; i < validImages.size(); i++) {
                    MultipartFile image = validImages.get(i);
                    String imageUrl = saveImage(image);
                    allImageUrls.add(imageUrl);
                    System.out.println("   Image " + i + ": " + imageUrl);

                    if (i == 0) {
                        post.setImageUrl(imageUrl);
                        System.out.println("🖼️ Image principale: " + imageUrl);
                    }
                }

                if (allImageUrls.size() > 1) {
                    List<String> additionalImages = allImageUrls.subList(1, allImageUrls.size());
                    post.setAdditionalImages(new ArrayList<>(additionalImages));
                    System.out.println("📚 Images supplémentaires: " + post.getAdditionalImages().size());
                } else {
                    post.setAdditionalImages(new ArrayList<>());
                }

            } catch (IOException e) {
                System.err.println("❌ Erreur lors de l'upload des images: " + e.getMessage());
                throw new RuntimeException("Erreur lors de l'upload des images", e);
            }
        } else {
            post.setAdditionalImages(new ArrayList<>());
            System.out.println("📸 Aucune image reçue pour ce signalement");
        }

        LostAnimalPost savedPost = lostAnimalPostRepository.save(post);
        System.out.println("✅ Signalement sauvegardé avec ID: " + savedPost.getId());
        return savedPost;
    }

    // Sauvegarder une image
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
            System.out.println("📁 Dossier créé: " + uploadPath.toAbsolutePath());
        }

        Path path = uploadPath.resolve(fileName);
        Files.write(path, file.getBytes());

        System.out.println("💾 Image sauvegardée: " + path.toAbsolutePath());

        return "/uploads/lost/" + fileName;
    }

    // ✅ NOUVELLE MÉTHODE: Mettre à jour un signalement
    @Transactional
    public LostAnimalPost updatePost(Long id, LostAnimalPostDTO postDTO, User user, List<MultipartFile> images) {
        LostAnimalPost post = getPostById(id);

        // Vérifier que l'utilisateur est le propriétaire
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Non autorisé");
        }

        // Mettre à jour les champs
        post.setTitle(postDTO.getTitle());
        post.setDescription(postDTO.getDescription());
        post.setAnimalType(postDTO.getAnimalType());
        post.setBreed(postDTO.getBreed());
        post.setName(postDTO.getName());
        post.setAge(postDTO.getAge());
        post.setGender(postDTO.getGender());
        post.setColor(postDTO.getColor());
        post.setDistinctFeatures(postDTO.getDistinctFeatures());
        post.setLastSeenLocation(postDTO.getLastSeenLocation());
        post.setLastSeenDate(postDTO.getLastSeenDate());
        post.setHasChip(postDTO.getHasChip());
        post.setChipNumber(postDTO.getChipNumber());
        post.setOwnerPhone(postDTO.getOwnerPhone());
        post.setOwnerEmail(postDTO.getOwnerEmail());

        // Gérer les nouvelles images si envoyées
        if (images != null && !images.isEmpty() && images.get(0) != null && !images.get(0).isEmpty()) {
            try {
                String imageUrl = saveImage(images.get(0));
                post.setImageUrl(imageUrl);
                System.out.println("🖼️ Nouvelle image principale: " + imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Erreur upload image", e);
            }
        }

        LostAnimalPost savedPost = lostAnimalPostRepository.save(post);
        System.out.println("✅ Signalement mis à jour: ID " + savedPost.getId());
        return savedPost;
    }

    // ✅ NOUVELLE MÉTHODE: Changer le statut
    @Transactional
    public void changeStatus(Long id, String status) {
        LostAnimalPost post = getPostById(id);
        LostAnimalStatus newStatus = LostAnimalStatus.valueOf(status);
        post.setStatus(newStatus);
        lostAnimalPostRepository.save(post);
        System.out.println("📝 Statut changé pour ID " + id + ": " + newStatus);
    }

    // Obtenir tous les signalements actifs
    public List<LostAnimalPost> getAllActive() {
        return lostAnimalPostRepository.findByStatusOrderByCreatedAtDesc(LostAnimalStatus.LOST);
    }

    // Obtenir les signalements récents
    public List<LostAnimalPost> getRecentPosts(int limit) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return lostAnimalPostRepository.findRecentLost(LostAnimalStatus.LOST, sevenDaysAgo)
                .stream().limit(limit).toList();
    }

    // Obtenir un signalement par ID
    public LostAnimalPost getPostById(Long id) {
        return lostAnimalPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signalement non trouvé"));
    }

    // Incrémenter les vues
    @Transactional
    public void incrementViewCount(Long id) {
        lostAnimalPostRepository.incrementViewCount(id);
    }

    // Incrémenter les partages
    @Transactional
    public void incrementShareCount(Long id) {
        lostAnimalPostRepository.incrementShareCount(id);
    }

    // Marquer comme trouvé (méthode existante, gardée pour compatibilité)
    @Transactional
    public void markAsFound(Long id) {
        changeStatus(id, "FOUND");
    }

    // Supprimer un signalement
    @Transactional
    public void deletePost(Long id) {
        LostAnimalPost post = getPostById(id);
        lostAnimalPostRepository.delete(post);
        System.out.println("🗑️ Signalement supprimé: ID " + id);
    }

    // Rechercher par localisation
    public List<LostAnimalPost> findByLocation(String location) {
        return lostAnimalPostRepository.findByLocation(location);
    }

    // Recherche avancée
    public List<LostAnimalPost> searchLostAnimals(String animalType, String location, LocalDate fromDate) {
        return lostAnimalPostRepository.searchLostAnimals(animalType, location, LostAnimalStatus.LOST, fromDate);
    }

    // Compter les animaux retrouvés
    public long countFound() {
        return lostAnimalPostRepository.findByStatusOrderByCreatedAtDesc(LostAnimalStatus.FOUND).size();
    }

    // Alerter pour les animaux perdus depuis longtemps
    @Transactional
    public void checkLongLostAnimals() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<LostAnimalPost> longLost = lostAnimalPostRepository.findLostBeforeDate(thirtyDaysAgo);

        for (LostAnimalPost post : longLost) {
            notificationService.sendLongLostAlert(post);
        }
    }
}
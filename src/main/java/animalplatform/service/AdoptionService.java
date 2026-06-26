package animalplatform.service;

import animalplatform.dto.AdoptionPostDTO;
import animalplatform.model.*;
import animalplatform.repository.AdoptionPostRepository;
import animalplatform.repository.AdoptionRequestRepository;
import animalplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdoptionService {

    @Autowired
    private AdoptionPostRepository adoptionPostRepository;

    @Autowired
    private AdoptionRequestRepository adoptionRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    private final String UPLOAD_DIR = "uploads/adoption/";

    // Créer une nouvelle annonce - ✅ CORRIGÉ
    @Transactional
    public AdoptionPost createPost(AdoptionPostDTO postDTO, User user, List<MultipartFile> images) {
        AdoptionPost post = new AdoptionPost();
        post.setTitle(postDTO.getTitle());
        post.setDescription(postDTO.getDescription());
        post.setAnimalType(postDTO.getAnimalType());
        post.setBreed(postDTO.getBreed());
        post.setAge(postDTO.getAge());
        post.setGender(postDTO.getGender());
        post.setSize(postDTO.getSize());
        post.setColor(postDTO.getColor());
        post.setLocation(postDTO.getLocation());
        post.setVaccinated(postDTO.getVaccinated());
        post.setSterilized(postDTO.getSterilized());
        post.setHealthStatus(postDTO.getHealthStatus());
        post.setPersonality(postDTO.getPersonality());
        post.setIsUrgent(postDTO.getIsUrgent());
        post.setUser(user);
        post.setStatus(PostStatus.AVAILABLE);

        // ✅ CORRECTION: Gérer TOUTES les images (principale + supplémentaires)
        List<String> allImageUrls = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            try {
                // Filtrer les images valides (non vides)
                List<MultipartFile> validImages = images.stream()
                        .filter(img -> img != null && !img.isEmpty())
                        .collect(Collectors.toList());

                System.out.println("📸 Images reçues: " + images.size() + ", Images valides: " + validImages.size());

                for (int i = 0; i < validImages.size(); i++) {
                    MultipartFile image = validImages.get(i);
                    String imageUrl = saveImage(image);
                    allImageUrls.add(imageUrl);
                    System.out.println("   Image " + i + ": " + imageUrl);

                    // La première image est l'image principale
                    if (i == 0) {
                        post.setImageUrl(imageUrl);
                        System.out.println("🖼️ Image principale: " + imageUrl);
                    }
                }

                // ✅ IMPORTANT: Les images supplémentaires = toutes sauf la première
                if (allImageUrls.size() > 1) {
                    List<String> additionalImages = allImageUrls.subList(1, allImageUrls.size());
                    post.setAdditionalImages(new ArrayList<>(additionalImages));
                    System.out.println("📚 Images supplémentaires: " + post.getAdditionalImages().size());
                } else {
                    post.setAdditionalImages(new ArrayList<>());
                    System.out.println("📚 Aucune image supplémentaire");
                }

            } catch (IOException e) {
                System.err.println("❌ Erreur lors de l'upload des images: " + e.getMessage());
                throw new RuntimeException("Erreur lors de l'upload des images", e);
            }
        } else {
            post.setAdditionalImages(new ArrayList<>());
            System.out.println("📸 Aucune image reçue");
        }

        AdoptionPost savedPost = adoptionPostRepository.save(post);
        System.out.println("✅ Annonce sauvegardée avec ID: " + savedPost.getId());
        return savedPost;
    }

    // Sauvegarder une image
    private String saveImage(MultipartFile file) throws IOException {
        // Vérifier que le fichier n'est pas vide
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Nettoyer le nom du fichier pour éviter les problèmes
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("📁 Dossier créé: " + uploadPath.toAbsolutePath());
        }

        Path path = uploadPath.resolve(fileName);
        Files.write(path, file.getBytes());

        System.out.println("💾 Image sauvegardée: " + path.toAbsolutePath());

        // Retourner le chemin relatif pour l'accès web
        return "/uploads/adoption/" + fileName;
    }

    // Obtenir les annonces avec filtres
    public Page<AdoptionPost> getFilteredPosts(String animalType, String location, Boolean urgent, Pageable pageable) {
        return adoptionPostRepository.findWithFilters(animalType, location, urgent, pageable);
    }

    // Récupérer tous les posts disponibles
    public List<AdoptionPost> getAllAvailablePosts() {
        return adoptionPostRepository.findByStatus(PostStatus.AVAILABLE, PageRequest.of(0, 50)).getContent();
    }

    // Filtrer par type d'animal
    public List<AdoptionPost> filterByAnimalType(String animalType) {
        return adoptionPostRepository.findByAnimalTypeAndStatus(animalType, PostStatus.AVAILABLE, PageRequest.of(0, 50)).getContent();
    }

    // Obtenir les dernières annonces
    public List<AdoptionPost> getRecentPosts(int limit) {
        return adoptionPostRepository.findTop6ByStatusOrderByCreatedAtDesc(PostStatus.AVAILABLE)
                .stream().limit(limit).toList();
    }

    // Obtenir une annonce par ID
    public AdoptionPost getPostById(Long id) {
        return adoptionPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));
    }

    // Incrémenter le compteur de vues
    @Transactional
    public void incrementViewCount(Long id) {
        adoptionPostRepository.incrementViewCount(id);
    }

    // Mettre à jour une annonce
    @Transactional
    public AdoptionPost updatePost(Long id, AdoptionPostDTO postDTO) {
        AdoptionPost post = getPostById(id);

        post.setTitle(postDTO.getTitle());
        post.setDescription(postDTO.getDescription());
        post.setAnimalType(postDTO.getAnimalType());
        post.setBreed(postDTO.getBreed());
        post.setAge(postDTO.getAge());
        post.setGender(postDTO.getGender());
        post.setSize(postDTO.getSize());
        post.setColor(postDTO.getColor());
        post.setLocation(postDTO.getLocation());
        post.setVaccinated(postDTO.getVaccinated());
        post.setSterilized(postDTO.getSterilized());
        post.setHealthStatus(postDTO.getHealthStatus());
        post.setPersonality(postDTO.getPersonality());
        post.setIsUrgent(postDTO.getIsUrgent());

        return adoptionPostRepository.save(post);
    }

    // Supprimer une annonce
    @Transactional
    public void deletePost(Long id) {
        AdoptionPost post = getPostById(id);
        adoptionPostRepository.delete(post);
    }

    // Marquer comme adopté
    @Transactional
    public void markAsAdopted(Long id) {
        AdoptionPost post = getPostById(id);
        post.setStatus(PostStatus.ADOPTED);
        adoptionPostRepository.save(post);
    }

    // Récupérer les annonces urgentes
    public List<AdoptionPost> getUrgentPosts() {
        return adoptionPostRepository.findByIsUrgentTrueAndStatusOrderByCreatedAtDesc(PostStatus.AVAILABLE);
    }

    // ✅ Créer une demande d'adoption - CORRIGÉ
    @Transactional
    public AdoptionRequest createRequest(Long postId, User requester, AdoptionRequest request) {
        AdoptionPost post = getPostById(postId);

        // ✅ CORRIGÉ: Utiliser RequestStatus.REJECTED directement (sans .toString())
        if (adoptionRequestRepository.existsByAdoptionPostIdAndRequesterIdAndStatusNot(
                postId, requester.getId(), RequestStatus.REJECTED)) {
            throw new RuntimeException("Vous avez déjà une demande en cours pour cet animal");
        }

        request.setAdoptionPost(post);
        request.setRequester(requester);
        request.setOwner(post.getUser());
        request.setStatus(RequestStatus.PENDING);

        AdoptionRequest savedRequest = adoptionRequestRepository.save(request);

        notificationService.sendAdoptionRequestNotification(post.getUser(), savedRequest);

        return savedRequest;
    }

    // ✅ NOUVELLE MÉTHODE: Accepter une demande avec message
    @Transactional
    public void acceptRequest(Long requestId, User owner, String responseMessage) {
        AdoptionRequest request = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (!request.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à répondre à cette demande");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée");
        }

        // Utiliser la méthode accept() du modèle
        request.accept(responseMessage);
        adoptionRequestRepository.save(request);

        // Sauvegarder l'annonce (car accept() a modifié son statut)
        adoptionPostRepository.save(request.getAdoptionPost());

        // Rejeter les autres demandes pour cette annonce
        List<AdoptionRequest> otherRequests = adoptionRequestRepository
                .findByAdoptionPostIdAndStatus(request.getAdoptionPost().getId(), RequestStatus.PENDING);

        for (AdoptionRequest other : otherRequests) {
            if (!other.getId().equals(requestId)) {
                other.reject("Cette annonce a été attribuée à une autre personne");
                adoptionRequestRepository.save(other);
                // Notifier les autres demandeurs
                notificationService.sendAdoptionResponseNotification(other.getRequester(), other);
            }
        }

        // Notifier le demandeur accepté
        notificationService.sendAdoptionResponseNotification(request.getRequester(), request);
    }

    // ✅ NOUVELLE MÉTHODE: Refuser une demande avec message
    @Transactional
    public void rejectRequest(Long requestId, User owner, String responseMessage) {
        AdoptionRequest request = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (!request.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à répondre à cette demande");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée");
        }

        request.reject(responseMessage);
        adoptionRequestRepository.save(request);

        notificationService.sendAdoptionResponseNotification(request.getRequester(), request);
    }

    // ✅ NOUVELLE MÉTHODE: Annuler une demande (par le demandeur)
    @Transactional
    public void cancelRequest(Long requestId, User requester) {
        AdoptionRequest request = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (!request.getRequester().getId().equals(requester.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à annuler cette demande");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Cette demande ne peut plus être annulée");
        }

        request.cancel();
        adoptionRequestRepository.save(request);
    }

    // ✅ NOUVELLE MÉTHODE: Récupérer les demandes envoyées par un utilisateur
    public List<AdoptionRequest> getRequestsByRequester(Long userId) {
        return adoptionRequestRepository.findByRequesterIdOrderByCreatedAtDesc(userId);
    }

    // ✅ NOUVELLE MÉTHODE: Récupérer les demandes reçues par un utilisateur (en tant que propriétaire)
    public List<AdoptionRequest> getRequestsByOwner(Long userId) {
        return adoptionRequestRepository.findByOwnerIdOrderByCreatedAtDesc(userId);
    }

    // ✅ NOUVELLE MÉTHODE: Récupérer les demandes en attente pour une annonce
    public List<AdoptionRequest> getPendingRequestsForPost(Long postId) {
        return adoptionRequestRepository.findByAdoptionPostIdAndStatus(postId, RequestStatus.PENDING);
    }

    // ✅ NOUVELLE MÉTHODE: Vérifier si l'utilisateur a déjà une demande pour cette annonce
    public boolean hasActiveRequest(Long postId, Long userId) {
        // ✅ CORRIGÉ: Utiliser RequestStatus.REJECTED directement
        return adoptionRequestRepository.existsByAdoptionPostIdAndRequesterIdAndStatusNot(
                postId, userId, RequestStatus.REJECTED);
    }

    // ✅ NOUVELLE MÉTHODE: Compter les demandes en attente pour une annonce
    public long countPendingRequestsForPost(Long postId) {
        return adoptionRequestRepository.countByAdoptionPostIdAndStatus(postId, RequestStatus.PENDING);
    }

    // Répondre à une demande d'adoption (méthode existante)
    @Transactional
    public void respondToRequest(Long requestId, RequestStatus status, User owner) {
        AdoptionRequest request = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (!request.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à répondre à cette demande");
        }

        request.setStatus(status);
        adoptionRequestRepository.save(request);

        if (status == RequestStatus.ACCEPTED) {
            notificationService.sendAdoptionResponseNotification(request.getRequester(), request);
        }
    }

    // Obtenir les annonces similaires
    public List<AdoptionPost> getSimilarPosts(AdoptionPost post, int limit) {
        return adoptionPostRepository.findSimilarPosts(
                post.getAnimalType(),
                post.getLocation(),
                post.getId(),
                PageRequest.of(0, limit)
        );
    }

    // Compter le total des annonces
    public long countTotal() {
        return adoptionPostRepository.count();
    }

    // Statistiques par type d'animal
    public List<Object[]> getStatsByAnimalType() {
        return adoptionPostRepository.countByAnimalType();
    }

    // Filtrer les annonces (pour AJAX) - Version améliorée
    public List<AdoptionPost> filterPosts(String type, String location) {
        Page<AdoptionPost> page = adoptionPostRepository.findWithFilters(
                type, location, null, PageRequest.of(0, 50));
        return page.getContent();
    }
}
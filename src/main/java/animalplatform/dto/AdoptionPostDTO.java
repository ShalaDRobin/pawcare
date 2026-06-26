package animalplatform.dto;

import animalplatform.model.AdoptionPost;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionPostDTO {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    private String title;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    private String description;

    @NotBlank(message = "Le type d'animal est obligatoire")
    private String animalType; // Chien, Chat, Lapin, etc.

    private String breed; // Race

    @NotNull(message = "L'âge est obligatoire")
    private Integer age;

    private String gender; // Mâle, Femelle

    private String size; // Petit, Moyen, Grand

    private String color;

    @NotBlank(message = "La localisation est obligatoire")
    private String location;

    private Boolean vaccinated = false;

    private Boolean sterilized = false;

    private Boolean dewormed = false;

    private String healthStatus;

    private String personality;

    private String imageUrl; // URL de l'image principale

    // ✅ AJOUT DE LA LISTE DES IMAGES SUPPLÉMENTAIRES
    private List<String> additionalImages = new ArrayList<>(); // AJOUTÉ

    private String status; // Statut de l'annonce (AVAILABLE, ADOPTED, PENDING)

    private Boolean isUrgent = false;

    private Integer viewCount = 0; // Nombre de vues

    // Informations sur l'utilisateur (pour éviter la récursion infinie)
    private Long userId;
    private String userFirstName;
    private String userLastName;

    // Indique si l'annonce est en favori (pour la réponse JSON)
    private Boolean favorited = false;

    // Pour les uploads d'images
    private List<MultipartFile> images;

    // ✅ CONSTRUCTEUR À PARTIR DE L'ENTITÉ
    public static AdoptionPostDTO fromEntity(AdoptionPost post) {
        return fromEntity(post, false);
    }

    public static AdoptionPostDTO fromEntity(AdoptionPost post, boolean isFavorited) {
        AdoptionPostDTO dto = new AdoptionPostDTO();

        // Informations de base
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setAnimalType(post.getAnimalType());
        dto.setBreed(post.getBreed());
        dto.setAge(post.getAge());
        dto.setGender(post.getGender());
        dto.setSize(post.getSize());
        dto.setColor(post.getColor());
        dto.setLocation(post.getLocation());
        dto.setVaccinated(post.getVaccinated());
        dto.setSterilized(post.getSterilized());
        dto.setDewormed(post.getDewormed());
        dto.setHealthStatus(post.getHealthStatus());
        dto.setPersonality(post.getPersonality());
        dto.setImageUrl(post.getImageUrl());

        // ✅ AJOUT DE LA LISTE DES IMAGES SUPPLÉMENTAIRES
        if (post.getAdditionalImages() != null) {
            dto.setAdditionalImages(post.getAdditionalImages());
        }

        dto.setStatus(post.getStatus() != null ? post.getStatus().toString() : "AVAILABLE");
        dto.setIsUrgent(post.getIsUrgent());
        dto.setViewCount(post.getViewCount());

        // Informations sur l'utilisateur
        if (post.getUser() != null) {
            dto.setUserId(post.getUser().getId());
            dto.setUserFirstName(post.getUser().getFirstName());
            dto.setUserLastName(post.getUser().getLastName());
        }

        dto.setFavorited(isFavorited);

        return dto;
    }

    // Méthode utilitaire pour validation
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
                description != null && !description.trim().isEmpty() &&
                animalType != null && !animalType.trim().isEmpty() &&
                location != null && !location.trim().isEmpty();
    }
}
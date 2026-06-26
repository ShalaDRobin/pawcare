package animalplatform.dto;

import animalplatform.model.LostAnimalPost;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostAnimalPostDTO {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    private String title;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    private String description;

    @NotBlank(message = "Le type d'animal est obligatoire")
    private String animalType;

    private String breed;

    private String name; // Nom de l'animal

    private Integer age;

    private String gender;

    private String color;

    private String distinctFeatures; // Signes distinctifs

    @NotBlank(message = "Le lieu de disparition est obligatoire")
    private String lastSeenLocation;

    @NotNull(message = "La date de disparition est obligatoire")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "La date ne peut pas être dans le futur")
    private LocalDate lastSeenDate;

    private String imageUrl; // ✅ AJOUTÉ - URL de l'image principale

    // ✅ AJOUTÉ - Liste des images supplémentaires
    private List<String> additionalImages = new ArrayList<>();

    private Boolean hasChip = false;

    private String chipNumber;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String ownerPhone;

    private String ownerEmail;

    private String status; // LOST, FOUND, RETURNED

    // Informations sur l'utilisateur
    private Long userId;
    private String userFirstName;
    private String userLastName;

    // Pour les uploads d'images
    private List<MultipartFile> images;

    // ✅ Méthode utilitaire
    public boolean hasValidChipInfo() {
        if (hasChip != null && hasChip) {
            return chipNumber != null && !chipNumber.trim().isEmpty();
        }
        return true;
    }

    // ✅ Conversion depuis l'entité
    public static LostAnimalPostDTO fromEntity(LostAnimalPost post) {
        LostAnimalPostDTO dto = new LostAnimalPostDTO();

        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setAnimalType(post.getAnimalType());
        dto.setBreed(post.getBreed());
        dto.setName(post.getName());
        dto.setAge(post.getAge());
        dto.setGender(post.getGender());
        dto.setColor(post.getColor());
        dto.setDistinctFeatures(post.getDistinctFeatures());
        dto.setLastSeenLocation(post.getLastSeenLocation());
        dto.setLastSeenDate(post.getLastSeenDate());
        dto.setImageUrl(post.getImageUrl());

        if (post.getAdditionalImages() != null) {
            dto.setAdditionalImages(post.getAdditionalImages());
        }

        dto.setHasChip(post.getHasChip());
        dto.setChipNumber(post.getChipNumber());
        dto.setOwnerPhone(post.getOwnerPhone());
        dto.setOwnerEmail(post.getOwnerEmail());
        dto.setStatus(post.getStatus() != null ? post.getStatus().toString() : "LOST");

        if (post.getUser() != null) {
            dto.setUserId(post.getUser().getId());
            dto.setUserFirstName(post.getUser().getFirstName());
            dto.setUserLastName(post.getUser().getLastName());
        }

        return dto;
    }
}
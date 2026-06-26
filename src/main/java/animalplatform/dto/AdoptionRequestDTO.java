package animalplatform.dto;

import animalplatform.model.AdoptionRequest;
import animalplatform.model.RequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionRequestDTO {

    private Long id;

    private Long adoptionPostId;
    private String adoptionPostTitle; // Pour affichage

    private Long requesterId;
    private String requesterFirstName;
    private String requesterLastName;
    private String requesterEmail;

    private Long ownerId;
    private String ownerFirstName;
    private String ownerLastName;

    @NotBlank(message = "Veuillez expliquer pourquoi vous voulez adopter cet animal")
    @Size(min = 20, max = 1000, message = "Le message doit contenir entre 20 et 1000 caractères")
    private String message; // Message de motivation

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^(\\+212|0)[0-9]{9}$", message = "Numéro de téléphone invalide (ex: 0612345678 ou +212612345678)")
    private String phoneNumber;

    private String livingSituation; // Maison, Appartement, etc.

    private Boolean hasOtherPets = false;
    private String otherPetsDetails;

    private Boolean hasChildren = false;

    @Size(max = 500, message = "L'expérience ne doit pas dépasser 500 caractères")
    private String experience;

    private String status; // PENDING, ACCEPTED, REJECTED, CANCELLED

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    private String ownerResponse; // Réponse du propriétaire

    // Constructeur à partir de l'entité
    public static AdoptionRequestDTO fromEntity(AdoptionRequest request) {
        AdoptionRequestDTO dto = new AdoptionRequestDTO();

        dto.setId(request.getId());

        // Infos sur l'annonce
        if (request.getAdoptionPost() != null) {
            dto.setAdoptionPostId(request.getAdoptionPost().getId());
            dto.setAdoptionPostTitle(request.getAdoptionPost().getTitle());
        }

        // Infos sur le demandeur
        if (request.getRequester() != null) {
            dto.setRequesterId(request.getRequester().getId());
            dto.setRequesterFirstName(request.getRequester().getFirstName());
            dto.setRequesterLastName(request.getRequester().getLastName());
            dto.setRequesterEmail(request.getRequester().getEmail());
        }

        // Infos sur le propriétaire
        if (request.getOwner() != null) {
            dto.setOwnerId(request.getOwner().getId());
            dto.setOwnerFirstName(request.getOwner().getFirstName());
            dto.setOwnerLastName(request.getOwner().getLastName());
        }

        dto.setMessage(request.getMessage());
        dto.setPhoneNumber(request.getPhoneNumber());
        dto.setLivingSituation(request.getLivingSituation());
        dto.setHasOtherPets(request.getHasOtherPets());
        dto.setOtherPetsDetails(request.getOtherPetsDetails());
        dto.setHasChildren(request.getHasChildren());
        dto.setExperience(request.getExperience());
        dto.setStatus(request.getStatus() != null ? request.getStatus().toString() : "PENDING");
        dto.setCreatedAt(request.getCreatedAt());
        dto.setRespondedAt(request.getRespondedAt());
        dto.setOwnerResponse(request.getOwnerResponse());

        return dto;
    }

    // Méthode pour valider les données avant envoi
    public boolean isValid() {
        return message != null && !message.trim().isEmpty() &&
                phoneNumber != null && !phoneNumber.trim().isEmpty() &&
                message.length() >= 20;
    }

    // Méthode pour obtenir le nom complet du demandeur
    public String getRequesterFullName() {
        return requesterFirstName + " " + requesterLastName;
    }

    // Méthode pour obtenir le nom complet du propriétaire
    public String getOwnerFullName() {
        return ownerFirstName + " " + ownerLastName;
    }

    // Vérifier si la demande est en attente
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    // Vérifier si la demande est acceptée
    public boolean isAccepted() {
        return "ACCEPTED".equals(status);
    }

    // Vérifier si la demande est rejetée
    public boolean isRejected() {
        return "REJECTED".equals(status);
    }

    // Options pour la situation de vie
    public static class LivingSituationOptions {
        public static final String HOUSE = "Maison avec jardin";
        public static final String HOUSE_NO_GARDEN = "Maison sans jardin";
        public static final String APARTMENT = "Appartement";
        public static final String FARM = "Ferme / Terrain";
        public static final String OTHER = "Autre";

        public static String[] getAll() {
            return new String[]{HOUSE, HOUSE_NO_GARDEN, APARTMENT, FARM, OTHER};
        }
    }
}

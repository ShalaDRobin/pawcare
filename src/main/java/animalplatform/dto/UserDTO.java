package animalplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    private String phone;

    private String address;

    private String city;

    private String role;

    // Pour la mise à jour du profil (sans mot de passe)
    public UserDTO forUpdate() {
        UserDTO dto = new UserDTO();
        dto.setId(this.id);
        dto.setEmail(this.email);
        dto.setFirstName(this.firstName);
        dto.setLastName(this.lastName);
        dto.setPhone(this.phone);
        dto.setAddress(this.address);
        dto.setCity(this.city);
        return dto;
    }
}

package animalplatform.service;

import animalplatform.dto.UserDTO;
import animalplatform.model.User;
import animalplatform.model.Role;
import animalplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Inscription d'un nouvel utilisateur
    @Transactional
    public User registerUser(UserDTO userDTO) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhone(userDTO.getPhone());
        user.setCity(userDTO.getCity());

        // Définir le rôle par défaut
        if (userDTO.getRole() != null) {
            user.setRole(Role.valueOf(userDTO.getRole()));
        } else {
            user.setRole(Role.USER);
        }

        return userRepository.save(user);
    }

    // Authentification
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        return user;
    }

    // Trouver un utilisateur par ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // Mettre à jour le profil
    @Transactional
    public User updateProfile(Long userId, UserDTO userDTO) {
        User user = getUserById(userId);

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhone(userDTO.getPhone());
        user.setCity(userDTO.getCity());
        user.setAddress(userDTO.getAddress());

        return userRepository.save(user);
    }

    // Changer le mot de passe
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Rechercher des utilisateurs
    public List<User> searchUsers(String query) {
        return userRepository.searchUsers(query);
    }

    // Obtenir les utilisateurs par rôle
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // Compter les associations
    public long countAssociations() {
        return userRepository.countByRole(Role.ASSOCIATION);
    }

    // Compter les vétérinaires
    public long countVeterinaires() {
        return userRepository.countByRole(Role.VETERINAIRE);
    }

    // Activer/Désactiver un utilisateur (admin)
    @Transactional
    public void toggleUserStatus(Long userId, boolean enabled) {
        User user = getUserById(userId);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    // Changer le rôle d'un utilisateur (admin)
    @Transactional
    public void changeUserRole(Long userId, Role newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        userRepository.save(user);
    }
}
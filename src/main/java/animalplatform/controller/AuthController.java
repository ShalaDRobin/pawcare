package animalplatform.controller;

import animalplatform.dto.UserDTO;
import animalplatform.dto.LoginRequest;
import animalplatform.model.User;
import animalplatform.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // Afficher la page de connexion (modal géré en JS)
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "pawcare/fragments/auth-modals :: loginForm";
    }

    // Traitement de la connexion - Version avec header explicite
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> login(@Valid @RequestBody LoginRequest loginRequest,
                                     BindingResult result,
                                     HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        System.out.println("=== TENTATIVE DE CONNEXION ===");
        System.out.println("Email: " + loginRequest.getEmail());
        System.out.println("Password reçu: " + (loginRequest.getPassword() != null ? "****" : "null"));

        if (result.hasErrors()) {
            System.err.println("Erreurs de validation: " + result.getAllErrors());
            response.put("success", false);
            response.put("message", "Email et mot de passe requis");
            return response;
        }

        try {
            User user = userService.authenticate(loginRequest.getEmail(),
                    loginRequest.getPassword());
            session.setAttribute("user", user);
            System.out.println("✅ Connexion réussie pour: " + user.getEmail());

            response.put("success", true);
            response.put("message", "Connexion réussie");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString()
            ));
            return response;
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    // Afficher le formulaire d'inscription
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "pawcare/fragments/auth-modals :: signupForm";
    }

    // Traitement de l'inscription
    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> signup(@Valid @RequestBody UserDTO userDTO,
                                      BindingResult result,
                                      HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        System.out.println("=== TENTATIVE D'INSCRIPTION ===");
        System.out.println("Email: " + userDTO.getEmail());

        if (result.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder("Erreurs de validation: ");
            result.getAllErrors().forEach(error -> {
                errorMsg.append(error.getDefaultMessage()).append("; ");
            });
            System.err.println("Erreurs de validation: " + errorMsg);
            response.put("success", false);
            response.put("message", errorMsg.toString());
            return response;
        }

        try {
            User user = userService.registerUser(userDTO);
            session.setAttribute("user", user);
            System.out.println("✅ Inscription réussie pour: " + user.getEmail());

            response.put("success", true);
            response.put("message", "Inscription réussie");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString()
            ));
            return response;
        } catch (Exception e) {
            System.err.println("❌ Erreur d'inscription: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    // Déconnexion
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // Vérifier si l'utilisateur est connecté
    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> isAuthenticated(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");

        System.out.println("=== VÉRIFICATION AUTH ===");
        System.out.println("Session ID: " + session.getId());
        System.out.println("Utilisateur en session: " + (user != null ? user.getEmail() : "null"));

        if (user != null) {
            response.put("authenticated", true);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString()
            ));
        } else {
            response.put("authenticated", false);
        }
        return response;
    }

    // Obtenir l'utilisateur actuel
    @GetMapping(value = "/current-user", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user != null) {
            response.put("authenticated", true);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString()
            ));
        } else {
            response.put("authenticated", false);
        }
        return response;
    }
}
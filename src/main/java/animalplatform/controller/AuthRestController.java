package animalplatform.controller;

import animalplatform.dto.LoginRequest;
import animalplatform.dto.UserDTO;
import animalplatform.model.User;
import animalplatform.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest loginRequest, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        System.out.println("=== API LOGIN ===");
        System.out.println("Email: " + loginRequest.getEmail());

        try {
            User user = userService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
            session.setAttribute("user", user);

            response.put("success", true);
            response.put("message", "Connexion réussie");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString()
            ));
        } catch (Exception e) {
            System.err.println("Erreur login: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/signup")
    public Map<String, Object> signup(@Valid @RequestBody UserDTO userDTO, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        System.out.println("=== API SIGNUP ===");
        System.out.println("Email: " + userDTO.getEmail());

        try {
            User user = userService.registerUser(userDTO);
            session.setAttribute("user", user);

            response.put("success", true);
            response.put("message", "Inscription réussie");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString()
            ));
        } catch (Exception e) {
            System.err.println("Erreur signup: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/check")
    public Map<String, Object> check(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");

        System.out.println("=== API CHECK ===");
        System.out.println("Session ID: " + session.getId());
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));

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

    @GetMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }
}
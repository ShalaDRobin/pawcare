package animalplatform.service;

import animalplatform.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Envoyer un email simple
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    // Envoyer un email HTML avec template
    public void sendHtmlEmail(String to, String subject, String templateName, Context context) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    // ✅ CORRIGÉ: Email pour demande d'adoption
    public void sendAdoptionRequestEmail(User owner, AdoptionRequest request) {
        Context context = new Context();
        context.setVariable("ownerName", owner.getFirstName());
        context.setVariable("requesterName", request.getRequester().getFirstName());
        context.setVariable("requesterEmail", request.getRequester().getEmail());
        context.setVariable("requesterPhone", request.getPhoneNumber()); // Utiliser phoneNumber du request
        context.setVariable("animalName", request.getAdoptionPost().getTitle());
        context.setVariable("message", request.getMessage());
        context.setVariable("livingSituation", request.getLivingSituation());
        context.setVariable("hasOtherPets", request.getHasOtherPets());
        context.setVariable("experience", request.getExperience());

        // ✅ CHEMIN CORRIGÉ: pawcare/email/adoption-request
        sendHtmlEmail(owner.getEmail(),
                "Nouvelle demande d'adoption - PawCare",
                "pawcare/email/adoption-request",
                context);
    }

    // ✅ CORRIGÉ: Email pour réponse à demande d'adoption
    public void sendAdoptionResponseEmail(User requester, AdoptionRequest request) {
        Context context = new Context();
        context.setVariable("requesterName", requester.getFirstName());
        context.setVariable("animalName", request.getAdoptionPost().getTitle());
        context.setVariable("status", request.getStatus().toString());
        context.setVariable("ownerName", request.getOwner().getFirstName());
        context.setVariable("ownerPhone", request.getOwner().getPhone());
        context.setVariable("ownerEmail", request.getOwner().getEmail());
        context.setVariable("ownerResponse", request.getOwnerResponse());

        // ✅ CHEMIN CORRIGÉ: pawcare/email/adoption-response
        sendHtmlEmail(requester.getEmail(),
                "Réponse à votre demande d'adoption - PawCare",
                "pawcare/email/adoption-response",
                context);
    }

    // ✅ CORRIGÉ: Email pour signalement d'animal perdu
    public void sendLostAnimalAlert(LostAnimalPost post, User user) {
        Context context = new Context();
        context.setVariable("userName", user.getFirstName());
        context.setVariable("animalName", post.getName());
        context.setVariable("animalType", post.getAnimalType());
        context.setVariable("lastSeenLocation", post.getLastSeenLocation());
        context.setVariable("lastSeenDate", post.getLastSeenDate());
        context.setVariable("description", post.getDescription());
        context.setVariable("ownerPhone", post.getOwnerPhone());
        context.setVariable("postId", post.getId());

        sendHtmlEmail(user.getEmail(),
                "🐾 Animal perdu dans votre région - PawCare",
                "pawcare/email/lost-animal-alert",
                context);
    }

    // ✅ CORRIGÉ: Email de contact
    public void sendContactEmail(User owner, User contact, String message, LostAnimalPost post) {
        Context context = new Context();
        context.setVariable("ownerName", owner.getFirstName());
        context.setVariable("contactName", contact.getFirstName());
        context.setVariable("contactEmail", contact.getEmail());
        context.setVariable("contactPhone", contact.getPhone());
        context.setVariable("message", message);
        context.setVariable("animalName", post.getName());
        context.setVariable("animalType", post.getAnimalType());

        sendHtmlEmail(owner.getEmail(),
                "Message concernant " + post.getName() + " - PawCare",
                "pawcare/email/contact-message",
                context);
    }

    // ✅ CORRIGÉ: Email de bienvenue
    public void sendWelcomeEmail(User user) {
        Context context = new Context();
        context.setVariable("userName", user.getFirstName());
        context.setVariable("loginUrl", "http://localhost:8080/auth/login");

        sendHtmlEmail(user.getEmail(),
                "Bienvenue sur PawCare ! 🐾",
                "pawcare/email/welcome",
                context);
    }

    // ✅ CORRIGÉ: Email de réinitialisation de mot de passe
    public void sendPasswordResetEmail(User user, String token) {
        Context context = new Context();
        context.setVariable("userName", user.getFirstName());
        context.setVariable("resetUrl", "http://localhost:8080/auth/reset-password?token=" + token);

        sendHtmlEmail(user.getEmail(),
                "Réinitialisation de votre mot de passe - PawCare",
                "pawcare/email/password-reset",
                context);
    }

    // ✅ CORRIGÉ: Newsletter
    public void sendNewsletter(String email, String content) {
        Context context = new Context();
        context.setVariable("content", content);
        context.setVariable("unsubscribeUrl", "http://localhost:8080/newsletter/unsubscribe?email=" + email);

        sendHtmlEmail(email,
                "Newsletter PawCare - Nos actualités",
                "pawcare/email/newsletter",
                context);
    }

    // Email simple pour animal trouvé
    public void sendLostAnimalFoundEmail(LostAnimalPost post) {
        String subject = "🐾 Bonne nouvelle - Votre animal a été trouvé !";
        String text = "Bonjour " + post.getUser().getFirstName() + ",\n\n" +
                "Quelqu'un a signalé que " +
                (post.getName() != null ? post.getName() : "votre animal") +
                " a été trouvé !\n\n" +
                "Connectez-vous à PawCare pour plus de détails et pour contacter la personne.\n\n" +
                "Cordialement,\nL'équipe PawCare";

        sendSimpleEmail(post.getUser().getEmail(), subject, text);
    }
}
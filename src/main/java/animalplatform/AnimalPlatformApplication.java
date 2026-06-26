package animalplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class AnimalPlatformApplication {

    private final Environment environment;

    public AnimalPlatformApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(AnimalPlatformApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() throws UnknownHostException {
        String port = environment.getProperty("server.port", "8080");
        String host = InetAddress.getLocalHost().getHostAddress();
        String appName = environment.getProperty("spring.application.name", "PawCare");

        System.out.println("\n" +
                "╔══════════════════════════════════════════════════════════════╗\n" +
                "║                                                              ║\n" +
                "║   🐾  PawCare - Plateforme de Protection Animale  🐾        ║\n" +
                "║                                                              ║\n" +
                "╠══════════════════════════════════════════════════════════════╣\n" +
                "║                                                              ║\n" +
                "║   Application démarrée avec succès !                        ║\n" +
                "║                                                              ║\n" +
                "║   📍 Local    : http://localhost:" + port + "                 \n" +
                "║   📍 Réseau   : http://" + host + ":" + port + "                \n" +
                "║                                                              ║\n" +
                "║   🚀 Profil actif : " + getActiveProfiles() + "                      \n" +
                "║   📂 Base de données : MySQL                                \n" +
                "║   🎨 Template : Thymeleaf                                   \n" +
                "║                                                              ║\n" +
                "╚══════════════════════════════════════════════════════════════╝\n"
        );

        System.out.println("✅ PawCare est prêt à vous aider à sauver des animaux ! 🐕🐈🐇");
    }

    private String getActiveProfiles() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            return "default";
        }
        return String.join(", ", profiles);
    }
}


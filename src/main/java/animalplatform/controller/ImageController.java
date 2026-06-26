package animalplatform.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ImageController {

    private final String UPLOAD_ADOPTION_DIR = "uploads/adoption/";
    private final String UPLOAD_LOST_DIR = "uploads/lost/";

    // ✅ Pour les images d'adoption
    @GetMapping("/uploads/adoption/{filename:.+}")
    public ResponseEntity<Resource> serveAdoptionImage(@PathVariable String filename) {
        return serveImage(UPLOAD_ADOPTION_DIR, filename);
    }

    // ✅ Pour les images d'animaux perdus (AJOUTÉ)
    @GetMapping("/uploads/lost/{filename:.+}")
    public ResponseEntity<Resource> serveLostImage(@PathVariable String filename) {
        return serveImage(UPLOAD_LOST_DIR, filename);
    }

    // Méthode commune pour servir les images
    private ResponseEntity<Resource> serveImage(String uploadDir, String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            System.out.println("🔍 Tentative de chargement image: " + filePath);
            System.out.println("   Fichier existe: " + resource.exists());
            System.out.println("   Fichier lisible: " + resource.isReadable());

            if (resource.exists() && resource.isReadable()) {
                String contentType = "image/jpeg";
                if (filename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (filename.endsWith(".webp")) {
                    contentType = "image/webp";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                        .body(resource);
            } else {
                System.err.println("❌ Image non trouvée: " + filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement image: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
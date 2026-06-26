package animalplatform.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FileUploadUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadUtil.class);

    // Extensions autorisées
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp"
    ));

    // Taille maximale (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * Sauvegarder un fichier uploadé
     */
    public static String saveFile(String uploadDir, String fileName, MultipartFile multipartFile)
            throws IOException {

        Path uploadPath = Paths.get(uploadDir);

        // Créer le répertoire s'il n'existe pas
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String fileCode = UUID.randomUUID().toString();
        String newFileName = fileCode + "-" + fileName;

        Path filePath = uploadPath.resolve(newFileName);

        // Copier le fichier
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Erreur lors de la sauvegarde du fichier: " + fileName, e);
        }

        return newFileName;
    }

    /**
     * Valider un fichier uploadé
     */
    public static void validateFile(MultipartFile file) {
        // Vérifier si le fichier est vide
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        // Vérifier la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Le fichier est trop volumineux (max 5MB)");
        }

        // Vérifier l'extension
        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null) {
            String extension = getFileExtension(originalFileName).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("Type de fichier non autorisé: " + extension);
            }
        } else {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }

        // Vérifier le type MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier doit être une image");
        }
    }

    /**
     * Supprimer un fichier
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.error("Erreur lors de la suppression du fichier: " + filePath, e);
            return false;
        }
    }

    /**
     * Obtenir l'extension d'un fichier
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * Nettoyer le nom de fichier
     */
    public static String cleanFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    /**
     * Obtenir l'URL publique d'un fichier
     */
    public static String getFileUrl(String uploadDir, String fileName) {
        return "/" + uploadDir + "/" + fileName;
    }

    /**
     * Redimensionner une image (simulé - nécessite une bibliothèque comme Thumbnails)
     */
    public static void resizeImage(String sourcePath, String destPath, int width, int height) {
        // Implémentation avec une bibliothèque comme Thumbnails
        // Thumbnails.of(sourcePath).size(width, height).toFile(destPath);
        logger.info("Redimensionnement d'image de {} à {}x{}", sourcePath, width, height);
        // Pour l'instant, on copie simplement
        try {
            Files.copy(Paths.get(sourcePath), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Erreur lors de la copie de l'image", e);
        }
    }
}

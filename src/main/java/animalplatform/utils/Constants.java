package animalplatform.utils;

public class Constants {

    // Rôles
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_ASSOCIATION = "ASSOCIATION";
    public static final String ROLE_VETERINAIRE = "VETERINAIRE";

    // Statuts des annonces
    public static final String POST_STATUS_AVAILABLE = "AVAILABLE";
    public static final String POST_STATUS_PENDING = "PENDING";
    public static final String POST_STATUS_ADOPTED = "ADOPTED";

    // Statuts des animaux perdus
    public static final String LOST_STATUS_LOST = "LOST";
    public static final String LOST_STATUS_FOUND = "FOUND";
    public static final String LOST_STATUS_RETURNED = "RETURNED";

    // Statuts des demandes d'adoption
    public static final String REQUEST_STATUS_PENDING = "PENDING";
    public static final String REQUEST_STATUS_ACCEPTED = "ACCEPTED";
    public static final String REQUEST_STATUS_REJECTED = "REJECTED";
    public static final String REQUEST_STATUS_CANCELLED = "CANCELLED";

    // Types d'animaux
    public static final String[] ANIMAL_TYPES = {"Chien", "Chat", "Lapin", "Oiseau", "Poisson", "Rongeur", "Autre"};

    // Tailles d'animaux
    public static final String[] ANIMAL_SIZES = {"Petit", "Moyen", "Grand"};

    // Genres
    public static final String[] GENDERS = {"Mâle", "Femelle"};

    // Types de notification
    public static final String NOTIF_ADOPTION_REQUEST = "ADOPTION_REQUEST";
    public static final String NOTIF_ADOPTION_RESPONSE = "ADOPTION_RESPONSE";
    public static final String NOTIF_LOST_ANIMAL = "LOST_ANIMAL";
    public static final String NOTIF_FORUM_REPLY = "FORUM_REPLY";
    public static final String NOTIF_SYSTEM = "SYSTEM";

    // Chemins de fichiers
    public static final String UPLOAD_DIR = "uploads/";
    public static final String ADOPTION_IMG_DIR = "uploads/adoption/";
    public static final String LOST_ANIMAL_IMG_DIR = "uploads/lost/";
    public static final String PROFILE_IMG_DIR = "uploads/profiles/";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 12;
    public static final int MAX_PAGE_SIZE = 50;

    // Dates
    public static final int MAX_LOST_DAYS = 30; // Jours avant alerte

    // Messages d'erreur
    public static final String ERROR_USER_NOT_FOUND = "Utilisateur non trouvé";
    public static final String ERROR_POST_NOT_FOUND = "Annonce non trouvée";
    public static final String ERROR_UNAUTHORIZED = "Vous n'êtes pas autorisé à effectuer cette action";
    public static final String ERROR_EMAIL_EXISTS = "Cet email est déjà utilisé";
    public static final String ERROR_INVALID_CREDENTIALS = "Email ou mot de passe incorrect";

    // Sessions
    public static final String SESSION_USER = "user";
    public static final String SESSION_CART = "cart";

    // API Endpoints
    public static final String API_BASE = "/api";
    public static final String API_AUTH = API_BASE + "/auth";
    public static final String API_ADOPTION = API_BASE + "/adoption";
    public static final String API_LOST = API_BASE + "/lost";
    public static final String API_FORUM = API_BASE + "/forum";

    // Constructeur privé pour empêcher l'instanciation
    private Constants() {
        throw new IllegalStateException("Classe utilitaire");
    }
}

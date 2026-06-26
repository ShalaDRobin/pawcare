package animalplatform.repository;

import animalplatform.model.LostAnimalPost;
import animalplatform.model.LostAnimalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LostAnimalPostRepository extends JpaRepository<LostAnimalPost, Long> {

    // Trouver par statut
    List<LostAnimalPost> findByStatusOrderByCreatedAtDesc(LostAnimalStatus status);

    // Trouver par localisation
    @Query("SELECT l FROM LostAnimalPost l WHERE " +
            "LOWER(l.lastSeenLocation) LIKE LOWER(CONCAT('%', :location, '%')) " +
            "AND l.status = :status " +
            "ORDER BY l.createdAt DESC")
    List<LostAnimalPost> findByLocation(@Param("location") String location);

    // Recherche avancée
    @Query("SELECT l FROM LostAnimalPost l WHERE " +
            "(:animalType IS NULL OR l.animalType = :animalType) AND " +
            "(:location IS NULL OR LOWER(l.lastSeenLocation) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:status IS NULL OR l.status = :status) AND " +
            "(:fromDate IS NULL OR l.lastSeenDate >= :fromDate) " +
            "ORDER BY l.createdAt DESC")
    List<LostAnimalPost> searchLostAnimals(@Param("animalType") String animalType,
                                           @Param("location") String location,
                                           @Param("status") LostAnimalStatus status,
                                           @Param("fromDate") LocalDate fromDate);

    // ✅ CORRECTION: Trouver les animaux perdus récents
    @Query("SELECT l FROM LostAnimalPost l WHERE " +
            "l.status = :status AND " +
            "l.createdAt >= :date " +  // ← CORRECTION: utiliser un paramètre
            "ORDER BY l.createdAt DESC")
    List<LostAnimalPost> findRecentLost(@Param("status") LostAnimalStatus status,
                                        @Param("date") LocalDateTime date);

    // Incrémenter les vues
    @Modifying
    @Transactional
    @Query("UPDATE LostAnimalPost l SET l.viewCount = l.viewCount + 1 WHERE l.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // Incrémenter les partages
    @Modifying
    @Transactional
    @Query("UPDATE LostAnimalPost l SET l.shareCount = l.shareCount + 1 WHERE l.id = :id")
    void incrementShareCount(@Param("id") Long id);

    // Trouver les animaux perdus avant une date
    @Query("SELECT l FROM LostAnimalPost l WHERE " +
            "l.status = :status AND " +
            "l.lastSeenDate <= :date")
    List<LostAnimalPost> findLostBeforeDate(@Param("date") LocalDate date);
}
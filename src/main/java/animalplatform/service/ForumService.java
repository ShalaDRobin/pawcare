package animalplatform.service;

import animalplatform.dto.ForumCommentDTO;
import animalplatform.dto.ForumPostDTO;
import animalplatform.model.*;
import animalplatform.repository.ForumCategoryRepository;
import animalplatform.repository.ForumPostRepository;
import animalplatform.repository.ForumCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumService {

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumCategoryRepository forumCategoryRepository;

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private NotificationService notificationService;

    // ============================================
    // CATÉGORIES - AVEC MISE À JOUR DES COMPTEURS
    // ============================================

    public List<ForumCategory> getAllCategories() {
        List<ForumCategory> categories = forumCategoryRepository.findAllByOrderByOrderIndexAsc();

        // ✅ Mettre à jour les compteurs de posts pour chaque catégorie
        for (ForumCategory category : categories) {
            long postCount = forumPostRepository.countByCategoryId(category.getId());
            category.setPostCount((int) postCount);
        }

        return categories;
    }

    public ForumCategory getCategoryById(Long id) {
        ForumCategory category = forumCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));

        // ✅ Mettre à jour le compteur
        long postCount = forumPostRepository.countByCategoryId(category.getId());
        category.setPostCount((int) postCount);

        return category;
    }

    // ✅ Méthode pour réinitialiser tous les compteurs de catégories
    @Transactional
    public void refreshAllCategoryCounts() {
        List<ForumCategory> categories = forumCategoryRepository.findAll();
        for (ForumCategory category : categories) {
            long postCount = forumPostRepository.countByCategoryId(category.getId());
            category.setPostCount((int) postCount);
            forumCategoryRepository.save(category);
        }
    }

    // ============================================
    // POSTS
    // ============================================

    public List<ForumPost> getRecentPosts(int limit) {
        return forumPostRepository.findTop10ByOrderByCreatedAtDesc()
                .stream().limit(limit).toList();
    }

    public List<ForumPost> getPopularPosts(int limit) {
        return forumPostRepository.findMostCommentedPosts(PageRequest.of(0, limit));
    }

    public Page<ForumPost> getPostsByCategory(Long categoryId, Pageable pageable) {
        return forumPostRepository.findByCategoryIdOrderByIsPinnedDescCreatedAtDesc(categoryId, pageable);
    }

    public Page<ForumPost> getAllPosts(Pageable pageable) {
        return forumPostRepository.findAll(pageable);
    }

    public ForumPost getPostById(Long id) {
        return forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post non trouvé"));
    }

    @Transactional
    public void incrementViewCount(Long id) {
        forumPostRepository.incrementViewCount(id);
    }

    @Transactional
    public ForumPost createPost(ForumPostDTO postDTO, User user) {
        ForumCategory category = getCategoryById(postDTO.getCategoryId());

        ForumPost post = new ForumPost();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setCategory(category);
        post.setUser(user);
        post.setIsPinned(false);
        post.setIsLocked(false);

        ForumPost savedPost = forumPostRepository.save(post);

        // ✅ Mettre à jour le compteur de la catégorie en comptant réellement
        long postCount = forumPostRepository.countByCategoryId(category.getId());
        category.setPostCount((int) postCount);
        forumCategoryRepository.save(category);

        return savedPost;
    }

    // ✅ Mettre à jour un post
    @Transactional
    public ForumPost updatePost(Long postId, ForumPostDTO postDTO, User user) {
        ForumPost post = getPostById(postId);

        if (!post.getUser().getId().equals(user.getId()) && !user.getRole().equals(Role.ADMIN)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce post");
        }

        // ✅ Si la catégorie change, mettre à jour les compteurs
        if (postDTO.getCategoryId() != null && !postDTO.getCategoryId().equals(post.getCategory().getId())) {
            ForumCategory oldCategory = post.getCategory();
            ForumCategory newCategory = getCategoryById(postDTO.getCategoryId());

            post.setCategory(newCategory);

            // Mettre à jour les compteurs des deux catégories
            long oldCount = forumPostRepository.countByCategoryId(oldCategory.getId());
            oldCategory.setPostCount((int) oldCount);
            forumCategoryRepository.save(oldCategory);

            long newCount = forumPostRepository.countByCategoryId(newCategory.getId());
            newCategory.setPostCount((int) newCount);
            forumCategoryRepository.save(newCategory);
        }

        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());

        return forumPostRepository.save(post);
    }

    // ✅ Supprimer un post
    @Transactional
    public void deletePost(Long postId, User user) {
        ForumPost post = getPostById(postId);

        if (!post.getUser().getId().equals(user.getId()) && !user.getRole().equals(Role.ADMIN)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce post");
        }

        ForumCategory category = post.getCategory();

        // Supprimer tous les commentaires associés
        forumCommentRepository.deleteByPostId(postId);

        // Supprimer le post
        forumPostRepository.delete(post);

        // ✅ Mettre à jour le compteur de la catégorie en comptant réellement
        long postCount = forumPostRepository.countByCategoryId(category.getId());
        category.setPostCount((int) postCount);
        forumCategoryRepository.save(category);
    }

    // ============================================
    // COMMENTAIRES
    // ============================================

    @Transactional
    public ForumComment addComment(Long postId, User user, String content) {
        ForumPost post = getPostById(postId);

        if (post.getIsLocked()) {
            throw new RuntimeException("Ce post est verrouillé et n'accepte plus de commentaires");
        }

        ForumComment comment = new ForumComment();
        comment.setContent(content);
        comment.setForumPost(post);
        comment.setUser(user);

        ForumComment savedComment = forumCommentRepository.save(comment);

        post.setCommentCount(post.getCommentCount() + 1);
        post.setLastActivityAt(LocalDateTime.now());
        post.setLastActivityBy(user.getFirstName() + " " + user.getLastName());
        forumPostRepository.save(post);

        // Notifier l'auteur du post
        if (!post.getUser().getId().equals(user.getId())) {
            notificationService.sendForumReplyNotification(post.getUser(), comment);
        }

        return savedComment;
    }

    // ✅ Répondre à un commentaire
    @Transactional
    public ForumComment replyToComment(Long postId, Long commentId, User user, String content) {
        ForumPost post = getPostById(postId);

        if (post.getIsLocked()) {
            throw new RuntimeException("Ce post est verrouillé et n'accepte plus de commentaires");
        }

        ForumComment parentComment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));

        ForumComment reply = new ForumComment();
        reply.setContent(content);
        reply.setForumPost(post);
        reply.setUser(user);
        reply.setParentComment(parentComment);

        ForumComment savedReply = forumCommentRepository.save(reply);

        post.setCommentCount(post.getCommentCount() + 1);
        post.setLastActivityAt(LocalDateTime.now());
        post.setLastActivityBy(user.getFirstName() + " " + user.getLastName());
        forumPostRepository.save(post);

        // Notifier l'auteur du commentaire parent
        if (!parentComment.getUser().getId().equals(user.getId())) {
            notificationService.sendForumReplyNotification(parentComment.getUser(), savedReply);
        }

        return savedReply;
    }

    // ✅ Mettre à jour un commentaire
    @Transactional
    public ForumComment updateComment(Long commentId, String content, User user) {
        ForumComment comment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));

        if (!comment.getUser().getId().equals(user.getId()) && !user.getRole().equals(Role.ADMIN)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce commentaire");
        }

        comment.setContent(content);
        return forumCommentRepository.save(comment);
    }

    // ✅ Supprimer un commentaire - CORRIGÉ
    @Transactional
    public void deleteComment(Long commentId, User user) {
        ForumComment comment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));

        boolean isAuthor = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().equals(Role.ADMIN);

        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce commentaire");
        }

        ForumPost post = comment.getForumPost();

        // Supprimer le commentaire (les réponses seront supprimées en cascade si configuré)
        forumCommentRepository.delete(comment);

        // Mettre à jour le compteur du post
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        forumPostRepository.save(post);
    }

    public List<ForumComment> getCommentsByPost(Long postId) {
        return forumCommentRepository.findByForumPostIdOrderByCreatedAtAsc(postId);
    }

    // ✅ Récupérer un commentaire par son ID
    public ForumComment getCommentById(Long commentId) {
        return forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
    }

    // ============================================
    // LIKES
    // ============================================

    @Transactional
    public void likePost(Long postId, User user) {
        forumPostRepository.incrementLikeCount(postId);
    }

    @Transactional
    public void likeComment(Long commentId, User user) {
        forumCommentRepository.incrementLikeCount(commentId);
    }

    // ============================================
    // ADMIN
    // ============================================

    @Transactional
    public void pinPost(Long postId) {
        ForumPost post = getPostById(postId);
        post.setIsPinned(!post.getIsPinned());
        forumPostRepository.save(post);
    }

    @Transactional
    public void lockPost(Long postId) {
        ForumPost post = getPostById(postId);
        post.setIsLocked(!post.getIsLocked());
        forumPostRepository.save(post);
    }

    // ============================================
    // RECHERCHE
    // ============================================

    public Page<ForumPost> searchPosts(String query, Pageable pageable) {
        return forumPostRepository.searchPosts(query, pageable);
    }

    public List<ForumPost> searchPosts(String query) {
        return searchPosts(query, PageRequest.of(0, 20)).getContent();
    }

    // ============================================
    // STATISTIQUES
    // ============================================

    public long countTotalPosts() {
        return forumPostRepository.count();
    }

    public long countActiveMembers() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return forumPostRepository.findRecentActivity(oneDayAgo).stream()
                .map(p -> p.getUser().getId())
                .distinct()
                .count();
    }

    // ✅ Récupérer les posts d'un utilisateur
    public Page<ForumPost> getPostsByUser(Long userId, Pageable pageable) {
        return forumPostRepository.findPostsByUser(userId, pageable);
    }

    // ✅ Récupérer les commentaires d'un utilisateur
    public List<ForumComment> getCommentsByUser(Long userId) {
        return forumCommentRepository.findRecentByUser(userId);
    }

    // ✅ Vérifier si l'utilisateur peut modifier un post
    public boolean canEditPost(Long postId, User user) {
        ForumPost post = getPostById(postId);
        return post.getUser().getId().equals(user.getId()) || user.getRole().equals(Role.ADMIN);
    }

    // ✅ Vérifier si l'utilisateur peut modifier un commentaire
    public boolean canEditComment(Long commentId, User user) {
        ForumComment comment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        return comment.getUser().getId().equals(user.getId()) || user.getRole().equals(Role.ADMIN);
    }
}
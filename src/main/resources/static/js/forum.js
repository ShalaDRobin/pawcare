// ============================================
// FORUM - FONCTIONS PRINCIPALES
// ============================================

// Filtrer le forum par catégorie
function filterForumCategory(categoryId) {
    // Mettre à jour la classe active
    document.querySelectorAll('.forum-category').forEach(c => c.classList.remove('active'));
    if (event && event.currentTarget) {
        event.currentTarget.classList.add('active');
    }

    showToast('Chargement des discussions...');

    fetch('/forum/categorie/' + categoryId + '?page=0')
        .then(response => response.text())
        .then(html => {
            const postsContainer = document.querySelector('.forum-posts');
            if (postsContainer) {
                // Garder le bouton "Créer un sujet" et remplacer les posts
                const createBtn = postsContainer.querySelector('.new-post-btn');
                const tempDiv = document.createElement('div');
                tempDiv.innerHTML = html;
                const newPosts = tempDiv.querySelector('.forum-posts');
                if (newPosts) {
                    postsContainer.innerHTML = newPosts.innerHTML;
                    if (createBtn) {
                        postsContainer.prepend(createBtn.parentElement);
                    }
                }
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            showToast('Erreur lors du chargement');
        });
}

// ============================================
// LIKES
// ============================================

// Liker un post
function likePost(postId) {
    if (!window.isAuthenticated) {
        openModal('login');
        return;
    }

    fetch('/forum/post/' + postId + '/liker', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            const likeCount = document.getElementById('likeCount');
            if (likeCount) {
                likeCount.textContent = parseInt(likeCount.textContent) + 1;
            }
            showToast('❤️ Like ajouté !');
        } else {
            showToast('❌ Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('Erreur lors du like');
    });
}

// Liker un commentaire
function likeComment(commentId) {
    if (!window.isAuthenticated) {
        openModal('login');
        return;
    }

    fetch('/forum/comment/' + commentId + '/liker', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('❤️ Like ajouté !');
            // Mettre à jour le compteur sans recharger
            const buttons = document.querySelectorAll(`button[onclick*="likeComment(${commentId})"]`);
            buttons.forEach(btn => {
                const span = btn.querySelector('span');
                if (span) {
                    span.textContent = parseInt(span.textContent) + 1;
                }
            });
        } else {
            showToast('❌ Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('Erreur lors du like');
    });
}

// ============================================
// COMMENTAIRES - VERSION CORRIGÉE AVEC DEBUG
// ============================================

// Ajouter un commentaire - VERSION CORRIGÉE
function addComment() {
    console.log('🟢 addComment() appelée depuis forum.js');

    // Vérification de l'authentification
    if (!window.isAuthenticated) {
        console.log('🔴 Utilisateur non authentifié');
        openModal('login');
        return;
    }
    console.log('✅ Utilisateur authentifié');

    // Récupération du textarea
    const textarea = document.getElementById('commentInput');
    console.log('📝 textarea trouvé ?', textarea);

    if (!textarea) {
        console.error('❌ textarea #commentInput non trouvé');
        showToast('❌ Erreur technique: champ de commentaire introuvable');
        return;
    }

    const content = textarea.value.trim();
    console.log('📝 Contenu du commentaire:', content);

    // Validation du contenu
    if (!content) {
        console.log('⚠️ Contenu vide');
        showToast('⚠️ Veuillez écrire un commentaire');
        return;
    }

    // Vérification si le post est verrouillé
    if (window.isLocked) {
        console.log('🔒 Post verrouillé');
        showToast('❌ Ce post est verrouillé');
        return;
    }

    // Vérification du postId
    if (!window.postId) {
        console.error('❌ postId non défini');
        showToast('❌ Erreur: identifiant du post manquant');
        return;
    }
    console.log('📝 postId =', window.postId);

    // Envoi de la requête
    showToast('📤 Envoi en cours...');
    console.log('📤 Envoi vers /forum/post/' + window.postId + '/commenter');

    fetch('/forum/post/' + window.postId + '/commenter', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: 'content=' + encodeURIComponent(content)
    })
    .then(response => {
        console.log('📥 Réponse reçue, status:', response.status);
        if (!response.ok) {
            throw new Error('HTTP ' + response.status);
        }
        return response.json();
    })
    .then(data => {
        console.log('📥 Données reçues:', data);
        if (data.success) {
            showToast('✅ Commentaire ajouté !');
            textarea.value = '';
            // Recharger la page pour voir le nouveau commentaire
            setTimeout(() => {
                console.log('🔄 Rechargement de la page...');
                window.location.reload();
            }, 1000);
        } else {
            console.error('❌ Erreur serveur:', data.message);
            showToast('❌ ' + (data.message || 'Erreur inconnue'));
        }
    })
    .catch(error => {
        console.error('❌ Erreur fetch:', error);
        showToast('❌ Erreur lors de l\'envoi: ' + error.message);
    });
}

// Soumettre une réponse à un commentaire - VERSION CORRIGÉE
function submitReply(commentId) {
    console.log('🟢 submitReply() appelée pour commentId:', commentId);

    if (!window.isAuthenticated) {
        console.log('🔴 Non authentifié');
        openModal('login');
        return;
    }

    const replyForm = document.getElementById('replyForm-' + commentId);
    if (!replyForm) {
        console.error('❌ Formulaire de réponse non trouvé');
        showToast('❌ Erreur technique');
        return;
    }

    const textarea = replyForm.querySelector('textarea');
    if (!textarea) {
        console.error('❌ Textarea non trouvé dans le formulaire');
        showToast('❌ Erreur technique');
        return;
    }

    const content = textarea.value.trim();
    console.log('📝 Contenu de la réponse:', content);

    if (!content) {
        showToast('⚠️ Veuillez écrire une réponse');
        return;
    }

    if (window.isLocked) {
        showToast('❌ Ce post est verrouillé');
        return;
    }

    showToast('📤 Envoi en cours...');

    fetch('/forum/post/' + window.postId + '/comment/' + commentId + '/repondre', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: 'content=' + encodeURIComponent(content)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('✅ Réponse ajoutée !');
            hideReplyForm(commentId);
            setTimeout(() => window.location.reload(), 1000);
        } else {
            showToast('❌ Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('❌ Erreur lors de l\'envoi');
    });
}

// Afficher le formulaire de réponse
function showReplyForm(commentId) {
    const form = document.getElementById('replyForm-' + commentId);
    if (form) {
        form.classList.toggle('show');
        if (form.classList.contains('show')) {
            const textarea = form.querySelector('textarea');
            if (textarea) textarea.focus();
        }
    }
}

// Cacher le formulaire de réponse
function hideReplyForm(commentId) {
    const form = document.getElementById('replyForm-' + commentId);
    if (form) {
        form.classList.remove('show');
        const textarea = form.querySelector('textarea');
        if (textarea) textarea.value = '';
    }
}

// Recharger les commentaires
function loadComments() {
    fetch('/forum/post/' + window.postId + '/comments', {
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            updateComments(data.comments);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
    });
}

// Mettre à jour l'affichage des commentaires
function updateComments(comments) {
    const container = document.getElementById('commentsList');
    if (!container) return;

    // TODO: Implémenter le rendu des commentaires
    // Pour l'instant, recharger la page
    window.location.reload();
}

// ============================================
// GESTION DES POSTS
// ============================================

// Modifier un post
function editPost(postId) {
    window.location.href = '/forum/post/' + postId + '/modifier';
}

// Supprimer un post
function deletePost(postId) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce post ? Cette action est irréversible.')) return;

    showToast('Suppression en cours...');

    fetch('/forum/post/' + postId + '/supprimer', {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('✅ Post supprimé avec succès');
            setTimeout(() => window.location.href = '/forum', 1500);
        } else {
            showToast('❌ Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('Erreur lors de la suppression');
    });
}

// ============================================
// ADMIN
// ============================================

// Épingler un post
function pinPost(postId) {
    fetch('/forum/post/' + postId + '/epingler', {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('✅ Post épinglé/désépinglé');
            setTimeout(() => window.location.reload(), 500);
        } else {
            showToast('❌ Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('Erreur lors de l\'épinglage');
    });
}

// Verrouiller un post
function lockPost(postId) {
    fetch('/forum/post/' + postId + '/verrouiller', {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('✅ Post verrouillé/déverrouillé');
            setTimeout(() => window.location.reload(), 500);
        } else {
            showToast('❌ Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('Erreur lors du verrouillage');
    });
}

// ============================================
// MODIFIER UN COMMENTAIRE
// ============================================

function editComment(commentId) {
    if (!window.isAuthenticated) {
        openModal('login');
        return;
    }

    // Récupérer le contenu actuel du commentaire
    const commentElement = document.querySelector(`.comment:has(button[onclick*="editComment(${commentId})"])`);
    if (!commentElement) return;

    const contentDiv = commentElement.querySelector('.comment-content');
    if (!contentDiv) return;

    const currentContent = contentDiv.textContent.trim();
    const newContent = prompt('Modifier votre commentaire :', currentContent);

    if (newContent && newContent.trim() !== '' && newContent.trim() !== currentContent) {
        fetch('/forum/comment/' + commentId + '/modifier', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: 'content=' + encodeURIComponent(newContent.trim())
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast('✅ Commentaire modifié');
                loadComments();
            } else {
                showToast('❌ Erreur: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            showToast('Erreur lors de la modification');
        });
    }
}

// ============================================
// SUPPRIMER UN COMMENTAIRE
// ============================================

function deleteComment(commentId) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce commentaire ?')) return;

    fetch('/forum/comment/' + commentId + '/supprimer', {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('✅ Commentaire supprimé');
            loadComments();
        } else {
            showToast('❌ Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('Erreur lors de la suppression');
    });
}

// ============================================
// RECHERCHE
// ============================================

function searchForum() {
    const query = document.getElementById('forum-search');
    if (!query) return;

    const q = query.value.trim();
    if (q.length < 2) {
        showToast('Veuillez entrer au moins 2 caractères');
        return;
    }

    window.location.href = '/forum?search=' + encodeURIComponent(q);
}

// ============================================
// INITIALISATION
// ============================================

// Exporter les fonctions globalement
window.likePost = likePost;
window.likeComment = likeComment;
window.addComment = addComment;
window.submitReply = submitReply;
window.showReplyForm = showReplyForm;
window.hideReplyForm = hideReplyForm;
window.editPost = editPost;
window.deletePost = deletePost;
window.pinPost = pinPost;
window.lockPost = lockPost;
window.editComment = editComment;
window.deleteComment = deleteComment;
window.searchForum = searchForum;
window.filterForumCategory = filterForumCategory;

// ✅ NOUVEAU : Log de confirmation au chargement
console.log('✅ forum.js chargé avec succès');
console.log('✅ addComment disponible:', typeof window.addComment);
console.log('✅ postId:', window.postId);
console.log('✅ isAuthenticated:', window.isAuthenticated);
console.log('✅ isLocked:', window.isLocked);
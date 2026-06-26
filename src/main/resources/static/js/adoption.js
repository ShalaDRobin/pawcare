// ============================================
// FILTRES D'ADOPTION - VERSION CORRIGÉE
// ============================================
function filterAdoptions(type) {
    console.log('🎯 Filtre sélectionné:', type);

    // Mettre à jour les boutons actifs
    const buttons = document.querySelectorAll('.filter-btn');
    buttons.forEach(b => b.classList.remove('active'));

    // Trouver le bouton correspondant au type et l'activer
    buttons.forEach(btn => {
        const btnText = btn.textContent.trim();
        if (type === 'all' && btnText === 'Tous') {
            btn.classList.add('active');
        } else if (type === 'urgent' && btnText.includes('Urgences')) {
            btn.classList.add('active');
        } else if (type !== 'all' && type !== 'urgent' && btnText.includes(type)) {
            btn.classList.add('active');
        }
    });

    // Construire l'URL en fonction du filtre
    let url = '/adoption/filter';
    let params = [];

    if (type === 'urgent') {
        params.push('urgent=true');
        showToast('Affichage des adoptions urgentes');
    } else if (type !== 'all') {
        params.push('animalType=' + encodeURIComponent(type));
        showToast('Filtre: ' + type);
    } else {
        showToast('Affichage de tous les animaux');
    }

    if (params.length > 0) {
        url += '?' + params.join('&');
    }

    console.log('🔍 Appel API:', url);

    fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include'
    })
    .then(response => {
        console.log('📡 Statut HTTP:', response.status);
        if (!response.ok) {
            throw new Error('Erreur HTTP: ' + response.status);
        }
        return response.json();
    })
    .then(posts => {
        console.log('📨 Données reçues:', posts.length, 'animaux');
        if (posts && Array.isArray(posts)) {
            updateAdoptionsGrid(posts);
            if (posts.length === 0) {
                showToast('Aucun animal trouvé pour ce filtre');
            } else {
                showToast(posts.length + ' animal(s) trouvé(s)');
            }
        } else {
            updateAdoptionsGrid([]);
            showToast('Aucune donnée reçue');
        }
    })
    .catch(error => {
        console.error('❌ Erreur détaillée:', error);
        showToast('Erreur lors du filtrage: ' + error.message);
        // Recharger tous les animaux en cas d'erreur
        loadAllAdoptions();
    });
}

// ============================================
// CHARGER TOUS LES ANIMAUX (FALLBACK)
// ============================================
function loadAllAdoptions() {
    console.log('🔄 Chargement de tous les animaux...');
    fetch('/adoption/filter', {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Erreur HTTP: ' + response.status);
        }
        return response.json();
    })
    .then(posts => {
        console.log('📨 Total des animaux:', posts.length);
        if (posts && Array.isArray(posts)) {
            updateAdoptionsGrid(posts);
        }
    })
    .catch(error => {
        console.error('❌ Erreur chargement:', error);
        showToast('Impossible de charger les annonces');
    });
}

// ============================================
// DEMANDE D'ADOPTION - ✅ CORRIGÉ
// ============================================
function openAdoptionRequest(postId) {
    if (!window.isAuthenticated) {
        openModal('login');
        return;
    }
    // ✅ Rediriger vers le formulaire de demande d'adoption (GET)
    window.location.href = '/adoption/' + postId + '/request-form';
}

// ============================================
// MODIFICATION D'UNE ANNONCE
// ============================================
function editAdoption(postId) {
    if (!window.isAuthenticated) {
        openModal('login');
        return;
    }
    window.location.href = '/adoption/' + postId + '/edit';
}

// ============================================
// SUPPRESSION D'UNE ANNONCE
// ============================================
function deleteAdoption(postId) {
    if (!window.isAuthenticated) {
        openModal('login');
        return;
    }

    if (confirm('Êtes-vous sûr de vouloir supprimer cette annonce ? Cette action est irréversible.')) {
        showToast('Suppression en cours...');

        fetch('/adoption/' + postId + '/delete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast('✅ Annonce supprimée avec succès !');
                // Recharger la liste des annonces
                setTimeout(() => loadAllAdoptions(), 1000);
            } else {
                showToast('Erreur: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            showToast('Erreur lors de la suppression');
        });
    }
}

// ❌ FONCTION FAVORIS SUPPRIMÉE
// function toggleFavorite(button, postId) { ... }

// ============================================
// PAGINATION
// ============================================
function loadMoreAdoptions(page) {
    fetch('/adoption?page=' + page, {
        credentials: 'include'
    })
    .then(response => response.text())
    .then(html => {
        const grid = document.getElementById('adoptions-grid');
        if (grid) {
            grid.insertAdjacentHTML('beforeend', html);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('Erreur de chargement');
    });
}

// ============================================
// RECHERCHE
// ============================================
function searchAdoptions(query) {
    if (query.length < 3) {
        if (query.length === 0) {
            loadAllAdoptions();
        }
        return;
    }

    fetch('/adoption/search?q=' + encodeURIComponent(query), {
        credentials: 'include'
    })
    .then(response => response.json())
    .then(posts => {
        if (posts && Array.isArray(posts)) {
            updateAdoptionsGrid(posts);
            showToast(posts.length + ' résultat(s) trouvé(s)');
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('Erreur lors de la recherche');
    });
}

// ============================================
// MISE À JOUR DE LA GRILLE
// ============================================
function updateAdoptionsGrid(posts) {
    const grid = document.getElementById('adoptions-grid');
    if (!grid) return;

    if (!posts || posts.length === 0) {
        grid.innerHTML = '<div class="empty-state">🐾 Aucun animal trouvé pour le moment</div>';
        return;
    }

    grid.innerHTML = '';
    posts.forEach(post => {
        const card = createAdoptionCard(post);
        grid.appendChild(card);
    });

    // Réinitialiser les animations fade-in
    document.querySelectorAll('.fade-in').forEach(el => {
        if (observer && observer.observe) {
            observer.observe(el);
        }
    });
}

// ============================================
// CRÉATION D'UNE CARTE - ✅ AVEC AFFICHAGE DES IMAGES
// ============================================
function createAdoptionCard(post) {
    const card = document.createElement('div');
    card.className = 'adopt-card fade-in';
    card.onclick = () => window.location.href = '/adoption/' + post.id;

    const animalEmoji = post.animalType === 'Chien' ? '🐕' :
                        post.animalType === 'Chat' ? '🐈' :
                        post.animalType === 'Lapin' ? '🐇' :
                        post.animalType === 'Oiseau' ? '🦜' : '🐾';

    // ✅ Vérifier si l'utilisateur connecté est le propriétaire
    const isOwner = window.currentUser && window.currentUser.id === post.userId;

    // ✅ Afficher les boutons Modifier/Supprimer seulement si propriétaire
    const ownerButtons = isOwner ? `
        <div class="owner-actions" style="display: flex; gap: 8px; margin-top: 10px; border-top: 1px solid var(--border); padding-top: 10px;">
            <button class="btn btn-outline" style="flex:1; padding:8px; font-size:0.8rem;"
                    onclick="event.stopPropagation(); editAdoption(${post.id})">✏️ Modifier</button>
            <button class="btn btn-outline" style="flex:1; padding:8px; font-size:0.8rem; color: var(--terracotta);"
                    onclick="event.stopPropagation(); deleteAdoption(${post.id})">🗑️ Supprimer</button>
        </div>
    ` : '';

    // ✅ HTML DE LA CARTE - AVEC IMAGE SI DISPONIBLE
    const hasImage = post.imageUrl && post.imageUrl !== '';
    const imageStyle = hasImage ? `background-image: url('${post.imageUrl}'); background-size: cover; background-position: center;` : '';
    const imageClass = hasImage ? 'has-image' : 'no-image';

    card.innerHTML = `
        <div class="adopt-img bg${(post.id % 4) + 1} ${imageClass}" style="${imageStyle}">
            ${!hasImage ? `<span>${animalEmoji}</span>` : ''}
            ${post.isUrgent ? '<div class="urgency-badge badge-urgent">Urgent</div>' : '<div class="urgency-badge badge-new">Nouveau</div>'}
        </div>
        <div class="adopt-body">
            <div class="adopt-name">${escapeHtml(post.title)}</div>
            <div class="adopt-details">
                <div class="adopt-detail">${escapeHtml(post.animalType)} ${escapeHtml(post.breed || '')}</div>
                <div class="adopt-detail">📅 ${post.age} ans</div>
                <div class="adopt-detail">📍 ${escapeHtml(post.location)}</div>
            </div>
            <div class="adopt-desc">${escapeHtml(post.description ? post.description.substring(0, 100) : '')}...</div>
            <!-- ✅ BOUTON ADOPTER UNIQUEMENT (PLEINE LARGEUR) -->
            <div class="adopt-actions">
                <button class="btn btn-primary" style="width: 100%; padding: 12px"
                        onclick="event.stopPropagation(); openAdoptionRequest(${post.id})">
                    🐾 Demander l'adoption
                </button>
            </div>
            ${ownerButtons}
        </div>
    `;

    return card;
}

// ============================================
// UTILITAIRE HTML ESCAPE
// ============================================
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ============================================
// CHARGEMENT INITIAL
// ============================================
document.addEventListener('DOMContentLoaded', function() {
    console.log('🔄 Chargement initial des annonces...');
    loadAllAdoptions();
});

// ============================================
// EXPORT DES FONCTIONS
// ============================================
window.editAdoption = editAdoption;
window.deleteAdoption = deleteAdoption;
// ❌ window.toggleFavorite SUPPRIMÉ
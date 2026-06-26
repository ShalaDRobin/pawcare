// ============================================
// MODAL FUNCTIONS
// ============================================
function openModal(tab) {
    const authModal = document.getElementById('authModal');
    if (authModal) {
        authModal.classList.add('active');
        switchTab(tab);
    }
}

function closeModal() {
    const authModal = document.getElementById('authModal');
    if (authModal) {
        authModal.classList.remove('active');
    }
}

function switchTab(tab) {
    const tabLogin = document.getElementById('tab-login');
    const tabSignup = document.getElementById('tab-signup');
    const panelLogin = document.getElementById('panel-login');
    const panelSignup = document.getElementById('panel-signup');

    if (tabLogin && tabSignup && panelLogin && panelSignup) {
        if (tab === 'login') {
            tabLogin.classList.add('active');
            tabSignup.classList.remove('active');
            panelLogin.classList.add('active');
            panelSignup.classList.remove('active');
        } else {
            tabLogin.classList.remove('active');
            tabSignup.classList.add('active');
            panelLogin.classList.remove('active');
            panelSignup.classList.add('active');
        }
    }
}

// ============================================
// TOAST FUNCTIONS
// ============================================
let toastTimer;

function showToast(msg) {
    const toast = document.getElementById('toast');
    const toastMsg = document.getElementById('toastMsg');

    if (toast && toastMsg) {
        clearTimeout(toastTimer);
        toastMsg.textContent = msg;
        toast.classList.add('active');

        toastTimer = setTimeout(() => {
            toast.classList.remove('active');
        }, 3500);
    } else {
        console.log('Toast:', msg);
    }
}

// ============================================
// SCROLL FUNCTIONS
// ============================================
function scrollToSection(id) {
    const el = document.getElementById(id);
    if (el) {
        el.scrollIntoView({ behavior: 'smooth' });
    }
}

const sections = [
    { id: 'home', tab: 'tab-home' },
    { id: 'adoption', tab: 'tab-adoption' },
    { id: 'perdus', tab: 'tab-perdus' },
    { id: 'sensibilisation', tab: 'tab-sensibilisation' },
    { id: 'forum', tab: 'tab-forum' }
];

function updateActiveTab() {
    const scrollY = window.scrollY + 100;
    let current = sections[0].tab;

    for (const s of sections) {
        const el = document.getElementById(s.id);
        if (el && el.offsetTop <= scrollY) {
            current = s.tab;
        }
    }

    document.querySelectorAll('.nav-tab-link').forEach(t => t.classList.remove('active'));
    const activeEl = document.getElementById(current);
    if (activeEl) {
        activeEl.classList.add('active');
    }
}

// ============================================
// FILTER FUNCTIONS
// ============================================
function setFilter(btn) {
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    if (typeof filterAdoptions === 'function') {
        const type = btn.textContent.trim().replace(/[🐕🐈🐇🦜🐾]/g, '').trim() || 'all';
        filterAdoptions(type === 'Tous' ? 'all' : type);
    } else {
        showToast('Filtre appliqué : ' + btn.textContent.trim());
    }
}

// ============================================
// FORUM CATEGORY FUNCTIONS
// ============================================
function setCat(el) {
    document.querySelectorAll('.forum-category').forEach(c => c.classList.remove('active'));
    el.classList.add('active');
    const catName = el.querySelector('.cat-name')?.textContent || 'catégorie';
    showToast('Catégorie sélectionnée : ' + catName);
}

// ============================================
// SCROLL ANIMATIONS
// ============================================
const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry, i) => {
        if (entry.isIntersecting) {
            setTimeout(() => {
                entry.target.classList.add('visible');
            }, i * 80);
        }
    });
}, { threshold: 0.1 });

// ============================================
// AUTH FUNCTIONS - APPELS VERS L'API REST
// ============================================

function fakeLogin() {
    const email = document.querySelector('#panel-login input[type="email"]').value;
    const password = document.querySelector('#panel-login input[type="password"]').value;

    if (!email || !password) {
        showToast('Veuillez remplir tous les champs');
        return;
    }

    const loginData = {
        email: email,
        password: password
    };

    console.log('🔐 Tentative de connexion avec:', email);
    showToast('Connexion en cours...');

    fetch('/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(loginData),
        credentials: 'include'
    })
    .then(response => {
        console.log('📡 Statut HTTP:', response.status);
        if (!response.ok) {
            throw new Error(`Erreur HTTP ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('📨 Réponse:', data);
        if (data.success) {
            closeModal();
            showToast('Connexion réussie ! Bienvenue 🐾');
            setTimeout(() => window.location.reload(), 1000);
        } else {
            showToast('Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('❌ Erreur:', error);
        showToast('Erreur de connexion: ' + error.message);
    });
}

function fakeSignup() {
    const firstName = document.querySelector('#panel-signup input[placeholder="Marie"]').value;
    const lastName = document.querySelector('#panel-signup input[placeholder="Dupont"]').value;
    const email = document.querySelector('#panel-signup input[placeholder="vous@exemple.com"]').value;
    const password = document.querySelector('#panel-signup input[placeholder="Au moins 8 caractères"]').value;
    const city = document.querySelector('#panel-signup input[placeholder="Casablanca"]').value;
    const roleSelect = document.querySelector('#panel-signup select');
    let role = 'USER';

    if (roleSelect) {
        const roleValue = roleSelect.value;
        if (roleValue === 'Bénévole / Association') role = 'ASSOCIATION';
        else if (roleValue === 'Vétérinaire') role = 'VETERINAIRE';
        else role = 'USER';
    }

    if (!firstName || !lastName || !email || !password) {
        showToast('Veuillez remplir tous les champs obligatoires');
        return;
    }

    if (password.length < 6) {
        showToast('Le mot de passe doit contenir au moins 6 caractères');
        return;
    }

    const signupData = {
        firstName: firstName,
        lastName: lastName,
        email: email,
        password: password,
        city: city,
        role: role
    };

    console.log('📝 Tentative d\'inscription avec:', email);
    showToast('Inscription en cours...');

    fetch('/api/auth/signup', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(signupData),
        credentials: 'include'
    })
    .then(response => {
        console.log('📡 Statut HTTP:', response.status);
        if (!response.ok) {
            throw new Error(`Erreur HTTP ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('📨 Réponse:', data);
        if (data.success) {
            closeModal();
            showToast('Compte créé avec succès ! Bienvenue dans la famille PawCare 🎉');
            setTimeout(() => window.location.reload(), 1500);
        } else {
            showToast('Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('❌ Erreur:', error);
        showToast('Erreur lors de l\'inscription: ' + error.message);
    });
}

function fakeReport() {
    const reportModal = document.getElementById('reportModal');
    if (reportModal) {
        reportModal.classList.remove('active');
    }
    showToast('Signalement publié ! La communauté va vous aider.');
}

// ============================================
// VÉRIFICATION DE L'ÉTAT DE CONNEXION
// ============================================
function checkAuthStatus() {
    fetch('/api/auth/check', {
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
    .then(data => {
        window.isAuthenticated = data.authenticated;
        if (data.authenticated && data.user) {
            window.currentUser = data.user;
            updateNavbarForLoggedInUser(data.user);
        }
    })
    .catch(error => console.error('Erreur vérification auth:', error));
}

// ✅ MENU UTILISATEUR - UNIQUEMENT DÉCONNEXION
function updateNavbarForLoggedInUser(user) {
    const navActions = document.querySelector('.nav-actions');
    if (navActions) {
        navActions.innerHTML = `
            <div class="profile-menu" id="profileMenu">
                <span class="user-name">${user.firstName} ${user.lastName.substring(0, 1)}.</span>
                <div class="dropdown">
                    <a href="/auth/logout">
                        <span class="dropdown-icon">🔒</span> Déconnexion
                    </a>
                </div>
            </div>
        `;
        setupProfileMenu();
    }
}

// ============================================
// GESTION DU MENU DÉROULANT DU PROFIL
// ============================================
function setupProfileMenu() {
    const profileMenu = document.getElementById('profileMenu');
    if (profileMenu) {
        profileMenu.addEventListener('click', function(e) {
            e.stopPropagation();
            this.classList.toggle('active');
        });

        document.addEventListener('click', function() {
            if (profileMenu) {
                profileMenu.classList.remove('active');
            }
        });
    }
}

// ============================================
// PUBLISH ADOPTION MODAL FUNCTIONS
// ============================================
let selectedImages = [];

function openPublishModal() {
    const publishModal = document.getElementById('publishModal');
    if (publishModal) {
        publishModal.classList.add('active');
        const form = document.getElementById('publishForm');
        if (form) form.reset();
        const preview = document.getElementById('imagePreview');
        if (preview) preview.innerHTML = '';
        selectedImages = [];
    }
}

function closePublishModal() {
    const publishModal = document.getElementById('publishModal');
    if (publishModal) {
        publishModal.classList.remove('active');
    }
}

function setupImageUpload() {
    const uploadArea = document.getElementById('uploadArea');
    const imageUpload = document.getElementById('imageUpload');
    const imagePreview = document.getElementById('imagePreview');

    if (!uploadArea || !imageUpload || !imagePreview) return;

    uploadArea.addEventListener('click', () => {
        imageUpload.click();
    });

    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        handleImageFiles(e.dataTransfer.files);
    });

    imageUpload.addEventListener('change', (e) => {
        handleImageFiles(e.target.files);
    });

    function handleImageFiles(files) {
        for (let file of files) {
            if (!file.type.startsWith('image/')) {
                showToast('Format non supporté : ' + file.name);
                continue;
            }
            if (file.size > 5 * 1024 * 1024) {
                showToast('Fichier trop volumineux : ' + file.name + ' (max 5 Mo)');
                continue;
            }

            selectedImages.push(file);

            const reader = new FileReader();
            reader.onload = (e) => {
                const preview = document.createElement('div');
                preview.className = 'preview-image';
                preview.innerHTML = `
                    <img src="${e.target.result}" alt="Preview">
                    <button class="remove-image" onclick="removeImage(this, '${file.name}')">✕</button>
                `;
                imagePreview.appendChild(preview);
            };
            reader.readAsDataURL(file);
        }
    }
}

function removeImage(button, fileName) {
    const previewDiv = button.parentElement;
    previewDiv.remove();
    selectedImages = selectedImages.filter(img => img.name !== fileName);
}

function publishAdoption(event) {
    event.preventDefault();

    if (!isAuthenticated) {
        closePublishModal();
        openModal('login');
        showToast('Veuillez vous connecter pour publier une annonce');
        return false;
    }

    const form = document.getElementById('publishForm');
    const formData = new FormData(form);

    for (let i = 0; i < selectedImages.length; i++) {
        formData.append('images', selectedImages[i]);
    }

    showToast('Publication en cours...');

    fetch('/adoption/create', {
        method: 'POST',
        body: formData,
        credentials: 'include'
    })
    .then(response => {
        console.log('📡 Statut HTTP:', response.status);
        if (!response.ok) {
            throw new Error('Erreur HTTP: ' + response.status);
        }
        return response.json();
    })
    .then(data => {
        console.log('📨 Réponse:', data);
        if (data.success) {
            closePublishModal();
            showToast('✅ Annonce publiée avec succès !');
            setTimeout(() => window.location.reload(), 1500);
        } else {
            showToast('Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('❌ Erreur détaillée:', error);
        showToast('Erreur lors de la publication: ' + error.message);
    });

    return false;
}

// ============================================
// ANIMAUX PERDUS FUNCTIONS
// ============================================

// ✅ Contacter le propriétaire d'un animal perdu
function contactLostAnimalOwner(postId) {
    if (!isAuthenticated) {
        openModal('login');
        return;
    }

    const message = prompt("Votre message pour le propriétaire :");
    if (!message || message.trim() === "") {
        showToast("Message vide, annulé");
        return;
    }

    showToast("Envoi du message...");

    fetch('/perdus/' + postId + '/contact', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: 'message=' + encodeURIComponent(message),
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast("✅ Message envoyé au propriétaire !");
        } else {
            showToast("❌ Erreur: " + data.message);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast("Erreur lors de l'envoi du message");
    });
}

// ✅ Marquer un animal comme trouvé
function markAnimalAsFound(postId) {
    if (!isAuthenticated) {
        openModal('login');
        return;
    }

    if (confirm("Confirmez-vous que cet animal a été retrouvé ?")) {
        showToast("Mise à jour en cours...");

        fetch('/perdus/' + postId + '/found', {
            method: 'POST',
            credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast("✅ Animal marqué comme trouvé !");
                setTimeout(() => window.location.reload(), 1500);
            } else {
                showToast("❌ Erreur: " + data.message);
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            showToast("Erreur lors de la mise à jour");
        });
    }
}

// ============================================
// SENSIBILISATION FUNCTIONS
// ============================================

// ✅ Liker un article
function likeArticle(articleId) {
    if (!window.isAuthenticated) {
        openModal('login');
        return;
    }

    fetch('/sensibilisation/' + articleId + '/like', {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('❤️ Like ajouté !');
            setTimeout(() => window.location.reload(), 500);
        } else {
            showToast('❌ Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('Erreur lors du like');
    });
}

// ✅ Modifier un article
function editArticle(articleId) {
    window.location.href = '/sensibilisation/' + articleId + '/edit';
}

// ✅ Supprimer un article
function deleteArticle(articleId) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet article ? Cette action est irréversible.')) return;

    showToast('Suppression en cours...');

    fetch('/sensibilisation/' + articleId + '/delete', {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('✅ Article supprimé avec succès !');
            setTimeout(() => window.location.href = '/', 1500);
        } else {
            showToast('❌ Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('Erreur lors de la suppression');
    });
}

// ✅ Changer le statut de publication (publier/dépublier)
function togglePublish(articleId) {
    fetch('/sensibilisation/' + articleId + '/toggle-publish', {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('✅ Statut modifié !');
            setTimeout(() => window.location.reload(), 500);
        } else {
            showToast('❌ Erreur: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        showToast('Erreur lors du changement de statut');
    });
}

// ✅ Rechercher des articles
function searchArticles(query) {
    if (!query || query.trim().length < 2) {
        showToast('Veuillez entrer au moins 2 caractères');
        return;
    }
    window.location.href = '/sensibilisation/search?q=' + encodeURIComponent(query);
}

// ✅ Filtrer par catégorie
function filterByCategory(category) {
    window.location.href = '/sensibilisation?category=' + encodeURIComponent(category);
}

// ❌ FONCTION FAVORIS SUPPRIMÉE
// function toggleFavorite(button, postId) { ... }

// ============================================
// INITIALISATION AU CHARGEMENT
// ============================================
document.addEventListener('DOMContentLoaded', function() {
    console.log('PawCare JavaScript chargé !');

    window.isAuthenticated = false;
    window.currentUser = null;

    checkAuthStatus();

    document.querySelectorAll('.fade-in').forEach(el => observer.observe(el));

    setupImageUpload();

    // Initialiser le menu profil s'il existe déjà dans le HTML
    setupProfileMenu();

    const authModal = document.getElementById('authModal');
    if (authModal) {
        authModal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal();
            }
        });
    }

    const reportModal = document.getElementById('reportModal');
    if (reportModal) {
        reportModal.addEventListener('click', function(e) {
            if (e.target === this) {
                this.classList.remove('active');
            }
        });
    }

    const publishModal = document.getElementById('publishModal');
    if (publishModal) {
        publishModal.addEventListener('click', function(e) {
            if (e.target === this) {
                closePublishModal();
            }
        });
    }

    window.addEventListener('scroll', updateActiveTab, { passive: true });
    updateActiveTab();

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeModal();
            const reportModal = document.getElementById('reportModal');
            if (reportModal && reportModal.classList.contains('active')) {
                reportModal.classList.remove('active');
            }
            const publishModal = document.getElementById('publishModal');
            if (publishModal && publishModal.classList.contains('active')) {
                closePublishModal();
            }
        }
    });

    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            setFilter(this);
        });
    });

    document.querySelectorAll('.forum-category').forEach(cat => {
        cat.addEventListener('click', function(e) {
            setCat(this);
        });
    });
});

// ============================================
// FONCTIONS UTILITAIRES SUPPLÉMENTAIRES
// ============================================

function openReportModal() {
    const reportModal = document.getElementById('reportModal');
    if (reportModal) {
        reportModal.classList.add('active');
    }
}

function closeReportModal() {
    const reportModal = document.getElementById('reportModal');
    if (reportModal) {
        reportModal.classList.remove('active');
    }
}

function openAuthModal(tab) {
    openModal(tab);
}

function loadMore(endpoint, containerId) {
    fetch(endpoint)
        .then(response => response.text())
        .then(html => {
            const container = document.getElementById(containerId);
            if (container) {
                container.insertAdjacentHTML('beforeend', html);
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            showToast('Erreur de chargement');
        });
}

// ============================================
// EXPORT DES FONCTIONS GLOBALES
// ============================================
window.openModal = openModal;
window.closeModal = closeModal;
window.switchTab = switchTab;
window.showToast = showToast;
window.scrollToSection = scrollToSection;
window.setFilter = setFilter;
window.setCat = setCat;
window.fakeLogin = fakeLogin;
window.fakeSignup = fakeSignup;
window.fakeReport = fakeReport;
window.openReportModal = openReportModal;
window.closeReportModal = closeReportModal;
window.openAuthModal = openAuthModal;
window.loadMore = loadMore;
window.openPublishModal = openPublishModal;
window.closePublishModal = closePublishModal;
window.publishAdoption = publishAdoption;
// ❌ window.toggleFavorite SUPPRIMÉ
window.checkAuthStatus = checkAuthStatus;

// ✅ EXPORTS ANIMAUX PERDUS
window.contactLostAnimalOwner = contactLostAnimalOwner;
window.markAnimalAsFound = markAnimalAsFound;

// ✅ EXPORTS SENSIBILISATION
window.likeArticle = likeArticle;
window.editArticle = editArticle;
window.deleteArticle = deleteArticle;
window.togglePublish = togglePublish;
window.searchArticles = searchArticles;
window.filterByCategory = filterByCategory;
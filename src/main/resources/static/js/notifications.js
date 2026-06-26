// Vérifier les nouvelles notifications
function checkNotifications() {
    if (!isAuthenticated) return;

    fetch('/notifications/unread')
        .then(response => response.json())
        .then(notifications => {
            updateNotificationBadge(notifications.length);
            if (notifications.length > 0) {
                showNotificationPreview(notifications[0]);
            }
        });
}

// Mettre à jour le badge de notification
function updateNotificationBadge(count) {
    const badge = document.querySelector('.notification-badge .badge');
    if (badge) {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'inline' : 'none';
    }
}

// Afficher un aperçu de notification
function showNotificationPreview(notification) {
    showToast(notification.message);
}

// Marquer une notification comme lue
function markAsRead(notificationId) {
    fetch('/notifications/' + notificationId + '/read', {
        method: 'POST'
    })
        .then(() => {
            const notificationElement = event.currentTarget.closest('.notification-item');
            notificationElement.classList.remove('unread');
            event.currentTarget.remove();

            // Mettre à jour le badge
            const currentCount = parseInt(document.querySelector('.notification-badge .badge').textContent);
            updateNotificationBadge(currentCount - 1);
        });
}

// Marquer toutes les notifications comme lues
function markAllAsRead() {
    fetch('/notifications/read-all', {
        method: 'POST'
    })
        .then(() => {
            document.querySelectorAll('.notification-item.unread').forEach(item => {
                item.classList.remove('unread');
            });
            document.querySelectorAll('.notification-item .btn-outline').forEach(btn => {
                btn.remove();
            });
            updateNotificationBadge(0);
            showToast('Toutes les notifications ont été marquées comme lues');
        });
}

// Charger les notifications (pour la page notifications)
function loadNotifications() {
    fetch('/notifications')
        .then(response => response.json())
        .then(notifications => {
            const container = document.getElementById('notifications-container');
            if (container) {
                container.innerHTML = '';
                notifications.forEach(notif => {
                    container.appendChild(createNotificationElement(notif));
                });
            }
        });
}

// Créer un élément de notification
function createNotificationElement(notification) {
    const div = document.createElement('div');
    div.className = 'notification-item' + (notification.isRead ? '' : ' unread');

    div.innerHTML = `
        <span class="avatar av${(notification.id % 4) + 1}">${notification.type.charAt(0)}</span>
        <div style="flex:1;">
            <h4>${notification.title}</h4>
            <p>${notification.message}</p>
            <small>${notification.timeAgo || notification.createdAt}</small>
        </div>
        ${!notification.isRead ?
        '<button class="btn btn-outline" style="padding:5px 15px;" onclick="markAsRead(' + notification.id + ')">Marquer lu</button>'
        : ''}
    `;

    return div;
}

// Rafraîchir les notifications toutes les 30 secondes
if (isAuthenticated) {
    setInterval(checkNotifications, 30000);
}

// Initialiser au chargement
document.addEventListener('DOMContentLoaded', function() {
    if (isAuthenticated) {
        checkNotifications();
    }
});
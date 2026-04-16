// ─── CONFIG ───────────────────────────────────────────────
const API_BASE = 'http://localhost:8080';

// ─── TOKEN HELPERS ────────────────────────────────────────
const getToken = () => localStorage.getItem('rm_token');
const setToken = (t) => localStorage.setItem('rm_token', t);
const clearToken = () => localStorage.removeItem('rm_token');

// ─── REDIRECT GUARDS ──────────────────────────────────────
// Call on protected pages: sends to login if no token
function requireAuth() {
  if (!getToken()) {
    window.location.href = 'index.html';
  }
}

// Call on login page: sends to profile if already logged in
function redirectIfLoggedIn() {
  if (getToken()) {
    window.location.href = 'profile.html';
  }
}

// ─── AUTH HEADERS ─────────────────────────────────────────
function authHeaders() {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${getToken()}`
  };
}

// ─── ALERT HELPER ─────────────────────────────────────────
// alertEl: DOM element with class .alert
// type: 'success' | 'error'
function showAlert(alertEl, type, message) {
  alertEl.className = `alert alert-${type} show`;
  alertEl.innerHTML = `<span>${type === 'success' ? '✅' : '❌'}</span><span>${message}</span>`;
  // Auto-hide after 5 s
  setTimeout(() => alertEl.classList.remove('show'), 5000);
}

// ─── SCORE COLOUR HELPER ──────────────────────────────────
function scoreBadgeClass(score) {
  if (score >= 70) return 'high';
  if (score >= 40) return 'mid';
  return 'low';
}

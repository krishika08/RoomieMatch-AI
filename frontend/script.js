/* ═══════════════════════════════════════════════════
   RoomieMatch AI — script.js
   Backend API contracts:
     POST /auth/signup  → ApiResponse<UserResponseDTO>
     POST /auth/login   → ApiResponse<LoginResponseDTO>  { token, userId, email }
     GET  /profile      → ApiResponse<StudentProfileResponseDTO>
     POST /profile      → ApiResponse<StudentProfileResponseDTO>
     PUT  /profile      → ApiResponse<StudentProfileResponseDTO>
     GET  /matches      → ApiResponse<List<MatchResponseDTO>>
     POST /requests/send     { receiverId }
     GET  /requests/incoming → ApiResponse<List<RoommateRequestResponseDTO>>
     GET  /requests/sent     → ApiResponse<List<RoommateRequestResponseDTO>>
     PUT  /requests/respond  { requestId, status }
   ═══════════════════════════════════════════════════ */

const API = 'http://localhost:8080';

/* ── AOS ──────────────────────────────────────────── */
if (typeof AOS !== 'undefined') {
  AOS.init({ once: true, duration: 450, easing: 'ease-out-quad', offset: 40 });
}

/* ── Storage helpers ──────────────────────────────── */
const getToken     = ()  => localStorage.getItem('jwt');
const setToken     = (t) => localStorage.setItem('jwt', t);
const clearToken   = ()  => localStorage.removeItem('jwt');
const getUserEmail = ()  => localStorage.getItem('userEmail') || '';
const getUserRole  = ()  => localStorage.getItem('userRole') || 'STUDENT';

/* ── Toast ────────────────────────────────────────── */
function toast(msg, type = 'success') {
  let stack = document.querySelector('.toast-stack');
  if (!stack) {
    stack = document.createElement('div');
    stack.className = 'toast-stack';
    document.body.appendChild(stack);
  }
  const icons = { success: 'fa-circle-check', error: 'fa-circle-exclamation', info: 'fa-circle-info' };
  const el = document.createElement('div');
  el.className = `toast-item ${type}`;
  el.innerHTML = `<i class="fa-solid ${icons[type] || icons.info} fa-sm"></i><span>${msg}</span>`;
  stack.appendChild(el);
  setTimeout(() => el.remove(), 3800);
}

/* ── API helper ───────────────────────────────────── */
async function api(endpoint, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };
  try {
    const res  = await fetch(`${API}${endpoint}`, { ...options, headers });
    if (res.status === 401) {
      clearToken();
      window.location.href = 'index.html';
      throw new Error('Session expired');
    }
    const ct   = res.headers.get('content-type') || '';
    const body = ct.includes('application/json') ? await res.json() : await res.text();
    if (!res.ok) {
      const msg = (body && body.message) ? body.message
                : (typeof body === 'string' && body) ? body
                : 'Something went wrong';
      throw new Error(msg);
    }
    return body;  // always the full ApiResponse object { success, message, data }
  } catch (err) {
    if (!err.message.includes('Session expired')) toast(err.message, 'error');
    throw err;
  }
}

/* ── Auth guard ───────────────────────────────────── */
function checkAuth() {
  const path     = window.location.pathname;
  const isPublic = ['index.html', 'signup.html'].some(p => path.endsWith(p)) || path.endsWith('/');
  const token    = getToken();
  if (!token && !isPublic) { window.location.href = 'index.html'; return false; }
  if (token  && isPublic)  {
    const role = getUserRole();
    if (role === 'MANAGER') {
      window.location.href = 'manager.html';
    } else if (role === 'WARDEN') {
      window.location.href = 'admin.html';
    } else {
      window.location.href = 'dashboard.html';
    }
    return false;
  }
  return true;
}

function logout() {
  clearToken();
  localStorage.removeItem('userId');
  localStorage.removeItem('userEmail');
  localStorage.removeItem('userRole');
  window.location.href = 'index.html';
}

/* ── Sidebar ──────────────────────────────────────── */
function initSidebar() {
  document.getElementById('logoutBtn')?.addEventListener('click', e => { e.preventDefault(); logout(); });
  const email    = getUserEmail();
  const initials = email ? email.substring(0, 2).toUpperCase() : 'U';
  const av = document.getElementById('sidebarAvatar');
  const em = document.getElementById('sidebarEmail');
  if (av) av.textContent = initials;
  if (em) em.textContent = email || 'user@app.com';

  // Show admin/manager link in sidebar for admin users
  const role = getUserRole();
  const isAdminPage = window.location.pathname.endsWith('admin.html') || window.location.pathname.endsWith('manager.html');
  if ((role === 'MANAGER' || role === 'WARDEN') && !isAdminPage) {
    const nav = document.querySelector('.sidebar-nav');
    const divider = document.querySelector('.nav-divider');
    if (nav && divider) {
      const adminLink = document.createElement('a');
      adminLink.href = role === 'MANAGER' ? 'manager.html' : 'admin.html';
      adminLink.className = 'nav-item';
      adminLink.innerHTML = role === 'MANAGER'
        ? '<i class="fa-solid fa-crown"></i> Manager Panel'
        : '<i class="fa-solid fa-shield-halved"></i> Warden Panel';
      adminLink.style.color = '#ef4444';
      nav.insertBefore(adminLink, divider);
    }
  }
}

/* ── Button loading ───────────────────────────────── */
function setBtnLoading(btn, loading) {
  if (!btn) return;
  btn.disabled = loading;
  if (loading) {
    btn.dataset.orig = btn.innerHTML;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> Loading…`;
  } else {
    btn.innerHTML = btn.dataset.orig || 'Submit';
  }
}

/* ── Boot ─────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  if (!checkAuth()) return;
  if (typeof AOS !== 'undefined') AOS.refresh();
  initSidebar();
  initLoginPage();
  initSignupPage();
  initDashboardPage();
  initProfilePage();
  initMatchesPage();
  initRequestsPage();
});

/* ════════════════════════════════════════════════════
   PAGE: Login
   ════════════════════════════════════════════════════ */
function initLoginPage() {
  const form = document.getElementById('loginForm');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn      = document.getElementById('loginBtn');
    const email    = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    if (!email || !password) { toast('Please fill in all fields.', 'error'); return; }

    setBtnLoading(btn, true);
    try {
      // Response: ApiResponse<LoginResponseDTO>  → { success, message, data: { token, userId, email, role } }
      const res = await api('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) });
      const d   = res.data;
      if (d && d.token) {
        setToken(d.token);
        localStorage.setItem('userId', d.userId);
        localStorage.setItem('userEmail', d.email);
        localStorage.setItem('userRole', d.role || 'STUDENT');
        if (d.hostel) localStorage.setItem('userHostel', d.hostel);
        toast('Welcome back, ' + d.email + '!');
        let dest = 'dashboard.html';
        if (d.role === 'MANAGER') dest = 'manager.html';
        else if (d.role === 'WARDEN') dest = 'admin.html';
        setTimeout(() => window.location.href = dest, 700);
      } else {
        throw new Error('Unexpected response from server.');
      }
    } catch (_) {
      setBtnLoading(btn, false);
    }
  });
}

/* ════════════════════════════════════════════════════
   PAGE: Signup
   ════════════════════════════════════════════════════ */
function initSignupPage() {
  const form = document.getElementById('signupForm');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn          = document.getElementById('signupBtn');
    const email        = document.getElementById('regEmail').value.trim();
    const password     = document.getElementById('regPassword').value;
    const organization = document.getElementById('regOrganization')?.value || '';
    const hostel       = document.getElementById('regHostel')?.value || '';

    if (!email || !password) { toast('Please fill in all fields.', 'error'); return; }
    if (password.length < 8) { toast('Password must be at least 8 characters.', 'error'); return; }
    if (!organization) { toast('Please select your organization.', 'error'); return; }
    if (!hostel) { toast('Please select your hostel.', 'error'); return; }

    setBtnLoading(btn, true);
    try {
      await api('/auth/signup', { method: 'POST', body: JSON.stringify({ email, password, organization, hostel }) });
      toast('Account created! Please sign in.');
      setTimeout(() => window.location.href = 'index.html', 1000);
    } catch (_) {
      setBtnLoading(btn, false);
    }
  });
}

/* ════════════════════════════════════════════════════
   PAGE: Dashboard
   ════════════════════════════════════════════════════ */
function initDashboardPage() {
  const el = document.getElementById('dashEmail');
  if (!el) return;
  el.textContent = getUserEmail() || 'there';
}

/* ════════════════════════════════════════════════════
   PAGE: Profile
   ════════════════════════════════════════════════════ */
function initProfilePage() {
  const form = document.getElementById('profileForm');
  if (!form) return;

  const FIELDS = ['sleepSchedule', 'cleanlinessLevel', 'noiseTolerance',
                  'socialLevel', 'studyHabits', 'guestFrequency', 'roomTemperature'];
  let profileExists = false;

  const statusEl = document.getElementById('profileStatus');
  if (statusEl) statusEl.textContent = 'Loading your preferences…';

  // Load existing profile
  (async () => {
    try {
      const res     = await api('/profile');
      const profile = res.data;
      if (profile && typeof profile === 'object') {
        profileExists = true;
        FIELDS.forEach(f => {
          const el = document.getElementById(f);
          if (el && profile[f]) el.value = profile[f];
        });
        if (statusEl) {
          statusEl.innerHTML = `<i class="fa-solid fa-circle-check" style="color:var(--success);"></i> Preferences loaded`;
        }
      }
    } catch (_) {
      if (statusEl) statusEl.textContent = 'No profile yet — fill in your preferences below.';
    }
  })();

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = document.getElementById('saveProfileBtn');

    // Validate all fields filled
    const payload = {};
    let missing = false;
    FIELDS.forEach(f => {
      const val = document.getElementById(f)?.value;
      if (!val) missing = true;
      payload[f] = val;
    });
    if (missing) { toast('Please fill in all preferences.', 'error'); return; }

    setBtnLoading(btn, true);
    try {
      const method = profileExists ? 'PUT' : 'POST';
      await api('/profile', { method, body: JSON.stringify(payload) });
      profileExists = true;
      toast('Preferences saved successfully!');
      if (statusEl) {
        statusEl.innerHTML = `<i class="fa-solid fa-circle-check" style="color:var(--success);"></i> Preferences saved`;
      }
    } catch (_) {
      // error already shown by api()
    } finally {
      setBtnLoading(btn, false);
    }
  });
}

/* ════════════════════════════════════════════════════
   PAGE: Matches
   ════════════════════════════════════════════════════ */
function initMatchesPage() {
  if (!document.getElementById('matchesContainer')) return;
  loadMatches();
  document.getElementById('refreshMatchesBtn')?.addEventListener('click', loadMatches);
}

async function loadMatches() {
  const c = document.getElementById('matchesContainer');
  if (!c) return;
  c.innerHTML = `<div class="col-12"><div class="spinner-wrap"><div class="spinner-ring"></div></div></div>`;
  try {
    const res     = await api('/matches');
    const matches = res.data;

    if (!Array.isArray(matches) || !matches.length) {
      c.innerHTML = emptyCard('No Matches Found',
        'Complete your profile so the AI engine can find compatible roommates for you.');
      return;
    }

    c.innerHTML = matches.map((m, i) => {
      const score = m.compatibilityScore || 0;
      const sc    = score >= 80 ? 'score-high' : score >= 50 ? 'score-medium' : 'score-low';
      const slbl  = score >= 80 ? 'High match'  : score >= 50 ? 'Good match'   : 'Fair match';
      const fc    = score >= 80 ? 'fill-high'   : score >= 50 ? 'fill-medium'  : 'fill-low';
      const init  = (m.email || 'U').substring(0, 2).toUpperCase();
      const delay = (i % 6) * 70;
      return `
      <div class="col-md-6 col-xl-4" data-aos="fade-up" data-aos-delay="${delay}">
        <div class="card match-card h-100">
          <div class="match-header">
            <div class="match-user">
              <div class="match-avatar">${init}</div>
              <div>
                <div class="match-name">${m.email || 'User #' + m.userId}</div>
                <div class="match-id">ID: ${m.userId}</div>
              </div>
            </div>
            <span class="score-pill ${sc}">
              <i class="fa-solid fa-star fa-xs"></i> ${score}%
            </span>
          </div>
          <div class="divider"></div>
          <div style="padding:12px 18px 4px;">
            <div style="font-size:11px;color:var(--text-3);margin-bottom:4px;">Compatibility</div>
            <div class="score-bar-wrap">
              <div class="score-bar-bg"><div class="score-bar-fill ${fc}" style="width:${score}%"></div></div>
            </div>
            <div style="font-size:11.5px;color:var(--text-3);text-align:right;margin-top:2px;">${slbl}</div>
          </div>
          <div class="divider" style="margin-top:8px;"></div>
          <div class="traits">
            ${traitRow('Sleep', m.sleepSchedule, m.breakdown, 'sleepSchedule', 20)}
            ${traitRow('Cleanliness', m.cleanlinessLevel, m.breakdown, 'cleanliness', 20)}
            ${traitRow('Noise', m.noiseTolerance, m.breakdown, 'noiseTolerance', 15)}
            ${traitRow('Social', m.socialLevel, m.breakdown, 'socialLevel', 15)}
            ${traitRow('Study', m.studyHabits, m.breakdown, 'studyHabits', 15)}
            ${traitRow('Guests', m.guestFrequency, m.breakdown, 'guestFrequency', 10)}
            ${traitRow('Temp', m.roomTemperature, m.breakdown, 'roomTemperature', 5)}
          </div>
          <div class="divider"></div>
          <div style="padding:14px 18px 18px;">
            <button id="req-btn-${m.userId}" class="btn btn-primary btn-sm w-full"
                    onclick="sendRequest(${m.userId}, this)">
              <i class="fa-solid fa-paper-plane fa-xs"></i> Send Request
            </button>
          </div>
        </div>
      </div>`;
    }).join('');

    if (typeof AOS !== 'undefined') AOS.refresh();
  } catch (_) {
    c.innerHTML = emptyCard('Could Not Load Matches',
      'Make sure your profile is saved and the backend is running.');
  }
}

window.sendRequest = async (receiverId, btn) => {
  const orig = btn?.innerHTML;
  if (btn) { btn.disabled = true; btn.innerHTML = `<span class="spinner-border spinner-border-sm"></span>`; }
  try {
    // POST /requests/send  body: { receiverId }
    await api('/requests/send', { method: 'POST', body: JSON.stringify({ receiverId }) });
    toast('Roommate request sent!');
    if (btn) { btn.innerHTML = `<i class="fa-solid fa-check fa-xs"></i> Sent`; btn.style.background = 'var(--success)'; }
  } catch (_) {
    if (btn) { btn.disabled = false; btn.innerHTML = orig; }
  }
};

/* ════════════════════════════════════════════════════
   PAGE: Requests
   ════════════════════════════════════════════════════ */
function initRequestsPage() {
  if (!document.getElementById('requestsContainer')) return;

  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      btn.dataset.tab === 'incoming' ? loadIncoming() : loadSent();
    });
  });

  loadIncoming();
}

async function loadIncoming() {
  const c = document.getElementById('requestsContainer');
  if (!c) return;
  c.innerHTML = `<div class="spinner-wrap"><div class="spinner-ring"></div></div>`;
  try {
    const res  = await api('/requests/incoming');
    const reqs = res.data;

    if (!Array.isArray(reqs) || !reqs.length) {
      c.innerHTML = emptyCard('No Incoming Requests', 'When someone sends you a request, it will appear here.');
      return;
    }

    c.innerHTML = reqs.map((r, i) => {
      const init = (r.senderEmail || 'U').substring(0, 2).toUpperCase();
      const bc   = badgeClass(r.status);
      const pend = r.status === 'PENDING';
      return `
      <div class="req-card" data-aos="fade-up" data-aos-delay="${i * 60}">
        <div class="req-left">
          <div class="req-avatar">${init}</div>
          <div>
            <div class="req-name">${r.senderEmail || 'User #' + r.senderId}</div>
            <div class="req-meta">Request #${r.id} &nbsp;·&nbsp; <span class="badge ${bc}">${r.status}</span></div>
          </div>
        </div>
        ${pend ? `
        <div class="req-actions">
          <button class="btn btn-success btn-sm" onclick="respond(${r.id}, 'ACCEPTED', this)">
            <i class="fa-solid fa-check fa-xs"></i> Accept
          </button>
          <button class="btn btn-danger btn-sm" onclick="respond(${r.id}, 'REJECTED', this)">
            <i class="fa-solid fa-xmark fa-xs"></i> Decline
          </button>
        </div>` : ''}
      </div>`;
    }).join('');

    if (typeof AOS !== 'undefined') AOS.refresh();
  } catch (_) {
    c.innerHTML = emptyCard('Could Not Load', 'Ensure the backend is running.');
  }
}

async function loadSent() {
  const c = document.getElementById('requestsContainer');
  if (!c) return;
  c.innerHTML = `<div class="spinner-wrap"><div class="spinner-ring"></div></div>`;
  try {
    const res  = await api('/requests/sent');
    const reqs = res.data;

    if (!Array.isArray(reqs) || !reqs.length) {
      c.innerHTML = emptyCard('No Sent Requests', "You haven't sent any requests yet. Browse your matches!");
      return;
    }

    c.innerHTML = reqs.map((r, i) => {
      const init = (r.receiverEmail || 'U').substring(0, 2).toUpperCase();
      const bc   = badgeClass(r.status);
      return `
      <div class="req-card" data-aos="fade-up" data-aos-delay="${i * 60}">
        <div class="req-left">
          <div class="req-avatar" style="background:var(--primary-light);color:var(--primary);border-color:rgba(99,102,241,0.3);">${init}</div>
          <div>
            <div class="req-name">To: ${r.receiverEmail || 'User #' + r.receiverId}</div>
            <div class="req-meta">Request #${r.id} &nbsp;·&nbsp; <span class="badge ${bc}">${r.status}</span></div>
          </div>
        </div>
      </div>`;
    }).join('');

    if (typeof AOS !== 'undefined') AOS.refresh();
  } catch (_) {
    c.innerHTML = emptyCard('Could Not Load', 'Ensure the backend is running.');
  }
}

// PUT /requests/respond  body: { requestId, status }
window.respond = async (requestId, status, btn) => {
  if (btn) { btn.disabled = true; }
  try {
    await api('/requests/respond', {
      method: 'PUT',
      body: JSON.stringify({ requestId, status })
    });
    toast(
      status === 'ACCEPTED' ? 'Request accepted!' : 'Request declined.',
      status === 'ACCEPTED' ? 'success' : 'info'
    );
    loadIncoming();
  } catch (_) {
    if (btn) { btn.disabled = false; }
  }
};

/* ── Helpers ──────────────────────────────────────── */
/** Renders a single trait row with preference value + per-trait score mini-bar */
function traitRow(label, value, breakdown, key, max) {
  const earned = (breakdown && breakdown[key] != null) ? breakdown[key] : null;
  const pct = earned !== null ? Math.round((earned / max) * 100) : null;
  const barColor = pct === null ? '' : pct >= 80 ? 'var(--success)' : pct >= 50 ? '#f59e0b' : 'var(--error)';
  const scoreHtml = pct !== null
    ? `<div style="display:flex;align-items:center;gap:5px;margin-top:2px;">
         <div style="flex:1;height:3px;background:var(--border);border-radius:2px;overflow:hidden;">
           <div style="width:${pct}%;height:100%;background:${barColor};border-radius:2px;"></div>
         </div>
         <span style="font-size:10px;color:var(--text-3);min-width:28px;text-align:right;">${earned}/${max}</span>
       </div>`
    : '';
  return `<div class="trait"><div class="t-label">${label}</div><div class="t-value">${fmt(value)}${scoreHtml}</div></div>`;
}

function fmt(val) {
  if (!val) return '—';
  return val.replace(/_/g, ' ').toLowerCase().replace(/^\w/, c => c.toUpperCase());
}

function badgeClass(status) {
  if (status === 'ACCEPTED') return 'badge-accepted';
  if (status === 'REJECTED') return 'badge-rejected';
  return 'badge-pending';
}

function emptyCard(title, desc) {
  return `
    <div class="card">
      <div class="empty-state">
        <div class="ei"><i class="fa-regular fa-folder-open"></i></div>
        <h4>${title}</h4>
        <p>${desc}</p>
      </div>
    </div>`;
}

/**
 * NovaPay Mobile Banking – Frontend Application
 * Single-page app consuming the Spring Boot REST API.
 */

const API = '/api';

// ── State ────────────────────────────────────────────────────────
const State = {
  userId:    null,
  fullName:  null,
  tier:      null,
  accountNumber: null,
  lastTransfer: null,
  currentPage: 'screen-home',
};

// ── App Controller ───────────────────────────────────────────────
const App = {

  // ── Boot ──────────────────────────────────────────────────────
  init() {
    document.getElementById('btn-login').addEventListener('click', App.login);
    document.getElementById('inp-pass').addEventListener('keydown', e => {
      if (e.key === 'Enter') App.login();
    });
    document.getElementById('btn-biometric').addEventListener('click', App.biometricLogin);
    document.getElementById('btn-logout').addEventListener('click', App.logout);
    document.getElementById('btn-transfer').addEventListener('click', App.submitTransfer);
    document.getElementById('btn-view-slip').addEventListener('click', App.viewSlip);
    document.getElementById('btn-register-device').addEventListener('click', App.registerDevice);
    document.getElementById('btn-back').addEventListener('click', App.goBack);
    document.getElementById('btn-notif').addEventListener('click', () => App.goPage('screen-notif'));
    document.getElementById('btn-avatar').addEventListener('click', () => App.goPage('screen-profile'));

    // Bottom nav
    document.querySelectorAll('.nav-item[data-target]').forEach(btn => {
      btn.addEventListener('click', () => App.goPage(btn.dataset.target));
    });

    // Pre-fill demo credentials
    document.getElementById('inp-user').value = 'alice';
    document.getElementById('inp-pass').value = 'pass123';
  },

  // ── Auth ──────────────────────────────────────────────────────
  async login() {
    const btn  = document.getElementById('btn-login');
    const user = document.getElementById('inp-user').value.trim();
    const pass = document.getElementById('inp-pass').value;
    const errEl = document.getElementById('login-error');

    errEl.classList.add('hidden');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span>';

    try {
      const resp = await fetch(`${API}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: user, password: pass }),
      });
      const data = await resp.json();

      if (!data.success) {
        errEl.textContent = data.message || 'Login failed.';
        errEl.classList.remove('hidden');
        return;
      }

      State.userId       = data.userId;
      State.fullName     = data.fullName;
      State.tier         = data.tier;
      State.accountNumber = data.accountNumber;

      App.enterApp();
    } catch (e) {
      errEl.textContent = 'Connection error. Is the server running?';
      errEl.classList.remove('hidden');
    } finally {
      btn.disabled = false;
      btn.innerHTML = 'Sign in';
    }
  },

  biometricLogin() {
    App.showToast('Authenticating with Face ID…');
    setTimeout(() => {
      document.getElementById('inp-user').value = 'alice';
      document.getElementById('inp-pass').value = 'pass123';
      App.login();
    }, 900);
  },

  logout() {
    State.userId = null;
    document.getElementById('app-shell').classList.add('hidden');
    document.getElementById('screen-login').classList.add('active');
    document.getElementById('inp-pass').value = '';
  },

  // ── App Shell ─────────────────────────────────────────────────
  enterApp() {
    document.getElementById('screen-login').classList.remove('active');
    document.getElementById('app-shell').classList.remove('hidden');

    // Set avatar
    const initials = App.getInitials(State.fullName);
    document.getElementById('avatar-initials').textContent = initials;
    document.getElementById('greet-avatar').textContent    = initials[0] || 'A';
    document.getElementById('greet-name').textContent      = State.fullName ? State.fullName.split(' ')[0] : 'there';

    App.goPage('screen-home');
    App.loadDashboard();
    App.loadNotifications();
  },

  getInitials(name) {
    if (!name) return 'AL';
    const parts = name.split(' ');
    return (parts[0][0] + (parts[1] ? parts[1][0] : '')).toUpperCase();
  },

  // ── Navigation ────────────────────────────────────────────────
  goPage(targetId) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    const target = document.getElementById(targetId);
    if (target) target.classList.add('active');

    State.currentPage = targetId;

    // Update bottom nav
    document.querySelectorAll('.nav-item[data-target]').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.target === targetId);
    });

    // Top bar
    const titles = {
      'screen-home':     'NovaPay',
      'screen-transfer': 'Send Money',
      'screen-success':  'Transfer Sent',
      'screen-slip':     'E-Slip',
      'screen-cards':    'My Accounts',
      'screen-profile':  'Profile',
      'screen-devices':  'Devices',
      'screen-notif':    'Notifications',
    };
    document.getElementById('top-bar-title').textContent = titles[targetId] || 'NovaPay';

    // Back button
    const showBack = !['screen-home', 'screen-cards', 'screen-notif', 'screen-profile'].includes(targetId);
    document.getElementById('btn-back').style.display = showBack ? 'flex' : 'none';

    // Page-specific loaders
    if (targetId === 'screen-devices')  App.loadDevices();
    if (targetId === 'screen-profile')  App.loadProfile();
  },

  goHome() {
    App.goPage('screen-home');
  },

  goTransfer() {
    App.goPage('screen-transfer');
  },

  goBack() {
    const prev = {
      'screen-transfer': 'screen-home',
      'screen-success':  'screen-home',
      'screen-slip':     'screen-success',
      'screen-devices':  'screen-profile',
    };
    const to = prev[State.currentPage] || 'screen-home';
    App.goPage(to);
  },

  // ── Dashboard ─────────────────────────────────────────────────
  async loadDashboard() {
    try {
      const resp = await fetch(`${API}/dashboard/${State.userId}`);
      const data = await resp.json();
      const profile = data.profile;

      // Update balances
      if (profile.currentBalance != null) {
        const cur = App.fmtMoney(profile.currentBalance);
        const sav = App.fmtMoney(profile.savingsBalance || 0);
        document.getElementById('balance-main').textContent  = cur;
        document.getElementById('balance-acct').textContent  = (profile.accountNumber || '') + ' · Current';
        document.getElementById('chip-current').textContent  = cur;
        document.getElementById('chip-savings').textContent  = sav;
        document.getElementById('cards-bal-1').textContent   = cur;
        document.getElementById('cards-bal-2').textContent   = sav;
        document.getElementById('cards-acct-1').textContent  = profile.accountNumber || '';
        document.getElementById('cards-holder').textContent  = profile.fullName || '';
        document.getElementById('cards-card-last4').textContent = profile.cardLast4 || '0000';
      }

      // Recent transactions
      App.renderTransactions(data.recentTransactions || []);

    } catch (e) {
      console.warn('Dashboard load error', e);
    }
  },

  renderTransactions(txns) {
    const list = document.getElementById('txn-list');
    if (!txns.length) {
      list.innerHTML = '<div style="text-align:center;color:var(--text3);padding:24px;font-size:13px;">No recent activity</div>';
      return;
    }
    const icons = {
      'Entertainment': '🎬', 'Income': '💰', 'Food & Drink': '☕',
      'Shopping': '🛍', 'Transfer': '↗', 'Utilities': '⚡',
    };
    list.innerHTML = txns.map(t => {
      const isCredit  = t.type === 'credit';
      const iconClass = isCredit ? 'icon-credit' : 'icon-debit';
      const amtClass  = isCredit ? 'positive' : 'negative';
      const icon      = icons[t.category] || '💳';
      return `
        <div class="txn-item">
          <div class="txn-icon ${iconClass}">${icon}</div>
          <div class="txn-desc">
            <div class="txn-name">${t.description}</div>
            <div class="txn-cat">${t.category} · ${t.date}</div>
          </div>
          <div class="txn-right">
            <div class="txn-amount ${amtClass}">${t.amount}</div>
          </div>
        </div>`;
    }).join('');
  },

  // ── Transfer ──────────────────────────────────────────────────
  setAmount(v) {
    document.getElementById('tf-amount').value = v;
  },

  async submitTransfer() {
    const btn    = document.getElementById('btn-transfer');
    const from   = document.getElementById('tf-from').value;
    const to     = document.getElementById('tf-to').value.trim();
    const amount = document.getElementById('tf-amount').value;
    const note   = document.getElementById('tf-note').value.trim();

    if (!to || !amount || parseFloat(amount) <= 0) {
      App.showToast('Please fill in all required fields.');
      return;
    }

    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Processing…';

    try {
      const resp = await fetch(`${API}/transfer`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fromAccount: from, toAccount: to, amount, note, userId: State.userId }),
      });
      const data = await resp.json();

      if (data.status !== 'SUCCESS') throw new Error('Transfer failed');

      State.lastTransfer = { ...data, note };

      // Populate success screen
      document.getElementById('suc-ref').textContent    = data.reference;
      document.getElementById('suc-to').textContent     = data.toAccount;
      document.getElementById('suc-amount').textContent = '$' + parseFloat(amount).toFixed(2);
      document.getElementById('suc-time').textContent   = data.timestamp;

      App.goPage('screen-success');
    } catch (e) {
      App.showToast('Transfer failed. Please try again.');
    } finally {
      btn.disabled = false;
      btn.innerHTML = 'Review Transfer';
    }
  },

  // ── E-Slip ────────────────────────────────────────────────────
  async viewSlip() {
    const t = State.lastTransfer;
    if (!t) { App.showToast('No transfer data.'); return; }

    try {
      const resp = await fetch(`${API}/slip`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          reference: t.reference, from: t.fromAccount, to: t.toAccount,
          amount: t.amount, timestamp: t.timestamp, note: t.note || '',
        }),
      });
      const slip = await resp.json();

      document.getElementById('slip-ref').textContent   = slip.reference;
      document.getElementById('slip-time').textContent  = slip.timestamp;
      document.getElementById('slip-from').textContent  = slip.from;
      document.getElementById('slip-to').textContent    = slip.to;
      document.getElementById('slip-amount').textContent = '$' + parseFloat(slip.amount).toFixed(2);
      document.getElementById('slip-total').textContent  = '$' + parseFloat(slip.amount).toFixed(2);
      document.getElementById('slip-note').textContent   = slip.note || '—';
      document.getElementById('slip-bank').textContent   = slip.bankStamp || 'NovaPay Bank N.A.';

      App.goPage('screen-slip');
    } catch (e) {
      App.showToast('Could not load e-slip.');
    }
  },

  // ── Profile ───────────────────────────────────────────────────
  async loadProfile() {
    try {
      const resp = await fetch(`${API}/profile/${State.userId}`);
      const p    = await resp.json();

      const initials = App.getInitials(p.fullName);
      document.getElementById('prof-avatar').textContent    = initials[0] || 'A';
      document.getElementById('prof-name').textContent      = p.fullName || '—';
      document.getElementById('prof-tier').textContent      = (p.tier || '') + ' Member';
      document.getElementById('prof-full-name').textContent = p.fullName || '—';
      document.getElementById('prof-email').textContent     = p.email || '—';
      document.getElementById('prof-phone').textContent     = p.phone || '—';
      document.getElementById('prof-acct').textContent      = p.accountNumber || '—';
      document.getElementById('prof-last-login').textContent= p.lastLogin || '—';
    } catch (e) {
      console.warn('Profile load error', e);
    }
  },

  // ── Devices ───────────────────────────────────────────────────
  async loadDevices() {
    try {
      const resp    = await fetch(`${API}/devices/${State.userId}`);
      const devices = await resp.json();
      const list    = document.getElementById('device-list');

      if (!devices.length) {
        list.innerHTML = '<div style="color:var(--text3);font-size:13px;padding:12px 0;">No registered devices.</div>';
        return;
      }

      list.innerHTML = devices.map(d => `
        <div class="device-item">
          <div class="device-icon">📱</div>
          <div class="device-info">
            <div class="device-name">${d.deviceName}</div>
            <div class="device-meta">${d.deviceId} · ${(d.registeredAt || '').split('T')[0]}</div>
          </div>
          <span class="device-badge">${d.status || 'ACTIVE'}</span>
        </div>`).join('');
    } catch (e) {
      console.warn('Devices load error', e);
    }
  },

  async registerDevice() {
    const btn = document.getElementById('btn-register-device');
    btn.disabled = true;
    btn.textContent = 'Registering…';
    try {
      const resp = await fetch(`${API}/devices/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: State.userId, deviceName: 'My Device', deviceModel: 'Web Browser'
        }),
      });
      await resp.json();
      App.showToast('Device registered successfully!');
      await App.loadDevices();
    } catch (e) {
      App.showToast('Device registration failed.');
    } finally {
      btn.disabled = false;
      btn.textContent = '+ Register This Device';
    }
  },

  // ── Notifications ─────────────────────────────────────────────
  async loadNotifications() {
    try {
      const resp = await fetch(`${API}/notifications`);
      const notes = await resp.json();
      const list  = document.getElementById('notif-list');

      const colors = {
        info:    { bg: '#eff6ff', icon: 'ℹ️' },
        warning: { bg: '#fffbeb', icon: '⚠️' },
        success: { bg: '#f0fdf4', icon: '✅' },
      };

      list.innerHTML = notes.map(n => {
        const c = colors[n.type] || colors.info;
        return `
          <div class="notif-item">
            <div class="notif-dot-icon" style="background:${c.bg}">${c.icon}</div>
            <div class="notif-info">
              <div class="notif-text">${n.message}</div>
              <div class="notif-time">${n.time}</div>
            </div>
          </div>`;
      }).join('');
    } catch (e) {
      console.warn('Notifications load error', e);
    }
  },

  // ── Helpers ───────────────────────────────────────────────────
  fmtMoney(n) {
    return '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  },

  showToast(msg) {
    const t = document.getElementById('toast');
    t.textContent = msg;
    t.classList.remove('hidden');
    clearTimeout(App._toastTimer);
    App._toastTimer = setTimeout(() => t.classList.add('hidden'), 2800);
  },
};

// ── Boot ──────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', App.init.bind(App));

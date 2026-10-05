// =============================================================
//  Frontend (HTML + JavaScript puro) - Sistema de Reservas
//  Backend esperado en http://localhost:8080
// =============================================================
const BACKEND = 'http://localhost:8080';
const API = BACKEND + '/api';
const PAGE_SIZE = 6;

const state = { page: 0, totalPages: 0, selectedEvent: null, editingId: null };

const $ = (id) => document.getElementById(id);

// ---------- utilidades ----------
function esc(value) {
    const div = document.createElement('div');
    div.textContent = value ?? '';
    return div.innerHTML;
}
const money = (n) => '$' + Number(n).toFixed(2);
const fmtDate = (iso) => new Date(iso).toLocaleString('es-SV', { dateStyle: 'medium', timeStyle: 'short' });

let alertTimer;
function showAlert(message, type = 'error') {
    const el = $('alert');
    el.textContent = message;
    el.className = 'alert ' + type;
    clearTimeout(alertTimer);
    alertTimer = setTimeout(() => el.classList.add('hidden'), 6000);
}

// ---------- manejo del token ----------
function saveSession(data) {
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
}

function clearSession() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
}

function parseJwt(token) {
    try {
        const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(decodeURIComponent(escape(atob(payload))));
    } catch (e) {
        return null;
    }
}

function currentUser() {
    const token = localStorage.getItem('accessToken');
    const claims = token ? parseJwt(token) : null;
    return claims ? { username: claims.sub, role: claims.role } : null;
}

const isAdmin = () => currentUser()?.role === 'ADMIN';

async function tryRefresh() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return false;
    try {
        const res = await fetch(API + '/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
        });
        if (!res.ok) return false;
        saveSession(await res.json());
        return true;
    } catch (e) {
        return false;
    }
}

// ---------- cliente HTTP: agrega el JWT en el header Authorization ----------
async function api(path, { method = 'GET', body, auth = true } = {}) {
    const doFetch = () => {
        const headers = { 'Content-Type': 'application/json' };
        const token = localStorage.getItem('accessToken');
        if (auth && token) headers['Authorization'] = 'Bearer ' + token;
        return fetch(API + path, { method, headers, body: body ? JSON.stringify(body) : undefined });
    };

    let res;
    try {
        res = await doFetch();
    } catch (e) {
        throw new Error('No se pudo conectar con el servidor. ¿Está corriendo el backend?');
    }

    // Token expirado: se intenta renovar una vez con el refresh token.
    if (res.status === 401 && auth) {
        if (await tryRefresh()) {
            res = await doFetch();
        }
        if (res.status === 401 || res.status === 403) {
            logout('Tu sesión expiró. Inicia sesión nuevamente.');
            throw new Error('Sesión expirada');
        }
    }

    if (res.status === 204) return null;
    const data = await res.json().catch(() => null);
    if (!res.ok) throw new Error(data?.message || 'Error ' + res.status);
    return data;
}

// ---------- vistas ----------
function showAuth() {
    $('authView').classList.remove('hidden');
    $('appView').classList.add('hidden');
    $('userBox').classList.add('hidden');
}

function showApp() {
    const user = currentUser();
    if (!user) return showAuth();
    $('authView').classList.add('hidden');
    $('appView').classList.remove('hidden');
    $('userBox').classList.remove('hidden');
    $('userName').textContent = user.username;
    $('userRole').textContent = user.role;
    $('newEventBtn').classList.toggle('hidden', !isAdmin());
    switchAppTab('events');
}

function logout(message) {
    clearSession();
    showAuth();
    if (message) showAlert(message, 'error');
}

function switchAppTab(tab) {
    document.querySelectorAll('[data-app-tab]').forEach(b =>
        b.classList.toggle('active', b.dataset.appTab === tab));
    $('eventsTab').classList.toggle('hidden', tab !== 'events');
    $('bookingsTab').classList.toggle('hidden', tab !== 'bookings');
    if (tab === 'events') loadEvents();
    else loadMyBookings();
}

// ---------- autenticacion ----------
document.querySelectorAll('[data-auth-tab]').forEach(btn => {
    btn.addEventListener('click', () => {
        const isLogin = btn.dataset.authTab === 'login';
        document.querySelectorAll('[data-auth-tab]').forEach(b => b.classList.toggle('active', b === btn));
        $('loginForm').classList.toggle('hidden', !isLogin);
        $('registerForm').classList.toggle('hidden', isLogin);
    });
});

$('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const data = await api('/auth/login', {
            method: 'POST', auth: false,
            body: { username: $('loginUsername').value.trim(), password: $('loginPassword').value }
        });
        saveSession(data);
        $('loginForm').reset();
        showApp();
    } catch (err) {
        showAlert(err.message);
    }
});

$('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        await api('/auth/register', {
            method: 'POST', auth: false,
            body: {
                username: $('regUsername').value.trim(),
                password: $('regPassword').value,
                firstname: $('regFirstname').value.trim(),
                lastname: $('regLastname').value.trim(),
                age: Number($('regAge').value)
            }
        });
        showAlert('Cuenta creada. Ahora puedes iniciar sesión.', 'success');
        $('registerForm').reset();
        document.querySelector('[data-auth-tab="login"]').click();
    } catch (err) {
        showAlert(err.message);
    }
});

$('githubBtn').addEventListener('click', (e) => {
    e.preventDefault();
    window.location.href = BACKEND + '/oauth2/authorization/github';
});

$('logoutBtn').addEventListener('click', () => {
    logout();
    showAlert('Sesión cerrada correctamente.', 'success');
});

// ---------- eventos ----------
async function loadEvents() {
    try {
        const data = await api(`/events?page=${state.page}&size=${PAGE_SIZE}&sort=eventDate,asc`);
        state.totalPages = data.totalPages;

        $('eventsList').innerHTML = data.content.length
            ? data.content.map(eventCard).join('')
            : '<p class="muted">No hay eventos disponibles.</p>';

        $('pageInfo').textContent = `Página ${data.page + 1} de ${Math.max(data.totalPages, 1)}`;
        $('prevPage').disabled = data.page <= 0;
        $('nextPage').disabled = data.page + 1 >= data.totalPages;
    } catch (err) {
        if (err.message !== 'Sesión expirada') showAlert(err.message);
    }
}

function eventCard(ev) {
    return `
    <article class="card event-card">
        <h3>${esc(ev.title)}</h3>
        <span class="meta">📅 ${fmtDate(ev.eventDate)}</span>
        <span class="meta">📍 ${esc(ev.venue)}</span>
        <span class="price">${money(ev.pricePerTicket)} por entrada</span>
        <span class="meta">Cupos disponibles: ${ev.availableSeats} / ${ev.capacity}</span>
        <button class="btn btn-outline" onclick="openDetail(${ev.idEvent})">Ver detalle</button>
    </article>`;
}

$('prevPage').addEventListener('click', () => { if (state.page > 0) { state.page--; loadEvents(); } });
$('nextPage').addEventListener('click', () => {
    if (state.page + 1 < state.totalPages) { state.page++; loadEvents(); }
});

async function openDetail(id) {
    try {
        const ev = await api('/events/' + id);
        state.selectedEvent = ev;
        $('detailTitle').textContent = ev.title;
        $('detailDesc').textContent = ev.description || 'Sin descripción';
        $('detailDate').textContent = fmtDate(ev.eventDate);
        $('detailVenue').textContent = ev.venue;
        $('detailPrice').textContent = money(ev.pricePerTicket);
        $('detailSeats').textContent = `${ev.availableSeats} de ${ev.capacity}`;
        $('bookingQty').value = 1;
        $('bookingQty').max = Math.max(ev.availableSeats, 1);
        updateTotal();
        $('adminActions').classList.toggle('hidden', !isAdmin());
        $('detailDialog').showModal();
    } catch (err) {
        if (err.message !== 'Sesión expirada') showAlert(err.message);
    }
}

function updateTotal() {
    const qty = Number($('bookingQty').value) || 0;
    const price = state.selectedEvent ? Number(state.selectedEvent.pricePerTicket) : 0;
    $('bookingTotal').textContent = money(qty * price);
}
$('bookingQty').addEventListener('input', updateTotal);

$('bookingForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const booking = await api('/bookings', {
            method: 'POST',
            body: { eventId: state.selectedEvent.idEvent, quantity: Number($('bookingQty').value) }
        });
        $('detailDialog').close();
        showAlert(`Reserva #${booking.idBooking} confirmada. Total: ${money(booking.totalAmount)}`, 'success');
        loadEvents();
    } catch (err) {
        if (err.message !== 'Sesión expirada') showAlert(err.message);
    }
});

// ---------- administracion de eventos (solo ADMIN) ----------
function openEventForm(ev) {
    state.editingId = ev ? ev.idEvent : null;
    $('eventFormTitle').textContent = ev ? 'Editar evento' : 'Nuevo evento';
    $('evTitle').value = ev?.title ?? '';
    $('evDescription').value = ev?.description ?? '';
    $('evDate').value = ev ? ev.eventDate.slice(0, 16) : '';
    $('evVenue').value = ev?.venue ?? '';
    $('evCapacity').value = ev?.capacity ?? '';
    $('evPrice').value = ev?.pricePerTicket ?? '';
    $('eventFormDialog').showModal();
}

$('newEventBtn').addEventListener('click', () => openEventForm(null));

$('editEventBtn').addEventListener('click', () => {
    $('detailDialog').close();
    openEventForm(state.selectedEvent);
});

$('deleteEventBtn').addEventListener('click', async () => {
    if (!confirm('¿Eliminar este evento?')) return;
    try {
        await api('/events/' + state.selectedEvent.idEvent, { method: 'DELETE' });
        $('detailDialog').close();
        showAlert('Evento eliminado.', 'success');
        loadEvents();
    } catch (err) {
        if (err.message !== 'Sesión expirada') showAlert(err.message);
    }
});

$('eventForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    let date = $('evDate').value;
    if (date.length === 16) date += ':00'; // datetime-local no trae segundos

    const body = {
        title: $('evTitle').value.trim(),
        description: $('evDescription').value.trim(),
        eventDate: date,
        venue: $('evVenue').value.trim(),
        capacity: Number($('evCapacity').value),
        pricePerTicket: Number($('evPrice').value)
    };

    try {
        if (state.editingId) {
            await api('/events/' + state.editingId, { method: 'PUT', body });
            showAlert('Evento actualizado.', 'success');
        } else {
            await api('/events', { method: 'POST', body });
            showAlert('Evento creado.', 'success');
        }
        $('eventFormDialog').close();
        loadEvents();
    } catch (err) {
        if (err.message !== 'Sesión expirada') showAlert(err.message);
    }
});

// ---------- mis reservas ----------
async function loadMyBookings() {
    try {
        const list = await api('/bookings/my');
        $('noBookings').classList.toggle('hidden', list.length > 0);
        $('bookingsBody').innerHTML = list.map(b => `
            <tr>
                <td>${b.idBooking}</td>
                <td>${esc(b.eventTitle)}</td>
                <td>${fmtDate(b.eventDate)}</td>
                <td>${b.quantity}</td>
                <td>${money(b.totalAmount)}</td>
                <td>${fmtDate(b.bookingDate)}</td>
                <td><span class="status ${b.status}">${b.status}</span></td>
                <td>${b.status === 'CONFIRMED'
            ? `<button class="btn btn-danger btn-small" onclick="cancelBooking(${b.idBooking})">Cancelar</button>`
            : ''}</td>
            </tr>`).join('');
    } catch (err) {
        if (err.message !== 'Sesión expirada') showAlert(err.message);
    }
}

async function cancelBooking(id) {
    if (!confirm('¿Cancelar esta reserva?')) return;
    try {
        await api('/bookings/' + id, { method: 'DELETE' });
        showAlert('Reserva cancelada.', 'success');
        loadMyBookings();
    } catch (err) {
        if (err.message !== 'Sesión expirada') showAlert(err.message);
    }
}

// ---------- cierre de dialogos ----------
document.querySelectorAll('[data-close]').forEach(btn =>
    btn.addEventListener('click', () => $(btn.dataset.close).close()));

// ---------- arranque ----------
(function init() {
    // Retorno del login con GitHub: el backend redirige con ?token=...&refresh=...
    const params = new URLSearchParams(window.location.search);
    if (params.get('token')) {
        saveSession({ accessToken: params.get('token'), refreshToken: params.get('refresh') });
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    if (currentUser()) showApp();
    else showAuth();
})();
/* ============================================================
   TravelTrek - auth.js
   Wraps the two endpoints exposed by AuthController:
     POST /api/v1/auth/register  -> RegisterRequest -> AuthResponse
     POST /api/v1/auth/login     -> LoginRequest    -> AuthResponse

   AuthResponse = { token, email, role }
   Stored in localStorage as tt_token / tt_email / tt_role.
   ============================================================ */

// Clears any stored session credentials
function clearSession() {
  localStorage.removeItem("tt_token");
  localStorage.removeItem("tt_email");
  localStorage.removeItem("tt_role");
}

// Persists the AuthResponse returned by the backend after successful login.
function storeSession(authResponse) {
  localStorage.setItem("tt_token", authResponse.token);
  localStorage.setItem("tt_email", authResponse.email);
  localStorage.setItem("tt_role", authResponse.role);
}

// email, password, optional role ("TRAVELER" | "TRAVEL_AGENT" | "AGENCY_MANAGER")
// Registration creates the account but does NOT authenticate or store JWT
async function registerUser(email, password, role) {
  clearSession();
  const payload = role ? { email, password, role } : { email, password };
  const response = await api.post("/auth/register", payload);
  return response.data;
}

async function loginUser(email, password) {
  const response = await api.post("/auth/login", { email, password });
  storeSession(response.data);
  return response.data;
}

function logout() {
  clearSession();
  window.location.href = "login.html";
}

function isAuthenticated() {
  return !!localStorage.getItem("tt_token");
}

function getCurrentUser() {
  if (!isAuthenticated()) return null;
  return {
    email: localStorage.getItem("tt_email"),
    role: localStorage.getItem("tt_role"),
  };
}

// Call this at the top of every protected page. Redirects to login.html
// immediately if there is no JWT token stored.
function requireAuth() {
  if (!isAuthenticated()) {
    window.location.href = "login.html";
  }
}

// Call this at the top of any page that only certain roles may open
// (e.g. requireRole(["AGENCY_MANAGER"]) on users.html). If the logged-in
// user's role isn't in allowedRoles, they're redirected to unauthorized.html
// instead of seeing the page - this backs up (does not replace) the
// server-side 401/403 checks, since a user could otherwise type the URL
// directly and briefly see a page they have no API access to.
function requireRole(allowedRoles) {
  requireAuth();
  const user = getCurrentUser();
  if (!user || !allowedRoles.includes(user.role)) {
    window.location.href = "unauthorized.html";
  }
}

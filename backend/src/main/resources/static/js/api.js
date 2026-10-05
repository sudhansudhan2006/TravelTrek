/* ============================================================
   TravelTrek - api.js
   Single, reusable Axios instance for the whole app.
   Every other JS file imports/uses this "api" object - never
   create a second axios.create() call anywhere else.

   NOTE ON BASE URL:
   The Spring Boot backend maps every controller under
   "/api/v1" (see @RequestMapping("/api/v1/...") in each
   controller), not just "/api". The base URL below matches
   the real backend exactly.
   ============================================================ */

const api = axios.create({
  baseURL: "/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

/* ---------- Request interceptor ----------
   Attaches "Authorization: Bearer <token>" to every outgoing
   request, if a token is present in localStorage. */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("tt_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/* ---------- Response interceptor ----------
   Handles 401 Unauthorized globally: clears the stored session
   and redirects back to the login page. */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("tt_token");
      localStorage.removeItem("tt_email");
      localStorage.removeItem("tt_role");

      // Avoid redirect loops if we are already on the login page
      if (!window.location.pathname.endsWith("login.html")) {
        window.location.href = "login.html";
      }
    }
    if (error.response && error.response.status === 403) {
      // The backend rejected this request because the logged-in role isn't
      // permitted (server-side RBAC is the real gate; the frontend nav/button
      // hiding is just a courtesy). Send the user to the 403 page.
      if (!window.location.pathname.endsWith("unauthorized.html")) {
        window.location.href = "unauthorized.html";
      }
    }
    return Promise.reject(error);
  }
);

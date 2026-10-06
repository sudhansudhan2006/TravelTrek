/* ============================================================
   TravelTrek - utils.js
   Shared helper functions used across every page:
   - loading spinner overlay
   - toast-style alerts
   - a promise-based Bootstrap confirmation modal (used before Delete)
   - navbar / sidebar renderer (keeps every page's layout consistent)
   - small formatting helpers
   ============================================================ */

/* ---------- Loading spinner ---------- */
// Call showSpinner() before an API call and hideSpinner() in a finally block.
function showSpinner() {
  let overlay = document.getElementById("tt-loading-overlay");
  if (!overlay) {
    overlay = document.createElement("div");
    overlay.id = "tt-loading-overlay";
    overlay.innerHTML = `
      <div class="text-center">
        <div class="spinner-border text-primary" role="status" style="width:3rem;height:3rem;"></div>
        <div class="mt-2 fw-semibold text-secondary">Loading...</div>
      </div>`;
    document.body.appendChild(overlay);
  }
  overlay.classList.add("active");
}

function hideSpinner() {
  const overlay = document.getElementById("tt-loading-overlay");
  if (overlay) overlay.classList.remove("active");
}

/* ---------- Alerts (success / error banners) ---------- */
// type: "success" | "danger" | "warning" | "info"
function showAlert(message, type = "info") {
  let stack = document.querySelector(".tt-alert-stack");
  if (!stack) {
    stack = document.createElement("div");
    stack.className = "tt-alert-stack";
    document.body.appendChild(stack);
  }

  const alertEl = document.createElement("div");
  alertEl.className = `alert alert-${type} alert-dismissible fade show`;
  alertEl.role = "alert";
  alertEl.innerHTML = `
    ${escapeHtml(message)}
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>`;
  stack.appendChild(alertEl);

  // Auto-dismiss after 5 seconds
  setTimeout(() => {
    alertEl.classList.remove("show");
    alertEl.addEventListener("transitionend", () => alertEl.remove());
  }, 5000);
}

// Extracts a readable message out of an Axios error, falling back to a generic one.
// Matches the {timestamp, status, message, errors} shape from GlobalExceptionHandler.
function getErrorMessage(error) {
  if (error.response && error.response.data && typeof error.response.data === "object") {
    const data = error.response.data;
    if (data.errors) {
      // Field validation errors -> join into one readable string
      return Object.entries(data.errors)
        .map(([field, msg]) => `${field}: ${msg}`)
        .join(" | ");
    }
    if (data.message) return data.message;
  }
  if (error.response && error.response.status === 404) {
    return "Backend endpoint not found (404). Please ensure your Spring Boot backend is deployed and configured in js/config.js.";
  }
  if (error.code === "ERR_NETWORK" || error.message === "Network Error") {
    return "Cannot reach backend server. Please check your network connection, ensure the backend is running, and verify CORS allows this origin.";
  }
  if (error.message) return error.message;
  return "Something went wrong. Please try again.";
}

/* ---------- Confirmation modal (used before Delete / Cancel actions) ---------- */
// Returns a Promise<boolean> - true if user confirmed.
function confirmAction(message = "Are you sure?") {
  return new Promise((resolve) => {
    let modalEl = document.getElementById("tt-confirm-modal");
    if (!modalEl) {
      modalEl = document.createElement("div");
      modalEl.id = "tt-confirm-modal";
      modalEl.className = "modal fade";
      modalEl.tabIndex = -1;
      modalEl.innerHTML = `
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Please Confirm</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body" id="tt-confirm-message"></div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
              <button type="button" class="btn btn-danger" id="tt-confirm-ok-btn">Yes, proceed</button>
            </div>
          </div>
        </div>`;
      document.body.appendChild(modalEl);
    }

    modalEl.querySelector("#tt-confirm-message").textContent = message;
    const bsModal = new bootstrap.Modal(modalEl);
    const okBtn = modalEl.querySelector("#tt-confirm-ok-btn");

    // Replace button node to clear any previously attached listeners
    const freshOkBtn = okBtn.cloneNode(true);
    okBtn.parentNode.replaceChild(freshOkBtn, okBtn);

    freshOkBtn.addEventListener("click", () => {
      bsModal.hide();
      resolve(true);
    });
    modalEl.addEventListener(
      "hidden.bs.modal",
      () => resolve(false),
      { once: true }
    );

    bsModal.show();
  });
}

/* ---------- Formatting helpers ---------- */
function formatCurrency(amount) {
  if (amount === null || amount === undefined) return "-";
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(amount);
}

function formatDateTime(value) {
  if (!value) return "-";
  const date = new Date(value);
  if (isNaN(date.getTime())) return value;
  return date.toLocaleString("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

// Reads a query-string parameter from the current URL, e.g. activities.html?itineraryId=3
function getQueryParam(name) {
  const params = new URLSearchParams(window.location.search);
  return params.get(name);
}

/* ---------- Shared navbar + sidebar (injected on every protected page) ---------- */
// activePage: "dashboard" | "packages" | "itinerary" | "activities" | "bookings" |
//             "users" | "reports" | "destinations" | "profile"
//
// Menu items are filtered by the logged-in user's role, per the RBAC spec:
//   TRAVELER       -> Dashboard, Travel Packages, My Bookings, My Itineraries,
//                      Planned Activities, Profile
//   TRAVEL_AGENT   -> Dashboard, Activities, Traveler Bookings, Itineraries, Profile
//   AGENCY_MANAGER -> Dashboard, Packages, Destinations, Activities, Users,
//                      Reports, All Bookings, Profile
function renderLayout(activePage) {
  const user = getCurrentUser();
  const role = user ? user.role : null;

  const navbarHtml = `
    <nav class="navbar navbar-expand tt-navbar px-3">
      <a class="navbar-brand" href="dashboard.html">
        <img src="assets/logo.png" alt="TravelTrek logo">
        TravelTrek
      </a>
      <div class="ms-auto d-flex align-items-center gap-3">
        <span class="user-chip">
          ${escapeHtml(user ? user.email : "")}
          <span class="badge badge-role">${escapeHtml(user ? user.role : "")}</span>
        </span>
        <button class="btn btn-sm btn-outline-light" id="tt-logout-btn">Logout</button>
      </div>
    </nav>`;

  // Build the link list directly from role, with role-appropriate labels
  // per the RBAC spec (e.g. a traveler sees "My Bookings", an agent sees
  // "Traveler Bookings", a manager sees "All Bookings" - same page, different label).
  const links = [
    { key: "dashboard", href: "dashboard.html", icon: "bi-speedometer2", label: "Dashboard" },
  ];

  if (role === "TRAVELER" || role === "AGENCY_MANAGER") {
    links.push({
      key: "packages", href: "packages.html", icon: "bi-suitcase2",
      label: role === "AGENCY_MANAGER" ? "Packages" : "Travel Packages",
    });
  }

  if (role === "AGENCY_MANAGER") {
    links.push({ key: "destinations", href: "destinations.html", icon: "bi-geo-alt", label: "Destinations" });
  }

  if (role === "TRAVEL_AGENT" || role === "AGENCY_MANAGER") {
    links.push({ key: "activities", href: "activities.html", icon: "bi-list-check", label: "Activities" });
  }
  if (role === "TRAVELER") {
    links.push({ key: "activities", href: "activities.html", icon: "bi-list-check", label: "Planned Activities" });
  }

  if (role === "AGENCY_MANAGER") {
    links.push({ key: "users", href: "users.html", icon: "bi-people", label: "Users" });
    links.push({ key: "reports", href: "reports.html", icon: "bi-bar-chart", label: "Reports" });
  }

  if (role === "TRAVELER") {
    links.push({ key: "bookings", href: "bookings.html", icon: "bi-journal-check", label: "My Bookings" });
  } else if (role === "TRAVEL_AGENT") {
    links.push({ key: "bookings", href: "bookings.html", icon: "bi-journal-check", label: "Traveler Bookings" });
  } else if (role === "AGENCY_MANAGER") {
    links.push({ key: "bookings", href: "bookings.html", icon: "bi-journal-check", label: "All Bookings" });
  }

  if (role === "TRAVELER") {
    links.push({ key: "itinerary", href: "itinerary.html", icon: "bi-map", label: "My Itineraries" });
  } else if (role === "TRAVEL_AGENT" || role === "AGENCY_MANAGER") {
    links.push({ key: "itinerary", href: "itinerary.html", icon: "bi-map", label: "Itineraries" });
  }

  links.push({ key: "profile", href: "profile.html", icon: "bi-person-circle", label: "Profile" });

  const sidebarLinksHtml = links
    .map(
      (link) => `
      <a class="nav-link ${link.key === activePage ? "active" : ""}" href="${link.href}">
        <i class="bi ${link.icon}"></i> ${link.label}
      </a>`
    )
    .join("");

  const shellHtml = `
    <div class="tt-shell">
      <aside class="tt-sidebar">
        <nav class="nav flex-column">${sidebarLinksHtml}</nav>
      </aside>
      <main class="tt-content" id="tt-page-content"></main>
    </div>`;

  document.body.insertAdjacentHTML("afterbegin", navbarHtml);
  document.body.insertAdjacentHTML("beforeend", shellHtml);

  document.getElementById("tt-logout-btn").addEventListener("click", logout);

  // Return the empty content container so the page script can fill it in.
  return document.getElementById("tt-page-content");
}

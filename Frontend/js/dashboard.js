/* ============================================================
   TravelTrek - dashboard.js
   Landing page after login: shows a welcome message and a set
   of navigation cards to each feature area.
   Cards shown are role-based, mirroring the sidebar menu in
   utils.js -> renderLayout() so a role never sees a shortcut to
   a page it isn't allowed to open.
   ============================================================ */

requireAuth();

const user = getCurrentUser();
const content = renderLayout("dashboard");

// key: icon background class, unique per card just for visual variety
const CARD_DEFS = {
  packages: { href: "packages.html", icon: "bi-suitcase2", bg: "bg-a",
    title: (r) => (r === "AGENCY_MANAGER" ? "Packages" : "Travel Packages"),
    desc: (r) => (r === "AGENCY_MANAGER" ? "Create, update, and delete travel packages." : "Browse and search available travel packages.") },
  destinations: { href: "destinations.html", icon: "bi-geo-alt", bg: "bg-b",
    title: () => "Destinations", desc: () => "View every destination currently offered." },
  itinerary: { href: "itinerary.html", icon: "bi-map", bg: "bg-b",
    title: (r) => (r === "TRAVELER" ? "My Itineraries" : "Itineraries"),
    desc: (r) => (r === "TRAVELER" ? "Create, edit, and delete your trip plans." : "View every traveler's trip itinerary.") },
  activities: { href: "activities.html", icon: "bi-list-check", bg: "bg-c",
    title: (r) => (r === "TRAVELER" ? "Planned Activities" : "Activities"),
    desc: (r) => (r === "TRAVELER" ? "View the activities planned for your trips." : "Create, update, and delete planned activities.") },
  bookings: { href: "bookings.html", icon: "bi-journal-check", bg: "bg-d",
    title: (r) => (r === "TRAVELER" ? "My Bookings" : r === "TRAVEL_AGENT" ? "Traveler Bookings" : "All Bookings"),
    desc: (r) => (r === "TRAVELER" ? "Book a package and manage your reservations." : "View every traveler's booking reservations.") },
  users: { href: "users.html", icon: "bi-people", bg: "bg-a",
    title: () => "Users", desc: () => "Manage user accounts, roles, and status." },
  reports: { href: "reports.html", icon: "bi-bar-chart", bg: "bg-c",
    title: () => "Reports", desc: () => "View system-wide reports and dashboard stats." },
  profile: { href: "profile.html", icon: "bi-person-circle", bg: "bg-d",
    title: () => "Profile", desc: () => "Update your own account details." },
};

const CARDS_BY_ROLE = {
  TRAVELER: ["packages", "itinerary", "activities", "bookings", "profile"],
  TRAVEL_AGENT: ["activities", "bookings", "itinerary", "profile"],
  AGENCY_MANAGER: ["packages", "destinations", "activities", "users", "reports", "bookings", "profile"],
};

const cardKeys = CARDS_BY_ROLE[user.role] || [];

const cardsHtml = cardKeys
  .map((key) => {
    const def = CARD_DEFS[key];
    return `
      <div class="col-md-6 col-lg-3">
        <a href="${def.href}" class="text-decoration-none">
          <div class="card card-tt nav-card p-3">
            <div class="icon-wrap ${def.bg}"><i class="bi ${def.icon}"></i></div>
            <h5>${escapeHtml(def.title(user.role))}</h5>
            <p class="text-secondary mb-0">${escapeHtml(def.desc(user.role))}</p>
          </div>
        </a>
      </div>`;
  })
  .join("");

content.innerHTML = `
  <div class="page-header">
    <div>
      <h2 class="mb-1">Welcome back, ${escapeHtml(user.email)}</h2>
      <p class="text-secondary mb-0">
        Logged in as <span class="badge badge-role">${escapeHtml(user.role)}</span>
      </p>
    </div>
  </div>

  <div class="row g-4">
    ${cardsHtml}
  </div>

  <div class="row g-4 mt-1">
    <div class="col-12">
      <div class="card card-tt p-3">
        <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
          <div>
            <h5 class="mb-1"><i class="bi bi-box-arrow-right"></i> Logout</h5>
            <p class="text-secondary mb-0">End your current session on this device.</p>
          </div>
          <button class="btn btn-outline-danger" id="dashboard-logout-btn">Logout</button>
        </div>
      </div>
    </div>
  </div>
`;

document.getElementById("dashboard-logout-btn").addEventListener("click", logout);

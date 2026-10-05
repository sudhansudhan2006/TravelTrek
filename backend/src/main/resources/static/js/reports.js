/* ============================================================
   TravelTrek - reports.js
   Maps to ReportController (/api/v1/reports), AGENCY_MANAGER only:
     GET /reports/summary -> { totalUsers, totalPackages, totalItineraries,
                                totalBookings, confirmedBookings,
                                cancelledBookings, pendingBookings }
   ============================================================ */

requireRole(["AGENCY_MANAGER"]);

const content = renderLayout("reports");

content.innerHTML = `
  <div class="page-header">
    <h2 class="mb-0"><i class="bi bi-bar-chart"></i> Reports &amp; Dashboard</h2>
  </div>

  <div class="row g-4" id="reports-cards"></div>
`;

const CARD_DEFS = [
  { key: "totalUsers", label: "Total Users", icon: "bi-people", bg: "bg-a" },
  { key: "totalPackages", label: "Total Packages", icon: "bi-suitcase2", bg: "bg-b" },
  { key: "totalItineraries", label: "Total Itineraries", icon: "bi-map", bg: "bg-c" },
  { key: "totalBookings", label: "Total Bookings", icon: "bi-journal-check", bg: "bg-d" },
  { key: "confirmedBookings", label: "Confirmed Bookings", icon: "bi-check-circle", bg: "bg-a" },
  { key: "pendingBookings", label: "Pending Bookings", icon: "bi-hourglass-split", bg: "bg-b" },
  { key: "cancelledBookings", label: "Cancelled Bookings", icon: "bi-x-circle", bg: "bg-c" },
];

async function loadSummary() {
  showSpinner();
  try {
    const response = await api.get("/reports/summary");
    const data = response.data;
    document.getElementById("reports-cards").innerHTML = CARD_DEFS.map(
      (def) => `
        <div class="col-md-6 col-lg-3">
          <div class="card card-tt nav-card p-3">
            <div class="icon-wrap ${def.bg}"><i class="bi ${def.icon}"></i></div>
            <h5>${data[def.key] ?? 0}</h5>
            <p class="text-secondary mb-0">${escapeHtml(def.label)}</p>
          </div>
        </div>`
    ).join("");
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

/* ---------- Init ---------- */
loadSummary();

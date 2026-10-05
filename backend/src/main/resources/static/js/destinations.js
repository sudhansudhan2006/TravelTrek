/* ============================================================
   TravelTrek - destinations.js
   Maps to DestinationController (/api/v1/destinations), AGENCY_MANAGER only:
     GET /destinations -> List<String> (distinct destination names)

   NOTE: Destinations are a field on TravelPackage rather than a
   first-class entity in this project's schema, so this view is a
   read-only summary. To actually add/rename a destination, edit it
   on the relevant package(s) in Packages.
   ============================================================ */

requireRole(["AGENCY_MANAGER"]);

const content = renderLayout("destinations");

content.innerHTML = `
  <div class="page-header">
    <h2 class="mb-0"><i class="bi bi-geo-alt"></i> Destinations</h2>
  </div>

  <div class="note-box mb-3">
    <i class="bi bi-info-circle"></i>
    Destinations are derived from your travel packages. To add, rename, or remove a
    destination, edit the corresponding package(s) on the
    <a href="packages.html">Packages</a> page.
  </div>

  <div class="card card-tt p-3">
    <ul class="list-group list-group-flush" id="destinations-list"></ul>
    <div id="destinations-empty" class="empty-state d-none">
      <i class="bi bi-inbox fs-1"></i>
      <p class="mb-0">No destinations found yet - add a travel package to get started.</p>
    </div>
  </div>
`;

async function loadDestinations() {
  showSpinner();
  try {
    const response = await api.get("/destinations");
    const destinations = response.data;
    const list = document.getElementById("destinations-list");
    const emptyState = document.getElementById("destinations-empty");

    if (!destinations || destinations.length === 0) {
      list.innerHTML = "";
      emptyState.classList.remove("d-none");
      return;
    }
    emptyState.classList.add("d-none");
    list.innerHTML = destinations
      .map((d) => `<li class="list-group-item"><i class="bi bi-geo-alt-fill text-secondary me-2"></i>${escapeHtml(d)}</li>`)
      .join("");
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

/* ---------- Init ---------- */
loadDestinations();

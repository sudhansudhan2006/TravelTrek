/* ============================================================
   TravelTrek - itinerary.js
   Maps to ItineraryController (/api/v1/itineraries):
     POST   /itineraries        (TRAVELER only)                -> 201
     GET    /itineraries/my                                     -> own itineraries
     GET    /itineraries/all    (TRAVEL_AGENT / AGENCY_MANAGER)  -> every itinerary
     GET    /itineraries/{id}                                    -> ownership checked server-side
     PUT    /itineraries/{id}   (owner TRAVELER or AGENCY_MANAGER)
     DELETE /itineraries/{id}   (owner TRAVELER or AGENCY_MANAGER)

   ItineraryRequest = { title, destination, targetBudget }

   RBAC on this page:
     - TRAVELER only sees/creates/edits/deletes THEIR OWN itineraries.
     - TRAVEL_AGENT sees ALL itineraries (read-only - "assist travelers").
     - AGENCY_MANAGER sees ALL itineraries and has full edit/delete access.
   ============================================================ */

requireAuth();

const user = getCurrentUser();
const role = user.role;
const isTraveler = role === "TRAVELER";
const isManager = role === "AGENCY_MANAGER";
const canModify = isTraveler || isManager; // agents are read-only

const content = renderLayout("itinerary");

content.innerHTML = `
  <div class="page-header">
    <h2 class="mb-0"><i class="bi bi-map"></i> ${isTraveler ? "My Trip Itineraries" : "Trip Itineraries"}</h2>
    ${isTraveler ? `<button class="btn btn-tt-accent" id="add-itinerary-btn"><i class="bi bi-plus-lg"></i> Add Itinerary</button>` : ""}
  </div>

  ${!isTraveler ? `<div class="note-box mb-3"><i class="bi bi-info-circle"></i>
    ${role === "TRAVEL_AGENT"
      ? "You can view every traveler's itinerary to assist them. Only the traveler who owns an itinerary (or a manager) can edit or delete it."
      : "As an Agency Manager you can view and manage every itinerary in the system."}
  </div>` : ""}

  <div class="card card-tt p-3">
    <div class="table-responsive">
      <table class="table table-tt align-middle mb-0">
        <thead>
          <tr>
            <th>Title</th>
            <th>Destination</th>
            <th>Target Budget</th>
            ${!isTraveler ? "<th>Owner</th>" : ""}
            <th>AI Generated?</th>
            <th class="text-end">Actions</th>
          </tr>
        </thead>
        <tbody id="itineraries-tbody"></tbody>
      </table>
    </div>
    <div id="itineraries-empty" class="empty-state d-none">
      <i class="bi bi-inbox fs-1"></i>
      <p class="mb-0">${isTraveler ? 'You don\'t have any itineraries yet. Click "Add Itinerary" to create one.' : "No itineraries found."}</p>
    </div>
  </div>
`;

function isOwner(it) {
  return it.user && it.user.email === user.email;
}

function renderItinerariesTable(itineraries) {
  const tbody = document.getElementById("itineraries-tbody");
  const emptyState = document.getElementById("itineraries-empty");

  if (!itineraries || itineraries.length === 0) {
    tbody.innerHTML = "";
    emptyState.classList.remove("d-none");
    return;
  }
  emptyState.classList.add("d-none");

  tbody.innerHTML = itineraries
    .map((it) => {
      // A traveler may edit/delete only their own itinerary; a manager may edit/delete any
      const showEditDelete = isManager || (isTraveler && isOwner(it));
      return `
      <tr>
        <td>${escapeHtml(it.title)}</td>
        <td>${escapeHtml(it.destination)}</td>
        <td>${formatCurrency(it.targetBudget)}</td>
        ${!isTraveler ? `<td>${escapeHtml(it.user ? it.user.email : "-")}</td>` : ""}
        <td>${it.aiGenerated ? '<span class="badge bg-info">Yes</span>' : '<span class="badge bg-secondary">No</span>'}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-primary view-itinerary-btn" data-id="${it.id}">
            <i class="bi bi-eye"></i> View
          </button>
          <a class="btn btn-sm btn-outline-success" href="activities.html?itineraryId=${it.id}">
            <i class="bi bi-list-check"></i> Activities
          </a>
          ${showEditDelete ? `<button class="btn btn-sm btn-outline-secondary edit-itinerary-btn" data-id="${it.id}"><i class="bi bi-pencil"></i></button>` : ""}
          ${showEditDelete ? `<button class="btn btn-sm btn-outline-danger delete-itinerary-btn" data-id="${it.id}"><i class="bi bi-trash"></i></button>` : ""}
        </td>
      </tr>`;
    })
    .join("");

  document.querySelectorAll(".view-itinerary-btn").forEach((btn) =>
    btn.addEventListener("click", () => viewItinerary(btn.dataset.id))
  );
  document.querySelectorAll(".edit-itinerary-btn").forEach((btn) =>
    btn.addEventListener("click", () => openEditModal(btn.dataset.id))
  );
  document.querySelectorAll(".delete-itinerary-btn").forEach((btn) =>
    btn.addEventListener("click", () => deleteItinerary(btn.dataset.id))
  );
}

async function loadItineraries() {
  showSpinner();
  try {
    // Travelers only ever see their own; agents/managers see every itinerary.
    const endpoint = isTraveler ? "/itineraries/my" : "/itineraries/all";
    const response = await api.get(endpoint);
    renderItinerariesTable(response.data);
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

/* ---------- Add / Edit itinerary ---------- */
const itineraryModal = new bootstrap.Modal(document.getElementById("itinerary-modal"));

if (isTraveler) {
  document.getElementById("add-itinerary-btn").addEventListener("click", () => {
    document.getElementById("itinerary-form").reset();
    document.getElementById("itinerary-id").value = "";
    document.getElementById("itinerary-modal-title").textContent = "Add Itinerary";
    document.getElementById("itinerary-form-error").classList.add("d-none");
    itineraryModal.show();
  });
}

document.getElementById("itinerary-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const errorBox = document.getElementById("itinerary-form-error");
  errorBox.classList.add("d-none");

  const id = document.getElementById("itinerary-id").value;
  const payload = {
    title: document.getElementById("itinerary-title").value.trim(),
    destination: document.getElementById("itinerary-destination").value.trim(),
    targetBudget: Number(document.getElementById("itinerary-budget").value),
  };

  showSpinner();
  try {
    if (id) {
      await api.put(`/itineraries/${id}`, payload);
      showAlert("Itinerary updated successfully.", "success");
    } else {
      await api.post("/itineraries", payload);
      showAlert("Itinerary created successfully.", "success");
    }
    itineraryModal.hide();
    loadItineraries();
  } catch (error) {
    errorBox.textContent = getErrorMessage(error);
    errorBox.classList.remove("d-none");
  } finally {
    hideSpinner();
  }
});

async function openEditModal(id) {
  showSpinner();
  try {
    const response = await api.get(`/itineraries/${id}`);
    const it = response.data;
    document.getElementById("itinerary-id").value = it.id;
    document.getElementById("itinerary-title").value = it.title;
    document.getElementById("itinerary-destination").value = it.destination;
    document.getElementById("itinerary-budget").value = it.targetBudget;
    document.getElementById("itinerary-modal-title").textContent = "Edit Itinerary";
    document.getElementById("itinerary-form-error").classList.add("d-none");
    itineraryModal.show();
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

async function deleteItinerary(id) {
  const confirmed = await confirmAction("Delete this itinerary? This action cannot be undone.");
  if (!confirmed) return;

  showSpinner();
  try {
    await api.delete(`/itineraries/${id}`);
    showAlert("Itinerary deleted.", "success");
    loadItineraries();
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

/* ---------- View itinerary ---------- */
const viewItineraryModal = new bootstrap.Modal(document.getElementById("view-itinerary-modal"));

async function viewItinerary(id) {
  showSpinner();
  try {
    const response = await api.get(`/itineraries/${id}`);
    const it = response.data;
    document.getElementById("view-itinerary-body").innerHTML = `
      <dl class="row mb-0">
        <dt class="col-sm-4">Title</dt><dd class="col-sm-8">${escapeHtml(it.title)}</dd>
        <dt class="col-sm-4">Destination</dt><dd class="col-sm-8">${escapeHtml(it.destination)}</dd>
        <dt class="col-sm-4">Target Budget</dt><dd class="col-sm-8">${formatCurrency(it.targetBudget)}</dd>
        ${!isTraveler ? `<dt class="col-sm-4">Owner</dt><dd class="col-sm-8">${escapeHtml(it.user ? it.user.email : "-")}</dd>` : ""}
        <dt class="col-sm-4">AI Generated</dt><dd class="col-sm-8">${it.aiGenerated ? "Yes" : "No"}</dd>
      </dl>
      <a class="btn btn-sm btn-tt-primary mt-2" href="activities.html?itineraryId=${it.id}">
        <i class="bi bi-list-check"></i> View Planned Activities
      </a>
    `;
    viewItineraryModal.show();
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

/* ---------- Init ---------- */
loadItineraries();

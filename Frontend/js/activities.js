/* ============================================================
   TravelTrek - activities.js
   Maps to ActivityController (/api/v1/activities):
     POST   /activities                (TRAVEL_AGENT / AGENCY_MANAGER) -> 201
     GET    /activities/itinerary/{id} -> any authenticated role
     PUT    /activities/{id}           (TRAVEL_AGENT / AGENCY_MANAGER)
     DELETE /activities/{id}           (TRAVEL_AGENT / AGENCY_MANAGER)

   ActivityRequest = { activityName, scheduledAt, estimatedCost, itineraryId }

   The itineraryId to view is read from the URL query string,
   e.g. activities.html?itineraryId=3 (linked from itinerary.html).
   ============================================================ */

requireAuth();

const user = getCurrentUser();
const canManage = user.role === "TRAVEL_AGENT" || user.role === "AGENCY_MANAGER";
const content = renderLayout("activities");

let currentItineraryId = getQueryParam("itineraryId") || "";

content.innerHTML = `
  <div class="page-header">
    <h2 class="mb-0"><i class="bi bi-list-check"></i> Planned Activities</h2>
    ${canManage ? `<button class="btn btn-tt-accent" id="add-activity-btn"><i class="bi bi-plus-lg"></i> Add Activity</button>` : ""}
  </div>

  ${!canManage ? `<div class="note-box mb-3"><i class="bi bi-info-circle"></i>
    Only <strong>Travel Agents</strong> and <strong>Agency Managers</strong> can add, edit, or delete planned activities. You can view activities for an itinerary below.
  </div>` : ""}

  <div class="card card-tt p-3 mb-3">
    <form id="itinerary-lookup-form" class="row g-2 align-items-end">
      <div class="col-sm-6 col-md-4">
        <label class="form-label mb-1">Itinerary ID</label>
        <input type="number" class="form-control" id="itinerary-id-input" value="${escapeHtml(currentItineraryId)}" placeholder="e.g. 1" required>
      </div>
      <div class="col-auto">
        <button type="submit" class="btn btn-tt-primary"><i class="bi bi-search"></i> Load Activities</button>
      </div>
    </form>
  </div>

  <div class="card card-tt p-3">
    <div class="table-responsive">
      <table class="table table-tt align-middle mb-0">
        <thead>
          <tr>
            <th>Activity Name</th>
            <th>Scheduled At</th>
            <th>Estimated Cost</th>
            ${canManage ? "<th class='text-end'>Actions</th>" : ""}
          </tr>
        </thead>
        <tbody id="activities-tbody"></tbody>
      </table>
    </div>
    <div id="activities-empty" class="empty-state d-none">
      <i class="bi bi-inbox fs-1"></i>
      <p class="mb-0" id="activities-empty-text">Enter an Itinerary ID above and click "Load Activities".</p>
    </div>
  </div>
`;

function renderActivitiesTable(activities) {
  const tbody = document.getElementById("activities-tbody");
  const emptyState = document.getElementById("activities-empty");

  if (!activities || activities.length === 0) {
    tbody.innerHTML = "";
    document.getElementById("activities-empty-text").textContent = "No planned activities found for this itinerary.";
    emptyState.classList.remove("d-none");
    return;
  }
  emptyState.classList.add("d-none");

  tbody.innerHTML = activities
    .map(
      (act) => `
      <tr>
        <td>${escapeHtml(act.activityName)}</td>
        <td>${formatDateTime(act.scheduledAt)}</td>
        <td>${formatCurrency(act.estimatedCost)}</td>
        ${
          canManage
            ? `<td class="text-end">
                <button class="btn btn-sm btn-outline-primary edit-activity-btn" data-id="${act.id}"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger delete-activity-btn" data-id="${act.id}"><i class="bi bi-trash"></i></button>
              </td>`
            : ""
        }
      </tr>`
    )
    .join("");

  if (canManage) {
    document.querySelectorAll(".edit-activity-btn").forEach((btn) =>
      btn.addEventListener("click", () => openEditModal(btn.dataset.id))
    );
    document.querySelectorAll(".delete-activity-btn").forEach((btn) =>
      btn.addEventListener("click", () => deleteActivity(btn.dataset.id))
    );
  }
}

async function loadActivities(itineraryId) {
  if (!itineraryId) return;
  showSpinner();
  try {
    const response = await api.get(`/activities/itinerary/${itineraryId}`);
    renderActivitiesTable(response.data);
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

document.getElementById("itinerary-lookup-form").addEventListener("submit", (e) => {
  e.preventDefault();
  currentItineraryId = document.getElementById("itinerary-id-input").value.trim();
  loadActivities(currentItineraryId);
});

/* ---------- Add / Edit activity (TRAVEL_AGENT / AGENCY_MANAGER only) ---------- */
let activityModalInstance;

if (canManage) {
  activityModalInstance = new bootstrap.Modal(document.getElementById("activity-modal"));

  document.getElementById("add-activity-btn").addEventListener("click", () => {
    document.getElementById("activity-form").reset();
    document.getElementById("activity-id").value = "";
    document.getElementById("activity-modal-title").textContent = "Add Planned Activity";
    if (currentItineraryId) {
      document.getElementById("activity-itinerary-id").value = currentItineraryId;
    }
    document.getElementById("activity-form-error").classList.add("d-none");
    activityModalInstance.show();
  });

  document.getElementById("activity-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const errorBox = document.getElementById("activity-form-error");
    errorBox.classList.add("d-none");

    const id = document.getElementById("activity-id").value;
    const scheduledAtRaw = document.getElementById("activity-scheduled-at").value; // "YYYY-MM-DDTHH:mm"
    const payload = {
      itineraryId: Number(document.getElementById("activity-itinerary-id").value),
      activityName: document.getElementById("activity-name").value.trim(),
      scheduledAt: scheduledAtRaw, // ISO-8601 local date-time string, matches LocalDateTime on backend
      estimatedCost: Number(document.getElementById("activity-cost").value),
    };

    showSpinner();
    try {
      if (id) {
        await api.put(`/activities/${id}`, payload);
        showAlert("Activity updated successfully.", "success");
      } else {
        await api.post("/activities", payload);
        showAlert("Activity added successfully.", "success");
      }
      activityModalInstance.hide();
      // Refresh the list if we're currently viewing this itinerary
      if (String(payload.itineraryId) === String(currentItineraryId)) {
        loadActivities(currentItineraryId);
      }
    } catch (error) {
      errorBox.textContent = getErrorMessage(error);
      errorBox.classList.remove("d-none");
    } finally {
      hideSpinner();
    }
  });
}

async function openEditModal(id) {
  // We already have the row's data rendered client-side; simplest reliable way
  // to populate the form without a GET /activities/{id} endpoint is to re-read
  // the currently loaded itinerary's activities and find this one.
  showSpinner();
  try {
    const response = await api.get(`/activities/itinerary/${currentItineraryId}`);
    const act = response.data.find((a) => String(a.id) === String(id));
    if (!act) throw new Error("Activity not found in the current list.");

    document.getElementById("activity-id").value = act.id;
    document.getElementById("activity-itinerary-id").value = currentItineraryId;
    document.getElementById("activity-name").value = act.activityName;
    document.getElementById("activity-scheduled-at").value = (act.scheduledAt || "").slice(0, 16);
    document.getElementById("activity-cost").value = act.estimatedCost;
    document.getElementById("activity-modal-title").textContent = "Edit Planned Activity";
    document.getElementById("activity-form-error").classList.add("d-none");
    activityModalInstance.show();
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

async function deleteActivity(id) {
  const confirmed = await confirmAction("Delete this planned activity? This action cannot be undone.");
  if (!confirmed) return;

  showSpinner();
  try {
    await api.delete(`/activities/${id}`);
    showAlert("Activity deleted.", "success");
    loadActivities(currentItineraryId);
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

/* ---------- Init ---------- */
if (currentItineraryId) {
  loadActivities(currentItineraryId);
}

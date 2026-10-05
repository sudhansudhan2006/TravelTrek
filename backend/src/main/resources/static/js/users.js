/* ============================================================
   TravelTrek - users.js
   Maps to UserController (/api/v1/users), AGENCY_MANAGER only:
     GET   /users            -> List<User>
     PATCH /users/{id}/role   -> RoleUpdateRequest{ role } -> User
     PATCH /users/{id}/status -> StatusUpdateRequest{ active } -> User

   Page guard: requireRole redirects to unauthorized.html for any
   role other than AGENCY_MANAGER (server also enforces this on every
   /api/v1/users/** call, per SecurityConfig).
   ============================================================ */

requireRole(["AGENCY_MANAGER"]);

const me = getCurrentUser();
const content = renderLayout("users");

const ROLES = ["TRAVELER", "TRAVEL_AGENT", "AGENCY_MANAGER"];

content.innerHTML = `
  <div class="page-header">
    <h2 class="mb-0"><i class="bi bi-people"></i> Users</h2>
  </div>

  <div class="note-box mb-3">
    <i class="bi bi-info-circle"></i>
    As an Agency Manager you can change any user's role or activate/deactivate their account.
  </div>

  <div class="card card-tt p-3">
    <div class="table-responsive">
      <table class="table table-tt align-middle mb-0">
        <thead>
          <tr>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th class="text-end">Actions</th>
          </tr>
        </thead>
        <tbody id="users-tbody"></tbody>
      </table>
    </div>
    <div id="users-empty" class="empty-state d-none">
      <i class="bi bi-inbox fs-1"></i>
      <p class="mb-0">No users found.</p>
    </div>
  </div>
`;

function renderUsersTable(users) {
  const tbody = document.getElementById("users-tbody");
  const emptyState = document.getElementById("users-empty");

  if (!users || users.length === 0) {
    tbody.innerHTML = "";
    emptyState.classList.remove("d-none");
    return;
  }
  emptyState.classList.add("d-none");

  tbody.innerHTML = users
    .map(
      (u) => `
      <tr>
        <td>${escapeHtml(u.email)}</td>
        <td>
          <select class="form-select form-select-sm role-select" data-id="${u.id}" style="width: auto;" ${u.email === me.email ? "disabled" : ""}>
            ${ROLES.map((r) => `<option value="${r}" ${r === u.role ? "selected" : ""}>${r}</option>`).join("")}
          </select>
        </td>
        <td>
          ${u.active
            ? '<span class="badge bg-success">Active</span>'
            : '<span class="badge bg-secondary">Inactive</span>'}
        </td>
        <td class="text-end">
          <button class="btn btn-sm ${u.active ? "btn-outline-danger" : "btn-outline-success"} toggle-status-btn"
                  data-id="${u.id}" data-active="${u.active}" ${u.email === me.email ? "disabled" : ""}>
            ${u.active ? "Deactivate" : "Activate"}
          </button>
        </td>
      </tr>`
    )
    .join("");

  document.querySelectorAll(".role-select").forEach((sel) =>
    sel.addEventListener("change", () => updateRole(sel.dataset.id, sel.value))
  );
  document.querySelectorAll(".toggle-status-btn").forEach((btn) =>
    btn.addEventListener("click", () => toggleStatus(btn.dataset.id, btn.dataset.active === "true"))
  );
}

async function loadUsers() {
  showSpinner();
  try {
    const response = await api.get("/users");
    renderUsersTable(response.data);
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

async function updateRole(id, role) {
  showSpinner();
  try {
    await api.patch(`/users/${id}/role`, { role });
    showAlert("User role updated.", "success");
    loadUsers();
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
    loadUsers();
  } finally {
    hideSpinner();
  }
}

async function toggleStatus(id, currentlyActive) {
  const confirmed = await confirmAction(
    currentlyActive ? "Deactivate this user's account?" : "Re-activate this user's account?"
  );
  if (!confirmed) return;

  showSpinner();
  try {
    await api.patch(`/users/${id}/status`, { active: !currentlyActive });
    showAlert("User status updated.", "success");
    loadUsers();
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

/* ---------- Init ---------- */
loadUsers();

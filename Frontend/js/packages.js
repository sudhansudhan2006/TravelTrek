/* ============================================================
   TravelTrek - packages.js
   Maps exactly to PackageController (/api/v1/packages):
     GET    /packages?destination=&page=&size=  -> Page<TravelPackage>
     GET    /packages/{id}                       -> TravelPackage
     POST   /packages   (AGENCY_MANAGER only)     -> PackageRequest -> TravelPackage
     PUT    /packages/{id} (AGENCY_MANAGER only)  -> PackageRequest -> TravelPackage
     DELETE /packages/{id} (AGENCY_MANAGER only)  -> 204 No Content

   PackageRequest = { packageName, destination, basePrice, availableSlots }
   ============================================================ */

requireAuth();

const user = getCurrentUser();
const isManager = user.role === "AGENCY_MANAGER";
const content = renderLayout("packages");

const PAGE_SIZE = 8;
let currentPage = 0;
let currentDestinationFilter = "";

content.innerHTML = `
  <div class="page-header">
    <h2 class="mb-0"><i class="bi bi-suitcase2"></i> Travel Packages</h2>
    ${isManager ? `<button class="btn btn-tt-accent" id="add-package-btn"><i class="bi bi-plus-lg"></i> Add Package</button>` : ""}
  </div>

  ${!isManager ? `<div class="note-box mb-3"><i class="bi bi-info-circle"></i>
    Only users with the <strong>AGENCY_MANAGER</strong> role can add, edit, or delete packages. You can browse and search below.
  </div>` : ""}

  <div class="card card-tt p-3 mb-3">
    <form id="search-form" class="row g-2 align-items-end">
      <div class="col-sm-8 col-md-6">
        <label class="form-label mb-1">Search by destination</label>
        <input type="text" class="form-control" id="destination-filter" placeholder="e.g. Goa">
      </div>
      <div class="col-auto">
        <button type="submit" class="btn btn-tt-primary"><i class="bi bi-search"></i> Search</button>
      </div>
      <div class="col-auto">
        <button type="button" class="btn btn-outline-secondary" id="clear-search-btn">Clear</button>
      </div>
    </form>
  </div>

  <div class="card card-tt p-3">
    <div class="table-responsive">
      <table class="table table-tt align-middle mb-0">
        <thead>
          <tr>
            <th>Package Name</th>
            <th>Destination</th>
            <th>Base Price</th>
            <th>Available Slots</th>
            ${isManager ? "<th class='text-end'>Actions</th>" : ""}
          </tr>
        </thead>
        <tbody id="packages-tbody"></tbody>
      </table>
    </div>
    <div id="packages-empty" class="empty-state d-none">
      <i class="bi bi-inbox fs-1"></i>
      <p class="mb-0">No packages found.</p>
    </div>
  </div>

  <nav class="mt-3">
    <ul class="pagination justify-content-center" id="pagination-controls"></ul>
  </nav>
`;

/* ---------- Rendering ---------- */
function renderPackagesTable(packages) {
  const tbody = document.getElementById("packages-tbody");
  const emptyState = document.getElementById("packages-empty");

  if (!packages || packages.length === 0) {
    tbody.innerHTML = "";
    emptyState.classList.remove("d-none");
    return;
  }
  emptyState.classList.add("d-none");

  tbody.innerHTML = packages
    .map(
      (pkg) => `
      <tr>
        <td>${escapeHtml(pkg.packageName)}</td>
        <td>${escapeHtml(pkg.destination)}</td>
        <td>${formatCurrency(pkg.basePrice)}</td>
        <td>${pkg.availableSlots}</td>
        ${
          isManager
            ? `<td class="text-end">
                <button class="btn btn-sm btn-outline-primary edit-package-btn" data-id="${pkg.id}"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger delete-package-btn" data-id="${pkg.id}"><i class="bi bi-trash"></i></button>
              </td>`
            : ""
        }
      </tr>`
    )
    .join("");

  if (isManager) {
    document.querySelectorAll(".edit-package-btn").forEach((btn) =>
      btn.addEventListener("click", () => openEditModal(btn.dataset.id))
    );
    document.querySelectorAll(".delete-package-btn").forEach((btn) =>
      btn.addEventListener("click", () => deletePackage(btn.dataset.id))
    );
  }
}

function renderPagination(pageData) {
  const el = document.getElementById("pagination-controls");
  const totalPages = pageData.totalPages ?? 1;
  if (totalPages <= 1) {
    el.innerHTML = "";
    return;
  }

  let html = "";
  for (let i = 0; i < totalPages; i++) {
    html += `
      <li class="page-item ${i === currentPage ? "active" : ""}">
        <button class="page-link page-jump-btn" data-page="${i}">${i + 1}</button>
      </li>`;
  }
  el.innerHTML = html;

  document.querySelectorAll(".page-jump-btn").forEach((btn) =>
    btn.addEventListener("click", () => {
      currentPage = Number(btn.dataset.page);
      loadPackages();
    })
  );
}

/* ---------- Data loading ---------- */
async function loadPackages() {
  showSpinner();
  try {
    const params = { page: currentPage, size: PAGE_SIZE };
    if (currentDestinationFilter) params.destination = currentDestinationFilter;

    const response = await api.get("/packages", { params });
    const pageData = response.data;
    // Spring Data Page shape: { content, totalPages, totalElements, number, ... }
    renderPackagesTable(pageData.content);
    renderPagination(pageData);
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

/* ---------- Search ---------- */
document.getElementById("search-form").addEventListener("submit", (e) => {
  e.preventDefault();
  currentDestinationFilter = document.getElementById("destination-filter").value.trim();
  currentPage = 0;
  loadPackages();
});

document.getElementById("clear-search-btn").addEventListener("click", () => {
  document.getElementById("destination-filter").value = "";
  currentDestinationFilter = "";
  currentPage = 0;
  loadPackages();
});

/* ---------- Add / Edit modal (manager only) ---------- */
let packageModalInstance;

if (isManager) {
  packageModalInstance = new bootstrap.Modal(document.getElementById("package-modal"));

  document.getElementById("add-package-btn").addEventListener("click", () => {
    document.getElementById("package-form").reset();
    document.getElementById("package-id").value = "";
    document.getElementById("package-modal-title").textContent = "Add Package";
    document.getElementById("package-form-error").classList.add("d-none");
    packageModalInstance.show();
  });

  document.getElementById("package-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const errorBox = document.getElementById("package-form-error");
    errorBox.classList.add("d-none");

    const id = document.getElementById("package-id").value;
    const payload = {
      packageName: document.getElementById("package-name").value.trim(),
      destination: document.getElementById("package-destination").value.trim(),
      basePrice: Number(document.getElementById("package-price").value),
      availableSlots: Number(document.getElementById("package-slots").value),
    };

    showSpinner();
    try {
      if (id) {
        await api.put(`/packages/${id}`, payload);
        showAlert("Package updated successfully.", "success");
      } else {
        await api.post("/packages", payload);
        showAlert("Package added successfully.", "success");
      }
      packageModalInstance.hide();
      loadPackages();
    } catch (error) {
      errorBox.textContent = getErrorMessage(error);
      errorBox.classList.remove("d-none");
    } finally {
      hideSpinner();
    }
  });
}

async function openEditModal(id) {
  showSpinner();
  try {
    const response = await api.get(`/packages/${id}`);
    const pkg = response.data;
    document.getElementById("package-id").value = pkg.id;
    document.getElementById("package-name").value = pkg.packageName;
    document.getElementById("package-destination").value = pkg.destination;
    document.getElementById("package-price").value = pkg.basePrice;
    document.getElementById("package-slots").value = pkg.availableSlots;
    document.getElementById("package-modal-title").textContent = "Edit Package";
    document.getElementById("package-form-error").classList.add("d-none");
    packageModalInstance.show();
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

async function deletePackage(id) {
  const confirmed = await confirmAction("Delete this travel package? This action cannot be undone.");
  if (!confirmed) return;

  showSpinner();
  try {
    await api.delete(`/packages/${id}`);
    showAlert("Package deleted.", "success");
    loadPackages();
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

/* ---------- Init ---------- */
loadPackages();

/* ============================================================
   TravelTrek - bookings.js
   Maps to BookingController (/api/v1/bookings):
     POST  /bookings             (TRAVELER only)     -> BookingRequest{ packageId } -> 201
     GET   /bookings/my                                -> the caller's own bookings
     GET   /bookings             (TRAVEL_AGENT/AGENCY_MANAGER) -> every booking
     PATCH /bookings/{id}/cancel (owner TRAVELER or AGENCY_MANAGER) -> status -> CANCELLED

   RBAC on this page:
     - TRAVELER sees only THEIR OWN bookings, can book a package, and can cancel
       only their own booking.
     - TRAVEL_AGENT sees EVERY traveler's booking (read-only - "assist travelers"),
       cannot book or cancel on anyone's behalf.
     - AGENCY_MANAGER sees EVERY booking and can cancel any of them (full access).
   ============================================================ */

requireAuth();

const user = getCurrentUser();
const role = user.role;
const isTraveler = role === "TRAVELER";
const isManager = role === "AGENCY_MANAGER";

const content = renderLayout("bookings");

const pageTitle = isTraveler ? "My Bookings" : isManager ? "All Bookings" : "Traveler Bookings";

content.innerHTML = `
  <div class="page-header">
    <h2 class="mb-0"><i class="bi bi-journal-check"></i> ${pageTitle}</h2>
    ${isTraveler ? `<button class="btn btn-tt-accent" id="add-booking-btn"><i class="bi bi-plus-lg"></i> Book a Package</button>` : ""}
  </div>

  ${!isTraveler ? `<div class="note-box mb-3"><i class="bi bi-info-circle"></i>
    ${isManager
      ? "As an Agency Manager you can view and cancel any booking in the system."
      : "You can view every traveler's booking to assist them. Only the traveler who owns a booking (or a manager) can cancel it."}
  </div>` : ""}

  <div class="card card-tt p-3">
    <div class="table-responsive">
      <table class="table table-tt align-middle mb-0">
        <thead>
          <tr>
            <th>Booking Reference</th>
            ${!isTraveler ? "<th>Traveler</th>" : ""}
            <th>Status</th>
            <th class="text-end">Actions</th>
          </tr>
        </thead>
        <tbody id="bookings-tbody"></tbody>
      </table>
    </div>
    <div id="bookings-empty" class="empty-state d-none">
      <i class="bi bi-inbox fs-1"></i>
      <p class="mb-0">${isTraveler ? 'You have no bookings yet. Click "Book a Package" to create one.' : "No bookings found."}</p>
    </div>
  </div>
`;

function statusBadge(status) {
  const map = {
    CONFIRMED: "bg-success",
    PENDING: "bg-warning text-dark",
    CANCELLED: "bg-secondary",
  };
  const cls = map[status] || "bg-secondary";
  // Display status exactly as returned by the backend, only the badge color is derived from it.
  return `<span class="badge ${cls}">${escapeHtml(status)}</span>`;
}

// Cancel button rule: TRAVELER can cancel only their own booking; AGENCY_MANAGER
// can cancel any booking; TRAVEL_AGENT can never cancel (view-only).
function canCancel(booking) {
  if (booking.status === "CANCELLED") return false;
  if (isManager) return true;
  if (isTraveler) return booking.user && booking.user.email === user.email;
  return false;
}

function renderBookingsTable(bookings) {
  const tbody = document.getElementById("bookings-tbody");
  const emptyState = document.getElementById("bookings-empty");

  if (!bookings || bookings.length === 0) {
    tbody.innerHTML = "";
    emptyState.classList.remove("d-none");
    return;
  }
  emptyState.classList.add("d-none");

  tbody.innerHTML = bookings
    .map(
      (b) => `
      <tr>
        <td>${escapeHtml(b.bookingReference)}</td>
        ${!isTraveler ? `<td>${escapeHtml(b.user ? b.user.email : "-")}</td>` : ""}
        <td>${statusBadge(b.status)}</td>
        <td class="text-end">
          ${
            canCancel(b)
              ? `<button class="btn btn-sm btn-outline-danger cancel-booking-btn" data-id="${b.id}">
                  <i class="bi bi-x-circle"></i> Cancel
                </button>`
              : `<span class="text-secondary">-</span>`
          }
        </td>
      </tr>`
    )
    .join("");

  document.querySelectorAll(".cancel-booking-btn").forEach((btn) =>
    btn.addEventListener("click", () => cancelBooking(btn.dataset.id))
  );
}

async function loadBookings() {
  showSpinner();
  try {
    // Travelers only ever see their own; agents/managers see every booking.
    const endpoint = isTraveler ? "/bookings/my" : "/bookings";
    const response = await api.get(endpoint);
    renderBookingsTable(response.data);
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

async function cancelBooking(id) {
  const confirmed = await confirmAction("Cancel this booking?");
  if (!confirmed) return;

  showSpinner();
  try {
    await api.patch(`/bookings/${id}/cancel`);
    showAlert("Booking cancelled.", "success");
    loadBookings();
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

/* ---------- Create booking (TRAVELER only) ---------- */
if (isTraveler) {
  const bookingModal = new bootstrap.Modal(document.getElementById("booking-modal"));

  document.getElementById("add-booking-btn").addEventListener("click", async () => {
    document.getElementById("booking-form").reset();
    document.getElementById("booking-form-error").classList.add("d-none");
    bookingModal.show();
    await populatePackageOptions();
  });

  // Loads available packages into the <select> so the user can pick one by name.
  async function populatePackageOptions() {
    const select = document.getElementById("booking-package-select");
    select.innerHTML = `<option value="" selected disabled>Loading packages...</option>`;
    try {
      const response = await api.get("/packages", { params: { page: 0, size: 100 } });
      const packages = response.data.content || [];
      if (packages.length === 0) {
        select.innerHTML = `<option value="" selected disabled>No packages available</option>`;
        return;
      }
      select.innerHTML =
        `<option value="" selected disabled>Choose a package...</option>` +
        packages
          .map(
            (pkg) =>
              `<option value="${pkg.id}">${escapeHtml(pkg.packageName)} - ${escapeHtml(pkg.destination)} (${formatCurrency(pkg.basePrice)})</option>`
          )
          .join("");
    } catch (error) {
      select.innerHTML = `<option value="" selected disabled>Failed to load packages</option>`;
      showAlert(getErrorMessage(error), "danger");
    }
  }

  document.getElementById("booking-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const errorBox = document.getElementById("booking-form-error");
    errorBox.classList.add("d-none");

    const packageId = Number(document.getElementById("booking-package-select").value);

    showSpinner();
    try {
      await api.post("/bookings", { packageId });
      showAlert("Package booked successfully.", "success");
      bookingModal.hide();
      loadBookings();
    } catch (error) {
      errorBox.textContent = getErrorMessage(error);
      errorBox.classList.remove("d-none");
    } finally {
      hideSpinner();
    }
  });
}

/* ---------- Init ---------- */
loadBookings();

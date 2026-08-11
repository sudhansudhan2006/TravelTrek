/* ============================================================
   TravelTrek - profile.js
   Maps to ProfileController (/api/v1/profile):
     GET /profile/me -> the caller's own account
     PUT /profile/me -> update the caller's own email and/or password

   Available to every role (TRAVELER, TRAVEL_AGENT, AGENCY_MANAGER) -
   "Update only their own profile" from the RBAC spec. There is no
   user-id in this request; the backend always resolves the target
   account from the JWT, so a user can never touch another account here.
   ============================================================ */

requireAuth();

const content = renderLayout("profile");

content.innerHTML = `
  <div class="page-header">
    <h2 class="mb-0"><i class="bi bi-person-circle"></i> My Profile</h2>
  </div>

  <div class="card card-tt p-4" style="max-width: 520px;">
    <form id="profile-form">
      <div class="mb-3">
        <label for="profile-email" class="form-label">Email address</label>
        <input type="email" class="form-control" id="profile-email" required>
      </div>
      <div class="mb-3">
        <label class="form-label">Role</label>
        <input type="text" class="form-control" id="profile-role" disabled>
        <div class="form-text">Only an Agency Manager can change your role.</div>
      </div>
      <div class="mb-3">
        <label for="profile-new-password" class="form-label">New Password</label>
        <input type="password" class="form-control" id="profile-new-password" minlength="6" placeholder="Leave blank to keep your current password">
      </div>
      <div id="profile-form-error" class="alert alert-danger d-none"></div>
      <div id="profile-form-success" class="alert alert-success d-none"></div>
      <button type="submit" class="btn btn-tt-primary" id="profile-save-btn">Save Changes</button>
    </form>
  </div>
`;

async function loadProfile() {
  showSpinner();
  try {
    const response = await api.get("/profile/me");
    const me = response.data;
    document.getElementById("profile-email").value = me.email;
    document.getElementById("profile-role").value = me.role;
  } catch (error) {
    showAlert(getErrorMessage(error), "danger");
  } finally {
    hideSpinner();
  }
}

document.getElementById("profile-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const errorBox = document.getElementById("profile-form-error");
  const successBox = document.getElementById("profile-form-success");
  errorBox.classList.add("d-none");
  successBox.classList.add("d-none");

  const payload = {
    email: document.getElementById("profile-email").value.trim(),
    newPassword: document.getElementById("profile-new-password").value,
  };

  showSpinner();
  try {
    const response = await api.put("/profile/me", payload);
    // Keep the stored session email in sync with what was just saved
    localStorage.setItem("tt_email", response.data.email);
    document.getElementById("profile-new-password").value = "";
    successBox.textContent = "Profile updated successfully.";
    successBox.classList.remove("d-none");
    showAlert("Profile updated.", "success");
  } catch (error) {
    errorBox.textContent = getErrorMessage(error);
    errorBox.classList.remove("d-none");
  } finally {
    hideSpinner();
  }
});

/* ---------- Init ---------- */
loadProfile();

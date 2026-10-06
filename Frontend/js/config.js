/* ============================================================
   TravelTrek - config.js
   Central runtime configuration for TravelTrek frontend.
   Use this file to configure your deployed backend API URL.
   ============================================================ */

window.APP_CONFIG = {
  // Production deployed backend base URL (without trailing slash).
  // Once you deploy your Spring Boot backend (e.g., on Render, Railway, AWS, Fly.io),
  // update this URL to your deployed backend domain.
  // Example: "https://traveltrek-backend.onrender.com/api/v1"
  PROD_API_BASE_URL: "https://traveltrek-production.up.railway.app/api/v1",

  // Local development backend URL
  DEV_API_BASE_URL: "http://localhost:8080/api/v1",

  /**
   * Resolves the active backend API base URL:
   * 1. Checks localStorage "tt_api_base_url" for a runtime override (great for testing).
   * 2. If running locally (localhost / 127.0.0.1), returns DEV_API_BASE_URL.
   * 3. Otherwise (in production on Vercel), returns PROD_API_BASE_URL.
   */
  getApiBaseUrl() {
    const override = localStorage.getItem("tt_api_base_url");
    if (override && override.trim()) {
      return override.trim().replace(/\/+$/, "");
    }

    const hostname = window.location.hostname;
    const isLocal = Boolean(
      hostname === "localhost" ||
      hostname === "127.0.0.1" ||
      hostname.endsWith(".local")
    );

    if (isLocal) {
      return this.DEV_API_BASE_URL.replace(/\/+$/, "");
    }

    return this.PROD_API_BASE_URL.replace(/\/+$/, "");
  }
};

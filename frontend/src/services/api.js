/**
 * AquaSense AI - API Service Layer
 * Centralized HTTP client communicating with Java Spring Boot backend
 */

const BASE_URL = '';

export async function fetchJson(endpoint, options = {}) {
  try {
    const res = await fetch(`${BASE_URL}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {})
      },
      ...options
    });
    if (!res.ok) {
      const errText = await res.text();
      throw new Error(`HTTP ${res.status}: ${errText || res.statusText}`);
    }
    return await res.json();
  } catch (err) {
    console.warn(`API call failed for ${endpoint}:`, err.message);
    throw err;
  }
}

// -----------------------------------------------------------------------------
// Unified Intelligence Endpoints (Spring Boot)
// -----------------------------------------------------------------------------
export async function getUnifiedDashboard(reservoir = 'B.A. Steinhagen', modelType = 'linear_regression', horizonDays = 14) {
  return fetchJson(`/api/v1/intelligence/dashboard?reservoir=${encodeURIComponent(reservoir)}&modelType=${modelType}&horizonDays=${horizonDays}`);
}

export async function getFutureForecast(modelType = 'linear_regression', horizonDays = 14, confidenceLevel = 0.95) {
  return fetchJson('/api/v1/intelligence/forecast', {
    method: 'POST',
    body: JSON.stringify({
      model_type: modelType,
      horizon_days: horizonDays,
      confidence_level: confidenceLevel
    })
  });
}

export async function getSustainabilityAssessment() {
  return fetchJson('/api/v1/intelligence/sustainability');
}

export async function getModelBenchmark() {
  return fetchJson('/api/v1/intelligence/benchmark');
}

// -----------------------------------------------------------------------------
// Water Body Management Endpoints
// -----------------------------------------------------------------------------
export async function getWaterBodies() {
  return fetchJson('/api/v1/water-bodies');
}

export async function getWaterBodyById(id) {
  return fetchJson(`/api/v1/water-bodies/${id}`);
}

export async function createWaterBody(data) {
  return fetchJson('/api/v1/water-bodies', {
    method: 'POST',
    body: JSON.stringify(data)
  });
}

export async function updateWaterBody(id, data) {
  return fetchJson(`/api/v1/water-bodies/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  });
}

export async function deleteWaterBody(id) {
  return fetch(`/api/v1/water-bodies/${id}`, { method: 'DELETE' });
}

// -----------------------------------------------------------------------------
// Hydrological Observations Endpoints
// -----------------------------------------------------------------------------
export async function getObservations(reservoir = 'B.A. Steinhagen', page = 0, size = 50, startDate = null, endDate = null) {
  let url = `/api/v1/observations?reservoir=${encodeURIComponent(reservoir)}&page=${page}&size=${size}`;
  if (startDate) url += `&startDate=${startDate}`;
  if (endDate) url += `&endDate=${endDate}`;
  return fetchJson(url);
}

export async function getLatestObservation(reservoir = 'B.A. Steinhagen') {
  return fetchJson(`/api/v1/observations/latest?reservoir=${encodeURIComponent(reservoir)}`);
}

export async function createObservation(data) {
  return fetchJson('/api/v1/observations', {
    method: 'POST',
    body: JSON.stringify(data)
  });
}

// -----------------------------------------------------------------------------
// Dataset Processing & ML Integration
// -----------------------------------------------------------------------------
export async function triggerDataCleaning() {
  return fetchJson('/api/v1/process-data', { method: 'POST' });
}

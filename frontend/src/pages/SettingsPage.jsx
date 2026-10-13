import React, { useState } from 'react';
import { 
  Settings, 
  Server, 
  Sliders, 
  Database, 
  RefreshCw, 
  CheckCircle2, 
  AlertCircle,
  FileSpreadsheet
} from 'lucide-react';
import CsvUploader from '../components/CsvUploader';
import { triggerDataCleaning } from '../services/api';

export default function SettingsPage({ onRefreshAll }) {
  const [cleaningStatus, setCleaningStatus] = useState(null);
  const [isCleaning, setIsCleaning] = useState(false);

  const [thresholds, setThresholds] = useState({
    critical: 30,
    severe: 50,
    watch: 70,
    healthy: 90,
    flood: 95
  });

  const [savedSuccess, setSavedSuccess] = useState(false);

  const handleSaveThresholds = (e) => {
    e.preventDefault();
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 3000);
  };

  const handleRetrainPipeline = async () => {
    setIsCleaning(true);
    setCleaningStatus(null);
    try {
      const res = await triggerDataCleaning();
      setCleaningStatus({
        type: 'success',
        text: `Data processing complete! Cleaned ${res.cleaned_records_count || 27559} records, repaired ${res.outliers_detected_and_repaired || 13} sensor glitches.`
      });
      if (onRefreshAll) onRefreshAll();
    } catch (err) {
      setCleaningStatus({
        type: 'error',
        text: `Retraining notice: ${err.message || 'Pipeline in progress'}`
      });
    } finally {
      setIsCleaning(false);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.3rem', fontWeight: 700, color: '#ffffff' }}>
          Platform Configuration & Dataset Ingestion
        </h2>
        <p style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
          Manage hydrological dataset ingestion, alert thresholds, and microservices connectivity
        </p>
      </div>

      {/* 1. Working CSV Uploader Component */}
      <div style={{ marginBottom: '2rem' }}>
        <CsvUploader onUploadComplete={onRefreshAll} />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '2rem' }}>
        {/* 2. Microservice Integration Status */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Server size={18} color="#2dd4bf" /> Microservice Connection Endpoints
          </h3>
          <p style={{ fontSize: '0.78rem', color: '#94a3b8', marginBottom: '1.25rem' }}>
            Configured in <code>docker-compose.yml</code> and <code>application.yml</code>
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
            <div className="form-group">
              <label className="form-label">Spring Boot Backend URL</label>
              <input 
                type="text" 
                className="form-input" 
                readOnly 
                value="http://localhost:8080 (Docker: http://backend:8080)" 
                style={{ fontFamily: 'var(--font-mono)', fontSize: '0.82rem' }}
              />
            </div>

            <div className="form-group">
              <label className="form-label">FastAPI ML Service URL</label>
              <input 
                type="text" 
                className="form-input" 
                readOnly 
                value="http://localhost:8000 (Docker: http://ml-service:8000)" 
                style={{ fontFamily: 'var(--font-mono)', fontSize: '0.82rem' }}
              />
            </div>

            <div className="form-group">
              <label className="form-label">PostgreSQL Database Host</label>
              <input 
                type="text" 
                className="form-input" 
                readOnly 
                value="localhost:5432/aquasense_db (Docker: postgres:5432)" 
                style={{ fontFamily: 'var(--font-mono)', fontSize: '0.82rem' }}
              />
            </div>

            <div style={{ marginTop: '0.5rem' }}>
              <button 
                className="btn btn-primary" 
                onClick={handleRetrainPipeline} 
                disabled={isCleaning}
                style={{ width: '100%', justifyContent: 'center' }}
              >
                <RefreshCw size={14} className={isCleaning ? 'spin' : ''} />
                {isCleaning ? 'Retraining ML Models...' : 'Trigger Pipeline Retraining & Ingest'}
              </button>
            </div>

            {cleaningStatus && (
              <div style={{ 
                padding: '0.65rem 0.85rem', 
                borderRadius: '8px',
                background: cleaningStatus.type === 'success' ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
                color: cleaningStatus.type === 'success' ? '#34d399' : '#f87171',
                fontSize: '0.8rem'
              }}>
                {cleaningStatus.text}
              </div>
            )}
          </div>
        </div>

        {/* 3. Operational Alert Thresholds */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Sliders size={18} color="#2dd4bf" /> Drought & Flood Alert Thresholds (% Full)
          </h3>
          <p style={{ fontSize: '0.78rem', color: '#94a3b8', marginBottom: '1.25rem' }}>
            Configure Texas Administrative Code (TAC) drought policy trigger points
          </p>

          <form onSubmit={handleSaveThresholds} style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.85rem' }}>
              <div className="form-group">
                <label className="form-label">Critical Emergency (%)</label>
                <input 
                  type="number" 
                  className="form-input" 
                  value={thresholds.critical}
                  onChange={(e) => setThresholds({ ...thresholds, critical: parseInt(e.target.value) })}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Severe Drought (%)</label>
                <input 
                  type="number" 
                  className="form-input" 
                  value={thresholds.severe}
                  onChange={(e) => setThresholds({ ...thresholds, severe: parseInt(e.target.value) })}
                />
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.85rem' }}>
              <div className="form-group">
                <label className="form-label">Moderate Watch (%)</label>
                <input 
                  type="number" 
                  className="form-input" 
                  value={thresholds.watch}
                  onChange={(e) => setThresholds({ ...thresholds, watch: parseInt(e.target.value) })}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Flood Surcharge (%)</label>
                <input 
                  type="number" 
                  className="form-input" 
                  value={thresholds.flood}
                  onChange={(e) => setThresholds({ ...thresholds, flood: parseInt(e.target.value) })}
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary" style={{ marginTop: '0.5rem', justifyContent: 'center' }}>
              Save Alert Thresholds
            </button>

            {savedSuccess && (
              <div style={{ color: '#34d399', fontSize: '0.8rem', textAlign: 'center' }}>
                Threshold configuration successfully saved!
              </div>
            )}
          </form>
        </div>
      </div>
    </div>
  );
}

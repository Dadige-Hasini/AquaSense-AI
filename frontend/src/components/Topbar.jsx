import React from 'react';
import { RefreshCw, MapPin, Database } from 'lucide-react';

export default function Topbar({ 
  title, 
  waterBodies = [], 
  selectedWaterBody, 
  onSelectWaterBody, 
  onRefresh, 
  refreshing = false 
}) {
  return (
    <header className="topbar">
      <div className="topbar-left">
        <h1 className="page-title">{title}</h1>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#94a3b8', fontSize: '0.85rem' }}>
          <MapPin size={14} color="#14b8a6" />
          <select 
            className="reservoir-selector"
            value={selectedWaterBody?.name || 'B.A. Steinhagen'}
            onChange={(e) => {
              const found = waterBodies.find(b => b.name === e.target.value);
              if (found) onSelectWaterBody(found);
            }}
          >
            {waterBodies.map((wb, idx) => (
              <option key={wb.id || idx} value={wb.name}>
                {wb.name} ({wb.riverBasin || 'Texas'})
              </option>
            ))}
            {waterBodies.length === 0 && (
              <option value="B.A. Steinhagen">B.A. Steinhagen (Neches Basin)</option>
            )}
          </select>
        </div>
      </div>

      <div className="topbar-right">
        <button 
          className="btn" 
          onClick={onRefresh} 
          disabled={refreshing}
          title="Refresh live telemetry from backend"
        >
          <RefreshCw size={14} className={refreshing ? 'spin' : ''} />
          <span>{refreshing ? 'Syncing...' : 'Sync Data'}</span>
        </button>

        <div className="status-indicator">
          <span className="pulse-dot"></span>
          <span>Live Sensor Telemetry</span>
        </div>
      </div>
    </header>
  );
}

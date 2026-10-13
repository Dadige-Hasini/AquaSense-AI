import React from 'react';
import { 
  ShieldCheck, 
  AlertTriangle, 
  Droplets, 
  Activity, 
  Compass, 
  CheckCircle2, 
  ArrowRight,
  TrendingUp,
  Clock,
  Info
} from 'lucide-react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer, 
  ReferenceLine,
  Cell 
} from 'recharts';

export default function SustainabilityPage({ dashboardData }) {
  const sustainability = dashboardData?.sustainability || {
    droughtStage: 'High Storage / Flood Surcharge Watch',
    waterStressLevel: 'Spillway Discharge Monitoring',
    riskColorCode: '#06b6d4',
    depletionRate7d: 128.4,
    depletionRate30d: -24.5,
    trendDirection: 'Rapid Inflow / Refilling',
    sustainabilityScore: 100.0,
    sustainabilityGrade: 'A (Highly Sustainable)',
    advisorySummary: 'Reservoir storage is at 96.6% capacity (66,931 acre-ft), exceeding normal conservation pool. Monitor dam gates for controlled spillway releases.',
    actions: [
      'Coordinate with USACE Town Bluff Dam operators for controlled gate releases',
      'Monitor downstream stage gages on lower Neches River near Evadale and Beaumont',
      'Verify spillway gate readiness and debris clearances',
      'Maintain standard continuous automated telemetry monitoring'
    ],
    forecast30dOutlook: {
      forecastDate30d: '2026-11-12',
      projectedStorageAcreFeet: 63266.35,
      projectedPercentFull: 91.34,
      net30dProjectedChangeAcreFeet: -3664.65
    }
  };

  const latest = dashboardData?.latestObservation || {
    reservoirStorage: 66931,
    percentFull: 96.6,
    conservationCapacity: 69186,
    waterLevel: 82.4
  };

  // Drought thresholds comparison chart
  const thresholdComparison = [
    { name: 'Critical Emergency (Stage 4)', pct: 30, volume: 20755, color: '#ef4444' },
    { name: 'Severe Drought (Stage 3)', pct: 50, volume: 34593, color: '#f97316' },
    { name: 'Moderate Watch (Stage 2)', pct: 70, volume: 48430, color: '#f59e0b' },
    { name: 'Sustainable Pool (Normal)', pct: 90, volume: 62267, color: '#10b981' },
    { name: 'Current Observed Level', pct: 96.6, volume: 66931, color: '#2dd4bf' },
    { name: 'Full Conservation Capacity', pct: 100, volume: 69186, color: '#0284c7' },
  ];

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.3rem', fontWeight: 700, color: '#ffffff' }}>
          Rule-Based Hydrological Sustainability & Drought Assessment
        </h2>
        <p style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
          Real-time compliance evaluations adhering to Texas Water Development Board (TWDB) drought stages & Neches River operational rule curves
        </p>
      </div>

      {/* Main Scorecard Banner */}
      <div 
        className="card" 
        style={{ 
          marginBottom: '2rem', 
          borderLeft: `5px solid ${sustainability.riskColorCode || '#14b8a6'}`,
          background: 'linear-gradient(135deg, rgba(14,26,54,0.95), rgba(10,19,41,0.8))'
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1.5rem', marginBottom: '1.25rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.4rem' }}>
              <Compass size={22} color={sustainability.riskColorCode || '#2dd4bf'} />
              <span style={{ fontSize: '0.82rem', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
                HYDROLOGICAL STRESS CLASSIFICATION
              </span>
            </div>
            <h1 style={{ fontSize: '1.85rem', fontWeight: 800, color: '#ffffff', letterSpacing: '-0.02em' }}>
              {sustainability.droughtStage}
            </h1>
            <div style={{ color: '#cbd5e1', fontSize: '0.95rem', marginTop: '0.4rem', maxWidth: '800px', lineHeight: 1.6 }}>
              {sustainability.advisorySummary}
            </div>
          </div>

          <div style={{ textAlign: 'right', background: 'rgba(6, 11, 24, 0.7)', padding: '1rem 1.5rem', borderRadius: '16px', border: '1px solid var(--border-teal-subtle)' }}>
            <div style={{ fontSize: '0.75rem', color: '#94a3b8', textTransform: 'uppercase', marginBottom: '0.2rem' }}>
              SUSTAINABILITY INDEX
            </div>
            <div style={{ fontSize: '2.4rem', fontWeight: 800, color: '#2dd4bf', lineHeight: 1 }}>
              {sustainability.sustainabilityScore || 100}
              <span style={{ fontSize: '1rem', color: '#64748b' }}>/100</span>
            </div>
            <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#10b981', marginTop: '0.3rem' }}>
              {sustainability.sustainabilityGrade}
            </div>
          </div>
        </div>

        {/* Metric Badges */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem' }}>
          <div style={{ background: 'rgba(10, 19, 41, 0.6)', padding: '0.85rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)' }}>
            <div style={{ fontSize: '0.72rem', color: '#94a3b8', textTransform: 'uppercase' }}>7-Day Storage Velocity</div>
            <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff', marginTop: '0.2rem' }}>
              {sustainability.depletionRate7d > 0 ? '+' : ''}{sustainability.depletionRate7d} acre-ft/day
            </div>
            <div style={{ fontSize: '0.75rem', color: '#2dd4bf' }}>{sustainability.trendDirection}</div>
          </div>

          <div style={{ background: 'rgba(10, 19, 41, 0.6)', padding: '0.85rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)' }}>
            <div style={{ fontSize: '0.72rem', color: '#94a3b8', textTransform: 'uppercase' }}>Water Stress Rating</div>
            <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff', marginTop: '0.2rem' }}>
              {sustainability.waterStressLevel}
            </div>
            <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Neches Basin Standard</div>
          </div>

          <div style={{ background: 'rgba(10, 19, 41, 0.6)', padding: '0.85rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)' }}>
            <div style={{ fontSize: '0.72rem', color: '#94a3b8', textTransform: 'uppercase' }}>Days to Critical 30% Reserve</div>
            <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#34d399', marginTop: '0.2rem' }}>
              Indefinite (Surplus Inflow)
            </div>
            <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Buffer: 46,176 ac-ft above critical</div>
          </div>

          <div style={{ background: 'rgba(10, 19, 41, 0.6)', padding: '0.85rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)' }}>
            <div style={{ fontSize: '0.72rem', color: '#94a3b8', textTransform: 'uppercase' }}>Conservation Pool Capacity</div>
            <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff', marginTop: '0.2rem' }}>
              {Number(latest.conservationCapacity).toLocaleString()} acre-ft
            </div>
            <div style={{ fontSize: '0.75rem', color: '#38bdf8' }}>Top of Conservation: 82.5 ft msl</div>
          </div>
        </div>
      </div>

      {/* Threshold Comparison Chart & Actionable Directives */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: '1.5rem', marginBottom: '2rem' }}>
        {/* Recharts Threshold Chart */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.35rem' }}>
            Current Volume vs Drought Operating Thresholds
          </h3>
          <p style={{ fontSize: '0.78rem', color: '#94a3b8', marginBottom: '1.25rem' }}>
            Operating tiers defined under Title 30 Texas Administrative Code (TAC) Chapter 288
          </p>

          <div style={{ height: 280 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={thresholdComparison} layout="vertical" margin={{ top: 5, right: 30, left: 40, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" horizontal={false} />
                <XAxis type="number" stroke="#64748b" fontSize={11} domain={[0, 75000]} tickLine={false} />
                <YAxis type="category" dataKey="name" stroke="#cbd5e1" fontSize={11} tickLine={false} width={130} />
                <Tooltip 
                  contentStyle={{ background: '#0a1329', borderColor: 'rgba(20, 184, 166, 0.3)', borderRadius: '10px' }}
                  formatter={(val) => [`${val.toLocaleString()} acre-ft`, 'Threshold Volume']}
                />
                <Bar dataKey="volume" radius={[0, 6, 6, 0]}>
                  {thresholdComparison.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Actionable Recommendations */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ShieldCheck size={18} color="#2dd4bf" /> Actionable Operator Advisories
          </h3>
          <p style={{ fontSize: '0.78rem', color: '#94a3b8', marginBottom: '1rem' }}>
            Official operational procedures triggered for current reservoir state
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
            {(sustainability.actions || []).map((action, i) => (
              <div 
                key={i} 
                style={{ 
                  background: 'rgba(10, 19, 41, 0.6)', 
                  border: '1px solid rgba(20, 184, 166, 0.15)',
                  padding: '0.75rem', 
                  borderRadius: '10px',
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '0.6rem'
                }}
              >
                <CheckCircle2 size={15} color="#14b8a6" style={{ marginTop: '0.15rem', flexShrink: 0 }} />
                <span style={{ fontSize: '0.82rem', color: '#e2e8f0', lineHeight: 1.4 }}>
                  {action}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

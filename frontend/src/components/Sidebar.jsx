import React from 'react';
import { 
  LayoutDashboard, 
  Waves, 
  BarChart3, 
  TrendingUp, 
  ShieldCheck, 
  Cpu, 
  FileText, 
  Settings,
  Droplets
} from 'lucide-react';

export default function Sidebar({ currentPage, onNavigate }) {
  const navItems = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'water-bodies', label: 'Water Bodies', icon: Waves },
    { id: 'historical', label: 'Historical Analytics', icon: BarChart3 },
    { id: 'predictions', label: 'Future Predictions', icon: TrendingUp },
    { id: 'sustainability', label: 'Sustainability Assessment', icon: ShieldCheck },
    { id: 'performance', label: 'Model Performance', icon: Cpu },
    { id: 'reports', label: 'Reports', icon: FileText },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="sidebar-brand-icon">
          <Droplets size={22} color="#ffffff" />
        </div>
        <div>
          <div className="sidebar-brand-title">AquaSense AI</div>
          <div className="sidebar-brand-sub">Hydrological Intelligence</div>
        </div>
      </div>

      <nav className="sidebar-nav">
        {navItems.map(item => {
          const Icon = item.icon;
          const isActive = currentPage === item.id;
          return (
            <button
              key={item.id}
              className={`nav-item ${isActive ? 'active' : ''}`}
              onClick={() => onNavigate(item.id)}
            >
              <Icon size={18} color={isActive ? '#2dd4bf' : '#94a3b8'} />
              <span>{item.label}</span>
            </button>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <div style={{ fontSize: '0.75rem', color: '#64748b', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
          <span className="pulse-dot" style={{ width: '6px', height: '6px' }}></span>
          Spring Boot & ML Active
        </div>
      </div>
    </aside>
  );
}

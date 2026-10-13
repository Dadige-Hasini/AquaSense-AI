import React, { useState, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import Topbar from './components/Topbar';
import OverviewPage from './pages/OverviewPage';
import WaterBodiesPage from './pages/WaterBodiesPage';
import HistoricalPage from './pages/HistoricalPage';
import PredictionsPage from './pages/PredictionsPage';
import SustainabilityPage from './pages/SustainabilityPage';
import PerformancePage from './pages/PerformancePage';
import ReportsPage from './pages/ReportsPage';
import SettingsPage from './pages/SettingsPage';
import { getUnifiedDashboard, getWaterBodies } from './services/api';

export default function App() {
  const [currentPage, setCurrentPage] = useState('overview');
  const [waterBodies, setWaterBodies] = useState([
    {
      id: 1,
      name: 'B.A. Steinhagen',
      state: 'Texas',
      county: 'Tyler / Jasper',
      riverBasin: 'Neches River Basin',
      conservationCapacity: 69186,
      conservationPoolElevation: 82.5,
      surfaceArea: 13700,
      damName: 'Town Bluff Dam / Dam B',
      managingAgency: 'US Army Corps of Engineers / TWDB',
      status: 'ACTIVE'
    }
  ]);
  const [selectedWaterBody, setSelectedWaterBody] = useState(waterBodies[0]);
  const [dashboardData, setDashboardData] = useState(null);
  const [refreshing, setRefreshing] = useState(false);

  const pageTitles = {
    overview: 'Executive Hydrological Overview',
    'water-bodies': 'Water Bodies & Reservoir Catalog',
    historical: 'Historical Hydrological Analytics (1951 - 2026)',
    predictions: 'Future Hydro-Forecasting Studio',
    sustainability: 'Sustainability & Drought Assessment',
    performance: 'Model Performance & Benchmark Studio',
    reports: 'Hydrological Intelligence Reports',
    settings: 'Platform Settings & CSV Ingestion'
  };

  const loadWaterBodiesList = async () => {
    try {
      const bodies = await getWaterBodies();
      if (bodies && Array.isArray(bodies) && bodies.length > 0) {
        setWaterBodies(bodies);
        if (!selectedWaterBody) setSelectedWaterBody(bodies[0]);
      }
    } catch {
      // Retain default seed
    }
  };

  const loadDashboard = async () => {
    setRefreshing(true);
    try {
      const targetName = selectedWaterBody?.name || 'B.A. Steinhagen';
      const data = await getUnifiedDashboard(targetName, 'linear_regression', 14);
      if (data) {
        setDashboardData(data);
      }
    } catch (err) {
      console.warn('Dashboard fetch offline notice:', err);
    } finally {
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadWaterBodiesList();
    loadDashboard();
  }, [selectedWaterBody?.name]);

  const renderActivePage = () => {
    switch (currentPage) {
      case 'overview':
        return <OverviewPage dashboardData={dashboardData} onNavigate={setCurrentPage} />;
      case 'water-bodies':
        return <WaterBodiesPage waterBodies={waterBodies} onRefresh={() => { loadWaterBodiesList(); loadDashboard(); }} />;
      case 'historical':
        return <HistoricalPage />;
      case 'predictions':
        return <PredictionsPage />;
      case 'sustainability':
        return <SustainabilityPage dashboardData={dashboardData} />;
      case 'performance':
        return <PerformancePage />;
      case 'reports':
        return <ReportsPage dashboardData={dashboardData} />;
      case 'settings':
        return <SettingsPage onRefreshAll={() => { loadWaterBodiesList(); loadDashboard(); }} />;
      default:
        return <OverviewPage dashboardData={dashboardData} onNavigate={setCurrentPage} />;
    }
  };

  return (
    <div className="app-layout">
      {/* 8-Page Navigation Sidebar */}
      <Sidebar currentPage={currentPage} onNavigate={setCurrentPage} />

      {/* Main Content Area */}
      <div className="main-wrapper">
        <Topbar 
          title={pageTitles[currentPage] || 'Dashboard'}
          waterBodies={waterBodies}
          selectedWaterBody={selectedWaterBody}
          onSelectWaterBody={setSelectedWaterBody}
          onRefresh={loadDashboard}
          refreshing={refreshing}
        />

        <main className="content-area">
          {renderActivePage()}
        </main>
      </div>
    </div>
  );
}

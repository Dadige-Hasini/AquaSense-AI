import React, { useState } from 'react';
import { Plus, Edit2, Trash2, MapPin, Anchor, Waves, CheckCircle2, AlertCircle } from 'lucide-react';
import { createWaterBody, updateWaterBody, deleteWaterBody } from '../services/api';

export default function WaterBodiesPage({ 
  waterBodies = [], 
  onRefresh 
}) {
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingBody, setEditingBody] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    state: 'Texas',
    county: '',
    riverBasin: 'Neches River Basin',
    conservationCapacity: 69186,
    conservationPoolElevation: 82.5,
    surfaceArea: 13700,
    damName: '',
    managingAgency: 'US Army Corps of Engineers / TWDB',
    status: 'ACTIVE'
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);

  const openAddModal = () => {
    setEditingBody(null);
    setFormData({
      name: '',
      state: 'Texas',
      county: 'Tyler / Jasper',
      riverBasin: 'Neches River Basin',
      conservationCapacity: 75000,
      conservationPoolElevation: 85.0,
      surfaceArea: 14000,
      damName: 'Sample Dam',
      managingAgency: 'TWDB / USACE',
      status: 'ACTIVE'
    });
    setIsModalOpen(true);
  };

  const openEditModal = (body) => {
    setEditingBody(body);
    setFormData({
      name: body.name || '',
      state: body.state || 'Texas',
      county: body.county || '',
      riverBasin: body.riverBasin || '',
      conservationCapacity: body.conservationCapacity || 0,
      conservationPoolElevation: body.conservationPoolElevation || 0,
      surfaceArea: body.surfaceArea || 0,
      damName: body.damName || '',
      managingAgency: body.managingAgency || '',
      status: body.status || 'ACTIVE'
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage(null);
    try {
      if (editingBody && editingBody.id) {
        await updateWaterBody(editingBody.id, formData);
        setMessage({ type: 'success', text: `Water body '${formData.name}' successfully updated.` });
      } else {
        await createWaterBody(formData);
        setMessage({ type: 'success', text: `New water body '${formData.name}' added to catalog.` });
      }
      setIsModalOpen(false);
      if (onRefresh) onRefresh();
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Operation failed.' });
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete water body '${name}'?`)) return;
    try {
      await deleteWaterBody(id);
      setMessage({ type: 'success', text: `Water body '${name}' removed.` });
      if (onRefresh) onRefresh();
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Delete failed.' });
    }
  };

  const safeList = Array.isArray(waterBodies) ? waterBodies : [];
  const filtered = safeList.filter(wb => 
    (wb?.name || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
    (wb?.riverBasin || '').toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 700, color: '#ffffff' }}>
            Water Bodies & Reservoir Catalog
          </h2>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
            Manage monitored lake basins, dam structures, and conservation storage parameters (Spring Boot JPA CRUD)
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <input 
            type="text"
            className="form-input"
            placeholder="Search basins..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: '220px' }}
          />
          <button className="btn btn-primary" onClick={openAddModal}>
            <Plus size={16} /> Register Water Body
          </button>
        </div>
      </div>

      {message && (
        <div style={{ 
          marginBottom: '1.25rem', 
          padding: '0.75rem 1rem', 
          borderRadius: '10px',
          background: message.type === 'success' ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
          border: `1px solid ${message.type === 'success' ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}`,
          color: message.type === 'success' ? '#34d399' : '#f87171',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
          fontSize: '0.85rem'
        }}>
          {message.type === 'success' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
          {message.text}
        </div>
      )}

      {/* Catalog Table */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ overflowX: 'auto' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Water Body Name</th>
                <th>River Basin</th>
                <th>County / State</th>
                <th>Conservation Pool</th>
                <th>Elevation</th>
                <th>Dam Infrastructure</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((wb, idx) => (
                <tr key={wb.id || idx}>
                  <td>
                    <div style={{ fontWeight: 600, color: '#ffffff', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <Waves size={14} color="#14b8a6" />
                      {wb.name}
                    </div>
                  </td>
                  <td style={{ color: '#cbd5e1' }}>{wb.riverBasin}</td>
                  <td style={{ color: '#94a3b8' }}>{wb.county}, {wb.state}</td>
                  <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>
                    {wb.conservationCapacity ? Number(wb.conservationCapacity).toLocaleString() : 'N/A'} <span style={{ color: '#14b8a6', fontSize: '0.75rem' }}>ac-ft</span>
                  </td>
                  <td style={{ fontFamily: 'var(--font-mono)' }}>
                    ~{wb.conservationPoolElevation} ft
                  </td>
                  <td style={{ color: '#cbd5e1' }}>{wb.damName}</td>
                  <td>
                    <span style={{ 
                      background: wb.status === 'ACTIVE' ? 'rgba(16, 185, 129, 0.15)' : 'rgba(245, 158, 11, 0.15)',
                      color: wb.status === 'ACTIVE' ? '#34d399' : '#fbbf24',
                      padding: '0.2rem 0.55rem',
                      borderRadius: '6px',
                      fontSize: '0.72rem',
                      fontWeight: 600
                    }}>
                      {wb.status || 'ACTIVE'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                      <button 
                        className="btn" 
                        style={{ padding: '0.35rem 0.6rem' }} 
                        onClick={() => openEditModal(wb)}
                        title="Edit Water Body"
                      >
                        <Edit2 size={13} />
                      </button>
                      <button 
                        className="btn btn-danger" 
                        style={{ padding: '0.35rem 0.6rem' }} 
                        onClick={() => handleDelete(wb.id, wb.name)}
                        title="Delete Water Body"
                      >
                        <Trash2 size={13} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add / Edit Modal */}
      {isModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: '#ffffff' }}>
                {editingBody ? `Edit '${editingBody.name}'` : 'Register New Water Body'}
              </h3>
              <button 
                onClick={() => setIsModalOpen(false)} 
                style={{ background: 'none', border: 'none', color: '#94a3b8', fontSize: '1.2rem', cursor: 'pointer' }}
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Water Body Name *</label>
                  <input 
                    type="text" 
                    className="form-input" 
                    required 
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="e.g. Sam Rayburn Reservoir"
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label className="form-label">River Basin</label>
                    <input 
                      type="text" 
                      className="form-input" 
                      value={formData.riverBasin}
                      onChange={(e) => setFormData({ ...formData, riverBasin: e.target.value })}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">County</label>
                    <input 
                      type="text" 
                      className="form-input" 
                      value={formData.county}
                      onChange={(e) => setFormData({ ...formData, county: e.target.value })}
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label className="form-label">Conservation Capacity (acre-ft)</label>
                    <input 
                      type="number" 
                      className="form-input" 
                      value={formData.conservationCapacity}
                      onChange={(e) => setFormData({ ...formData, conservationCapacity: parseFloat(e.target.value) })}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Pool Elevation (ft msl)</label>
                    <input 
                      type="number" 
                      step="0.1" 
                      className="form-input" 
                      value={formData.conservationPoolElevation}
                      onChange={(e) => setFormData({ ...formData, conservationPoolElevation: parseFloat(e.target.value) })}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Dam Structure</label>
                  <input 
                    type="text" 
                    className="form-input" 
                    value={formData.damName}
                    onChange={(e) => setFormData({ ...formData, damName: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Managing Agency</label>
                  <input 
                    type="text" 
                    className="form-input" 
                    value={formData.managingAgency}
                    onChange={(e) => setFormData({ ...formData, managingAgency: e.target.value })}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn" onClick={() => setIsModalOpen(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Saving...' : (editingBody ? 'Save Changes' : 'Create Record')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

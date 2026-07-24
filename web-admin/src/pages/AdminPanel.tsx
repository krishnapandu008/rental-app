import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import {
  getAllUsers,
  createUser,
  updateUser,
  updateUserRole,
  toggleUserActive,
  deleteUser,
  getAuditLogs,
} from '../api/adminApi';
import { Owner, AuditLog } from '../types';
import styles from './AdminPanel.module.scss';

const AdminPanel: React.FC = () => {
  const { owner } = useAuth();
  const [users, setUsers] = useState<Owner[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAudit, setShowAudit] = useState(false);

  // Form states
  const [editingUser, setEditingUser] = useState<Owner | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newUser, setNewUser] = useState({
    email: '',
    password: '',
    name: '',
    phone: '',
    role: 'USER',
  });

  const isSuperAdmin = owner?.role === 'SUPER_ADMIN';
  const isAdmin = owner?.role === 'ADMIN' || isSuperAdmin;

  useEffect(() => {
    if (isAdmin) {
      loadUsers();
      if (isSuperAdmin) loadAuditLogs();
    }
  }, [owner]);

  const loadUsers = async () => {
    try {
      const res = await getAllUsers();
      setUsers(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadAuditLogs = async () => {
    try {
      const res = await getAuditLogs();
      setAuditLogs(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  // ---------- CRUD ----------
  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createUser(newUser);
      setShowCreateModal(false);
      setNewUser({ email: '', password: '', name: '', phone: '', role: 'USER' });
      loadUsers();
    } catch (err) {
      alert('Failed to create user');
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingUser) return;
    try {
      await updateUser(editingUser.id, {
        email: editingUser.email,
        name: editingUser.name,
        phone: editingUser.phone,
      });
      setEditingUser(null);
      loadUsers();
    } catch (err) {
      alert('Failed to update user');
    }
  };

  const handleRoleChange = async (userId: number, role: string) => {
    if (!isSuperAdmin) return;
    if (!window.confirm(`Change role to ${role}?`)) return;
    try {
      await updateUserRole(userId, role);
      loadUsers();
    } catch (err) {
      alert('Failed to update role');
    }
  };

  const handleToggleActive = async (userId: number) => {
    if (!isAdmin) return;
    try {
      await toggleUserActive(userId);
      loadUsers();
    } catch (err) {
      alert('Failed to toggle active status');
    }
  };

  const handleDelete = async (userId: number) => {
    if (!isSuperAdmin) return;
    if (!window.confirm('Delete this user permanently?')) return;
    try {
      await deleteUser(userId);
      loadUsers();
    } catch (err) {
      alert('Failed to delete user');
    }
  };

  if (!isAdmin) {
    return <div className={styles.error}>You are not authorized to view this page.</div>;
  }

  if (loading) return <div>Loading users...</div>;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>Admin Panel – User Management</h1>
        {isSuperAdmin && (
          <button className={styles.createBtn} onClick={() => setShowCreateModal(true)}>
            + Create User
          </button>
        )}
        <button className={styles.auditBtn} onClick={() => setShowAudit(!showAudit)}>
          {showAudit ? 'Hide Audit Logs' : 'Show Audit Logs'}
        </button>
      </div>

      {/* Users Table */}
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Email</th>
            <th>Name</th>
            <th>Phone</th>
            <th>Role</th>
            <th>Status</th>
            <th>Created</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.id}</td>
              <td>{u.email}</td>
              <td>{u.name}</td>
              <td>{u.phone}</td>
              <td>
                {isSuperAdmin ? (
                  <select
                    value={u.role}
                    onChange={(e) => handleRoleChange(u.id, e.target.value)}
                  >
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                    <option value="SUPER_ADMIN">SUPER_ADMIN</option>
                  </select>
                ) : (
                  u.role
                )}
              </td>
              <td>
                <span className={u.isActive ? styles.activeBadge : styles.inactiveBadge}>
                  {u.isActive ? 'Active' : 'Inactive'}
                </span>
              </td>
              <td>{u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-'}</td>
              <td>
                <button className={styles.editBtn} onClick={() => setEditingUser(u)}>
                  Edit
                </button>
                <button
                  className={styles.toggleBtn}
                  onClick={() => handleToggleActive(u.id)}
                >
                  {u.isActive ? 'Deactivate' : 'Restore'}
                </button>
                {isSuperAdmin && (
                  <button className={styles.deleteBtn} onClick={() => handleDelete(u.id)}>
                    Delete
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Audit Logs Section */}
      {showAudit && isSuperAdmin && (
        <div className={styles.auditSection}>
          <h2>Audit Logs</h2>
          <table className={styles.auditTable}>
            <thead>
              <tr>
                <th>Time</th>
                <th>Admin</th>
                <th>Action</th>
                <th>Details</th>
                <th>IP</th>
              </tr>
            </thead>
            <tbody>
              {auditLogs.length === 0 ? (
                <tr><td colSpan={5}>No logs yet.</td></tr>
              ) : (
                auditLogs.map((log) => (
                  <tr key={log.id}>
                    <td>{new Date(log.timestamp).toLocaleString()}</td>
                    <td>{log.adminEmail}</td>
                    <td>{log.action}</td>
                    <td>{log.details}</td>
                    <td>{log.ipAddress}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Create User Modal */}
      {showCreateModal && isSuperAdmin && (
        <div className={styles.modalOverlay} onClick={() => setShowCreateModal(false)}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <h2>Create User</h2>
            <form onSubmit={handleCreate}>
              <input
                placeholder="Email"
                value={newUser.email}
                onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                required
              />
              <input
                placeholder="Password"
                type="password"
                value={newUser.password}
                onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                required
              />
              <input
                placeholder="Name"
                value={newUser.name}
                onChange={(e) => setNewUser({ ...newUser, name: e.target.value })}
                required
              />
              <input
                placeholder="Phone"
                value={newUser.phone}
                onChange={(e) => setNewUser({ ...newUser, phone: e.target.value })}
                required
              />
              <select
                value={newUser.role}
                onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}
              >
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
                <option value="SUPER_ADMIN">SUPER_ADMIN</option>
              </select>
              <div className={styles.modalActions}>
                <button type="submit">Create</button>
                <button type="button" onClick={() => setShowCreateModal(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit User Modal */}
      {editingUser && (
        <div className={styles.modalOverlay} onClick={() => setEditingUser(null)}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <h2>Edit User</h2>
            <form onSubmit={handleUpdate}>
              <input
                placeholder="Email"
                value={editingUser.email}
                onChange={(e) => setEditingUser({ ...editingUser, email: e.target.value })}
                required
              />
              <input
                placeholder="Name"
                value={editingUser.name}
                onChange={(e) => setEditingUser({ ...editingUser, name: e.target.value })}
                required
              />
              <input
                placeholder="Phone"
                value={editingUser.phone}
                onChange={(e) => setEditingUser({ ...editingUser, phone: e.target.value })}
                required
              />
              <div className={styles.modalActions}>
                <button type="submit">Update</button>
                <button type="button" onClick={() => setEditingUser(null)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminPanel;
import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { getAllUsers, updateUserRole, deleteUser } from '../api/adminApi';
import { Owner } from '../types';
import styles from './AdminPanel.module.scss';

const AdminPanel: React.FC = () => {
    const { owner } = useAuth();
    const [users, setUsers] = useState<Owner[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (owner?.role === 'ADMIN') {
            loadUsers();
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

    const handleRoleChange = async (userId: number, newRole: string) => {
        if (!window.confirm(`Change role to ${newRole}?`)) return;
        try {
            await updateUserRole(userId, newRole);
            await loadUsers(); // refresh list
        } catch (err) {
            alert('Failed to update role');
        }
    };

    const handleDelete = async (userId: number) => {
        if (!window.confirm('Delete this user permanently?')) return;
        try {
            await deleteUser(userId);
            await loadUsers();
        } catch (err) {
            alert('Failed to delete user');
        }
    };

    if (!owner || owner.role !== 'ADMIN') {
        return <div className={styles.error}>You are not authorized to view this page.</div>;
    }

    if (loading) return <div>Loading users...</div>;

    return (
        <div className={styles.container}>
            <h1>Admin Panel – User Management</h1>
            <table className={styles.table}>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Email</th>
                        <th>Name</th>
                        <th>Role</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map((u) => (
                        <tr key={u.id}>
                            <td>{u.id}</td>
                            <td>{u.email}</td>
                            <td>{u.name}</td>
                            <td>{u.role}</td>
                            <td>
                                <select
                                    value={u.role}
                                    onChange={(e) => handleRoleChange(u.id, e.target.value)}
                                >
                                    <option value="USER">USER</option>
                                    <option value="ADMIN">ADMIN</option>
                                </select>
                                <button onClick={() => handleDelete(u.id)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default AdminPanel;
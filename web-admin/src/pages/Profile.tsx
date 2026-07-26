import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { api } from '../api/client';
import styles from './Profile.module.scss';

interface ProfileData {
  id: number;
  email: string;
  name: string;
  phone: string;
  role: string;
  avatarUrl?: string;
  createdAt: string;
}

const Profile: React.FC = () => {
  const { owner } = useAuth();
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [editForm, setEditForm] = useState({ email: '', name: '', phone: '' });
  const [passwordForm, setPasswordForm] = useState({ oldPassword: '', newPassword: '' });
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const res = await api.get('/owners/profile');
      setProfile(res.data);
      setEditForm({
        email: res.data.email,
        name: res.data.name,
        phone: res.data.phone,
      });
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.put('/owners/profile', editForm);
      setMessage('Profile updated successfully');
      loadProfile();
    } catch (err) {
      setMessage('Failed to update profile');
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.put('/owners/password', passwordForm);
      setMessage('Password changed successfully');
      setPasswordForm({ oldPassword: '', newPassword: '' });
    } catch (err) {
      setMessage('Failed to change password');
    }
  };

  const handleAvatarUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!avatarFile) return;
    const formData = new FormData();
    formData.append('avatar', avatarFile);
    try {
      await api.post('/owners/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setMessage('Avatar uploaded successfully');
      loadProfile();
      setAvatarFile(null);
    } catch (err) {
      setMessage('Failed to upload avatar');
    }
  };

  if (loading) return <div>Loading profile...</div>;

  return (
    <div className={styles.container}>
      <h2>Profile</h2>
      {message && <div className={styles.message}>{message}</div>}

      <div className={styles.avatarSection}>
        <img
          src={profile?.avatarUrl || '/default-avatar.png'}
          alt="Avatar"
          className={styles.avatar}
        />
        <form onSubmit={handleAvatarUpload}>
          <input
            type="file"
            accept="image/*"
            onChange={(e) => setAvatarFile(e.target.files?.[0] || null)}
          />
          <button type="submit">Upload Avatar</button>
        </form>
      </div>

      <form onSubmit={handleUpdateProfile}>
        <h3>Edit Profile</h3>
        <div className={styles.formGroup}>
          <label>Name</label>
          <input
            value={editForm.name}
            onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
            required
          />
        </div>
        <div className={styles.formGroup}>
          <label>Email</label>
          <input
            type="email"
            value={editForm.email}
            onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
            required
          />
        </div>
        <div className={styles.formGroup}>
          <label>Phone</label>
          <input
            value={editForm.phone}
            onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })}
            required
          />
        </div>
        <button type="submit">Update Profile</button>
      </form>

      <form onSubmit={handleChangePassword}>
        <h3>Change Password</h3>
        <div className={styles.formGroup}>
          <label>Current Password</label>
          <input
            type="password"
            value={passwordForm.oldPassword}
            onChange={(e) =>
              setPasswordForm({ ...passwordForm, oldPassword: e.target.value })
            }
            required
          />
        </div>
        <div className={styles.formGroup}>
          <label>New Password</label>
          <input
            type="password"
            value={passwordForm.newPassword}
            onChange={(e) =>
              setPasswordForm({ ...passwordForm, newPassword: e.target.value })
            }
            required
          />
        </div>
        <button type="submit">Change Password</button>
      </form>
    </div>
  );
};

export default Profile;
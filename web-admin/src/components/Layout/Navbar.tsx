import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import styles from './Navbar.module.scss';

const Navbar: React.FC = () => {
  const { owner, logout } = useAuth();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isAdmin = owner?.role === 'ADMIN' || owner?.role === 'SUPER_ADMIN';

  const getInitials = () => {
    if (!owner?.name) return 'U';
    const parts = owner.name.trim().split(' ');
    if (parts.length >= 2) return parts[0][0] + parts[1][0];
    return parts[0][0];
  };

  const avatarUrl = owner?.avatarUrl;

  return (
    <nav className={styles.navbar}>
      <div className={styles.logo}>
        <Link to="/">Rental Admin</Link>
      </div>
      <div className={styles.links}>
        {owner ? (
          <>
            <Link to="/">Dashboard</Link>
            <Link to="/add">Add Property</Link>
            <Link to="/favorites">Favorites</Link>   {/* ✅ NEW */}
            <div className={styles.avatarWrapper}>
              <div
                className={styles.avatar}
                onClick={() => setDropdownOpen(!dropdownOpen)}
              >
                {avatarUrl ? (
                  <img
                    src={avatarUrl}
                    alt="Avatar"
                    onError={(e) => {
                      (e.target as HTMLImageElement).style.display = 'none';
                    }}
                  />
                ) : (
                  <span>{getInitials()}</span>
                )}
              </div>
              {dropdownOpen && (
                <div className={styles.dropdown}>
                  <Link to="/profile" onClick={() => setDropdownOpen(false)}>
                    Profile
                  </Link>
                  {isAdmin && (
                    <Link to="/admin" onClick={() => setDropdownOpen(false)}>
                      Admin Panel
                    </Link>
                  )}
                  <button onClick={handleLogout}>Logout</button>
                </div>
              )}
            </div>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
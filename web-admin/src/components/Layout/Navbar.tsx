import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import styles from './Navbar.module.scss';

const Navbar: React.FC = () => {
  const { owner, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isAdmin = owner?.role === 'ADMIN' || owner?.role === 'SUPER_ADMIN';

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
            {isAdmin && (
              <Link to="/admin" className={styles.adminLink}>
                Admin Panel
              </Link>
            )}
            <button onClick={handleLogout} className={styles.logoutBtn}>
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
            <Link to="/profile">Profile</Link>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { api } from '../../api/client';
import styles from './Navbar.module.scss';

// Bell Icon SVG
const BellIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
    <path d="M13.73 21a2 2 0 0 1-3.46 0" />
  </svg>
);

// Hamburger & Close Icons
const HamburgerIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="3" y1="6" x2="21" y2="6" />
    <line x1="3" y1="12" x2="21" y2="12" />
    <line x1="3" y1="18" x2="21" y2="18" />
  </svg>
);

const CloseIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="18" y1="6" x2="6" y2="18" />
    <line x1="6" y1="6" x2="18" y2="18" />
  </svg>
);

const Navbar: React.FC = () => {
  const { owner, logout } = useAuth();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [notificationDropdownOpen, setNotificationDropdownOpen] = useState(false);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const dropdownRef = useRef<HTMLDivElement>(null);
  const avatarRef = useRef<HTMLDivElement>(null);
  const notificationRef = useRef<HTMLDivElement>(null);
  const mobileMenuRef = useRef<HTMLDivElement>(null);

  const handleLogout = () => {
    logout();
    navigate('/login');
    setMobileMenuOpen(false);
  };

  const isAdmin = owner?.role === 'ADMIN' || owner?.role === 'SUPER_ADMIN';

  const getInitials = () => {
    if (!owner?.name) return 'U';
    const parts = owner.name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return parts[0][0].toUpperCase();
  };

  const avatarUrl = owner?.avatarUrl;

  // Load notifications
  const loadNotifications = async () => {
    if (!owner) return;
    try {
      setLoading(true);
      const [notificationsRes, countRes] = await Promise.all([
        api.get('/notifications'),
        api.get('/notifications/unread-count'),
      ]);
      setNotifications(notificationsRes.data || []);
      setUnreadCount(countRes.data || 0);
    } catch (err) {
      console.error('Failed to load notifications:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (owner) {
      loadNotifications();
      const interval = setInterval(loadNotifications, 30000);
      return () => clearInterval(interval);
    }
  }, [owner]);

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node) &&
        avatarRef.current &&
        !avatarRef.current.contains(event.target as Node)
      ) {
        setDropdownOpen(false);
      }
      if (
        notificationRef.current &&
        !notificationRef.current.contains(event.target as Node) &&
        !(event.target as Element).closest(`.${styles.notificationBtn}`)
      ) {
        setNotificationDropdownOpen(false);
      }
      if (
        mobileMenuRef.current &&
        !mobileMenuRef.current.contains(event.target as Node) &&
        !(event.target as Element).closest(`.${styles.hamburgerBtn}`)
      ) {
        setMobileMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // Close on Escape key
  useEffect(() => {
    const handleEsc = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setDropdownOpen(false);
        setNotificationDropdownOpen(false);
        setMobileMenuOpen(false);
      }
    };
    document.addEventListener('keydown', handleEsc);
    return () => {
      document.removeEventListener('keydown', handleEsc);
    };
  }, []);

  const handleMarkAllAsRead = async () => {
    try {
      await api.patch('/notifications/mark-all-read');
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Failed to mark all as read:', err);
    }
  };

  const handleNotificationClick = async (notification: any) => {
    if (!notification.isRead) {
      try {
        await api.patch(`/notifications/${notification.id}/read`);
        setNotifications(prev =>
          prev.map(n => n.id === notification.id ? { ...n, isRead: true } : n)
        );
        setUnreadCount(prev => Math.max(0, prev - 1));
      } catch (err) {
        console.error('Failed to mark notification as read:', err);
      }
    }
    if (notification.link) {
      navigate(notification.link);
    }
    setNotificationDropdownOpen(false);
    setMobileMenuOpen(false);
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    return date.toLocaleDateString();
  };

  const toggleMobileMenu = () => {
    setMobileMenuOpen(!mobileMenuOpen);
  };

  const closeMobileMenu = () => {
    setMobileMenuOpen(false);
  };

  // Navigation items
  const navItems = owner ? [
    { path: '/', label: 'Dashboard', icon: '📊' },
    { path: '/add', label: 'Add Property', icon: '➕' },
    { path: '/favorites', label: 'Favorites', icon: '❤️' },
    { path: '/inquiries', label: 'Inquiries', icon: '📩' },
  ] : [];

  return (
    <nav className={styles.navbar}>
      <div className={styles.navbarInner}>
        {/* Logo */}
        <div className={styles.logo}>
          <Link to="/" onClick={closeMobileMenu}>
            Rental Admin
          </Link>
        </div>

        {/* Right side: Bell → Avatar → Hamburger */}
        <div className={styles.navRight}>
          {owner ? (
            <>
              {/* Notification Bell */}
              <div className={styles.notificationWrapper} ref={notificationRef}>
                <button
                  className={`${styles.notificationBtn} ${unreadCount > 0 ? styles.hasUnread : ''}`}
                  onClick={() => setNotificationDropdownOpen(!notificationDropdownOpen)}
                  aria-label="Notifications"
                >
                  <BellIcon />
                  {unreadCount > 0 && (
                    <span className={styles.notificationBadge}>
                      {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                  )}
                </button>

                {notificationDropdownOpen && (
                  <div className={styles.notificationDropdown}>
                    <div className={styles.notificationHeader}>
                      <span className={styles.notificationTitle}>🔔 Notifications</span>
                      {unreadCount > 0 && (
                        <button className={styles.markAllReadBtn} onClick={handleMarkAllAsRead}>
                          Mark all as read
                        </button>
                      )}
                    </div>
                    <div className={styles.notificationList}>
                      {loading ? (
                        <div className={styles.notificationEmpty}>Loading...</div>
                      ) : notifications.length === 0 ? (
                        <div className={styles.notificationEmpty}>
                          <span>🎉</span>
                          <p>No notifications yet</p>
                        </div>
                      ) : (
                        notifications.slice(0, 10).map((notification) => (
                          <div
                            key={notification.id}
                            className={`${styles.notificationItem} ${!notification.isRead ? styles.unread : ''}`}
                            onClick={() => handleNotificationClick(notification)}
                          >
                            <div className={styles.notificationContent}>
                              <span className={styles.notificationType}>
                                {notification.type === 'INQUIRY' && '📩'}
                                {notification.type === 'REPLY' && '💬'}
                                {notification.type === 'SYSTEM' && '⚙️'}
                                {notification.type === 'VIEW' && '👁️'}
                              </span>
                              <div className={styles.notificationText}>
                                <div className={styles.notificationTitleText}>
                                  {notification.title}
                                </div>
                                <div className={styles.notificationMessage}>
                                  {notification.message}
                                </div>
                                <span className={styles.notificationTime}>
                                  {formatTime(notification.createdAt)}
                                </span>
                              </div>
                              {!notification.isRead && <span className={styles.unreadDot} />}
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                    <div className={styles.notificationFooter}>
                      <Link to="/inquiries" onClick={() => setNotificationDropdownOpen(false)}>
                        View all notifications →
                      </Link>
                    </div>
                  </div>
                )}
              </div>

              {/* Avatar - Dropdown with Profile icon */}
              <div className={styles.avatarWrapper} ref={avatarRef}>
                <div
                  className={styles.avatar}
                  onClick={() => setDropdownOpen(!dropdownOpen)}
                >
                  {avatarUrl ? (
                    <img src={avatarUrl} alt="Avatar" onError={(e) => {
                      (e.target as HTMLImageElement).style.display = 'none';
                    }} />
                  ) : (
                    <span>{getInitials()}</span>
                  )}
                </div>
                {dropdownOpen && (
                  <div className={styles.dropdown} ref={dropdownRef}>
                    <Link to="/profile" onClick={() => setDropdownOpen(false)}>
                      <span className={styles.dropdownIcon}>👤</span> Profile
                    </Link>
                  </div>
                )}
              </div>

              {/* Hamburger Menu Button */}
              <button
                className={styles.hamburgerBtn}
                onClick={toggleMobileMenu}
                aria-label="Toggle menu"
              >
                {mobileMenuOpen ? <CloseIcon /> : <HamburgerIcon />}
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className={styles.loginLink}>Login</Link>
              <Link to="/register" className={styles.registerLink}>Register</Link>
            </>
          )}
        </div>
      </div>

      {/* Hamburger Menu Dropdown */}
      {mobileMenuOpen && owner && (
        <div className={styles.mobileMenu} ref={mobileMenuRef}>
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={styles.mobileLink}
              onClick={closeMobileMenu}
            >
              <span className={styles.mobileLinkIcon}>{item.icon}</span>
              {item.label}
            </Link>
          ))}
          {isAdmin && (
            <Link to="/admin" className={styles.mobileLink} onClick={closeMobileMenu}>
              <span className={styles.mobileLinkIcon}>⚙️</span> Admin Panel
            </Link>
          )}
          <button className={styles.mobileLogoutBtn} onClick={handleLogout}>
            <span className={styles.mobileLinkIcon}>🚪</span> Logout
          </button>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
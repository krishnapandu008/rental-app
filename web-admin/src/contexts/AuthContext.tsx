import { createContext, useState, useContext, ReactNode, useEffect } from 'react';
import { loginOwner, registerOwner } from '../api/ownerApi';
import { logout as logoutApi } from '../api/authApi';
import { Owner } from '../types';

interface AuthContextType {
  owner: Owner | null;
  login: (email: string, password: string) => Promise<void>;
  register: (data: { email: string; password: string; name: string; phone: string }) => Promise<void>;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [owner, setOwner] = useState<Owner | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedOwner = localStorage.getItem('owner');
    const token = localStorage.getItem('token');
    if (storedOwner && token) {
      setOwner(JSON.parse(storedOwner));
    }
    setLoading(false);
  }, []);

  const persistSession = (data: Owner) => {
    setOwner(data);
    localStorage.setItem('owner', JSON.stringify(data));
    localStorage.setItem('token', data.token);
    localStorage.setItem('refreshToken', data.refreshToken);
  };

  const login = async (email: string, password: string) => {
    const res = await loginOwner({ email, password });
    persistSession(res.data);
  };

  const register = async (data: { email: string; password: string; name: string; phone: string }) => {
    const res = await registerOwner(data);
    persistSession(res.data);
  };

  const logout = () => {
    const rt = localStorage.getItem('refreshToken');
    if (rt) logoutApi(rt).catch(() => {}); // best-effort server-side revoke
    setOwner(null);
    localStorage.removeItem('owner');
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
  };

  return (
    <AuthContext.Provider value={{ owner, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
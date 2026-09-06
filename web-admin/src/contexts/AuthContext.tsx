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
  updateOwner: (updated: Owner) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [owner, setOwner] = useState<Owner | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Only restore owner data (no tokens)
    const storedOwner = localStorage.getItem('owner');
    if (storedOwner) {
      setOwner(JSON.parse(storedOwner));
    }
    setLoading(false);
  }, []);

  const persistSession = (data: Owner) => {
    // data should not contain token/refreshToken (they are null from backend)
    setOwner(data);
    localStorage.setItem('owner', JSON.stringify(data));
  };

  const login = async (email: string, password: string) => {
    const res = await loginOwner({ email, password });
    persistSession(res.data);
  };

  const register = async (data: { email: string; password: string; name: string; phone: string }) => {
    const res = await registerOwner(data);
    persistSession(res.data);
  };

  const logout = async () => {
    try {
      await logoutApi(); // cookie will be cleared by server
    } catch (e) {
      // ignore
    }
    setOwner(null);
    localStorage.removeItem('owner');
  };

  const updateOwner = (updated: Owner) => {
    setOwner(updated);
    localStorage.setItem('owner', JSON.stringify(updated));
  };

  return (
    <AuthContext.Provider value={{ owner, login, register, logout, loading, updateOwner }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
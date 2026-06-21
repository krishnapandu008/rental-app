import { createContext, useState, useContext, ReactNode, useEffect } from 'react';
import { loginOwner, registerOwner } from '../api/ownerApi';
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

  const login = async (email: string, password: string) => {
  try {
    const res = await loginOwner({ email, password });
    console.log('Login response:', res.data);   // 👈 ADD THIS
    setOwner(res.data);
    localStorage.setItem('owner', JSON.stringify(res.data));
    localStorage.setItem('token', res.data.token);
    console.log('Token stored:', localStorage.getItem('token')); // 👈 ADD THIS
  } catch (error) {
    console.error('Login error:', error);
  }
};

  const register = async (data: { email: string; password: string; name: string; phone: string }) => {
    const res = await registerOwner(data);
    setOwner(res.data);
    localStorage.setItem('owner', JSON.stringify(res.data));
    localStorage.setItem('token', res.data.token);
  };

  const logout = () => {
    setOwner(null);
    localStorage.removeItem('owner');
    localStorage.removeItem('token');
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

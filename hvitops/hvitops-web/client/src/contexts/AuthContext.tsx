import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

export interface User {
  id: string;
  name: string;
  email: string;
  role: 'PATIENT' | 'PHYSICIAN' | 'LAB_TECHNICIAN' | 'ADMIN';
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
  error: string | null;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Restaurar autenticação ao carregar
  useEffect(() => {
    const storedToken = localStorage.getItem('auth_token');
    const storedUser = localStorage.getItem('auth_user');
    
    if (storedToken && storedUser) {
      try {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
      } catch (err) {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('auth_user');
      }
    }
  }, []);

  const login = async (email: string, password: string) => {
    setIsLoading(true);
    setError(null);
    
    try {
      // Simular login - em produção seria uma chamada real à API
      // Para teste, aceitar qualquer email/senha
      const mockUsers: Record<string, User> = {
        'patient@example.com': {
          id: '1',
          name: 'João Silva',
          email: 'patient@example.com',
          role: 'PATIENT'
        },
        'doctor@example.com': {
          id: '101',
          name: 'Dr. Carlos Santos',
          email: 'doctor@example.com',
          role: 'PHYSICIAN'
        },
        'lab@example.com': {
          id: '201',
          name: 'Maria Técnica',
          email: 'lab@example.com',
          role: 'LAB_TECHNICIAN'
        },
        'admin@example.com': {
          id: '301',
          name: 'Admin User',
          email: 'admin@example.com',
          role: 'ADMIN'
        }
      };

      const mockUser = mockUsers[email];
      if (!mockUser) {
        throw new Error('Usuário não encontrado');
      }

      // Criar token JWT mock
      const mockToken = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIke21vY2tVc2VyLmlkfSIsInJvbGUiOiIke21vY2tVc2VyLnJvbGV9IiwiaWF0IjoxNjM0NTY3ODkwLCJleHAiOjE2MzQ2NTQyOTB9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ`;

      setToken(mockToken);
      setUser(mockUser);
      
      localStorage.setItem('auth_token', mockToken);
      localStorage.setItem('auth_user', JSON.stringify(mockUser));
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erro ao fazer login';
      setError(errorMessage);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    setError(null);
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        login,
        logout,
        isLoading,
        error
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider');
  }
  return context;
};

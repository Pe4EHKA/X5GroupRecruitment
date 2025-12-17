'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { User, UserRole } from '@/types';
import { apiClient, ApiError, getErrorMessage } from '@/lib/api';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  hasRole: (role: UserRole) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    // Check if user is logged in on mount
    const storedUser = localStorage.getItem('user');
    const storedAuth = localStorage.getItem('authCredentials');
    
    if (storedUser && storedAuth) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        console.error('Failed to parse stored user', e);
        localStorage.removeItem('user');
        localStorage.removeItem('authCredentials');
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    try {
      // Store credentials for Basic Auth
      const credentials = btoa(`${username}:${password}`);
      localStorage.setItem('authCredentials', credentials);

      // Call /api/auth/me to get user info
      const response = await apiClient.get('/api/auth/me', {
        headers: {
          Authorization: `Basic ${credentials}`,
        },
      });

      const userInfo = response.data;
      const mappedUser: User = {
        id: userInfo.id,
        username: userInfo.username,
        email: userInfo.email,
        fullName: userInfo.displayName,
        roles: userInfo.roles,
      };

      setUser(mappedUser);
      localStorage.setItem('user', JSON.stringify(mappedUser));

      // Auto-redirect based on primary role
      const roles = userInfo.roles;
      if (roles.includes('STAGER') || roles.includes('CANDIDATE')) {
        router.push('/stager');
      } else if (roles.includes('RECRUITER')) {
        router.push('/hr');
      } else if (roles.includes('HM')) {
        router.push('/hm/inbox');
      } else if (roles.includes('ADMIN')) {
        router.push('/admin/programs');
      } else {
        router.push('/');
      }
    } catch (error) {
      console.error('Login error:', error);
      const message = getErrorMessage(error as ApiError);
      localStorage.removeItem('authCredentials');
      localStorage.removeItem('user');
      throw new Error(message);
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
    localStorage.removeItem('authCredentials');
    router.push('/login');
  };

  const hasRole = (role: UserRole): boolean => {
    return user?.roles.includes(role) ?? false;
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

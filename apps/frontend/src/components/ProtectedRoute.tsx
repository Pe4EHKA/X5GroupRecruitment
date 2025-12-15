'use client';

import { useEffect, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/providers/AuthProvider';
import { UserRole } from '@/types';
import { Box, CircularProgress } from '@mui/material';

interface ProtectedRouteProps {
  children: ReactNode;
  allowedRoles?: UserRole[];
}

export default function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const router = useRouter();
  const { isAuthenticated, user, isLoading } = useAuth();

  useEffect(() => {
    if (!isLoading) {
      if (!isAuthenticated) {
        router.push('/login');
      } else if (allowedRoles && user) {
        const hasAllowedRole = allowedRoles.some((role) => user.roles.includes(role));
        if (!hasAllowedRole) {
          router.push('/unauthorized');
        }
      }
    }
  }, [isAuthenticated, user, isLoading, allowedRoles, router]);

  if (isLoading || !isAuthenticated) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  if (allowedRoles && user) {
    const hasAllowedRole = allowedRoles.some((role) => user.roles.includes(role));
    if (!hasAllowedRole) {
      return null;
    }
  }

  return <>{children}</>;
}

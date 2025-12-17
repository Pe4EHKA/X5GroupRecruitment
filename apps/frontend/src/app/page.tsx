'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/providers/AuthProvider';
import { Box, CircularProgress } from '@mui/material';
import { UserRole } from '@/types';

export default function HomePage() {
  const router = useRouter();
  const { isAuthenticated, user, isLoading } = useAuth();

  useEffect(() => {
    if (!isLoading) {
      if (!isAuthenticated) {
        router.push('/login');
      } else if (user) {
        // Redirect based on role (priority order)
        if (user.roles.includes(UserRole.STAGER) || user.roles.includes(UserRole.CANDIDATE)) {
          router.push('/stager');
        } else if (user.roles.includes(UserRole.RECRUITER)) {
          router.push('/hr');
        } else if (user.roles.includes(UserRole.HM)) {
          router.push('/hm/inbox');
        } else if (user.roles.includes(UserRole.ADMIN)) {
          router.push('/admin/programs');
        } else {
          router.push('/login');
        }
      }
    }
  }, [isAuthenticated, user, isLoading, router]);

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

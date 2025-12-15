'use client';

import {
  Box,
  Typography,
  Paper,
} from '@mui/material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole } from '@/types';

export default function UsersPage() {
  return (
    <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
      <DashboardLayout>
        <Box>
          <Typography variant="h4" gutterBottom>
            Управление пользователями
          </Typography>
          <Paper sx={{ p: 3 }}>
            <Typography variant="body1" color="text.secondary">
              Управление пользователями и ролями
            </Typography>
            <Typography variant="body2" sx={{ mt: 2 }}>
              (В разработке - минимальная функциональность для MVP)
            </Typography>
          </Paper>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

'use client';

import {
  Box,
  Typography,
  Paper,
} from '@mui/material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole } from '@/types';

export default function TemplatesPage() {
  return (
    <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
      <DashboardLayout>
        <Box>
          <Typography variant="h4" gutterBottom>
            Шаблоны уведомлений
          </Typography>
          <Paper sx={{ p: 3 }}>
            <Typography variant="body1" color="text.secondary">
              CRUD операции для шаблонов email уведомлений
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

'use client';

import {
  Box,
  Typography,
  Paper,
} from '@mui/material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole } from '@/types';

export default function AuditPage() {
  return (
    <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
      <DashboardLayout>
        <Box>
          <Typography variant="h4" gutterBottom>
            Журнал аудита
          </Typography>
          <Paper sx={{ p: 3 }}>
            <Typography variant="body1" color="text.secondary">
              Просмотр логов и истории действий в системе
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

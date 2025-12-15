'use client';

import {
  Box,
  Typography,
  Paper,
} from '@mui/material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole } from '@/types';

export default function ProgramsPage() {
  return (
    <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
      <DashboardLayout>
        <Box>
          <Typography variant="h4" gutterBottom>
            Управление программами
          </Typography>
          <Paper sx={{ p: 3 }}>
            <Typography variant="body1" color="text.secondary">
              CRUD операции для программ стажировок и вакансий
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

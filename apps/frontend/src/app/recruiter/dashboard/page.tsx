'use client';

import { Box, Grid, Paper, Typography, CircularProgress } from '@mui/material';
import {
  Assignment,
  CheckCircle,
  Cancel,
  Schedule,
  Warning,
  HourglassEmpty,
} from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole } from '@/types';
import { useDashboardMetrics } from '@/hooks/useRecruiter';

interface MetricCardProps {
  title: string;
  value: number;
  icon: React.ReactNode;
  color: string;
}

function MetricCard({ title, value, icon, color }: MetricCardProps) {
  return (
    <Paper sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            {title}
          </Typography>
          <Typography variant="h4" component="div">
            {value}
          </Typography>
        </Box>
        <Box
          sx={{
            width: 56,
            height: 56,
            borderRadius: '50%',
            backgroundColor: color,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
          }}
        >
          {icon}
        </Box>
      </Box>
    </Paper>
  );
}

export default function RecruiterDashboard() {
  const { data: metrics, isLoading } = useDashboardMetrics();

  if (isLoading) {
    return (
      <ProtectedRoute allowedRoles={[UserRole.RECRUITER]}>
        <DashboardLayout>
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
            <CircularProgress />
          </Box>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute allowedRoles={[UserRole.RECRUITER]}>
      <DashboardLayout>
        <Box>
          <Typography variant="h4" gutterBottom>
            Dashboard
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
            Обзор заявок и метрик
          </Typography>

          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={4}>
              <MetricCard
                title="Новые"
                value={metrics?.newCount || 0}
                icon={<Assignment />}
                color="#1976d2"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <MetricCard
                title="На скрининге"
                value={metrics?.screeningCount || 0}
                icon={<HourglassEmpty />}
                color="#9c27b0"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <MetricCard
                title="На интервью"
                value={metrics?.interviewCount || 0}
                icon={<Schedule />}
                color="#2196f3"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <MetricCard
                title="Одобрено"
                value={metrics?.approvedCount || 0}
                icon={<CheckCircle />}
                color="#4caf50"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <MetricCard
                title="Отклонено"
                value={metrics?.rejectedCount || 0}
                icon={<Cancel />}
                color="#f44336"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <MetricCard
                title="Нарушение SLA"
                value={metrics?.slaBreachCount || 0}
                icon={<Warning />}
                color="#ff5722"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <MetricCard
                title="Всего заявок"
                value={metrics?.totalCount || 0}
                icon={<Assignment />}
                color="#607d8b"
              />
            </Grid>
          </Grid>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

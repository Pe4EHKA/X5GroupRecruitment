'use client';

import { Box, Grid, Paper, Typography, Stack, Skeleton } from '@mui/material';
import {
  Assignment,
  CheckCircle,
  Cancel,
  Schedule,
  Warning,
  HourglassEmpty,
} from '@mui/icons-material';
import { PageHeader } from '@/components/ui/PageHeader';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole } from '@/types';
import { useDashboardMetrics } from '@/hooks/useRecruiter';
import { palette } from '@/theme/tokens';

interface MetricCardProps {
  title: string;
  value: number;
  icon: React.ReactNode;
  color: string;
}

function MetricCard({ title, value, icon, color }: MetricCardProps) {
  return (
    <Paper data-variant="elevated" sx={{ p: 3 }}>
      <Stack spacing={2}>
        <Stack direction="row" spacing={1.5} alignItems="center" justifyContent="space-between">
          <Stack spacing={0.5}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              {title}
            </Typography>
            <Typography variant="h4" component="div">
              {value}
            </Typography>
          </Stack>
          <Box
            sx={{
              width: 56,
              height: 56,
              borderRadius: '50%',
              background: color,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff',
              boxShadow: '0 14px 30px rgba(0,0,0,0.12)',
            }}
          >
            {icon}
          </Box>
        </Stack>
        <Box
          sx={{
            height: 6,
            borderRadius: 999,
            backgroundColor: `${color}20`,
            overflow: 'hidden',
          }}
        >
          <Box
            sx={{
              width: `${Math.min(100, value * 8)}%`,
              backgroundColor: color,
              height: '100%',
              transition: 'width 240ms ease',
            }}
          />
        </Box>
      </Stack>
    </Paper>
  );
}

export default function RecruiterDashboard() {
  const { data: metrics, isLoading } = useDashboardMetrics();

  return (
    <ProtectedRoute allowedRoles={[UserRole.RECRUITER]}>
      <DashboardLayout>
        <Box>
          <PageHeader
            title="Дашборд рекрутера"
            subtitle="Быстрый обзор воронки подбора и зон риска по SLA. Все метрики обновляются в реальном времени."
            chipLabel="Recruitment Ops"
          />

          <Grid container spacing={3}>
            {[
              { title: 'Новые', value: metrics?.newCount, icon: <Assignment />, color: '#4338ca' },
              { title: 'На скрининге', value: metrics?.screeningCount, icon: <HourglassEmpty />, color: '#7c3aed' },
              { title: 'На интервью', value: metrics?.interviewCount, icon: <Schedule />, color: '#0ea5e9' },
              { title: 'Одобрено', value: metrics?.approvedCount, icon: <CheckCircle />, color: '#22c55e' },
              { title: 'Отклонено', value: metrics?.rejectedCount, icon: <Cancel />, color: '#ef4444' },
              { title: 'Нарушение SLA', value: metrics?.slaBreachCount, icon: <Warning />, color: '#f59e0b' },
              { title: 'Всего заявок', value: metrics?.totalCount, icon: <Assignment />, color: palette.neutral[600] },
            ].map((item, idx) => (
              <Grid key={item.title} item xs={12} sm={6} md={4}>
                {isLoading ? (
                  <Paper data-variant="elevated" sx={{ p: 3 }}>
                    <Stack spacing={2}>
                      <Skeleton width="60%" height={20} />
                      <Skeleton width="40%" height={36} />
                      <Skeleton variant="rectangular" height={8} sx={{ borderRadius: 999 }} />
                    </Stack>
                  </Paper>
                ) : (
                  <MetricCard {...item} value={item.value || 0} />
                )}
              </Grid>
            ))}
          </Grid>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

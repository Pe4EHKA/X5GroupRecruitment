'use client';

import { useRouter } from 'next/navigation';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  CircularProgress,
  IconButton,
  Chip,
} from '@mui/material';
import { Visibility } from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import StatusBadge from '@/components/StatusBadge';
import { UserRole } from '@/types';
import { usePendingApplications } from '@/hooks/useHm';
import { format } from 'date-fns';

export default function HmInboxPage() {
  const router = useRouter();
  const { data, isLoading } = usePendingApplications();

  const handleViewApplication = (id: number) => {
    router.push(`/hm/applications/${id}`);
  };

  if (isLoading) {
    return (
      <ProtectedRoute allowedRoles={[UserRole.HM]}>
        <DashboardLayout>
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
            <CircularProgress />
          </Box>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute allowedRoles={[UserRole.HM]}>
      <DashboardLayout>
        <Box>
          <Typography variant="h4" gutterBottom>
            Входящие заявки
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
            Заявки на рассмотрение
          </Typography>

          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Кандидат</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Вакансия</TableCell>
                  <TableCell>Статус</TableCell>
                  <TableCell>Рекрутер</TableCell>
                  <TableCell>Дата создания</TableCell>
                  <TableCell>Действия</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data?.content?.map((application) => (
                  <TableRow key={application.id} hover>
                    <TableCell>{application.id}</TableCell>
                    <TableCell>{application.candidate.fullName}</TableCell>
                    <TableCell>{application.candidate.email}</TableCell>
                    <TableCell>{application.vacancyTitle}</TableCell>
                    <TableCell>
                      <StatusBadge status={application.status} />
                    </TableCell>
                    <TableCell>{application.recruiterName || '-'}</TableCell>
                    <TableCell>
                      {format(new Date(application.createdAt), 'dd.MM.yyyy HH:mm')}
                    </TableCell>
                    <TableCell>
                      <IconButton
                        size="small"
                        color="primary"
                        onClick={() => handleViewApplication(application.id)}
                      >
                        <Visibility />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
                {(!data?.content || data.content.length === 0) && (
                  <TableRow>
                    <TableCell colSpan={8} align="center">
                      Нет заявок на рассмотрении
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

'use client';

import { useState } from 'react';
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
  TablePagination,
  Typography,
  CircularProgress,
  IconButton,
  Chip,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Button,
} from '@mui/material';
import { Visibility, Warning } from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import StatusBadge from '@/components/StatusBadge';
import { UserRole, ApplicationStatus, ApplicationFilters } from '@/types';
import { useApplications } from '@/hooks/useRecruiter';
import { getCandidateFullName, getCandidateEmail } from '@/lib/utils';
import { format } from 'date-fns';

export default function ApplicationsPage() {
  const router = useRouter();
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [filters, setFilters] = useState<ApplicationFilters>({
    page: 0,
    size: 25,
  });

  const { data, isLoading } = useApplications(filters);

  const handleChangePage = (_: unknown, newPage: number) => {
    setPage(newPage);
    setFilters({ ...filters, page: newPage });
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newSize = parseInt(event.target.value, 10);
    setRowsPerPage(newSize);
    setPage(0);
    setFilters({ ...filters, size: newSize, page: 0 });
  };

  const handleFilterChange = (key: keyof ApplicationFilters, value: any) => {
    setPage(0);
    setFilters({ ...filters, [key]: value, page: 0 });
  };

  const handleViewApplication = (id: number) => {
    router.push(`/recruiter/applications/${id}`);
  };

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
            Заявки
          </Typography>

          {/* Filters */}
          <Paper sx={{ p: 2, mb: 3 }}>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6} md={3}>
                <FormControl fullWidth size="small">
                  <InputLabel>Статус</InputLabel>
                  <Select
                    value={filters.status || ''}
                    label="Статус"
                    onChange={(e) => handleFilterChange('status', e.target.value || undefined)}
                  >
                    <MenuItem value="">Все</MenuItem>
                    <MenuItem value={ApplicationStatus.NEW}>Новые</MenuItem>
                    <MenuItem value={ApplicationStatus.SCREENING}>Скрининг</MenuItem>
                    <MenuItem value={ApplicationStatus.PENDING_HM_REVIEW}>Ожидает HM</MenuItem>
                    <MenuItem value={ApplicationStatus.APPROVED}>Одобрено</MenuItem>
                    <MenuItem value={ApplicationStatus.REJECTED}>Отклонено</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <TextField
                  fullWidth
                  size="small"
                  label="Дата от"
                  type="date"
                  InputLabelProps={{ shrink: true }}
                  value={filters.dateFrom || ''}
                  onChange={(e) => handleFilterChange('dateFrom', e.target.value || undefined)}
                />
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <TextField
                  fullWidth
                  size="small"
                  label="Дата до"
                  type="date"
                  InputLabelProps={{ shrink: true }}
                  value={filters.dateTo || ''}
                  onChange={(e) => handleFilterChange('dateTo', e.target.value || undefined)}
                />
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <FormControl fullWidth size="small">
                  <InputLabel>SLA</InputLabel>
                  <Select
                    value={filters.slaBreached !== undefined ? (filters.slaBreached ? 'true' : 'false') : ''}
                    label="SLA"
                    onChange={(e) =>
                      handleFilterChange(
                        'slaBreached',
                        e.target.value === '' ? undefined : e.target.value === 'true'
                      )
                    }
                  >
                    <MenuItem value="">Все</MenuItem>
                    <MenuItem value="true">Нарушен</MenuItem>
                    <MenuItem value="false">В норме</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <Button
                  variant="outlined"
                  onClick={() => {
                    setFilters({ page: 0, size: rowsPerPage });
                    setPage(0);
                  }}
                >
                  Сбросить фильтры
                </Button>
              </Grid>
            </Grid>
          </Paper>

          {/* Table */}
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
                    <TableCell>{getCandidateFullName(application.candidate)}</TableCell>
                    <TableCell>{getCandidateEmail(application.candidate)}</TableCell>
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
                      Нет заявок
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
            <TablePagination
              component="div"
              count={data?.totalElements || 0}
              page={page}
              onPageChange={handleChangePage}
              rowsPerPage={rowsPerPage}
              onRowsPerPageChange={handleChangeRowsPerPage}
              rowsPerPageOptions={[10, 25, 50, 100]}
            />
          </TableContainer>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  Box,
  Typography,
  Paper,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Chip,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  CircularProgress,
  Stack,
  IconButton,
  InputAdornment,
} from '@mui/material';
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  Clear as ClearIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole, ApplicationStatus } from '@/types';
import { useApplications } from '@/hooks/useHr';

const STATUS_COLORS: Record<ApplicationStatus, string> = {
  [ApplicationStatus.NEW]: '#2196f3',
  [ApplicationStatus.SCREENING]: '#9c27b0',
  [ApplicationStatus.INTERVIEW_SCHEDULED]: '#00bcd4',
  [ApplicationStatus.INTERVIEW_COMPLETED]: '#00bcd4',
  [ApplicationStatus.APPROVED]: '#4caf50',
  [ApplicationStatus.REJECTED]: '#f44336',
  [ApplicationStatus.OFFER_SENT]: '#8bc34a',
  [ApplicationStatus.OFFER_ACCEPTED]: '#4caf50',
  [ApplicationStatus.OFFER_DECLINED]: '#f44336',
  [ApplicationStatus.WITHDRAWN]: '#9e9e9e',
  [ApplicationStatus.ON_HOLD]: '#607d8b',
};

const STATUS_LABELS: Record<ApplicationStatus, string> = {
  [ApplicationStatus.NEW]: 'Новые',
  [ApplicationStatus.SCREENING]: 'На скрининге',
  [ApplicationStatus.INTERVIEW_SCHEDULED]: 'Интервью назначено',
  [ApplicationStatus.INTERVIEW_COMPLETED]: 'Интервью завершено',
  [ApplicationStatus.APPROVED]: 'Одобрено',
  [ApplicationStatus.REJECTED]: 'Отклонено',
  [ApplicationStatus.OFFER_SENT]: 'Оффер отправлен',
  [ApplicationStatus.OFFER_ACCEPTED]: 'Оффер принят',
  [ApplicationStatus.OFFER_DECLINED]: 'Оффер отклонён',
  [ApplicationStatus.WITHDRAWN]: 'Отозвано',
  [ApplicationStatus.ON_HOLD]: 'На удержании',
};

export default function HRDashboard() {
  const router = useRouter();
  const [search, setSearch] = useState('');
  const [selectedStatuses, setSelectedStatuses] = useState<ApplicationStatus[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);

  const { data, isLoading, refetch } = useApplications({
    statuses: selectedStatuses.length > 0 ? selectedStatuses : undefined,
    search: search || undefined,
    page,
    size: rowsPerPage,
  });

  const handleStatusToggle = (status: ApplicationStatus) => {
    setSelectedStatuses((prev) =>
      prev.includes(status) ? prev.filter((s) => s !== status) : [...prev, status]
    );
    setPage(0); // Reset to first page when filter changes
  };

  const handleClearFilters = () => {
    setSelectedStatuses([]);
    setSearch('');
    setPage(0);
  };

  const handleChangePage = (_event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <ProtectedRoute allowedRoles={[UserRole.RECRUITER, UserRole.ADMIN]}>
      <DashboardLayout>
        <Box>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h4">HR Dashboard</Typography>
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={() => refetch()}
            >
              Обновить
            </Button>
          </Box>

          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              Фильтры
            </Typography>

            {/* Quick filter chips */}
            <Box sx={{ mb: 2 }}>
              <Typography variant="caption" color="text.secondary" gutterBottom>
                Быстрые фильтры:
              </Typography>
              <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mt: 1 }}>
                {[
                  ApplicationStatus.NEW,
                  ApplicationStatus.SCREENING,
                  ApplicationStatus.INTERVIEW_SCHEDULED,
                  ApplicationStatus.APPROVED,
                  ApplicationStatus.REJECTED,
                ].map((status) => (
                  <Chip
                    key={status}
                    label={STATUS_LABELS[status]}
                    onClick={() => handleStatusToggle(status)}
                    color={selectedStatuses.includes(status) ? 'primary' : 'default'}
                    variant={selectedStatuses.includes(status) ? 'filled' : 'outlined'}
                    sx={{
                      borderColor: selectedStatuses.includes(status) ? undefined : STATUS_COLORS[status],
                      color: selectedStatuses.includes(status) ? undefined : STATUS_COLORS[status],
                    }}
                  />
                ))}
              </Stack>
            </Box>

            {/* Search */}
            <TextField
              fullWidth
              placeholder="Поиск по ФИО, email или телефону..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
                endAdornment: search && (
                  <InputAdornment position="end">
                    <IconButton size="small" onClick={() => setSearch('')}>
                      <ClearIcon />
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            {(selectedStatuses.length > 0 || search) && (
              <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
                <Button
                  size="small"
                  startIcon={<ClearIcon />}
                  onClick={handleClearFilters}
                >
                  Сбросить фильтры
                </Button>
              </Box>
            )}
          </Paper>

          <Paper>
            {isLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                <CircularProgress />
              </Box>
            ) : (
              <>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Кандидат</TableCell>
                        <TableCell>Email</TableCell>
                        <TableCell>Телефон</TableCell>
                        <TableCell>Вакансия</TableCell>
                        <TableCell>Статус</TableCell>
                        <TableCell>Дата подачи</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {data?.content.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={6} align="center">
                            <Typography variant="body2" color="text.secondary">
                              Заявки не найдены
                            </Typography>
                          </TableCell>
                        </TableRow>
                      ) : (
                        data?.content.map((application) => (
                          <TableRow
                            key={application.id}
                            hover
                            sx={{ cursor: 'pointer' }}
                            onClick={() => router.push(`/hr/applications/${application.id}`)}
                          >
                            <TableCell>
                              {application.candidate?.fullName || application.candidateName || 'N/A'}
                            </TableCell>
                            <TableCell>
                              {application.candidate?.email || application.candidateEmail || 'N/A'}
                            </TableCell>
                            <TableCell>
                              {application.candidate?.phone || 'N/A'}
                            </TableCell>
                            <TableCell>{application.vacancyTitle}</TableCell>
                            <TableCell>
                              <Chip
                                label={STATUS_LABELS[application.status]}
                                size="small"
                                sx={{
                                  backgroundColor: STATUS_COLORS[application.status],
                                  color: 'white',
                                }}
                              />
                            </TableCell>
                            <TableCell>{formatDate(application.createdAt)}</TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
                <TablePagination
                  component="div"
                  count={data?.totalElements || 0}
                  page={page}
                  onPageChange={handleChangePage}
                  rowsPerPage={rowsPerPage}
                  onRowsPerPageChange={handleChangeRowsPerPage}
                  rowsPerPageOptions={[10, 20, 50, 100]}
                  labelRowsPerPage="Строк на странице:"
                  labelDisplayedRows={({ from, to, count }) =>
                    `${from}–${to} из ${count !== -1 ? count : `более ${to}`}`
                  }
                />
              </>
            )}
          </Paper>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

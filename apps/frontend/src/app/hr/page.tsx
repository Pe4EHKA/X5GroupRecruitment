'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  Box,
  Typography,
  Paper,
  TextField,
  Chip,
  Button,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Stack,
  IconButton,
  InputAdornment,
  Grid,
  Skeleton,
  Divider,
  Collapse,
} from '@mui/material';
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  Clear as ClearIcon,
  Refresh as RefreshIcon,
  LockReset,
} from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole, ApplicationStatus } from '@/types';
import { useApplications, useResetTraineePassword } from '@/hooks/useHr';
import { useSnackbar } from 'notistack';
import { PageHeader } from '@/components/ui/PageHeader';
import { EmptyState } from '@/components/ui/EmptyState';
import { keyframes } from '@mui/system';
import { palette } from '@/theme/tokens';

const STATUS_COLORS: Record<ApplicationStatus, string> = {
  [ApplicationStatus.NEW]: palette.primary.main,
  [ApplicationStatus.SCREENING]: palette.secondary.main,
  [ApplicationStatus.INTERVIEW_SCHEDULED]: palette.semantic.info,
  [ApplicationStatus.INTERVIEW_COMPLETED]: palette.semantic.info,
  [ApplicationStatus.APPROVED]: palette.semantic.success,
  [ApplicationStatus.REJECTED]: palette.semantic.error,
  [ApplicationStatus.OFFER_SENT]: palette.secondary.light,
  [ApplicationStatus.OFFER_ACCEPTED]: palette.primary.dark,
  [ApplicationStatus.OFFER_DECLINED]: palette.semantic.error,
  [ApplicationStatus.WITHDRAWN]: palette.neutral[600],
  [ApplicationStatus.ON_HOLD]: palette.semantic.warning,
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

const fadeIn = keyframes`
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
`;

export default function HRDashboard() {
  const router = useRouter();
  const { enqueueSnackbar } = useSnackbar();
  const [search, setSearch] = useState('');
  const [selectedStatuses, setSelectedStatuses] = useState<ApplicationStatus[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [traineeId, setTraineeId] = useState('');
  const [customPassword, setCustomPassword] = useState('');
  const [temporaryPassword, setTemporaryPassword] = useState<string | null>(null);
  const [filtersOpen, setFiltersOpen] = useState(true);

  const { data, isLoading, refetch } = useApplications({
    statuses: selectedStatuses.length > 0 ? selectedStatuses : undefined,
    search: search || undefined,
    page,
    size: rowsPerPage,
  });

  const resetPassword = useResetTraineePassword();

  const handleStatusToggle = (status: ApplicationStatus) => {
    setSelectedStatuses((prev) =>
      prev.includes(status) ? prev.filter((s) => s !== status) : [...prev, status]
    );
    setPage(0);
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

  const handlePasswordReset = async () => {
    const parsedId = parseInt(traineeId, 10);
    if (!parsedId) {
      enqueueSnackbar('Введите корректный ID стажера', { variant: 'warning' });
      return;
    }

    try {
      const response = await resetPassword.mutateAsync({
        traineeId: parsedId,
        newPassword: customPassword || undefined,
      });
      setTemporaryPassword(response.temporaryPassword);
      setCustomPassword('');
    } catch (error) {
      console.error(error);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const tableSkeleton = useMemo(
    () =>
      Array.from({ length: 6 }).map((_, index) => (
        <TableRow key={index}>
          {Array.from({ length: 6 }).map((__, cellIndex) => (
            <TableCell key={cellIndex}>
              <Skeleton variant="text" width={cellIndex === 0 ? 140 : 100} />
            </TableCell>
          ))}
        </TableRow>
      )),
    []
  );

  return (
    <ProtectedRoute allowedRoles={[UserRole.RECRUITER, UserRole.ADMIN]}>
      <DashboardLayout>
        <Box sx={{ animation: `${fadeIn} 220ms ease`, display: 'flex', flexDirection: 'column', gap: 2.5 }}>
          <PageHeader
            title="HR кабинет"
            subtitle="Следите за статусами кандидатов, управляйте доступом и быстро реагируйте на воронку подбора."
            chipLabel="Hiring Operations"
            actions={
              <Button
                variant="outlined"
                startIcon={<RefreshIcon />}
                onClick={() => refetch()}
                sx={{ alignSelf: 'flex-start' }}
              >
                Обновить данные
              </Button>
            }
          />

          <Grid container spacing={2} sx={{ mb: 1.5 }}>
            <Grid item xs={12} sm={6} md={4}>
              <Paper data-variant="elevated" sx={{ p: 2.5 }}>
                <Typography variant="body2" color="text.secondary">Всего заявок</Typography>
                <Typography variant="h4">{data?.totalElements ?? '—'}</Typography>
                <Typography variant="caption" color="text.secondary">
                  В обработке с учётом фильтров
                </Typography>
              </Paper>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Paper data-variant="elevated" sx={{ p: 2.5 }}>
                <Typography variant="body2" color="text.secondary">Активные статусы</Typography>
                <Typography variant="h4">{selectedStatuses.length || 'Все'}</Typography>
                <Typography variant="caption" color="text.secondary">
                  {selectedStatuses.length ? 'Применено' : 'Без фильтрации'} по статусам
                </Typography>
              </Paper>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Paper data-variant="elevated" sx={{ p: 2.5 }}>
                <Typography variant="body2" color="text.secondary">Строк на странице</Typography>
                <Typography variant="h4">{rowsPerPage}</Typography>
                <Typography variant="caption" color="text.secondary">Настройте под вашу скорость обзора</Typography>
              </Paper>
            </Grid>
          </Grid>

          <Grid container spacing={3}>
            <Grid item xs={12} md={5}>
              <Paper data-variant="elevated" sx={{ p: 3, display: 'grid', gap: 2 }}>
                <Stack direction="row" spacing={1.5} alignItems="center">
                  <LockReset color="primary" />
                  <Box>
                    <Typography variant="h6">Доступ к кабинету стажера</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Сбросьте пароль стажера, чтобы выдать временные учётные данные для проверки роли.
                    </Typography>
                  </Box>
                </Stack>
                <Stack spacing={1.5}>
                  <TextField
                    label="ID стажера"
                    type="number"
                    value={traineeId}
                    onChange={(e) => {
                      setTraineeId(e.target.value);
                      setTemporaryPassword(null);
                    }}
                  />
                  <TextField
                    label="Новый пароль (опционально)"
                    type="text"
                    value={customPassword}
                    onChange={(e) => setCustomPassword(e.target.value)}
                  />
                  <Button
                    variant="contained"
                    onClick={handlePasswordReset}
                    disabled={resetPassword.isPending}
                  >
                    Сбросить пароль
                  </Button>
                  {temporaryPassword && (
                    <Alert severity="success" sx={{ mt: 1 }}>
                      Временный пароль: <strong>{temporaryPassword}</strong>
                    </Alert>
                  )}
                </Stack>
              </Paper>
            </Grid>

            <Grid item xs={12} md={7}>
              <Paper data-variant="elevated" sx={{ p: 3, display: 'grid', gap: 2 }}>
                <Stack direction="row" spacing={1.5} alignItems="center" justifyContent="space-between">
                  <Stack spacing={0.5}>
                    <Typography variant="h6">Фильтры по заявкам</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Быстро переключайтесь между основными статусами или найдите кандидата по контактам.
                    </Typography>
                  </Stack>
                  <IconButton onClick={() => setFiltersOpen((prev) => !prev)}>
                    <FilterIcon />
                  </IconButton>
                </Stack>
                <Collapse in={filtersOpen}>
                  <Divider sx={{ my: 1 }} />
                  <Box sx={{ mb: 1 }}>
                    <Typography variant="caption" color="text.secondary">
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
                            transition: 'all 0.18s ease',
                          }}
                        />
                      ))}
                    </Stack>
                  </Box>
                  <TextField
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
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                      <Button size="small" startIcon={<ClearIcon />} onClick={handleClearFilters}>
                        Сбросить фильтры
                      </Button>
                    </Box>
                  )}
                </Collapse>
              </Paper>
            </Grid>
          </Grid>

          <Paper data-variant="elevated" sx={{ p: 3 }}>
            <Stack direction={{ xs: 'column', md: 'row' }} alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
              <Box>
                <Typography variant="h6">Заявки кандидатов</Typography>
                <Typography variant="body2" color="text.secondary">
                  Отсортировано по дате подачи, кликайте строку, чтобы открыть карточку.
                </Typography>
              </Box>
              <Chip icon={<FilterIcon />} label={`${data?.totalElements ?? 0} в обработке`} color="primary" />
            </Stack>
            {isLoading ? (
              <TableContainer sx={{ maxHeight: { xs: 420, md: 'none' }, minWidth: 960 }}>
                <Table stickyHeader sx={{ minWidth: 960 }}>
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
                  <TableBody>{tableSkeleton}</TableBody>
                </Table>
              </TableContainer>
            ) : data?.content.length === 0 ? (
              <EmptyState
                title="Заявки не найдены"
                description="Попробуйте изменить фильтры или сбросить поиск, чтобы увидеть больше кандидатов."
                actionLabel="Сбросить фильтры"
                onAction={handleClearFilters}
              />
            ) : (
              <>
                <TableContainer sx={{ maxHeight: { xs: 420, md: 'none' }, minWidth: 960 }}>
                  <Table stickyHeader sx={{ minWidth: 960 }}>
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
                      {data?.content.map((application) => (
                        <TableRow
                          key={application.id}
                          hover
                          sx={{ cursor: 'pointer' }}
                          onClick={() => router.push(`/hr/applications/${application.id}`)}
                        >
                          <TableCell sx={{ maxWidth: 220 }}>
                            {application.candidate?.fullName || application.candidateName || 'N/A'}
                          </TableCell>
                          <TableCell sx={{ maxWidth: 220 }}>
                            {application.candidate?.email || application.candidateEmail || 'N/A'}
                          </TableCell>
                          <TableCell sx={{ maxWidth: 180 }}>{application.candidate?.phone || 'N/A'}</TableCell>
                          <TableCell sx={{ maxWidth: 220 }}>{application.vacancyTitle}</TableCell>
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
                      ))}
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
                  labelDisplayedRows={({ from, to, count }) => `${from}–${to} из ${count !== -1 ? count : `более ${to}`}`}
                />
              </>
            )}
          </Paper>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

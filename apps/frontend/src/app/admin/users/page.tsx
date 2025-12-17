'use client';

import { useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Chip,
  IconButton,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  FilterList as FilterIcon,
} from '@mui/icons-material';
import { useRouter } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole, UserStatus, AdminUser } from '@/types';
import { adminUserService } from '@/services/adminUserService';

export default function UsersPage() {
  const router = useRouter();
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<UserStatus | ''>('');
  const [roleFilter, setRoleFilter] = useState<UserRole | ''>('');

  // Fetch users
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['admin-users', page, rowsPerPage, searchQuery, statusFilter, roleFilter],
    queryFn: () => adminUserService.getUsers({
      q: searchQuery || undefined,
      status: statusFilter || undefined,
      role: roleFilter || undefined,
      page,
      size: rowsPerPage,
      sort: 'updatedAt',
      direction: 'desc',
    }),
  });

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  const getRoleColor = (role: UserRole) => {
    switch (role) {
      case UserRole.ADMIN:
        return 'error';
      case UserRole.RECRUITER:
        return 'primary';
      case UserRole.HM:
        return 'secondary';
      default:
        return 'default';
    }
  };

  const getStatusColor = (status: UserStatus) => {
    switch (status) {
      case UserStatus.ACTIVE:
        return 'success';
      case UserStatus.DISABLED:
        return 'error';
      case UserStatus.INVITED:
        return 'warning';
      default:
        return 'default';
    }
  };

  return (
    <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
      <DashboardLayout>
        <Box>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Typography variant="h4">
              Управление пользователями
            </Typography>
            <Button
              variant="contained"
              color="primary"
              startIcon={<AddIcon />}
              onClick={() => router.push('/admin/users/create')}
            >
              Создать пользователя
            </Button>
          </Box>

          {/* Filters */}
          <Paper sx={{ p: 2, mb: 3 }}>
            <Box display="flex" gap={2} alignItems="center" flexWrap="wrap">
              <TextField
                label="Поиск"
                placeholder="Email, имя пользователя..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                size="small"
                sx={{ minWidth: 250 }}
              />
              
              <FormControl size="small" sx={{ minWidth: 150 }}>
                <InputLabel>Статус</InputLabel>
                <Select
                  value={statusFilter}
                  label="Статус"
                  onChange={(e) => setStatusFilter(e.target.value as UserStatus | '')}
                >
                  <MenuItem value="">Все</MenuItem>
                  <MenuItem value={UserStatus.ACTIVE}>Активный</MenuItem>
                  <MenuItem value={UserStatus.DISABLED}>Отключен</MenuItem>
                  <MenuItem value={UserStatus.INVITED}>Приглашен</MenuItem>
                </Select>
              </FormControl>

              <FormControl size="small" sx={{ minWidth: 150 }}>
                <InputLabel>Роль</InputLabel>
                <Select
                  value={roleFilter}
                  label="Роль"
                  onChange={(e) => setRoleFilter(e.target.value as UserRole | '')}
                >
                  <MenuItem value="">Все</MenuItem>
                  <MenuItem value={UserRole.ADMIN}>Администратор</MenuItem>
                  <MenuItem value={UserRole.RECRUITER}>Рекрутер</MenuItem>
                  <MenuItem value={UserRole.HM}>Руководитель</MenuItem>
                </Select>
              </FormControl>

              <Button
                variant="outlined"
                startIcon={<FilterIcon />}
                onClick={handleSearch}
              >
                Применить
              </Button>
            </Box>
          </Paper>

          {/* Table */}
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              Ошибка загрузки пользователей: {(error as Error).message}
            </Alert>
          )}

          <TableContainer component={Paper}>
            {isLoading ? (
              <Box display="flex" justifyContent="center" p={4}>
                <CircularProgress />
              </Box>
            ) : (
              <>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Имя пользователя</TableCell>
                      <TableCell>Email</TableCell>
                      <TableCell>ФИО</TableCell>
                      <TableCell>Роли</TableCell>
                      <TableCell>Статус</TableCell>
                      <TableCell>Обновлено</TableCell>
                      <TableCell align="right">Действия</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {data?.content.map((user: AdminUser) => (
                      <TableRow key={user.id} hover>
                        <TableCell>{user.username}</TableCell>
                        <TableCell>{user.email}</TableCell>
                        <TableCell>{user.fullName}</TableCell>
                        <TableCell>
                          <Box display="flex" gap={0.5} flexWrap="wrap">
                            {user.roles.map((role) => (
                              <Chip
                                key={role}
                                label={role}
                                size="small"
                                color={getRoleColor(role)}
                              />
                            ))}
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={user.status}
                            size="small"
                            color={getStatusColor(user.status)}
                          />
                        </TableCell>
                        <TableCell>
                          {new Date(user.updatedAt).toLocaleString('ru-RU')}
                        </TableCell>
                        <TableCell align="right">
                          <IconButton
                            size="small"
                            onClick={() => router.push(`/admin/users/${user.id}`)}
                          >
                            <EditIcon />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                    {data?.content.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={7} align="center">
                          <Typography variant="body2" color="text.secondary">
                            Пользователи не найдены
                          </Typography>
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
                  rowsPerPageOptions={[10, 20, 50, 100]}
                  labelRowsPerPage="Строк на странице:"
                  labelDisplayedRows={({ from, to, count }) =>
                    `${from}-${to} из ${count}`
                  }
                />
              </>
            )}
          </TableContainer>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

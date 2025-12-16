'use client';

import { useState } from 'react';
import { use } from 'react';
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  FormControl,
  FormLabel,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Select,
  MenuItem,
  InputLabel,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Grid2 as Grid,
} from '@mui/material';
import {
  Save as SaveIcon,
  ArrowBack as ArrowBackIcon,
} from '@mui/icons-material';
import { useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole, UserStatus, AdminUser, UpdateUserRequest, UpdateRolesRequest, UpdateStatusRequest } from '@/types';
import { adminUserService } from '@/services/adminUserService';

interface PageProps {
  params: Promise<{ id: string }>;
}

export default function UserDetailsPage({ params }: PageProps) {
  const resolvedParams = use(params);
  const userId = parseInt(resolvedParams.id);
  const router = useRouter();
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();

  const [formData, setFormData] = useState<UpdateUserRequest>({});
  const [selectedRoles, setSelectedRoles] = useState<UserRole[]>([]);
  const [selectedStatus, setSelectedStatus] = useState<UserStatus>(UserStatus.ACTIVE);
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    action: 'disable' | 'removeAdmin' | null;
  }>({ open: false, action: null });

  // Fetch user
  const { data: user, isLoading, error } = useQuery({
    queryKey: ['admin-user', userId],
    queryFn: () => adminUserService.getUserById(userId),
    enabled: !isNaN(userId),
  });

  // Initialize form when user loads
  useState(() => {
    if (user) {
      setFormData({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone,
        department: user.department,
        comment: user.comment,
      });
      setSelectedRoles(user.roles);
      setSelectedStatus(user.status);
    }
  });

  // Update profile mutation
  const updateProfileMutation = useMutation({
    mutationFn: (data: UpdateUserRequest) => adminUserService.updateUser(userId, data),
    onSuccess: () => {
      enqueueSnackbar('Профиль обновлен', { variant: 'success' });
      queryClient.invalidateQueries({ queryKey: ['admin-user', userId] });
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
    },
    onError: (error: any) => {
      enqueueSnackbar(
        `Ошибка обновления профиля: ${error.response?.data?.message || error.message}`,
        { variant: 'error' }
      );
    },
  });

  // Update roles mutation
  const updateRolesMutation = useMutation({
    mutationFn: (data: UpdateRolesRequest) => adminUserService.updateUserRoles(userId, data),
    onSuccess: () => {
      enqueueSnackbar('Роли обновлены', { variant: 'success' });
      queryClient.invalidateQueries({ queryKey: ['admin-user', userId] });
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      setConfirmDialog({ open: false, action: null });
    },
    onError: (error: any) => {
      enqueueSnackbar(
        `Ошибка обновления ролей: ${error.response?.data?.message || error.message}`,
        { variant: 'error' }
      );
      setConfirmDialog({ open: false, action: null });
    },
  });

  // Update status mutation
  const updateStatusMutation = useMutation({
    mutationFn: (data: UpdateStatusRequest) => adminUserService.updateUserStatus(userId, data),
    onSuccess: () => {
      enqueueSnackbar('Статус обновлен', { variant: 'success' });
      queryClient.invalidateQueries({ queryKey: ['admin-user', userId] });
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      setConfirmDialog({ open: false, action: null });
    },
    onError: (error: any) => {
      enqueueSnackbar(
        `Ошибка обновления статуса: ${error.response?.data?.message || error.message}`,
        { variant: 'error' }
      );
      setConfirmDialog({ open: false, action: null });
    },
  });

  const handleSaveProfile = () => {
    updateProfileMutation.mutate(formData);
  };

  const handleSaveRoles = () => {
    // Check if removing admin role
    if (user?.roles.includes(UserRole.ADMIN) && !selectedRoles.includes(UserRole.ADMIN)) {
      setConfirmDialog({ open: true, action: 'removeAdmin' });
    } else {
      updateRolesMutation.mutate({ roles: selectedRoles });
    }
  };

  const handleSaveStatus = () => {
    // Check if disabling user
    if (selectedStatus === UserStatus.DISABLED && user?.status === UserStatus.ACTIVE) {
      setConfirmDialog({ open: true, action: 'disable' });
    } else {
      updateStatusMutation.mutate({ status: selectedStatus });
    }
  };

  const handleConfirmAction = () => {
    if (confirmDialog.action === 'disable') {
      updateStatusMutation.mutate({ status: selectedStatus });
    } else if (confirmDialog.action === 'removeAdmin') {
      updateRolesMutation.mutate({ roles: selectedRoles });
    }
  };

  const handleRoleToggle = (role: UserRole) => {
    setSelectedRoles((prev) =>
      prev.includes(role)
        ? prev.filter((r) => r !== role)
        : [...prev, role]
    );
  };

  if (isLoading) {
    return (
      <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
        <DashboardLayout>
          <Box display="flex" justifyContent="center" p={4}>
            <CircularProgress />
          </Box>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  if (error || !user) {
    return (
      <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
        <DashboardLayout>
          <Alert severity="error">
            Ошибка загрузки пользователя: {(error as Error)?.message || 'Пользователь не найден'}
          </Alert>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
      <DashboardLayout>
        <Box>
          <Box mb={3}>
            <Button
              startIcon={<ArrowBackIcon />}
              onClick={() => router.push('/admin/users')}
            >
              Назад к списку
            </Button>
          </Box>

          <Typography variant="h4" gutterBottom>
            Редактирование пользователя
          </Typography>

          <Grid container spacing={3}>
            {/* Profile Information */}
            <Grid size={{ xs: 12, md: 6 }}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Профиль
                </Typography>
                <Box display="flex" flexDirection="column" gap={2}>
                  <TextField
                    label="Имя пользователя"
                    value={user.username}
                    disabled
                    fullWidth
                  />
                  <TextField
                    label="Email"
                    value={formData.email || ''}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    fullWidth
                  />
                  <TextField
                    label="Имя"
                    value={formData.firstName || ''}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    fullWidth
                  />
                  <TextField
                    label="Фамилия"
                    value={formData.lastName || ''}
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    fullWidth
                  />
                  <TextField
                    label="Телефон"
                    value={formData.phone || ''}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    fullWidth
                  />
                  <TextField
                    label="Отдел"
                    value={formData.department || ''}
                    onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                    fullWidth
                  />
                  <TextField
                    label="Комментарий"
                    value={formData.comment || ''}
                    onChange={(e) => setFormData({ ...formData, comment: e.target.value })}
                    multiline
                    rows={3}
                    fullWidth
                  />
                  <Button
                    variant="contained"
                    startIcon={<SaveIcon />}
                    onClick={handleSaveProfile}
                    disabled={updateProfileMutation.isPending}
                  >
                    Сохранить профиль
                  </Button>
                </Box>
              </Paper>
            </Grid>

            {/* Roles and Status */}
            <Grid size={{ xs: 12, md: 6 }}>
              {/* Roles */}
              <Paper sx={{ p: 3, mb: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Роли
                </Typography>
                <FormControl component="fieldset" fullWidth>
                  <FormGroup>
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={selectedRoles.includes(UserRole.ADMIN)}
                          onChange={() => handleRoleToggle(UserRole.ADMIN)}
                        />
                      }
                      label="Администратор"
                    />
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={selectedRoles.includes(UserRole.RECRUITER)}
                          onChange={() => handleRoleToggle(UserRole.RECRUITER)}
                        />
                      }
                      label="Рекрутер"
                    />
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={selectedRoles.includes(UserRole.HM)}
                          onChange={() => handleRoleToggle(UserRole.HM)}
                        />
                      }
                      label="Руководитель"
                    />
                  </FormGroup>
                </FormControl>
                <Button
                  variant="contained"
                  startIcon={<SaveIcon />}
                  onClick={handleSaveRoles}
                  disabled={updateRolesMutation.isPending || selectedRoles.length === 0}
                  sx={{ mt: 2 }}
                  fullWidth
                >
                  Сохранить роли
                </Button>
              </Paper>

              {/* Status */}
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Статус
                </Typography>
                <FormControl fullWidth sx={{ mb: 2 }}>
                  <InputLabel>Статус</InputLabel>
                  <Select
                    value={selectedStatus}
                    label="Статус"
                    onChange={(e) => setSelectedStatus(e.target.value as UserStatus)}
                  >
                    <MenuItem value={UserStatus.ACTIVE}>Активный</MenuItem>
                    <MenuItem value={UserStatus.DISABLED}>Отключен</MenuItem>
                    <MenuItem value={UserStatus.INVITED}>Приглашен</MenuItem>
                  </Select>
                </FormControl>
                <Button
                  variant="contained"
                  color={selectedStatus === UserStatus.DISABLED ? 'error' : 'primary'}
                  startIcon={<SaveIcon />}
                  onClick={handleSaveStatus}
                  disabled={updateStatusMutation.isPending}
                  fullWidth
                >
                  Обновить статус
                </Button>
              </Paper>

              {/* Metadata */}
              <Paper sx={{ p: 3, mt: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Метаданные
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>ID:</strong> {user.id}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Создан:</strong> {new Date(user.createdAt).toLocaleString('ru-RU')}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Обновлен:</strong> {new Date(user.updatedAt).toLocaleString('ru-RU')}
                </Typography>
                {user.lastLoginAt && (
                  <Typography variant="body2" color="text.secondary">
                    <strong>Последний вход:</strong> {new Date(user.lastLoginAt).toLocaleString('ru-RU')}
                  </Typography>
                )}
              </Paper>
            </Grid>
          </Grid>

          {/* Confirmation Dialog */}
          <Dialog
            open={confirmDialog.open}
            onClose={() => setConfirmDialog({ open: false, action: null })}
          >
            <DialogTitle>Подтверждение</DialogTitle>
            <DialogContent>
              <DialogContentText>
                {confirmDialog.action === 'disable' && 
                  'Вы уверены, что хотите отключить этого пользователя? Он не сможет войти в систему.'}
                {confirmDialog.action === 'removeAdmin' && 
                  'Вы уверены, что хотите снять роль администратора с этого пользователя? Убедитесь, что в системе есть другие активные администраторы.'}
              </DialogContentText>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setConfirmDialog({ open: false, action: null })}>
                Отмена
              </Button>
              <Button onClick={handleConfirmAction} color="error" autoFocus>
                Подтвердить
              </Button>
            </DialogActions>
          </Dialog>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

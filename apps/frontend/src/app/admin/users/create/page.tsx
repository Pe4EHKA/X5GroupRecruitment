'use client';

import { useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  FormControl,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Select,
  MenuItem,
  InputLabel,
  Alert,
  Grid2 as Grid,
} from '@mui/material';
import {
  Save as SaveIcon,
  ArrowBack as ArrowBackIcon,
} from '@mui/icons-material';
import { useRouter } from 'next/navigation';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole, UserStatus, CreateUserRequest } from '@/types';
import { adminUserService } from '@/services/adminUserService';

export default function CreateUserPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();

  const [formData, setFormData] = useState<CreateUserRequest>({
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    phone: '',
    department: '',
    comment: '',
    roles: [],
    status: UserStatus.ACTIVE,
    password: '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  // Create user mutation
  const createUserMutation = useMutation({
    mutationFn: (data: CreateUserRequest) => adminUserService.createUser(data),
    onSuccess: (user) => {
      enqueueSnackbar(`Пользователь ${user.username} создан успешно`, { variant: 'success' });
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      router.push('/admin/users');
    },
    onError: (error: any) => {
      const message = error.response?.data?.message || error.message;
      enqueueSnackbar(`Ошибка создания пользователя: ${message}`, { variant: 'error' });
      
      // Handle validation errors
      if (error.response?.data?.errors) {
        setErrors(error.response.data.errors);
      }
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});

    // Basic validation
    const newErrors: Record<string, string> = {};
    if (!formData.username) newErrors.username = 'Обязательное поле';
    if (!formData.email) newErrors.email = 'Обязательное поле';
    if (!formData.firstName) newErrors.firstName = 'Обязательное поле';
    if (!formData.lastName) newErrors.lastName = 'Обязательное поле';
    if (formData.roles.length === 0) newErrors.roles = 'Выберите хотя бы одну роль';
    if (formData.password && formData.password.length < 8) {
      newErrors.password = 'Пароль должен быть не менее 8 символов';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    createUserMutation.mutate(formData);
  };

  const handleRoleToggle = (role: UserRole) => {
    setFormData((prev) => ({
      ...prev,
      roles: prev.roles.includes(role)
        ? prev.roles.filter((r) => r !== role)
        : [...prev, role],
    }));
  };

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
            Создание пользователя
          </Typography>

          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              {/* Basic Information */}
              <Grid size={{ xs: 12, md: 6 }}>
                <Paper sx={{ p: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    Основная информация
                  </Typography>
                  <Box display="flex" flexDirection="column" gap={2}>
                    <TextField
                      label="Имя пользователя *"
                      value={formData.username}
                      onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                      error={!!errors.username}
                      helperText={errors.username}
                      fullWidth
                    />
                    <TextField
                      label="Email *"
                      type="email"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                      error={!!errors.email}
                      helperText={errors.email}
                      fullWidth
                    />
                    <TextField
                      label="Имя *"
                      value={formData.firstName}
                      onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                      error={!!errors.firstName}
                      helperText={errors.firstName}
                      fullWidth
                    />
                    <TextField
                      label="Фамилия *"
                      value={formData.lastName}
                      onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                      error={!!errors.lastName}
                      helperText={errors.lastName}
                      fullWidth
                    />
                    <TextField
                      label="Телефон"
                      value={formData.phone}
                      onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                      fullWidth
                    />
                    <TextField
                      label="Отдел"
                      value={formData.department}
                      onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                      fullWidth
                    />
                    <TextField
                      label="Комментарий"
                      value={formData.comment}
                      onChange={(e) => setFormData({ ...formData, comment: e.target.value })}
                      multiline
                      rows={3}
                      fullWidth
                    />
                  </Box>
                </Paper>
              </Grid>

              {/* Roles, Status, and Password */}
              <Grid size={{ xs: 12, md: 6 }}>
                {/* Roles */}
                <Paper sx={{ p: 3, mb: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    Роли *
                  </Typography>
                  <FormControl component="fieldset" fullWidth error={!!errors.roles}>
                    <FormGroup>
                      <FormControlLabel
                        control={
                          <Checkbox
                            checked={formData.roles.includes(UserRole.ADMIN)}
                            onChange={() => handleRoleToggle(UserRole.ADMIN)}
                          />
                        }
                        label="Администратор"
                      />
                      <FormControlLabel
                        control={
                          <Checkbox
                            checked={formData.roles.includes(UserRole.RECRUITER)}
                            onChange={() => handleRoleToggle(UserRole.RECRUITER)}
                          />
                        }
                        label="Рекрутер"
                      />
                      <FormControlLabel
                        control={
                          <Checkbox
                            checked={formData.roles.includes(UserRole.HM)}
                            onChange={() => handleRoleToggle(UserRole.HM)}
                          />
                        }
                        label="Руководитель"
                      />
                    </FormGroup>
                    {errors.roles && (
                      <Typography variant="caption" color="error">
                        {errors.roles}
                      </Typography>
                    )}
                  </FormControl>
                </Paper>

                {/* Status */}
                <Paper sx={{ p: 3, mb: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    Статус
                  </Typography>
                  <FormControl fullWidth>
                    <InputLabel>Статус</InputLabel>
                    <Select
                      value={formData.status}
                      label="Статус"
                      onChange={(e) => setFormData({ ...formData, status: e.target.value as UserStatus })}
                    >
                      <MenuItem value={UserStatus.ACTIVE}>Активный</MenuItem>
                      <MenuItem value={UserStatus.INVITED}>Приглашен</MenuItem>
                      <MenuItem value={UserStatus.DISABLED}>Отключен</MenuItem>
                    </Select>
                  </FormControl>
                </Paper>

                {/* Password */}
                <Paper sx={{ p: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    Пароль
                  </Typography>
                  <TextField
                    label="Пароль (опционально)"
                    type="password"
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    error={!!errors.password}
                    helperText={errors.password || 'Если не указан, будет сгенерирован автоматически'}
                    fullWidth
                  />
                </Paper>
              </Grid>

              {/* Submit Button */}
              <Grid size={{ xs: 12 }}>
                <Box display="flex" justifyContent="flex-end" gap={2}>
                  <Button
                    variant="outlined"
                    onClick={() => router.push('/admin/users')}
                  >
                    Отмена
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={<SaveIcon />}
                    disabled={createUserMutation.isPending}
                  >
                    Создать пользователя
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </form>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

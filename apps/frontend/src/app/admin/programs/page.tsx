'use client';

import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Typography,
  Paper,
  Grid,
  TextField,
  Button,
  Switch,
  FormControlLabel,
  List,
  ListItemButton,
  ListItemText,
  Divider,
  Stack,
  CircularProgress,
} from '@mui/material';
import { Save, Add } from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole, VacancyRequest } from '@/types';
import { vacancyService } from '@/services/vacancyService';
import { useSnackbar } from 'notistack';

export default function ProgramsPage() {
  const { enqueueSnackbar } = useSnackbar();
  const queryClient = useQueryClient();
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [formData, setFormData] = useState<VacancyRequest>({
    title: '',
    code: '',
    active: true,
  });

  const { data: vacancies, isLoading } = useQuery({
    queryKey: ['vacancies'],
    queryFn: () => vacancyService.list(),
  });

  const selectedVacancy = useMemo(() => {
    return vacancies?.find((v) => v.id === selectedId) || null;
  }, [selectedId, vacancies]);

  useEffect(() => {
    if (selectedVacancy) {
      setFormData({
        title: selectedVacancy.title,
        code: selectedVacancy.code,
        description: selectedVacancy.description,
        department: selectedVacancy.department,
        location: selectedVacancy.location,
        positionsAvailable: selectedVacancy.positionsAvailable,
        startDate: selectedVacancy.startDate?.slice(0, 10),
        endDate: selectedVacancy.endDate?.slice(0, 10),
        active: selectedVacancy.active,
      });
    }
  }, [selectedVacancy]);

  const resetForm = () => {
    setSelectedId(null);
    setFormData({ title: '', code: '', active: true });
  };

  const mutation = useMutation({
    mutationFn: async () => {
      if (selectedId) {
        return vacancyService.update(selectedId, formData);
      }
      return vacancyService.create(formData);
    },
    onSuccess: (data) => {
      enqueueSnackbar('Программа сохранена', { variant: 'success' });
      queryClient.invalidateQueries({ queryKey: ['vacancies'] });
      setSelectedId(data.id);
    },
    onError: (error: any) => {
      enqueueSnackbar(error.response?.data?.message || error.message, { variant: 'error' });
    },
  });

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (!formData.title) {
      enqueueSnackbar('Название обязательно', { variant: 'warning' });
      return;
    }
    mutation.mutate();
  };

  return (
    <ProtectedRoute allowedRoles={[UserRole.ADMIN, UserRole.RECRUITER]}>
      <DashboardLayout>
        <Box>
          <Typography variant="h4" gutterBottom>
            Управление программами стажировок
          </Typography>

          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <Paper sx={{ p: 2 }}>
                <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
                  <Typography variant="h6">Список программ</Typography>
                  <Button startIcon={<Add />} onClick={resetForm}>
                    Новая
                  </Button>
                </Stack>
                {isLoading ? (
                  <Box display="flex" justifyContent="center" py={4}>
                    <CircularProgress />
                  </Box>
                ) : (
                  <List>
                    {vacancies?.map((vacancy) => (
                      <ListItemButton
                        key={vacancy.id}
                        selected={selectedId === vacancy.id}
                        onClick={() => setSelectedId(vacancy.id)}
                      >
                        <ListItemText
                          primary={vacancy.title}
                          secondary={`${vacancy.code} • ${vacancy.active ? 'Активна' : 'Выключена'}`}
                        />
                      </ListItemButton>
                    ))}
                  </List>
                )}
              </Paper>
            </Grid>

            <Grid item xs={12} md={8}>
              <Paper sx={{ p: 3 }} component="form" onSubmit={handleSubmit}>
                <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
                  <Typography variant="h6">
                    {selectedId ? 'Редактировать программу' : 'Создать программу'}
                  </Typography>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={formData.active ?? true}
                        onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                      />
                    }
                    label={formData.active ? 'Активна' : 'Выключена'}
                  />
                </Stack>

                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <TextField
                      label="Название программы"
                      value={formData.title}
                      onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                      required
                      fullWidth
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      label="Код"
                      value={formData.code || ''}
                      onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                      helperText="Если не указать, код сгенерируется автоматически"
                      fullWidth
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      label="Описание"
                      value={formData.description || ''}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                      fullWidth
                      multiline
                      rows={3}
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      label="Подразделение"
                      value={formData.department || ''}
                      onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                      fullWidth
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      label="Локация"
                      value={formData.location || ''}
                      onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                      fullWidth
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      label="Количество мест"
                      type="number"
                      value={formData.positionsAvailable ?? ''}
                      onChange={(e) =>
                        setFormData({ ...formData, positionsAvailable: e.target.value ? Number(e.target.value) : undefined })
                      }
                      fullWidth
                    />
                  </Grid>
                  <Grid item xs={12} md={3}>
                    <TextField
                      label="Дата начала"
                      type="date"
                      value={formData.startDate || ''}
                      onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                      fullWidth
                      InputLabelProps={{ shrink: true }}
                    />
                  </Grid>
                  <Grid item xs={12} md={3}>
                    <TextField
                      label="Дата окончания"
                      type="date"
                      value={formData.endDate || ''}
                      onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                      fullWidth
                      InputLabelProps={{ shrink: true }}
                    />
                  </Grid>
                </Grid>

                <Divider sx={{ my: 2 }} />
                <Stack direction="row" spacing={2}>
                  <Button
                    variant="contained"
                    startIcon={<Save />}
                    type="submit"
                    disabled={mutation.isPending}
                  >
                    Сохранить
                  </Button>
                  <Button variant="outlined" onClick={resetForm} disabled={mutation.isPending}>
                    Очистить
                  </Button>
                </Stack>
              </Paper>
            </Grid>
          </Grid>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

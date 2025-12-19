'use client';

import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
  Box,
  Paper,
  Typography,
  CircularProgress,
  Grid,
  Divider,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemText,
  Chip,
  Alert,
  Stack,
} from '@mui/material';
import { ArrowBack } from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import StatusBadge from '@/components/StatusBadge';
import { UserRole, ApplicationStatus, TranscriptionStatus } from '@/types';
import { useApplication, useChangeStatus } from '@/hooks/useRecruiter';
import { getCandidateFullName, getCandidateEmail, getCandidatePhone, getCandidateUniversity, getCandidateCourse } from '@/lib/utils';
import { format } from 'date-fns';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { API_BASE_URL } from '@/lib/api';

const changeStatusSchema = z.object({
  newStatus: z.nativeEnum(ApplicationStatus),
  comment: z.string().optional(),
});

type ChangeStatusForm = z.infer<typeof changeStatusSchema>;

export default function ApplicationDetailPage() {
  const params = useParams();
  const router = useRouter();
  const applicationId = parseInt(params.id as string);

  const { data: application, isLoading } = useApplication(applicationId);
  const changeStatusMutation = useChangeStatus();

  const [statusDialogOpen, setStatusDialogOpen] = useState(false);

  const statusForm = useForm<ChangeStatusForm>({
    resolver: zodResolver(changeStatusSchema),
    defaultValues: {
      newStatus: ApplicationStatus.SCREENING,
      comment: '',
    },
  });

  const handleChangeStatus = (data: ChangeStatusForm) => {
    changeStatusMutation.mutate(
      { id: applicationId, data },
      {
        onSuccess: () => {
          setStatusDialogOpen(false);
          statusForm.reset();
        },
      }
    );
  };

  const getTranscriptionLabel = (status?: TranscriptionStatus) => {
    switch (status) {
      case TranscriptionStatus.DONE:
        return { label: 'Готово', color: 'success' as const };
      case TranscriptionStatus.PROCESSING:
        return { label: 'Обработка', color: 'info' as const };
      case TranscriptionStatus.FAILED:
        return { label: 'Ошибка', color: 'error' as const };
      default:
        return { label: 'Ожидание', color: 'warning' as const };
    }
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

  if (!application) {
    return (
      <ProtectedRoute allowedRoles={[UserRole.RECRUITER]}>
        <DashboardLayout>
          <Typography variant="h6">Заявка не найдена</Typography>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute allowedRoles={[UserRole.RECRUITER]}>
      <DashboardLayout>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
          <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" gap={1.5}>
            <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
              <Button startIcon={<ArrowBack />} onClick={() => router.back()} variant="outlined" size="small">
                Назад
              </Button>
              <Chip label={`ID ${application.id}`} color="secondary" sx={{ fontWeight: 700 }} />
            </Stack>
            <Stack direction="row" spacing={1} flexWrap="wrap">
              <Button variant="outlined" onClick={() => setStatusDialogOpen(true)}>
                Изменить статус
              </Button>
            </Stack>
          </Stack>

          <Paper data-variant="elevated" sx={{ p: { xs: 2.5, md: 3 }, borderRadius: 3 }}>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2.5} alignItems={{ md: 'center' }}>
              <Stack spacing={0.5} flex={1} minWidth={0}>
                <Typography variant="overline" color="text.secondary">
                  Заявка
                </Typography>
                <Typography variant="h4" sx={{ lineHeight: 1.2, wordBreak: 'break-word' }}>
                  {getCandidateFullName(application.candidate)}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ wordBreak: 'break-word' }}>
                  {application.vacancyTitle}
                </Typography>
              </Stack>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} flexWrap="wrap">
                <Chip label={application.status} color="primary" variant="outlined" />
                <Chip label={application.recruiterName || 'Рекрутер не назначен'} variant="outlined" />
                <Chip label={application.hmName || 'HM не назначен'} variant="outlined" />
              </Stack>
            </Stack>
          </Paper>

          <Grid container spacing={3}>
            {/* Candidate Info */}
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Информация о кандидате
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Grid container spacing={2}>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        ФИО
                      </Typography>
                      <Typography variant="body1" sx={{ wordBreak: 'break-word' }}>
                        {getCandidateFullName(application.candidate)}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Email
                      </Typography>
                      <Typography variant="body1" sx={{ wordBreak: 'break-word' }}>
                        {getCandidateEmail(application.candidate)}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Телефон
                      </Typography>
                      <Typography variant="body1" sx={{ wordBreak: 'break-word' }}>
                        {getCandidatePhone(application.candidate)}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Университет
                      </Typography>
                      <Typography variant="body1" sx={{ wordBreak: 'break-word' }}>
                        {getCandidateUniversity(application.candidate)}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Курс
                      </Typography>
                      <Typography variant="body1" sx={{ wordBreak: 'break-word' }}>
                        {getCandidateCourse(application.candidate)}
                      </Typography>
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            </Grid>

            {/* Application Info */}
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Информация о заявке
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Grid container spacing={2}>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Вакансия
                      </Typography>
                      <Typography variant="body1" sx={{ wordBreak: 'break-word' }}>
                        {application.vacancyTitle}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Статус
                      </Typography>
                      <StatusBadge status={application.status} />
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Рекрутер
                      </Typography>
                      <Typography variant="body1">{application.recruiterName || '-'}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        HM
                      </Typography>
                      <Typography variant="body1">{application.hmName || '-'}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Дата создания
                      </Typography>
                      <Typography variant="body1">
                        {format(new Date(application.createdAt), 'dd.MM.yyyy HH:mm')}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Последнее обновление
                      </Typography>
                      <Typography variant="body1">
                        {format(new Date(application.updatedAt), 'dd.MM.yyyy HH:mm')}
                      </Typography>
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Видео-визитка кандидата
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  {application.videoPresentation ? (
                    <Grid container spacing={2}>
                      <Grid item xs={12} md={6}>
                        <video
                          controls
                          style={{ width: '100%', borderRadius: 8 }}
                          src={`${API_BASE_URL}${application.videoPresentation.streamUrl}`}
                        />
                      </Grid>
                      <Grid item xs={12} md={6}>
                        <Stack spacing={1}>
                          <Stack direction="row" alignItems="center" spacing={1}>
                            <Typography variant="body2" color="text.secondary">
                              Статус транскрипции:
                            </Typography>
                            <Chip size="small" {...getTranscriptionLabel(application.videoPresentation.transcription?.status)} />
                          </Stack>
                          {application.videoPresentation.transcription?.text && (
                            <Paper variant="outlined" sx={{ p: 2 }}>
                              <Typography variant="subtitle2" gutterBottom>
                                Транскрипция
                              </Typography>
                              <Typography variant="body2">
                                {application.videoPresentation.transcription.text}
                              </Typography>
                            </Paper>
                          )}
                          {application.videoPresentation.transcription?.errorMessage && (
                            <Alert severity="error">{application.videoPresentation.transcription.errorMessage}</Alert>
                          )}
                          {(!application.videoPresentation.transcription ||
                            application.videoPresentation.transcription.status !== TranscriptionStatus.DONE) && (
                            <Alert severity="info">
                              Транскрипция формируется автоматически после загрузки видео. Обработка может занять несколько минут.
                            </Alert>
                          )}
                        </Stack>
                      </Grid>
                    </Grid>
                  ) : (
                    <Typography variant="body2" color="text.secondary">
                      Кандидат пока не загрузил видео-визитку.
                    </Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>

            {/* Status History */}
            <Grid item xs={12}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    История статусов
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <List>
                    {application.statusHistory?.map((history) => (
                      <ListItem key={history.id}>
                        <ListItemText
                          primary={
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                              <StatusBadge status={history.status} />
                              <Typography variant="body2" sx={{ wordBreak: 'break-word' }}>
                                {history.comment}
                              </Typography>
                            </Box>
                          }
                          secondary={`${history.changedBy} • ${format(
                            new Date(history.changedAt),
                            'dd.MM.yyyy HH:mm'
                          )}`}
                        />
                      </ListItem>
                    ))}
                    {(!application.statusHistory || application.statusHistory.length === 0) && (
                      <ListItem>
                        <ListItemText primary="Нет истории" />
                      </ListItem>
                    )}
                  </List>
                </CardContent>
              </Card>
            </Grid>

            {/* Feedbacks */}
            {application.feedbacks && application.feedbacks.length > 0 && (
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Фидбеки
                    </Typography>
                    <Divider sx={{ mb: 2 }} />
                    {application.feedbacks.map((feedback) => (
                      <Box key={feedback.id} sx={{ mb: 2 }}>
                        <Typography variant="subtitle2">
                          {feedback.hmName} •{' '}
                          <Chip
                            label={feedback.decision}
                            size="small"
                            color={feedback.decision === 'APPROVE' ? 'success' : 'error'}
                          />
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {feedback.overallAssessment}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {format(new Date(feedback.createdAt), 'dd.MM.yyyy HH:mm')}
                        </Typography>
                      </Box>
                    ))}
                  </CardContent>
                </Card>
              </Grid>
            )}
          </Grid>

          {/* Change Status Dialog */}
          <Dialog open={statusDialogOpen} onClose={() => setStatusDialogOpen(false)} maxWidth="sm" fullWidth>
            <form onSubmit={statusForm.handleSubmit(handleChangeStatus)}>
              <DialogTitle>Изменить статус</DialogTitle>
              <DialogContent>
                <FormControl fullWidth sx={{ mt: 2 }}>
                  <InputLabel>Статус</InputLabel>
                  <Select
                    {...statusForm.register('newStatus')}
                    defaultValue={ApplicationStatus.SCREENING}
                    label="Статус"
                  >
                    <MenuItem value={ApplicationStatus.SCREENING}>Скрининг</MenuItem>
                    <MenuItem value={ApplicationStatus.INTERVIEW_SCHEDULED}>Интервью назначено</MenuItem>
                    <MenuItem value={ApplicationStatus.APPROVED}>Одобрено</MenuItem>
                    <MenuItem value={ApplicationStatus.REJECTED}>Отклонено</MenuItem>
                  </Select>
                </FormControl>
                <TextField
                  {...statusForm.register('comment')}
                  fullWidth
                  label="Комментарий"
                  multiline
                  rows={3}
                  sx={{ mt: 2 }}
                />
              </DialogContent>
              <DialogActions>
                <Button onClick={() => setStatusDialogOpen(false)}>Отмена</Button>
                <Button type="submit" variant="contained" disabled={changeStatusMutation.isPending}>
                  Сохранить
                </Button>
              </DialogActions>
            </form>
          </Dialog>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

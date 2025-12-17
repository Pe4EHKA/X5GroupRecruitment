'use client';

import { useState } from 'react';

import {
  Box,
  Typography,
  Paper,
  CircularProgress,
  Chip,
  Alert,
  Grid,
  Card,
  CardContent,
  Divider,
  Stack,
  Button,
} from '@mui/material';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineOppositeContent,
} from '@mui/lab';
import {
  CheckCircle as CheckCircleIcon,
  Schedule as ScheduleIcon,
  Cancel as CancelIcon,
  Info as InfoIcon,
} from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import VideoRecorder from '@/components/VideoRecorder';
import { UserRole, ApplicationStatus, TranscriptionStatus, ApplicationDetailDto } from '@/types';
import { useMyApplications, useMyProfile, useUploadStagerVideo } from '@/hooks/useStager';
import { API_BASE_URL } from '@/lib/api';

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
  [ApplicationStatus.NEW]: 'Новая заявка',
  [ApplicationStatus.SCREENING]: 'На рассмотрении',
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

const getStatusIcon = (status: ApplicationStatus) => {
  switch (status) {
    case ApplicationStatus.APPROVED:
    case ApplicationStatus.OFFER_ACCEPTED:
      return <CheckCircleIcon />;
    case ApplicationStatus.REJECTED:
    case ApplicationStatus.OFFER_DECLINED:
    case ApplicationStatus.WITHDRAWN:
      return <CancelIcon />;
    default:
      return <ScheduleIcon />;
  }
};

const getTranscriptionLabel = (status?: TranscriptionStatus) => {
  switch (status) {
    case TranscriptionStatus.DONE:
      return { label: 'Готово', color: 'success' as const };
    case TranscriptionStatus.PROCESSING:
      return { label: 'Обработка', color: 'info' as const };
    case TranscriptionStatus.FAILED:
      return { label: 'Ошибка', color: 'error' as const };
    case TranscriptionStatus.PENDING:
      return { label: 'В очереди', color: 'warning' as const };
    default:
      return { label: 'Неизвестно', color: 'default' as const };
  }
};

function VideoSection({ application }: { application: ApplicationDetailDto }) {
  const [recordedFile, setRecordedFile] = useState<File | null>(null);
  const videoUpload = useUploadStagerVideo(application.id);

  const handleUpload = () => {
    if (!recordedFile) return;

    videoUpload.mutate(recordedFile, {
      onSuccess: () => setRecordedFile(null),
    });
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Видео-визитка
        </Typography>
        <Divider sx={{ mb: 2 }} />
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            {application.videoPresentation ? (
              <Stack spacing={2}>
                <video
                  controls
                  style={{ width: '100%', borderRadius: 8 }}
                  src={`${API_BASE_URL}${application.videoPresentation.streamUrl}`}
                />
                <Stack direction="row" spacing={1} alignItems="center">
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
                    <Typography variant="body2">{application.videoPresentation.transcription.text}</Typography>
                  </Paper>
                )}
                {application.videoPresentation.transcription?.errorMessage && (
                  <Alert severity="error">{application.videoPresentation.transcription.errorMessage}</Alert>
                )}
                {(!application.videoPresentation.transcription ||
                  application.videoPresentation.transcription.status !== TranscriptionStatus.DONE) && (
                  <Alert severity="info">Транскрипция появится после обработки. Обычно это занимает пару минут.</Alert>
                )}
              </Stack>
            ) : (
              <Alert severity="info">Сначала запишите и загрузите видео-визитку.</Alert>
            )}
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Запишите короткое видео до 5 минут с рассказом о себе. После загрузки рекрутер увидит ролик и его текстовую
              расшифровку.
            </Typography>
            <VideoRecorder onRecordingComplete={(file) => setRecordedFile(file)} maxDurationSeconds={300} />
            <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
              <Button
                variant="contained"
                onClick={handleUpload}
                disabled={!recordedFile || videoUpload.isPending}
              >
                {videoUpload.isPending ? 'Загрузка...' : 'Загрузить'}
              </Button>
              {recordedFile && (
                <Typography variant="body2" color="text.secondary" sx={{ alignSelf: 'center' }}>
                  Файл: {recordedFile.name}
                </Typography>
              )}
            </Stack>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
}

export default function StagerDashboard() {
  const { data: applications, isLoading } = useMyApplications();
  const { data: profile, isLoading: isProfileLoading } = useMyProfile();

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ru-RU', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <ProtectedRoute allowedRoles={[UserRole.STAGER, UserRole.CANDIDATE, UserRole.ADMIN]}>
      <DashboardLayout>
        <Box>
          <Typography variant="h4" gutterBottom>
            Моя заявка
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Здесь вы можете отслеживать статус вашей заявки на стажировку
          </Typography>

          {isLoading || isProfileLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : !applications || applications.length === 0 ? (
            <Alert severity="info" icon={<InfoIcon />}>
              У вас пока нет заявок. Свяжитесь с HR для подачи заявки.
            </Alert>
          ) : (
            <Box>
              {profile && (
                <Paper sx={{ p: 3, mb: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    Мои данные
                  </Typography>
                  <Grid container spacing={2}>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        ФИО
                      </Typography>
                      <Typography variant="body1">{`${profile.firstName} ${profile.lastName}`}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Email
                      </Typography>
                      <Typography variant="body1">{profile.email}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Телефон
                      </Typography>
                      <Typography variant="body1">{profile.phone || '—'}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Город
                      </Typography>
                      <Typography variant="body1">{profile.city || '—'}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Университет
                      </Typography>
                      <Typography variant="body1">{profile.university || '—'}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Специальность
                      </Typography>
                      <Typography variant="body1">{profile.speciality || '—'}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Курс
                      </Typography>
                      <Typography variant="body1">{profile.course || '—'}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Telegram
                      </Typography>
                      <Typography variant="body1">{profile.telegram || '—'}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Год рождения
                      </Typography>
                      <Typography variant="body1">{profile.birthYear || '—'}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Гражданство
                      </Typography>
                      <Typography variant="body1">{profile.citizenship || '—'}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        График
                      </Typography>
                      <Typography variant="body1">{profile.schedule || '—'}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Источник
                      </Typography>
                      <Typography variant="body1">{profile.source || '—'}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Языки
                      </Typography>
                      <Typography variant="body1">{profile.languages?.join(', ') || '—'}</Typography>
                    </Grid>
                    <Grid item xs={12}>
                      <Typography variant="body2" color="text.secondary">
                        Дополнительная информация
                      </Typography>
                      <Typography variant="body1">{profile.additionalInfo || '—'}</Typography>
                    </Grid>
                  </Grid>
                </Paper>
              )}
              {applications.map((application) => (
                <Paper key={application.id} sx={{ p: 3, mb: 3 }}>
                  <Grid container spacing={3}>
                    <Grid item xs={12} md={6}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="h6" gutterBottom>
                            Информация о заявке
                          </Typography>
                          <Box sx={{ mt: 2 }}>
                            <Typography variant="body2" color="text.secondary">
                              Вакансия
                            </Typography>
                            <Typography variant="body1" gutterBottom>
                              {application.vacancyTitle}
                            </Typography>

                            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                              Текущий статус
                            </Typography>
                            <Box sx={{ mt: 1 }}>
                              <Chip
                                icon={getStatusIcon(application.status)}
                                label={STATUS_LABELS[application.status]}
                                sx={{
                                  backgroundColor: STATUS_COLORS[application.status],
                                  color: 'white',
                                }}
                              />
                            </Box>

                            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                              Дата подачи
                            </Typography>
                            <Typography variant="body1">
                              {formatDate(application.createdAt)}
                            </Typography>

                            {application.currentComment && (
                              <>
                                <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                                  Комментарий
                                </Typography>
                                <Typography variant="body1">
                                  {application.currentComment}
                                </Typography>
                              </>
                            )}
                          </Box>
                        </CardContent>
                      </Card>
                    </Grid>

                    <Grid item xs={12} md={6}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="h6" gutterBottom>
                            История статусов
                          </Typography>
                          
                          {application.statusHistory && application.statusHistory.length > 0 ? (
                            <Timeline sx={{ mt: 2, p: 0 }}>
                              {application.statusHistory.map((history, index) => (
                                <TimelineItem key={history.id}>
                                  <TimelineOppositeContent color="text.secondary" sx={{ flex: 0.3 }}>
                                    <Typography variant="caption">
                                      {formatDate(history.changedAt)}
                                    </Typography>
                                  </TimelineOppositeContent>
                                  <TimelineSeparator>
                                    <TimelineDot
                                      sx={{
                                        backgroundColor: STATUS_COLORS[history.status],
                                      }}
                                    >
                                      {getStatusIcon(history.status)}
                                    </TimelineDot>
                                    {index < (application.statusHistory?.length ?? 0) - 1 && <TimelineConnector />}
                                  </TimelineSeparator>
                                  <TimelineContent>
                                    <Typography variant="body2" fontWeight="medium">
                                      {STATUS_LABELS[history.status]}
                                    </Typography>
                                    {history.comment && (
                                      <Typography variant="caption" color="text.secondary">
                                        {history.comment}
                                      </Typography>
                                    )}
                                    <Typography variant="caption" color="text.secondary" display="block">
                                      {history.changedBy}
                                    </Typography>
                                  </TimelineContent>
                                </TimelineItem>
                              ))}
                            </Timeline>
                          ) : (
                            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                              История пока пуста
                            </Typography>
                          )}
                        </CardContent>
                      </Card>
                    </Grid>

                    <Grid item xs={12}>
                      <VideoSection application={application} />
                    </Grid>
                  </Grid>
                </Paper>
              ))}
            </Box>
          )}
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

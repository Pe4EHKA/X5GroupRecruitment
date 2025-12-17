'use client';

import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Grid,
  Button,
  Divider,
  List,
  ListItem,
  ListItemText,
  Tabs,
  Tab,
  Paper,
  Chip,
  Alert,
  Stack,
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
  CheckCircle,
  HourglassEmpty,
  Cancel,
  Info,
} from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import StatusBadge from '@/components/StatusBadge';
import VideoRecorder from '@/components/VideoRecorder';
import { UserRole, ApplicationStatus, TranscriptionStatus } from '@/types';
import { useCandidateApplications, useUploadVideoPresentation } from '@/hooks/useCandidate';
import { getCandidateFullName, getCandidateEmail, getCandidatePhone, getCandidateUniversity, getCandidateCourse } from '@/lib/utils';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';
import { API_BASE_URL } from '@/lib/api';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`tabpanel-${index}`}
      aria-labelledby={`tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
}

function getStatusIcon(status: ApplicationStatus) {
  switch (status) {
    case ApplicationStatus.APPROVED:
      return <CheckCircle color="success" />;
    case ApplicationStatus.REJECTED:
      return <Cancel color="error" />;
    case ApplicationStatus.NEW:
    case ApplicationStatus.SCREENING:
    case ApplicationStatus.INTERVIEW_SCHEDULED:
      return <HourglassEmpty color="warning" />;
    default:
      return <Info color="info" />;
  }
}

function getStatusColor(status: ApplicationStatus): 'success' | 'error' | 'warning' | 'info' {
  switch (status) {
    case ApplicationStatus.APPROVED:
      return 'success';
    case ApplicationStatus.REJECTED:
      return 'error';
    case ApplicationStatus.NEW:
    case ApplicationStatus.SCREENING:
    case ApplicationStatus.INTERVIEW_SCHEDULED:
      return 'warning';
    default:
      return 'info';
  }
}

export default function CandidateProfilePage() {
  const [tabValue, setTabValue] = useState(0);
  const { data: applications, isLoading } = useCandidateApplications();
  const [recordedFile, setRecordedFile] = useState<File | null>(null);
  const applicationId = applications && applications.length > 0 ? applications[0].id : 0;
  const videoUpload = useUploadVideoPresentation(applicationId);

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleUploadVideo = () => {
    if (!recordedFile || !applicationId) return;

    videoUpload.mutate(recordedFile, {
      onSuccess: () => setRecordedFile(null),
    });
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
      <ProtectedRoute allowedRoles={[UserRole.CANDIDATE]}>
        <DashboardLayout>
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
            <CircularProgress />
          </Box>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  const application = applications && applications.length > 0 ? applications[0] : null;

  if (!application) {
    return (
      <ProtectedRoute allowedRoles={[UserRole.CANDIDATE]}>
        <DashboardLayout>
          <Box>
            <Typography variant="h4" gutterBottom>
              Моя заявка
            </Typography>
            <Paper sx={{ p: 3, mt: 3 }}>
              <Typography variant="body1" color="text.secondary">
                У вас пока нет активных заявок.
              </Typography>
            </Paper>
          </Box>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute allowedRoles={[UserRole.CANDIDATE]}>
      <DashboardLayout>
        <Box>
          <Typography variant="h4" gutterBottom>
            Моя заявка
          </Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            {application.vacancyTitle}
          </Typography>

          {/* Status Summary Card */}
          <Card sx={{ mb: 3, mt: 2 }}>
            <CardContent>
              <Grid container spacing={2} alignItems="center">
                <Grid item>
                  {getStatusIcon(application.status)}
                </Grid>
                <Grid item xs>
                  <Typography variant="h6">Текущий статус</Typography>
                  <StatusBadge status={application.status} />
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    {application.currentComment || 'Ожидайте обновлений'}
                  </Typography>
                </Grid>
                <Grid item>
                  <Typography variant="caption" color="text.secondary">
                    Обновлено: {format(new Date(application.updatedAt), 'dd MMMM yyyy, HH:mm', { locale: ru })}
                  </Typography>
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Tabs */}
          <Paper sx={{ mb: 3 }}>
            <Tabs value={tabValue} onChange={handleTabChange} aria-label="profile tabs">
              <Tab label="Статус" id="tab-0" aria-controls="tabpanel-0" />
              <Tab label="История" id="tab-1" aria-controls="tabpanel-1" />
              <Tab label="Данные" id="tab-2" aria-controls="tabpanel-2" />
              <Tab label="Видео-визитка" id="tab-3" aria-controls="tabpanel-3" />
            </Tabs>
          </Paper>

          {/* Status Tab */}
          <TabPanel value={tabValue} index={0}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Информация о заявке
                    </Typography>
                    <Divider sx={{ mb: 2 }} />
                    <List dense>
                      <ListItem>
                        <ListItemText
                          primary="Вакансия"
                          secondary={application.vacancyTitle}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Дата подачи"
                          secondary={format(new Date(application.createdAt), 'dd MMMM yyyy, HH:mm', { locale: ru })}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Статус"
                          secondary={<StatusBadge status={application.status} />}
                        />
                      </ListItem>
                    </List>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Следующие шаги
                    </Typography>
                    <Divider sx={{ mb: 2 }} />
                    <Typography variant="body2" color="text.secondary">
                      {application.currentComment || 'Мы свяжемся с вами по электронной почте, как только будут обновления.'}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </TabPanel>

          {/* History Tab */}
          <TabPanel value={tabValue} index={1}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  История статусов
                </Typography>
                <Divider sx={{ mb: 3 }} />
                {application.statusHistory && application.statusHistory.length > 0 ? (
                  <Timeline position="right">
                    {application.statusHistory.map((history, index) => (
                      <TimelineItem key={history.id}>
                        <TimelineOppositeContent color="text.secondary" sx={{ flex: 0.2 }}>
                          {format(new Date(history.changedAt), 'dd MMM yyyy', { locale: ru })}
                          <br />
                          {format(new Date(history.changedAt), 'HH:mm', { locale: ru })}
                        </TimelineOppositeContent>
                        <TimelineSeparator>
                          <TimelineDot color={getStatusColor(history.status)}>
                            {getStatusIcon(history.status)}
                          </TimelineDot>
                          {index < application.statusHistory!.length - 1 && <TimelineConnector />}
                        </TimelineSeparator>
                        <TimelineContent>
                          <StatusBadge status={history.status} />
                          {history.comment && (
                            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                              {history.comment}
                            </Typography>
                          )}
                          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                            {history.changedBy}
                          </Typography>
                        </TimelineContent>
                      </TimelineItem>
                    ))}
                  </Timeline>
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    Нет истории изменений
                  </Typography>
                )}
              </CardContent>
            </Card>
          </TabPanel>

          {/* Data Tab */}
          <TabPanel value={tabValue} index={2}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Личная информация
                    </Typography>
                    <Divider sx={{ mb: 2 }} />
                    <List dense>
                      <ListItem>
                        <ListItemText
                          primary="ФИО"
                          secondary={getCandidateFullName(application.candidate)}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Email"
                          secondary={getCandidateEmail(application.candidate)}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Телефон"
                          secondary={getCandidatePhone(application.candidate)}
                        />
                      </ListItem>
                    </List>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Образование
                    </Typography>
                    <Divider sx={{ mb: 2 }} />
                    <List dense>
                      <ListItem>
                        <ListItemText
                          primary="Университет"
                          secondary={getCandidateUniversity(application.candidate)}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Курс"
                          secondary={getCandidateCourse(application.candidate)}
                        />
                      </ListItem>
                    </List>
                  </CardContent>
                </Card>
              </Grid>
              {application.preferences && application.preferences.length > 0 && (
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Предпочтения
                      </Typography>
                      <Divider sx={{ mb: 2 }} />
                      <List>
                        {application.preferences.map((pref, index) => (
                          <ListItem key={index}>
                            <ListItemText
                              primary={`${pref.preferenceOrder}. ${pref.preferredPosition}`}
                              secondary={pref.preferredLocation}
                            />
                          </ListItem>
                        ))}
                      </List>
                    </CardContent>
                  </Card>
                </Grid>
              )}
            </Grid>
          </TabPanel>

          <TabPanel value={tabValue} index={3}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Моя видео-визитка
                    </Typography>
                    <Divider sx={{ mb: 2 }} />
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
                          <Chip
                            size="small"
                            {...getTranscriptionLabel(application.videoPresentation.transcription?.status)}
                          />
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
                            Транскрипция появится после обработки. Обычно это занимает пару минут.
                          </Alert>
                        )}
                      </Stack>
                    ) : (
                      <Alert severity="info">
                        Вы еще не добавили видео-визитку. Запишите её и загрузите через форму справа.
                      </Alert>
                    )}
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Записать и загрузить
                    </Typography>
                    <Divider sx={{ mb: 2 }} />
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                      Запишите короткое видео до 5 минут с рассказом о себе. Видео сохранится в системе и будет доступно
                      рекрутеру вместе с текстовой расшифровкой.
                    </Typography>
                    <VideoRecorder
                      onRecordingComplete={(file) => setRecordedFile(file)}
                      disabled={videoUpload.isPending || !applicationId}
                      maxDurationSeconds={300}
                    />
                    <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
                      <Button
                        variant="contained"
                        onClick={handleUploadVideo}
                        disabled={!recordedFile || videoUpload.isPending || !applicationId}
                      >
                        {videoUpload.isPending ? 'Загрузка...' : 'Загрузить'}
                      </Button>
                      {recordedFile && (
                        <Typography variant="body2" color="text.secondary" sx={{ alignSelf: 'center' }}>
                          Файл: {recordedFile.name}
                        </Typography>
                      )}
                    </Stack>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </TabPanel>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

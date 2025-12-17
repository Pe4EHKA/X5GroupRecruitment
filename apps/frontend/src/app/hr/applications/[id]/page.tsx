'use client';

import { useParams, useRouter } from 'next/navigation';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Divider,
  Grid,
  List,
  ListItem,
  ListItemText,
  Stack,
  Typography,
} from '@mui/material';
import { ArrowBack } from '@mui/icons-material';
import { format } from 'date-fns';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import StatusBadge from '@/components/StatusBadge';
import { useApplication } from '@/hooks/useHr';
import { UserRole } from '@/types';

const formatDateTime = (value?: string) => {
  if (!value) return '—';
  return format(new Date(value), 'dd.MM.yyyy HH:mm');
};

const InfoItem = ({ label, value }: { label: string; value?: string | number | null }) => (
  <Grid item xs={12} sm={6} md={4}>
    <Typography variant="body2" color="text.secondary">
      {label}
    </Typography>
    <Typography variant="body1">{value || '—'}</Typography>
  </Grid>
);

export default function HrApplicationDetailPage() {
  const params = useParams();
  const router = useRouter();
  const applicationId = Number(params.id);

  const { data: application, isLoading } = useApplication(applicationId);

  if (isLoading) {
    return (
      <ProtectedRoute allowedRoles={[UserRole.RECRUITER, UserRole.ADMIN]}>
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
      <ProtectedRoute allowedRoles={[UserRole.RECRUITER, UserRole.ADMIN]}>
        <DashboardLayout>
          <Typography>Заявка не найдена</Typography>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  const languages = application.candidate?.languages?.join(', ');

  return (
    <ProtectedRoute allowedRoles={[UserRole.RECRUITER, UserRole.ADMIN]}>
      <DashboardLayout>
        <Box>
          <Button startIcon={<ArrowBack />} onClick={() => router.back()} sx={{ mb: 2 }}>
            Назад
          </Button>

          <Typography variant="h4" gutterBottom>
            Заявка #{application.id}
          </Typography>

          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Информация о кандидате
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Grid container spacing={2}>
                    <InfoItem label="ФИО" value={application.candidate?.fullName || application.candidateName} />
                    <InfoItem label="Email" value={application.candidate?.email || application.candidateEmail} />
                    <InfoItem label="Телефон" value={application.candidate?.phone} />
                    <InfoItem label="Telegram" value={application.candidate?.telegram} />
                    <InfoItem label="Город" value={application.candidate?.city || application.candidate?.otherCity} />
                    <InfoItem label="Гражданство" value={application.candidate?.citizenship} />
                    <InfoItem label="Университет" value={application.candidate?.university || application.candidate?.otherUniversity} />
                    <InfoItem label="Специальность" value={application.candidate?.speciality || application.candidate?.otherSpeciality} />
                    <InfoItem label="Курс" value={application.candidate?.course} />
                    <InfoItem label="Год рождения" value={application.candidate?.birthYear} />
                    <InfoItem label="Знание языков" value={languages} />
                    <InfoItem label="Доп. информация" value={application.candidate?.additionalInfo} />
                  </Grid>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Информация о заявке
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Grid container spacing={2}>
                    <InfoItem label="Вакансия" value={application.vacancyTitle} />
                    <Grid item xs={12} sm={6} md={4}>
                      <Typography variant="body2" color="text.secondary">
                        Статус
                      </Typography>
                      <StatusBadge status={application.status} />
                    </Grid>
                    <InfoItem label="Рекрутер" value={application.recruiterName} />
                    <InfoItem label="Дата создания" value={formatDateTime(application.createdAt)} />
                    <InfoItem label="Обновлено" value={formatDateTime(application.updatedAt)} />
                    <InfoItem label="Комментарий" value={application.currentComment} />
                    {application.coverLetter && <InfoItem label="Сопроводительное" value={application.coverLetter} />}
                  </Grid>
                </CardContent>
              </Card>
            </Grid>

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
                            <Stack direction="row" spacing={1} alignItems="center">
                              <Chip label={history.status} size="small" />
                              <Typography variant="body2">{history.comment || 'Без комментария'}</Typography>
                            </Stack>
                          }
                          secondary={`${history.changedBy} • ${formatDateTime(history.changedAt)}`}
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
          </Grid>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

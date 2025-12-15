'use client';

import { useParams } from 'next/navigation';
import {
  Box,
  Container,
  Paper,
  Typography,
  CircularProgress,
  Card,
  CardContent,
  Divider,
  Stepper,
  Step,
  StepLabel,
  Chip,
} from '@mui/material';
import StatusBadge from '@/components/StatusBadge';
import { useCandidateStatus } from '@/hooks/useCandidate';
import { format } from 'date-fns';
import { ApplicationStatus } from '@/types';

const statusSteps: ApplicationStatus[] = [
  ApplicationStatus.NEW,
  ApplicationStatus.SCREENING,
  ApplicationStatus.PENDING_HM_REVIEW,
  ApplicationStatus.INTERVIEW_SCHEDULED,
  ApplicationStatus.APPROVED,
];

function getActiveStep(status: ApplicationStatus): number {
  const index = statusSteps.indexOf(status);
  return index >= 0 ? index : 0;
}

export default function CandidateStatusPage() {
  const params = useParams();
  const token = params.token as string;

  const { data, isLoading } = useCandidateStatus(token);

  if (isLoading) {
    return (
      <Container maxWidth="md">
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (!data || !data.applications || data.applications.length === 0) {
    return (
      <Container maxWidth="md">
        <Box sx={{ minHeight: '100vh', py: 8 }}>
          <Typography variant="h4" align="center">
            Заявки не найдены
          </Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="md">
      <Box sx={{ minHeight: '100vh', py: 8 }}>
        <Typography variant="h4" gutterBottom align="center">
          X5 Tech - Статус вашей заявки
        </Typography>
        <Typography variant="body1" color="text.secondary" align="center" sx={{ mb: 4 }}>
          Отслеживайте статус ваших заявок на программу стажировок
        </Typography>

        {data.applications.map((application) => (
          <Card key={application.id} sx={{ mb: 3 }}>
            <CardContent>
              <Box sx={{ mb: 3 }}>
                <Typography variant="h6" gutterBottom>
                  {application.vacancyTitle}
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <StatusBadge status={application.status} size="medium" />
                  <Typography variant="body2" color="text.secondary">
                    Обновлено: {format(new Date(application.statusChangedAt), 'dd.MM.yyyy HH:mm')}
                  </Typography>
                </Box>
              </Box>

              <Divider sx={{ my: 3 }} />

              {/* Progress stepper */}
              <Stepper activeStep={getActiveStep(application.status)} alternativeLabel sx={{ mb: 3 }}>
                <Step>
                  <StepLabel>Новая заявка</StepLabel>
                </Step>
                <Step>
                  <StepLabel>Скрининг</StepLabel>
                </Step>
                <Step>
                  <StepLabel>Рассмотрение</StepLabel>
                </Step>
                <Step>
                  <StepLabel>Интервью</StepLabel>
                </Step>
                <Step>
                  <StepLabel>Финал</StepLabel>
                </Step>
              </Stepper>

              {application.currentComment && (
                <Paper sx={{ p: 2, bgcolor: 'grey.50', mb: 2 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Комментарий:
                  </Typography>
                  <Typography variant="body2">{application.currentComment}</Typography>
                </Paper>
              )}

              {application.nextStep && (
                <Paper sx={{ p: 2, bgcolor: 'primary.50', borderLeft: 4, borderColor: 'primary.main' }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Следующий шаг:
                  </Typography>
                  <Typography variant="body2">{application.nextStep}</Typography>
                </Paper>
              )}

              {application.status === ApplicationStatus.APPROVED && (
                <Box sx={{ mt: 2, textAlign: 'center' }}>
                  <Chip label="Поздравляем! Вы одобрены!" color="success" size="medium" />
                </Box>
              )}

              {application.status === ApplicationStatus.REJECTED && (
                <Box sx={{ mt: 2, textAlign: 'center' }}>
                  <Chip label="К сожалению, в этот раз не получилось" color="error" size="medium" />
                </Box>
              )}
            </CardContent>
          </Card>
        ))}

        <Paper sx={{ p: 3, bgcolor: 'grey.50' }}>
          <Typography variant="body2" color="text.secondary">
            Если у вас есть вопросы, пожалуйста, свяжитесь с нами по адресу:{' '}
            <a href="mailto:recruitment@x5.ru">recruitment@x5.ru</a>
          </Typography>
        </Paper>
      </Box>
    </Container>
  );
}

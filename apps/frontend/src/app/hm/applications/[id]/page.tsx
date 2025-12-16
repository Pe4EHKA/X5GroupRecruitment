'use client';

import { useParams, useRouter } from 'next/navigation';
import {
  Box,
  Paper,
  Typography,
  CircularProgress,
  Grid,
  Divider,
  Button,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  FormControlLabel,
  Checkbox,
} from '@mui/material';
import { ArrowBack, Send } from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import StatusBadge from '@/components/StatusBadge';
import { UserRole } from '@/types';
import { useHmApplication, useSubmitDecision } from '@/hooks/useHm';
import { getCandidateFullName, getCandidateEmail, getCandidatePhone, getCandidateUniversity, getCandidateCourse } from '@/lib/utils';
import { format } from 'date-fns';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const decisionSchema = z.object({
  decision: z.enum(['APPROVE', 'REJECT', 'NEEDS_INFO']),
  overallAssessment: z.string().min(10, 'Минимум 10 символов'),
  strengths: z.string().optional(),
  areasForGrowth: z.string().optional(),
  recommendations: z.string().optional(),
  talentPool: z.boolean(),
});

type DecisionForm = z.infer<typeof decisionSchema>;

export default function HmApplicationPage() {
  const params = useParams();
  const router = useRouter();
  const applicationId = parseInt(params.id as string);

  const { data: application, isLoading } = useHmApplication(applicationId);
  const submitDecisionMutation = useSubmitDecision();

  const form = useForm<DecisionForm>({
    resolver: zodResolver(decisionSchema),
    defaultValues: {
      decision: 'APPROVE',
      overallAssessment: '',
      strengths: '',
      areasForGrowth: '',
      recommendations: '',
      talentPool: false,
    },
  });

  const handleSubmit = (data: DecisionForm) => {
    submitDecisionMutation.mutate(
      { id: applicationId, data },
      {
        onSuccess: () => {
          router.push('/hm/inbox');
        },
      }
    );
  };

  if (isLoading) {
    return (
      <ProtectedRoute allowedRoles={[UserRole.HM]}>
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
      <ProtectedRoute allowedRoles={[UserRole.HM]}>
        <DashboardLayout>
          <Typography>Заявка не найдена</Typography>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute allowedRoles={[UserRole.HM]}>
      <DashboardLayout>
        <Box>
          <Button startIcon={<ArrowBack />} onClick={() => router.back()} sx={{ mb: 2 }}>
            Назад
          </Button>

          <Typography variant="h4" gutterBottom>
            Заявка #{application.id}
          </Typography>

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
                      <Typography variant="body1">{getCandidateFullName(application.candidate)}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Email
                      </Typography>
                      <Typography variant="body1">{getCandidateEmail(application.candidate)}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Телефон
                      </Typography>
                      <Typography variant="body1">{getCandidatePhone(application.candidate)}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Университет
                      </Typography>
                      <Typography variant="body1">{getCandidateUniversity(application.candidate)}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">
                        Курс
                      </Typography>
                      <Typography variant="body1">{getCandidateCourse(application.candidate)}</Typography>
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
                      <Typography variant="body1">{application.vacancyTitle}</Typography>
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
                        Дата создания
                      </Typography>
                      <Typography variant="body1">
                        {format(new Date(application.createdAt), 'dd.MM.yyyy HH:mm')}
                      </Typography>
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            </Grid>

            {/* Decision Form */}
            <Grid item xs={12}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Принять решение
                  </Typography>
                  <Divider sx={{ mb: 3 }} />
                  <Box component="form" onSubmit={form.handleSubmit(handleSubmit)}>
                    <Grid container spacing={3}>
                      <Grid item xs={12} md={6}>
                        <FormControl fullWidth>
                          <InputLabel>Решение</InputLabel>
                          <Select {...form.register('decision')} defaultValue="APPROVE" label="Решение">
                            <MenuItem value="APPROVE">Одобрить</MenuItem>
                            <MenuItem value="REJECT">Отклонить</MenuItem>
                            <MenuItem value="NEEDS_INFO">Требуется информация</MenuItem>
                          </Select>
                        </FormControl>
                      </Grid>
                      <Grid item xs={12}>
                        <TextField
                          {...form.register('overallAssessment')}
                          fullWidth
                          label="Общая оценка"
                          multiline
                          rows={4}
                          required
                          error={!!form.formState.errors.overallAssessment}
                          helperText={form.formState.errors.overallAssessment?.message}
                        />
                      </Grid>
                      <Grid item xs={12}>
                        <TextField
                          {...form.register('strengths')}
                          fullWidth
                          label="Сильные стороны"
                          multiline
                          rows={3}
                        />
                      </Grid>
                      <Grid item xs={12}>
                        <TextField
                          {...form.register('areasForGrowth')}
                          fullWidth
                          label="Зоны роста"
                          multiline
                          rows={3}
                        />
                      </Grid>
                      <Grid item xs={12}>
                        <TextField
                          {...form.register('recommendations')}
                          fullWidth
                          label="Рекомендации"
                          multiline
                          rows={3}
                        />
                      </Grid>
                      <Grid item xs={12}>
                        <FormControlLabel
                          control={<Checkbox {...form.register('talentPool')} />}
                          label="Добавить в кадровый резерв"
                        />
                      </Grid>
                      <Grid item xs={12}>
                        <Button
                          type="submit"
                          variant="contained"
                          startIcon={<Send />}
                          disabled={submitDecisionMutation.isPending}
                        >
                          {submitDecisionMutation.isPending ? 'Отправка...' : 'Отправить решение'}
                        </Button>
                      </Grid>
                    </Grid>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

'use client';

import {
  Box,
  Paper,
  Typography,
  Button,
  Alert,
} from '@mui/material';
import { Download } from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole } from '@/types';
import { useExportApproved } from '@/hooks/useRecruiter';

export default function ExportPage() {
  const exportMutation = useExportApproved();

  const handleExport = () => {
    exportMutation.mutate();
  };

  return (
    <ProtectedRoute allowedRoles={[UserRole.RECRUITER]}>
      <DashboardLayout>
        <Box>
          <Typography variant="h4" gutterBottom>
            Экспорт кандидатов
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
            Экспорт одобренных кандидатов в Excel
          </Typography>

          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              Экспорт одобренных кандидатов
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Скачать список всех кандидатов со статусом &quot;Одобрено&quot; для дальнейшей передачи в ATS
            </Typography>
            <Button
              variant="contained"
              startIcon={<Download />}
              onClick={handleExport}
              disabled={exportMutation.isPending}
            >
              {exportMutation.isPending ? 'Экспорт...' : 'Скачать Excel'}
            </Button>
          </Paper>

          <Alert severity="info">
            <Typography variant="subtitle2">Формат экспорта</Typography>
            <Typography variant="body2">
              Excel файл будет содержать следующие поля:
              <ul>
                <li>ID заявки</li>
                <li>ФИО кандидата</li>
                <li>Email</li>
                <li>Телефон</li>
                <li>Университет</li>
                <li>Курс</li>
                <li>Вакансия</li>
                <li>Дата одобрения</li>
                <li>HM</li>
              </ul>
            </Typography>
          </Alert>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

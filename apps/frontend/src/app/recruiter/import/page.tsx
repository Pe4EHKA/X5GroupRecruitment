'use client';

import { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Alert,
  CircularProgress,
  LinearProgress,
} from '@mui/material';
import { CloudUpload } from '@mui/icons-material';
import DashboardLayout from '@/components/DashboardLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import { UserRole } from '@/types';
import { useImportXlsx } from '@/hooks/useRecruiter';

export default function ImportPage() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const importMutation = useImportXlsx();

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      setSelectedFile(event.target.files[0]);
    }
  };

  const handleUpload = () => {
    if (selectedFile) {
      importMutation.mutate(selectedFile);
    }
  };

  const result = importMutation.data;

  return (
    <ProtectedRoute allowedRoles={[UserRole.RECRUITER]}>
      <DashboardLayout>
        <Box>
          <Typography variant="h4" gutterBottom>
            Импорт заявок
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
            Загрузите Excel файл с заявками кандидатов
          </Typography>

          {/* Upload section */}
          <Paper sx={{ p: 3, mb: 3 }}>
            <input
              accept=".xlsx,.xls"
              style={{ display: 'none' }}
              id="upload-file"
              type="file"
              onChange={handleFileChange}
            />
            <label htmlFor="upload-file">
              <Button variant="outlined" component="span" startIcon={<CloudUpload />}>
                Выбрать файл
              </Button>
            </label>
            {selectedFile && (
              <Box sx={{ mt: 2 }}>
                <Typography variant="body2">Выбран файл: {selectedFile.name}</Typography>
                <Button
                  variant="contained"
                  sx={{ mt: 2 }}
                  onClick={handleUpload}
                  disabled={importMutation.isPending}
                >
                  {importMutation.isPending ? 'Загрузка...' : 'Загрузить'}
                </Button>
              </Box>
            )}
            {importMutation.isPending && <LinearProgress sx={{ mt: 2 }} />}
          </Paper>

          {/* Result section */}
          {result && (
            <>
              <Alert severity={result.batch.errorCount > 0 ? 'warning' : 'success'} sx={{ mb: 3 }}>
                <Typography variant="subtitle1">Результат импорта</Typography>
                <Typography variant="body2">
                  Обработано строк: {result.batch.totalRows}
                  <br />
                  Успешно: {result.batch.successCount}
                  <br />
                  Ошибок: {result.batch.errorCount}
                </Typography>
              </Alert>

              {/* Errors table */}
              {result.errors.length > 0 && (
                <Paper sx={{ mb: 3 }}>
                  <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
                    <Typography variant="h6">Ошибки импорта</Typography>
                  </Box>
                  <TableContainer>
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell>Строка</TableCell>
                          <TableCell>Код ошибки</TableCell>
                          <TableCell>Сообщение</TableCell>
                          <TableCell>Данные</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {result.errors.map((error) => (
                          <TableRow key={error.id}>
                            <TableCell>{error.rowNumber}</TableCell>
                            <TableCell>{error.errorCode}</TableCell>
                            <TableCell>{error.errorMessage}</TableCell>
                            <TableCell>
                              <Typography variant="caption" sx={{ maxWidth: 300, display: 'block' }}>
                                {error.rowData || '-'}
                              </Typography>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </Paper>
              )}
            </>
          )}

          {/* Instructions */}
          <Paper sx={{ p: 3, bgcolor: 'grey.50' }}>
            <Typography variant="h6" gutterBottom>
              Инструкция
            </Typography>
            <Typography variant="body2" component="div">
              <ol>
                <li>Подготовьте Excel файл с заявками кандидатов</li>
                <li>Файл должен содержать колонки: ФИО, Email, Телефон, Университет, Курс</li>
                <li>Нажмите "Выбрать файл" и выберите подготовленный Excel файл</li>
                <li>Нажмите "Загрузить" для начала импорта</li>
                <li>Дождитесь завершения импорта и проверьте результаты</li>
                <li>При наличии ошибок исправьте данные в файле и повторите импорт</li>
              </ol>
            </Typography>
          </Paper>
        </Box>
      </DashboardLayout>
    </ProtectedRoute>
  );
}

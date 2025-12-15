'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Container,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
} from '@mui/material';
import { useAuth } from '@/providers/AuthProvider';
import { UserRole } from '@/types';

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>(UserRole.RECRUITER);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(username, password, role);
      
      // Redirect based on role
      switch (role) {
        case UserRole.ADMIN:
          router.push('/admin/programs');
          break;
        case UserRole.HM:
          router.push('/hm/inbox');
          break;
        case UserRole.RECRUITER:
          router.push('/recruiter/dashboard');
          break;
        default:
          router.push('/');
      }
    } catch (err) {
      setError('Ошибка авторизации. Проверьте учетные данные.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = (user: string, pass: string, userRole: UserRole) => {
    setUsername(user);
    setPassword(pass);
    setRole(userRole);
    
    login(user, pass, userRole).then(() => {
      switch (userRole) {
        case UserRole.ADMIN:
          router.push('/admin/programs');
          break;
        case UserRole.HM:
          router.push('/hm/inbox');
          break;
        case UserRole.RECRUITER:
          router.push('/recruiter/dashboard');
          break;
      }
    });
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
        }}
      >
        <Card>
          <CardContent sx={{ p: 4 }}>
            <Typography variant="h4" component="h1" gutterBottom align="center">
              X5 Tech Recruitment
            </Typography>
            <Typography variant="body2" color="text.secondary" align="center" sx={{ mb: 3 }}>
              Система управления стажировками
            </Typography>

            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}

            <Box component="form" onSubmit={handleSubmit} sx={{ mt: 2 }}>
              <TextField
                fullWidth
                label="Имя пользователя"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                margin="normal"
                required
              />
              <TextField
                fullWidth
                label="Пароль"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                margin="normal"
                required
              />
              <FormControl fullWidth margin="normal">
                <InputLabel>Роль</InputLabel>
                <Select value={role} onChange={(e) => setRole(e.target.value as UserRole)} label="Роль">
                  <MenuItem value={UserRole.RECRUITER}>Recruiter</MenuItem>
                  <MenuItem value={UserRole.HM}>Hiring Manager</MenuItem>
                  <MenuItem value={UserRole.ADMIN}>Admin</MenuItem>
                </Select>
              </FormControl>

              <Button
                type="submit"
                fullWidth
                variant="contained"
                size="large"
                disabled={loading}
                sx={{ mt: 3, mb: 2 }}
              >
                {loading ? 'Вход...' : 'Войти'}
              </Button>
            </Box>

            <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
              <Typography variant="caption" display="block" gutterBottom>
                Быстрый вход (DEV режим):
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, flexDirection: 'column', mt: 1 }}>
                <Button
                  size="small"
                  variant="outlined"
                  onClick={() => handleQuickLogin('recruiter', 'recruiter123', UserRole.RECRUITER)}
                >
                  Recruiter
                </Button>
                <Button
                  size="small"
                  variant="outlined"
                  onClick={() => handleQuickLogin('hm', 'hm123', UserRole.HM)}
                >
                  Hiring Manager
                </Button>
                <Button
                  size="small"
                  variant="outlined"
                  onClick={() => handleQuickLogin('admin', 'admin123', UserRole.ADMIN)}
                >
                  Admin
                </Button>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Container>
  );
}

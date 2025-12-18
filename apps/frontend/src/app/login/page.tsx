'use client';

import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Container,
  Alert,
  Grid,
  Stack,
  Chip,
  Divider,
  InputAdornment,
  LinearProgress,
} from '@mui/material';
import { keyframes } from '@mui/system';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';
import RocketLaunchIcon from '@mui/icons-material/RocketLaunch';
import VerifiedIcon from '@mui/icons-material/Verified';
import TimelineIcon from '@mui/icons-material/Timeline';
import { useAuth } from '@/providers/AuthProvider';

const float = keyframes`
  0% { transform: translateY(0px); }
  50% { transform: translateY(-12px); }
  100% { transform: translateY(0px); }
`;

const pulse = keyframes`
  0% { opacity: 0.6; transform: scale(0.98); }
  50% { opacity: 1; transform: scale(1.02); }
  100% { opacity: 0.6; transform: scale(0.98); }
`;

export default function LoginPage() {
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(username, password);
    } catch (err) {
      setError('Ошибка авторизации. Проверьте учетные данные.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = (user: string, pass: string) => {
    setUsername(user);
    setPassword(pass);

    login(user, pass).catch(() => {
      setError('Ошибка авторизации. Проверьте учетные данные.');
    });
  };

  return (
    <Container
      maxWidth="lg"
      disableGutters
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        position: 'relative',
        overflow: 'hidden',
        py: { xs: 6, md: 8 },
        px: { xs: 2, md: 6 },
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          inset: 0,
          background:
            'radial-gradient(120% 120% at 10% 10%, rgba(107, 91, 255, 0.25), transparent 40%), radial-gradient(80% 80% at 90% 20%, rgba(34, 193, 195, 0.25), transparent 40%), linear-gradient(135deg, #0f1424 0%, #0b1020 100%)',
          zIndex: 0,
        }}
      />

      <Box
        sx={{
          position: 'absolute',
          inset: 0,
          overflow: 'hidden',
          zIndex: 0,
        }}
      >
        <Box
          sx={{
            position: 'absolute',
            width: { xs: 180, md: 260 },
            height: { xs: 180, md: 260 },
            background: 'linear-gradient(145deg, rgba(107,91,255,0.25), rgba(34,193,195,0.2))',
            borderRadius: '50%',
            filter: 'blur(70px)',
            top: '-40px',
            right: { xs: '-80px', md: '-120px' },
            animation: `${float} 18s ease-in-out infinite alternate`,
          }}
        />
        <Box
          sx={{
            position: 'absolute',
            width: { xs: 140, md: 220 },
            height: { xs: 140, md: 220 },
            background: 'linear-gradient(135deg, rgba(34,193,195,0.25), rgba(6,214,160,0.15))',
            borderRadius: '50%',
            filter: 'blur(60px)',
            bottom: '-60px',
            left: '-60px',
            animation: `${pulse} 14s ease-in-out infinite`,
          }}
        />
      </Box>

      <Grid
        container
        spacing={6}
        alignItems="center"
        sx={{ position: 'relative', zIndex: 1 }}
      >
        <Grid item xs={12} md={6}>
          <Stack spacing={3} sx={{ color: 'white' }}>
            <Chip
              icon={<VerifiedIcon sx={{ color: 'white !important' }} />}
              label="X5 Tech • Digital Talent"
              sx={{
                color: 'white',
                alignSelf: 'flex-start',
                background: 'rgba(255,255,255,0.14)',
                borderColor: 'rgba(255,255,255,0.24)',
                backdropFilter: 'blur(8px)',
                '& .MuiChip-icon': { color: 'white' },
              }}
              variant="outlined"
            />

            <Typography variant="h3" component="h1" sx={{ color: 'white', maxWidth: 540 }} gutterBottom>
              Создаем карьерные траектории для будущих лидеров X5 Tech
            </Typography>
            <Typography variant="body1" sx={{ color: 'rgba(255,255,255,0.7)', maxWidth: 540 }}>
              Единая платформа отбора, развития и вовлечения стажёров. Управляйте потоками кандидатов,
              автоматизируйте процессы и предоставляйте команде безупречный опыт взаимодействия.
            </Typography>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Box
                sx={{
                  p: 2.5,
                  pr: 3,
                  borderRadius: 3,
                  background: 'rgba(255,255,255,0.08)',
                  backdropFilter: 'blur(12px)',
                  border: '1px solid rgba(255,255,255,0.12)',
                  flex: 1,
                }}
              >
                <Stack direction="row" spacing={1.5} alignItems="center">
                  <RocketLaunchIcon sx={{ color: '#8de1ff' }} />
                  <Box>
                    <Typography variant="h4" sx={{ color: 'white', lineHeight: 1 }}>
                      240+
                    </Typography>
                    <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.7)' }}>
                      активных кандидатов
                    </Typography>
                  </Box>
                </Stack>
              </Box>
              <Box
                sx={{
                  p: 2.5,
                  pr: 3,
                  borderRadius: 3,
                  background: 'rgba(255,255,255,0.08)',
                  backdropFilter: 'blur(12px)',
                  border: '1px solid rgba(255,255,255,0.12)',
                  flex: 1,
                }}
              >
                <Stack direction="row" spacing={1.5} alignItems="center">
                  <TimelineIcon sx={{ color: '#c7b6ff' }} />
                  <Box>
                    <Typography variant="h4" sx={{ color: 'white', lineHeight: 1 }}>
                      12 дней
                    </Typography>
                    <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.7)' }}>
                      средний time-to-hire
                    </Typography>
                  </Box>
                </Stack>
              </Box>
            </Stack>

            <Box
              sx={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: 1,
                px: 2,
                py: 1.25,
                borderRadius: 999,
                background: 'rgba(255,255,255,0.12)',
                border: '1px solid rgba(255,255,255,0.2)',
                backdropFilter: 'blur(10px)',
              }}
            >
              <Box
                sx={{
                  width: 32,
                  height: 32,
                  borderRadius: '50%',
                  background: 'linear-gradient(135deg, #22c1c3, #6b5bff)',
                  display: 'grid',
                  placeItems: 'center',
                  color: 'white',
                  fontWeight: 700,
                }}
              >
                →
              </Box>
              <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.85)' }}>
                Авторизация для ролей HR, HM, Stager и Admin
              </Typography>
            </Box>
          </Stack>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card
            elevation={0}
            sx={{
              maxWidth: 520,
              ml: { md: 'auto' },
              overflow: 'hidden',
              border: '1px solid rgba(255,255,255,0.1)',
            }}
          >
            <CardContent sx={{ p: { xs: 3.5, md: 4 } }}>
              <Stack spacing={2} alignItems="center" textAlign="center" sx={{ mb: 1 }}>
                <Button
                  variant="outlined"
                  size="small"
                  startIcon={<VerifiedIcon />}
                  sx={{
                    alignSelf: 'center',
                    borderRadius: 999,
                    px: 2,
                    textTransform: 'none',
                    fontWeight: 700,
                    letterSpacing: 0.2,
                  }}
                >
                  X5 Tech Secure Access
                </Button>
                <Box>
                  <Typography variant="h4" component="h2" sx={{ mb: 0.5 }}>
                    Добро пожаловать обратно
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Авторизуйтесь, чтобы продолжить работу с потоками кандидатов
                  </Typography>
                </Box>
              </Stack>

              {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                  {error}
                </Alert>
              )}

              <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
                <Stack spacing={2.5}>
                  <TextField
                    fullWidth
                    label="Имя пользователя"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <PersonOutlineIcon color="action" />
                        </InputAdornment>
                      ),
                    }}
                  />
                  <TextField
                    fullWidth
                    label="Пароль"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <LockOutlinedIcon color="action" />
                        </InputAdornment>
                      ),
                    }}
                  />

                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} alignItems="center">
                    <Button
                      type="submit"
                      fullWidth
                      variant="contained"
                      size="large"
                      disabled={loading}
                      endIcon={<RocketLaunchIcon />}
                      sx={{ py: 1.6 }}
                    >
                      {loading ? 'Выполняем вход...' : 'Войти в систему'}
                    </Button>
                  </Stack>
                </Stack>
              </Box>

              {loading && <LinearProgress sx={{ mt: 2 }} />}

              <Divider sx={{ my: 3 }}>Быстрый вход (DEV)</Divider>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
                <Button
                  fullWidth
                  variant="outlined"
                  onClick={() => handleQuickLogin('recruiter', 'recruiter123')}
                  startIcon={<VerifiedIcon />}
                >
                  HR (Recruiter)
                </Button>
                <Button
                  fullWidth
                  variant="outlined"
                  onClick={() => handleQuickLogin('stager', 'stager123')}
                  startIcon={<RocketLaunchIcon />}
                >
                  Stager
                </Button>
              </Stack>
              <Button
                fullWidth
                variant="outlined"
                sx={{ mt: 1.2 }}
                onClick={() => handleQuickLogin('admin', 'admin123')}
                startIcon={<LockOutlinedIcon />}
              >
                Admin
              </Button>

              <Box sx={{ mt: 3, p: 2.5, borderRadius: 2, bgcolor: 'rgba(12,12,12,0.03)' }}>
                <Stack direction="row" spacing={1.5} alignItems="center">
                  <Box
                    sx={{
                      width: 40,
                      height: 40,
                      borderRadius: '12px',
                      background: 'linear-gradient(135deg, #22c1c3, #6b5bff)',
                      display: 'grid',
                      placeItems: 'center',
                      color: 'white',
                      fontWeight: 700,
                    }}
                  >
                    ?
                  </Box>
                  <Box>
                    <Typography variant="subtitle2" sx={{ mb: 0.5 }}>
                      Нужна помощь с доступом?
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Обратитесь к администратору программы или воспользуйтесь корпоративной поддержкой X5 Tech.
                    </Typography>
                  </Box>
                </Stack>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
}

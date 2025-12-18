'use client';

import { ThemeProvider as MuiThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { ReactNode } from 'react';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#6b5bff',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#22c1c3',
      contrastText: '#0c0c0c',
    },
    background: {
      default: '#0f1424',
      paper: 'rgba(20, 26, 45, 0.75)',
    },
    text: {
      primary: '#0c0c0c',
      secondary: 'rgba(12, 12, 12, 0.72)',
    },
  },
  shape: {
    borderRadius: 16,
  },
  typography: {
    fontFamily: '"Inter", "Manrope", "Segoe UI", system-ui, -apple-system, sans-serif',
    h3: {
      fontWeight: 700,
      letterSpacing: '-0.02em',
    },
    h4: {
      fontWeight: 700,
      letterSpacing: '-0.02em',
    },
    body1: {
      lineHeight: 1.6,
    },
    button: {
      fontWeight: 600,
    },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          background:
            'radial-gradient(120% 120% at 15% 20%, rgba(107, 91, 255, 0.18), transparent 40%), radial-gradient(90% 90% at 85% 10%, rgba(34, 193, 195, 0.18), transparent 45%), #0f1424',
          color: '#0c0c0c',
        },
        a: {
          color: 'inherit',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: 12,
          padding: '12px 18px',
          transition: 'all 0.25s ease',
          boxShadow: '0 15px 40px rgba(107, 91, 255, 0.35)',
        },
        containedPrimary: {
          background: 'linear-gradient(135deg, #6b5bff 0%, #3f35c5 100%)',
          ':hover': {
            background: 'linear-gradient(135deg, #7c6eff 0%, #5147d2 100%)',
            transform: 'translateY(-1px) scale(1.01)',
            boxShadow: '0 25px 50px rgba(107, 91, 255, 0.4)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 20,
          border: '1px solid rgba(255, 255, 255, 0.08)',
          background:
            'linear-gradient(180deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.08) 100%)',
          boxShadow: '0 20px 70px rgba(15, 20, 36, 0.55)',
          backdropFilter: 'blur(14px)',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          borderRadius: 18,
          boxShadow: '0 20px 60px rgba(15, 20, 36, 0.18)',
        },
      },
    },
    MuiTextField: {
      defaultProps: {
        variant: 'outlined',
      },
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            borderRadius: 14,
            transition: 'all 0.2s ease',
            ':hover': {
              backgroundColor: '#ffffff',
            },
            '&.Mui-focused fieldset': {
              borderColor: '#6b5bff',
              boxShadow: '0 0 0 3px rgba(107, 91, 255, 0.18)',
            },
          },
        },
      },
    },
    MuiInputLabel: {
      styleOverrides: {
        root: {
          fontWeight: 600,
          color: 'rgba(12, 12, 12, 0.7)',
        },
      },
    },
    MuiContainer: {
      defaultProps: {
        maxWidth: 'lg',
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: 12,
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 600,
        },
      },
    },
  },
});

interface ThemeProviderProps {
  children: ReactNode;
}

export function ThemeProvider({ children }: ThemeProviderProps) {
  return (
    <MuiThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </MuiThemeProvider>
  );
}

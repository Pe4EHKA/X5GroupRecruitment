import { createTheme } from '@mui/material/styles';
import { palette, radii, shadows, spacing, transitions, typography } from './tokens';

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: palette.primary,
    secondary: palette.secondary,
    background: {
      default: palette.backgrounds.base,
      paper: palette.backgrounds.surface,
    },
    text: {
      primary: palette.neutral[900],
      secondary: palette.neutral[600],
    },
    success: { main: palette.semantic.success },
    warning: { main: palette.semantic.warning },
    error: { main: palette.semantic.error },
    info: { main: palette.semantic.info },
  },
  shape: {
    borderRadius: radii.md,
  },
  spacing,
  typography: {
    ...typography,
    allVariants: {
      color: palette.neutral[900],
    },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          margin: 0,
          background: `${palette.backgrounds.base} radial-gradient(circle at 20% 20%, rgba(99,102,241,0.08), transparent 35%)`,
          color: palette.neutral[900],
          fontFamily: typography.fontFamily,
          minHeight: '100vh',
        },
        '*': {
          boxSizing: 'border-box',
        },
        '*::selection': {
          background: palette.primary.light,
          color: '#fff',
        },
        a: {
          color: 'inherit',
          textDecoration: 'none',
        },
        '@media (prefers-reduced-motion: reduce)': {
          '*': {
            animationDuration: '0.01ms !important',
            animationIterationCount: '1 !important',
            transitionDuration: '0.01ms !important',
            scrollBehavior: 'auto !important',
          },
        },
      },
    },
    MuiContainer: {
      defaultProps: {
        maxWidth: 'lg',
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: radii.lg,
          backgroundColor: palette.backgrounds.surface,
          boxShadow: shadows.soft,
          border: `1px solid ${palette.neutral[200]}`,
          transition: transitions.base,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: radii.lg,
          boxShadow: shadows.medium,
          border: `1px solid ${palette.neutral[200]}`,
          backgroundImage: `${palette.accents.glow}, ${palette.backgrounds.surface}`,
        },
      },
    },
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          borderRadius: radii.sm,
          textTransform: 'none',
          fontWeight: 700,
          transition: transitions.base,
          boxShadow: 'none',
          '&:focus-visible': {
            outline: `3px solid rgba(79, 70, 229, 0.25)`,
            outlineOffset: 2,
          },
        },
        containedPrimary: {
          backgroundImage: palette.accents.gradient,
          color: '#fff',
          boxShadow: shadows.medium,
          '&:hover': {
            boxShadow: shadows.strong,
            transform: 'translateY(-1px)',
          },
        },
        outlined: {
          borderColor: palette.neutral[300],
          '&:hover': {
            borderColor: palette.primary.main,
            backgroundColor: palette.neutral[100],
          },
        },
        text: {
          color: palette.primary.main,
          '&:hover': {
            backgroundColor: palette.neutral[100],
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: radii.pill,
          fontWeight: 600,
        },
      },
    },
    MuiTextField: {
      defaultProps: {
        variant: 'outlined',
        fullWidth: true,
      },
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: radii.sm,
            backgroundColor: '#fff',
            transition: transitions.base,
            '& fieldset': {
              borderColor: palette.neutral[200],
            },
            '&:hover fieldset': {
              borderColor: palette.primary.main,
            },
            '&.Mui-focused fieldset': {
              borderColor: palette.primary.main,
              boxShadow: `0 0 0 3px rgba(79, 70, 229, 0.12)`,
            },
          },
        },
      },
    },
    MuiInputLabel: {
      styleOverrides: {
        root: {
          fontWeight: 700,
          color: palette.neutral[600],
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        select: {
          borderRadius: radii.sm,
        },
      },
    },
    MuiTableContainer: {
      styleOverrides: {
        root: {
          borderRadius: radii.md,
          border: `1px solid ${palette.neutral[200]}`,
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: palette.neutral[100],
          '& .MuiTableCell-root': {
            color: palette.neutral[700],
            fontWeight: 700,
          },
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          transition: transitions.base,
          '&:hover': {
            backgroundColor: palette.neutral[100],
          },
        },
      },
    },
    MuiTabs: {
      styleOverrides: {
        indicator: {
          height: 4,
          borderRadius: radii.pill,
        },
      },
    },
    MuiTab: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 700,
        },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: radii.md,
          alignItems: 'flex-start',
        },
      },
    },
    MuiSkeleton: {
      defaultProps: {
        animation: 'wave',
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: '#0f172a',
          color: '#e2e8f0',
          borderRight: `1px solid rgba(255,255,255,0.08)`,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        colorInherit: {
          backgroundColor: palette.backgrounds.surface,
          color: palette.neutral[900],
          borderBottom: `1px solid ${palette.neutral[200]}`,
          boxShadow: shadows.soft,
        },
      },
    },
  },
});

import { createTheme } from '@mui/material/styles';
import { motion, palette, radii, shadows, spacing, transitions, typography } from './tokens';

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
      primary: palette.text.primary,
      secondary: palette.text.secondary,
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
      color: palette.text.primary,
    },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          margin: 0,
          background:
            `${palette.backgrounds.base} radial-gradient(circle at 18% 20%, rgba(31,191,117,0.1), transparent 35%), radial-gradient(circle at 80% 6%, rgba(15,158,94,0.08), transparent 32%)`,
          color: palette.text.primary,
          fontFamily: typography.fontFamily,
          minHeight: '100vh',
        },
        '*': {
          boxSizing: 'border-box',
          minWidth: 0,
        },
        '*::selection': {
          background: palette.primary.light,
          color: palette.primary.contrastText,
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
          border: `1px solid ${palette.borders.subtle}`,
          transition: transitions.base,
          backgroundImage: palette.accents.gradientSoft,
          '&[data-variant="elevated"]': {
            backgroundColor: palette.backgrounds.elevated,
            backgroundImage: 'none',
            boxShadow: shadows.medium,
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: radii.lg,
          boxShadow: shadows.medium,
          border: `1px solid ${palette.borders.subtle}`,
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
          letterSpacing: 0.2,
          '&:focus-visible': {
            outline: `3px solid ${palette.focusRing}`,
            outlineOffset: 2,
          },
        },
        containedPrimary: {
          backgroundImage: palette.accents.gradient,
          color: palette.primary.contrastText,
          boxShadow: shadows.medium,
          '&:hover': {
            boxShadow: shadows.strong,
            transform: 'translateY(-1px)',
            backgroundImage: palette.accents.gradient,
          },
          '&:active': {
            transform: 'translateY(0)',
            boxShadow: shadows.medium,
          },
        },
        outlined: {
          borderColor: palette.borders.subtle,
          '&:hover': {
            borderColor: palette.primary.main,
            backgroundColor: palette.backgrounds.muted,
          },
        },
        text: {
          color: palette.primary.dark,
          '&:hover': {
            backgroundColor: palette.backgrounds.muted,
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: radii.pill,
          fontWeight: 600,
          backgroundColor: palette.backgrounds.muted,
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
                borderColor: palette.borders.subtle,
                transition: transitions.base,
              },
              '&:hover fieldset': {
                borderColor: palette.primary.main,
              },
              '&.Mui-focused fieldset': {
                borderColor: palette.primary.main,
                boxShadow: `0 0 0 3px ${palette.focusRing}`,
              },
            },
          },
        },
      },
    MuiInputLabel: {
      styleOverrides: {
        root: {
          fontWeight: 700,
          color: palette.text.muted,
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        select: {
          borderRadius: radii.sm,
          '&:focus': {
            backgroundColor: 'transparent',
          },
        },
      },
    },
    MuiTableContainer: {
      styleOverrides: {
        root: {
          borderRadius: radii.md,
          border: `1px solid ${palette.borders.subtle}`,
          backgroundColor: palette.backgrounds.surface,
          boxShadow: shadows.soft,
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: palette.backgrounds.muted,
          borderBottom: `1px solid ${palette.borders.subtle}`,
          '& .MuiTableCell-root': {
            color: palette.text.secondary,
            fontWeight: 700,
            fontSize: '0.95rem',
          },
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          transition: transitions.base,
          '&:hover': {
            backgroundColor: palette.backgrounds.muted,
          },
        },
      },
    },
    MuiTabs: {
      styleOverrides: {
        indicator: {
          height: 4,
          borderRadius: radii.pill,
          boxShadow: shadows.inner,
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
          padding: '12px 16px',
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
          backgroundColor: palette.backgrounds.sidebar,
          color: palette.text.onDark,
          borderRight: `1px solid rgba(255,255,255,0.08)`,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        colorInherit: {
          backgroundColor: palette.backgrounds.surface,
          color: palette.text.primary,
          borderBottom: `1px solid ${palette.borders.subtle}`,
          boxShadow: shadows.soft,
        },
      },
    },
  },
});

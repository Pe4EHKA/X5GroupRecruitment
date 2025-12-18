export const palette = {
  primary: {
    main: '#4338ca',
    light: '#6366f1',
    dark: '#312e81',
    contrastText: '#ffffff',
  },
  secondary: {
    main: '#0ea5e9',
    light: '#38bdf8',
    dark: '#0b79aa',
    contrastText: '#ffffff',
  },
  neutral: {
    50: '#f8fafc',
    100: '#eff3f9',
    200: '#e2e8f0',
    300: '#cbd5e1',
    400: '#94a3b8',
    500: '#64748b',
    600: '#475569',
    700: '#334155',
    800: '#1e293b',
    900: '#0f172a',
  },
  semantic: {
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    info: '#0ea5e9',
  },
  backgrounds: {
    base: '#f5f7fb',
    surface: '#ffffff',
    muted: '#eef2f6',
    elevated: '#f9fbff',
    glass: 'rgba(255,255,255,0.72)',
  },
  accents: {
    gradient: 'linear-gradient(135deg, #4338ca 0%, #0ea5e9 100%)',
    gradientSoft: 'linear-gradient(145deg, rgba(67,56,202,0.12), rgba(14,165,233,0.08))',
    glow:
      'radial-gradient(circle at 20% 20%, rgba(99,102,241,0.18), transparent 35%), radial-gradient(circle at 80% 10%, rgba(14,165,233,0.18), transparent 35%)',
  },
};

export const typography = {
  fontFamily: '"Inter", "Manrope", "Segoe UI", system-ui, -apple-system, sans-serif',
  h1: { fontSize: '2.75rem', fontWeight: 800, lineHeight: 1.15, letterSpacing: '-0.04em' },
  h2: { fontSize: '2.25rem', fontWeight: 800, lineHeight: 1.2, letterSpacing: '-0.03em' },
  h3: { fontSize: '1.75rem', fontWeight: 700, lineHeight: 1.25, letterSpacing: '-0.02em' },
  h4: { fontSize: '1.5rem', fontWeight: 700, lineHeight: 1.3, letterSpacing: '-0.02em' },
  h5: { fontSize: '1.25rem', fontWeight: 700, lineHeight: 1.35 },
  h6: { fontSize: '1.125rem', fontWeight: 700, lineHeight: 1.35 },
  subtitle1: { fontSize: '1rem', fontWeight: 600, lineHeight: 1.4 },
  body1: { fontSize: '1rem', lineHeight: 1.6, fontWeight: 500 },
  body2: { fontSize: '0.95rem', lineHeight: 1.55, fontWeight: 500 },
  caption: { fontSize: '0.85rem', lineHeight: 1.4, fontWeight: 600 },
  button: { fontSize: '0.95rem', fontWeight: 700, letterSpacing: 0.2 },
};

export const radii = {
  xs: 8,
  sm: 12,
  md: 16,
  lg: 20,
  xl: 28,
  pill: 999,
};

export const spacing = [4, 8, 12, 16, 24, 32, 40, 48];

export const shadows = {
  soft: '0 10px 30px rgba(15, 23, 42, 0.08)',
  medium: '0 18px 44px rgba(15, 23, 42, 0.12)',
  strong: '0 28px 72px rgba(15, 23, 42, 0.16)',
  inner: 'inset 0 1px 0 rgba(255,255,255,0.6)',
};

export const transitions = {
  base: 'all 200ms ease',
  quicker: 'all 150ms ease',
};

export const motion = {
  duration: {
    shortest: 120,
    short: 180,
    base: 220,
  },
  easing: {
    standard: 'cubic-bezier(0.4, 0, 0.2, 1)',
    emphasized: 'cubic-bezier(0.2, 0.8, 0.2, 1)',
  },
};

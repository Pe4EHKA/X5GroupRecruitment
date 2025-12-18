export const palette = {
  primary: {
    main: '#1fbf75',
    light: '#34d399',
    dark: '#178a55',
    contrastText: '#f7fff9',
  },
  secondary: {
    main: '#0f766e',
    light: '#16a39a',
    dark: '#0a4f49',
    contrastText: '#e6f4ed',
  },
  neutral: {
    50: '#f4f7f4',
    100: '#e6eee5',
    200: '#d0e1d6',
    300: '#b5c7bd',
    400: '#8fa69b',
    500: '#688274',
    600: '#4b6356',
    700: '#34493f',
    800: '#22332d',
    900: '#132019',
  },
  semantic: {
    success: '#1f9d55',
    warning: '#f2b247',
    error: '#e5484d',
    info: '#1c9ab7',
  },
  text: {
    primary: '#0f2418',
    secondary: '#2f3f36',
    muted: '#4b6356',
    onDark: '#e8f5ed',
    onMuted: '#d3e3d8',
  },
  backgrounds: {
    base: '#f2f6f2',
    surface: '#ffffff',
    muted: '#e7efe8',
    elevated: '#f8fbf8',
    glass: 'rgba(255,255,255,0.78)',
    sidebar: '#0f1f18',
  },
  accents: {
    gradient: 'linear-gradient(135deg, #1fbf75 0%, #0f9e5e 100%)',
    gradientSoft: 'linear-gradient(145deg, rgba(31,191,117,0.12), rgba(15,158,94,0.08))',
    glow:
      'radial-gradient(circle at 20% 20%, rgba(31,191,117,0.18), transparent 35%), radial-gradient(circle at 80% 10%, rgba(15,158,94,0.18), transparent 35%)',
  },
  focusRing: 'rgba(31, 191, 117, 0.35)',
  borders: {
    subtle: '#d6e5da',
    strong: '#b6c9bd',
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
  soft: '0 10px 30px rgba(10, 31, 20, 0.08)',
  medium: '0 18px 44px rgba(10, 31, 20, 0.12)',
  strong: '0 28px 72px rgba(10, 31, 20, 0.16)',
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

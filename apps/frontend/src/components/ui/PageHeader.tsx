'use client';

import { Box, Chip, Stack, Typography } from '@mui/material';
import ArrowForwardIosRoundedIcon from '@mui/icons-material/ArrowForwardIosRounded';
import { ReactNode } from 'react';
import { palette } from '@/theme/tokens';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  chipLabel?: string;
  actions?: ReactNode;
}

export function PageHeader({ title, subtitle, chipLabel, actions }: PageHeaderProps) {
  return (
    <Box
      sx={{
        mb: 3,
        p: { xs: 2.5, md: 3.5 },
        borderRadius: 3,
        position: 'relative',
        overflow: 'hidden',
        background: palette.accents.gradientSoft,
        border: `1px solid ${palette.borders.subtle}`,
        boxShadow: '0 22px 80px rgba(10,31,20,0.14)',
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          inset: 0,
          background:
            'linear-gradient(135deg, rgba(255,255,255,0.42), transparent), radial-gradient(circle at 12% 24%, rgba(31,191,117,0.15), transparent 35%)',
          pointerEvents: 'none',
        }}
      />
      <Stack spacing={1.25} sx={{ position: 'relative', zIndex: 1 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} alignItems={{ xs: 'flex-start', sm: 'center' }} spacing={1.5} flexWrap="wrap">
          {chipLabel && (
            <Chip
              label={chipLabel}
              color="primary"
              variant="outlined"
              size="small"
              sx={{ px: 0.5, fontWeight: 700, borderRadius: 2 }}
            />
          )}
          <Stack direction="row" alignItems="center" spacing={1}>
            <Typography variant="h3" component="h1" sx={{ lineHeight: 1.1 }}>
              {title}
            </Typography>
            <ArrowForwardIosRoundedIcon fontSize="small" color="primary" />
          </Stack>
        </Stack>
        {subtitle && (
          <Typography
            variant="body1"
            color="text.secondary"
            sx={{ maxWidth: 960, wordBreak: 'break-word', lineHeight: 1.6 }}
          >
            {subtitle}
          </Typography>
        )}
        {actions && (
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', pt: 0.5 }}>{actions}</Box>
        )}
      </Stack>
    </Box>
  );
}

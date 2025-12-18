'use client';

import { Box, Chip, Stack, Typography } from '@mui/material';
import ArrowForwardIosRoundedIcon from '@mui/icons-material/ArrowForwardIosRounded';
import { ReactNode } from 'react';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  chipLabel?: string;
  actions?: ReactNode;
}

export function PageHeader({ title, subtitle, chipLabel, actions }: PageHeaderProps) {
  return (
    <Stack spacing={1.5} sx={{ mb: 3 }}>
      <Stack direction="row" alignItems="center" spacing={1.5} flexWrap="wrap">
        {chipLabel && (
          <Chip
            label={chipLabel}
            color="primary"
            variant="outlined"
            size="small"
            sx={{ px: 0.5, fontWeight: 700 }}
          />
        )}
        <Stack direction="row" alignItems="center" spacing={1}>
          <Typography variant="h3" component="h1">
            {title}
          </Typography>
          <ArrowForwardIosRoundedIcon fontSize="small" color="primary" />
        </Stack>
      </Stack>
      {subtitle && (
        <Typography variant="body1" color="text.secondary" sx={{ maxWidth: 960 }}>
          {subtitle}
        </Typography>
      )}
      {actions && (
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>{actions}</Box>
      )}
    </Stack>
  );
}

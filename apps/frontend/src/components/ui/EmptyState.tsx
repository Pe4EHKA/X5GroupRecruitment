'use client';

import { Box, Button, Stack, Typography } from '@mui/material';
import InboxOutlinedIcon from '@mui/icons-material/InboxOutlined';
import { ReactNode } from 'react';
import { palette, shadows } from '@/theme/tokens';

interface EmptyStateProps {
  title: string;
  description?: string;
  actionLabel?: string;
  onAction?: () => void;
  icon?: ReactNode;
}

export function EmptyState({
  title,
  description,
  actionLabel,
  onAction,
  icon = <InboxOutlinedIcon fontSize="large" color="disabled" />,
}: EmptyStateProps) {
  return (
    <Stack
      spacing={1.5}
      alignItems="center"
      sx={{
        py: 6,
        px: 2,
        borderRadius: 3,
        border: `1px dashed ${palette.neutral[300]}`,
        background: palette.backgrounds.surface,
        boxShadow: shadows.soft,
      }}
    >
      <Box
        sx={{
          width: 64,
          height: 64,
          borderRadius: '50%',
          background: 'linear-gradient(135deg, rgba(79,70,229,0.12), rgba(14,165,233,0.12))',
          display: 'grid',
          placeItems: 'center',
          color: 'text.secondary',
        }}
      >
        {icon}
      </Box>
      <Typography variant="subtitle1" color="text.primary">
        {title}
      </Typography>
      {description && (
        <Typography variant="body2" color="text.secondary" textAlign="center" sx={{ maxWidth: 420 }}>
          {description}
        </Typography>
      )}
      {actionLabel && (
        <Button variant="contained" color="primary" onClick={onAction}>
          {actionLabel}
        </Button>
      )}
    </Stack>
  );
}

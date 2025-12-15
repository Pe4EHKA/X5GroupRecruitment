'use client';

import { SnackbarProvider as NotistackProvider } from 'notistack';
import { ReactNode } from 'react';

interface SnackbarProviderProps {
  children: ReactNode;
}

export function SnackbarProvider({ children }: SnackbarProviderProps) {
  return (
    <NotistackProvider
      maxSnack={3}
      anchorOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      autoHideDuration={5000}
    >
      {children}
    </NotistackProvider>
  );
}

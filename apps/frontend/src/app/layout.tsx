import type { Metadata } from 'next';
import { AuthProvider } from '@/providers/AuthProvider';
import { QueryProvider } from '@/providers/QueryProvider';
import { ThemeProvider } from '@/providers/ThemeProvider';
import { SnackbarProvider } from '@/providers/SnackbarProvider';

export const metadata: Metadata = {
  title: 'X5 Tech Recruitment System',
  description: 'Автоматизация процессов отбора и обратной связи в рамках программы стажировок',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ru">
      <body>
        <ThemeProvider>
          <QueryProvider>
            <AuthProvider>
              <SnackbarProvider>{children}</SnackbarProvider>
            </AuthProvider>
          </QueryProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}

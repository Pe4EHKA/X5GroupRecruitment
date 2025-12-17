import { Chip } from '@mui/material';
import { ApplicationStatus } from '@/types';

const statusConfig: Record<
  ApplicationStatus,
  { label: string; color: 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' }
> = {
  [ApplicationStatus.NEW]: { label: 'Новая', color: 'info' },
  [ApplicationStatus.SCREENING]: { label: 'Скрининг', color: 'primary' },
  [ApplicationStatus.INTERVIEW_SCHEDULED]: { label: 'Интервью назначено', color: 'primary' },
  [ApplicationStatus.INTERVIEW_COMPLETED]: { label: 'Интервью завершено', color: 'primary' },
  [ApplicationStatus.APPROVED]: { label: 'Одобрено', color: 'success' },
  [ApplicationStatus.REJECTED]: { label: 'Отклонено', color: 'error' },
  [ApplicationStatus.OFFER_SENT]: { label: 'Оффер отправлен', color: 'success' },
  [ApplicationStatus.OFFER_ACCEPTED]: { label: 'Оффер принят', color: 'success' },
  [ApplicationStatus.OFFER_DECLINED]: { label: 'Оффер отклонен', color: 'error' },
  [ApplicationStatus.WITHDRAWN]: { label: 'Отозвано', color: 'default' },
  [ApplicationStatus.ON_HOLD]: { label: 'На удержании', color: 'secondary' },
};

interface StatusBadgeProps {
  status: ApplicationStatus;
  size?: 'small' | 'medium';
}

export default function StatusBadge({ status, size = 'small' }: StatusBadgeProps) {
  const config = statusConfig[status];
  return <Chip label={config.label} color={config.color} size={size} />;
}

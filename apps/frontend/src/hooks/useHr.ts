import { useMutation, useQuery } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import api, { getErrorMessage } from '@/lib/api';
import { ApplicationDto, ApplicationDetailDto, PageResponse, ApplicationStatus, PasswordResetResponse } from '@/types';

interface ApplicationFilters {
  statuses?: ApplicationStatus[];
  vacancyId?: number;
  dateFrom?: string;
  dateTo?: string;
  search?: string;
  page?: number;
  size?: number;
}

export const useApplications = (filters: ApplicationFilters) => {
  return useQuery<PageResponse<ApplicationDto>>({
    queryKey: ['hr', 'applications', filters],
    queryFn: async () => {
      const params = new URLSearchParams();
      
      if (filters.statuses && filters.statuses.length > 0) {
        filters.statuses.forEach(status => params.append('statuses', status));
      }
      if (filters.vacancyId) {
        params.append('vacancyId', filters.vacancyId.toString());
      }
      if (filters.dateFrom) {
        params.append('dateFrom', filters.dateFrom);
      }
      if (filters.dateTo) {
        params.append('dateTo', filters.dateTo);
      }
      if (filters.search) {
        params.append('search', filters.search);
      }
      params.append('page', (filters.page ?? 0).toString());
      params.append('size', (filters.size ?? 20).toString());
      
      const response = await api.get<PageResponse<ApplicationDto>>(
        `/api/hr/applications?${params.toString()}`
      );
      return response.data;
    },
  });
};

export const useApplication = (id: number) => {
  return useQuery<ApplicationDetailDto>({
    queryKey: ['hr', 'application', id],
    queryFn: async () => {
      const response = await api.get<ApplicationDetailDto>(
        `/api/hr/applications/${id}`
      );
      return response.data;
    },
    enabled: !!id,
  });
};

export const useResetTraineePassword = () => {
  const { enqueueSnackbar } = useSnackbar();

  return useMutation({
    mutationFn: async ({ traineeId, newPassword }: { traineeId: number; newPassword?: string }) => {
      const response = await api.post<PasswordResetResponse>(
        `/api/hr/trainees/${traineeId}/password/reset`,
        newPassword ? { newPassword } : undefined
      );
      return response.data;
    },
    onSuccess: () => {
      enqueueSnackbar('Временный пароль сгенерирован', { variant: 'success' });
    },
    onError: (error) => {
      enqueueSnackbar(getErrorMessage(error), { variant: 'error' });
    },
  });
};

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import api from '@/lib/api';
import { Application, ApplicationDetail, HmDecisionRequest, PageResponse } from '@/types';

// Query keys
export const hmKeys = {
  all: ['hm'] as const,
  pending: () => [...hmKeys.all, 'pending'] as const,
  application: (id: number) => [...hmKeys.all, 'application', id] as const,
};

// Get pending applications
export function usePendingApplications() {
  return useQuery({
    queryKey: hmKeys.pending(),
    queryFn: async () => {
      const response = await api.get<PageResponse<Application>>('/api/hm/pending');
      return response.data;
    },
  });
}

// Get single application
export function useHmApplication(id: number) {
  return useQuery({
    queryKey: hmKeys.application(id),
    queryFn: async () => {
      const response = await api.get<ApplicationDetail>(`/api/hm/applications/${id}`);
      return response.data;
    },
    enabled: !!id,
  });
}

// Submit HM decision
export function useSubmitDecision() {
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();

  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: HmDecisionRequest }) => {
      const response = await api.post<Application>(`/api/hm/applications/${id}/decision`, data);
      return response.data;
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: hmKeys.application(variables.id) });
      queryClient.invalidateQueries({ queryKey: hmKeys.pending() });
      enqueueSnackbar('Решение успешно отправлено', { variant: 'success' });
    },
    onError: (error: any) => {
      enqueueSnackbar(error.response?.data?.message || 'Ошибка отправки решения', { variant: 'error' });
    },
  });
}

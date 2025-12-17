import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import api, { getErrorMessage } from '@/lib/api';
import {
  Application,
  ApplicationDetail,
  ApplicationFilters,
  PageResponse,
  ChangeStatusRequest,
  DashboardMetrics,
  ImportResult,
} from '@/types';

// Query keys
export const recruiterKeys = {
  all: ['recruiter'] as const,
  dashboard: () => [...recruiterKeys.all, 'dashboard'] as const,
  applications: (filters?: ApplicationFilters) => [...recruiterKeys.all, 'applications', filters] as const,
  application: (id: number) => [...recruiterKeys.all, 'application', id] as const,
};

// Get dashboard metrics
export function useDashboardMetrics() {
  return useQuery({
    queryKey: recruiterKeys.dashboard(),
    queryFn: async () => {
      const response = await api.get<DashboardMetrics>('/api/recruiter/dashboard/metrics');
      return response.data;
    },
  });
}

// Get applications list
export function useApplications(filters?: ApplicationFilters) {
  return useQuery({
    queryKey: recruiterKeys.applications(filters),
    queryFn: async () => {
      const params = new URLSearchParams();
      if (filters?.programId) params.append('programId', filters.programId.toString());
      if (filters?.status) params.append('status', filters.status);
      if (filters?.recruiterId) params.append('recruiterId', filters.recruiterId.toString());
      if (filters?.hmId) params.append('hmId', filters.hmId.toString());
      if (filters?.vacancyId) params.append('vacancyId', filters.vacancyId.toString());
      if (filters?.dateFrom) params.append('dateFrom', filters.dateFrom);
      if (filters?.dateTo) params.append('dateTo', filters.dateTo);
      if (filters?.search) params.append('search', filters.search);
      if (filters?.slaBreached !== undefined) params.append('slaBreached', filters.slaBreached.toString());
      if (filters?.page !== undefined) params.append('page', filters.page.toString());
      if (filters?.size !== undefined) params.append('size', filters.size.toString());
      if (filters?.sort) params.append('sort', filters.sort);

      const response = await api.get<PageResponse<Application>>(`/api/recruiter/applications?${params}`);
      return response.data;
    },
  });
}

// Get single application
export function useApplication(id: number) {
  return useQuery({
    queryKey: recruiterKeys.application(id),
    queryFn: async () => {
      const response = await api.get<ApplicationDetail>(`/api/recruiter/applications/${id}`);
      return response.data;
    },
    enabled: !!id,
  });
}

// Change application status
export function useChangeStatus() {
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();

  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: ChangeStatusRequest }) => {
      const response = await api.patch<Application>(`/api/recruiter/applications/${id}/status`, data);
      return response.data;
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: recruiterKeys.application(variables.id) });
      queryClient.invalidateQueries({ queryKey: recruiterKeys.applications() });
      queryClient.invalidateQueries({ queryKey: recruiterKeys.dashboard() });
      enqueueSnackbar('Статус успешно изменен', { variant: 'success' });
    },
    onError: (error: any) => {
      enqueueSnackbar(getErrorMessage(error), { variant: 'error' });
    },
  });
}

// Import XLSX
export function useImportXlsx() {
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();

  return useMutation({
    mutationFn: async (file: File) => {
      const formData = new FormData();
      formData.append('file', file);
      
      const response = await api.post<ImportResult>('/api/import-export/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: recruiterKeys.applications() });
      queryClient.invalidateQueries({ queryKey: recruiterKeys.dashboard() });
      enqueueSnackbar('Файл успешно импортирован', { variant: 'success' });
    },
    onError: (error: any) => {
      enqueueSnackbar(getErrorMessage(error), { variant: 'error' });
    },
  });
}

// Export approved
export function useExportApproved() {
  const { enqueueSnackbar } = useSnackbar();

  return useMutation({
    mutationFn: async () => {
      const response = await api.get('/api/import-export/export/approved', {
        responseType: 'blob',
      });
      
      // Create download link
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `approved_candidates_${new Date().toISOString().split('T')[0]}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      
      return response.data;
    },
    onSuccess: () => {
      enqueueSnackbar('Файл успешно скачан', { variant: 'success' });
    },
    onError: (error: any) => {
      enqueueSnackbar(getErrorMessage(error), { variant: 'error' });
    },
  });
}

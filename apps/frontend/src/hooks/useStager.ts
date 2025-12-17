import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import api, { getErrorMessage } from '@/lib/api';
import { ApplicationDetailDto, MediaResponse, StagerProfileDto } from '@/types';

export const useMyApplications = () => {
  return useQuery<ApplicationDetailDto[]>({
    queryKey: ['stager', 'applications'],
    queryFn: async () => {
      const response = await api.get<ApplicationDetailDto[]>(
        `/api/stager/application`
      );
      return response.data;
    },
  });
};

export const useMyProfile = () => {
  return useQuery<StagerProfileDto>({
    queryKey: ['stager', 'profile'],
    queryFn: async () => {
      const response = await api.get<StagerProfileDto>(
        `/api/stager/profile`
      );
      return response.data;
    },
  });
};

export const useUploadStagerVideo = (applicationId: number) => {
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();

  return useMutation<MediaResponse, unknown, File>({
    mutationFn: async (file: File) => {
      const formData = new FormData();
      formData.append('file', file);

      const response = await api.post<MediaResponse>(`/api/stager/application/${applicationId}/video`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['stager', 'applications'] });
      enqueueSnackbar('Видео-визитка успешно загружена', { variant: 'success' });
    },
    onError: (error) => {
      enqueueSnackbar(getErrorMessage(error), { variant: 'error' });
    },
  });
};

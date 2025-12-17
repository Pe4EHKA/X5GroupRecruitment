import { useQuery } from '@tanstack/react-query';
import api from '@/lib/api';
import { ApplicationDetailDto, StagerProfileDto } from '@/types';

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

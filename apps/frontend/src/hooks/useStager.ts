import { useQuery } from '@tanstack/react-query';
import axios from 'axios';
import { ApplicationDetailDto, StagerProfileDto } from '@/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

// Get auth credentials from localStorage
const getAuthHeader = () => {
  if (typeof window !== 'undefined') {
    const credentials = localStorage.getItem('authCredentials');
    if (credentials) {
      return { Authorization: `Basic ${credentials}` };
    }
  }
  return {};
};

export const useMyApplications = () => {
  return useQuery<ApplicationDetailDto[]>({
    queryKey: ['stager', 'applications'],
    queryFn: async () => {
      const response = await axios.get(
        `${API_URL}/api/stager/application`,
        { headers: getAuthHeader() }
      );
      return response.data;
    },
  });
};

export const useMyProfile = () => {
  return useQuery<StagerProfileDto>({
    queryKey: ['stager', 'profile'],
    queryFn: async () => {
      const response = await axios.get(
        `${API_URL}/api/stager/profile`,
        { headers: getAuthHeader() }
      );
      return response.data;
    },
  });
};

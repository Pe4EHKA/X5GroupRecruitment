import { api } from '@/lib/api';
import { Vacancy, VacancyRequest } from '@/types';

export const vacancyService = {
  async list(): Promise<Vacancy[]> {
    const { data } = await api.get<Vacancy[]>('/api/vacancies');
    return data;
  },

  async get(id: number): Promise<Vacancy> {
    const { data } = await api.get<Vacancy>(`/api/vacancies/${id}`);
    return data;
  },

  async create(request: VacancyRequest): Promise<Vacancy> {
    const { data } = await api.post<Vacancy>('/api/vacancies', request);
    return data;
  },

  async update(id: number, request: VacancyRequest): Promise<Vacancy> {
    const { data } = await api.put<Vacancy>(`/api/vacancies/${id}`, request);
    return data;
  },
};

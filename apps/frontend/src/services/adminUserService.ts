import { api } from '@/lib/api';
import {
  AdminUser,
  CreateUserRequest,
  UpdateUserRequest,
  UpdateRolesRequest,
  UpdateStatusRequest,
  UserRole,
  UserStatus,
  PageResponse,
} from '@/types';

/**
 * Admin API service for user management
 */

export interface GetUsersParams {
  q?: string;
  status?: UserStatus;
  role?: UserRole;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export const adminUserService = {
  /**
   * Get all users with filters and pagination
   */
  async getUsers(params: GetUsersParams = {}): Promise<PageResponse<AdminUser>> {
    const { data } = await api.get<PageResponse<AdminUser>>('/api/admin/users', { params });
    return data;
  },

  /**
   * Get user by ID
   */
  async getUserById(id: number): Promise<AdminUser> {
    const { data } = await api.get<AdminUser>(`/api/admin/users/${id}`);
    return data;
  },

  /**
   * Create new user
   */
  async createUser(request: CreateUserRequest): Promise<AdminUser> {
    const { data } = await api.post<AdminUser>('/api/admin/users', request);
    return data;
  },

  /**
   * Update user profile
   */
  async updateUser(id: number, request: UpdateUserRequest): Promise<AdminUser> {
    const { data } = await api.put<AdminUser>(`/api/admin/users/${id}`, request);
    return data;
  },

  /**
   * Update user roles
   */
  async updateUserRoles(id: number, request: UpdateRolesRequest): Promise<AdminUser> {
    const { data } = await api.put<AdminUser>(`/api/admin/users/${id}/roles`, request);
    return data;
  },

  /**
   * Update user status
   */
  async updateUserStatus(id: number, request: UpdateStatusRequest): Promise<AdminUser> {
    const { data } = await api.put<AdminUser>(`/api/admin/users/${id}/status`, request);
    return data;
  },

  /**
   * Reset user password and optionally set a custom one
   */
  async resetPassword(id: number, newPassword?: string) {
    const { data } = await api.post(`/api/admin/users/${id}/password/reset`, {
      newPassword,
    });
    return data;
  },
};

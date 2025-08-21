import { apiService } from './api';
import type { LeaveRequest, TimeModificationRequest, ApiResponse } from '../types';

export interface CreateLeaveRequestData {
  type: 'ANNUAL' | 'SICK' | 'PERSONAL' | 'SPECIAL' | 'MATERNITY' | 'PATERNITY' | 'PAID';
  startDate: string;
  endDate: string;
  reason: string;
}

export interface CreateTimeModificationData {
  date: string;
  requestedClockIn?: string;
  requestedClockOut?: string;
  reason: string;
}

export const requestService = {
  async createLeaveRequest(data: CreateLeaveRequestData): Promise<ApiResponse<LeaveRequest>> {
    return apiService.post('/requests/leave', data);
  },

  async getLeaveRequests(status?: string): Promise<ApiResponse<LeaveRequest[]>> {
    const params = status ? `?status=${status}` : '';
    return apiService.get(`/requests/leave${params}`);
  },

  async updateLeaveRequestStatus(
    requestId: string, 
    status: 'APPROVED' | 'REJECTED',
    comment?: string
  ): Promise<ApiResponse<LeaveRequest>> {
    return apiService.patch(`/requests/leave/${requestId}`, { status, comment });
  },

  async createTimeModification(data: CreateTimeModificationData): Promise<ApiResponse<TimeModificationRequest>> {
    return apiService.post('/requests/time-modification', data);
  },

  async getTimeModificationRequests(status?: string): Promise<ApiResponse<TimeModificationRequest[]>> {
    const params = status ? `?status=${status}` : '';
    return apiService.get(`/requests/time-modification${params}`);
  },

  async updateTimeModificationStatus(
    requestId: string, 
    status: 'APPROVED' | 'REJECTED',
    comment?: string
  ): Promise<ApiResponse<TimeModificationRequest>> {
    return apiService.patch(`/requests/time-modification/${requestId}`, { status, comment });
  },

  async deleteLeaveRequest(requestId: string): Promise<ApiResponse<void>> {
    return apiService.delete(`/requests/leave/${requestId}`);
  },

  async deleteTimeModificationRequest(requestId: string): Promise<ApiResponse<void>> {
    return apiService.delete(`/requests/time-modification/${requestId}`);
  },
};
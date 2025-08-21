import { apiService } from './api';
import type { TimeRecord, User, AlertItem, MonthlyReport, ApiResponse } from '../types';

export const managerService = {
  async getDashboard(): Promise<ApiResponse<{
    teamSize: number;
    todayPresent: number;
    todayLate: number;
    todayAbsent: number;
    unreadAlerts: number;
    pendingApprovals: number;
    teamMembers: User[];
  }>> {
    return apiService.get('/manager/dashboard');
  },

  async getTeamMembers(): Promise<ApiResponse<User[]>> {
    return apiService.get('/manager/team');
  },

  async getTeamAttendance(date: string): Promise<ApiResponse<TimeRecord[]>> {
    return apiService.get(`/manager/team/attendance?date=${date}`);
  },

  async getTeamAttendanceRange(
    startDate: string, 
    endDate: string
  ): Promise<ApiResponse<TimeRecord[]>> {
    return apiService.get(`/manager/team/attendance?start=${startDate}&end=${endDate}`);
  },

  async getMemberAttendance(
    userId: string, 
    startDate: string, 
    endDate: string
  ): Promise<ApiResponse<TimeRecord[]>> {
    return apiService.get(`/manager/member/${userId}/attendance?start=${startDate}&end=${endDate}`);
  },

  async getAlerts(limit?: number): Promise<ApiResponse<AlertItem[]>> {
    const params = limit ? `?limit=${limit}` : '';
    return apiService.get(`/manager/alerts${params}`);
  },

  async markAlertAsRead(alertId: string): Promise<ApiResponse<AlertItem>> {
    return apiService.patch(`/manager/alerts/${alertId}/read`);
  },

  async exportTeamReport(
    startDate: string, 
    endDate: string, 
    format: 'csv' | 'excel' = 'csv'
  ): Promise<ApiResponse<{ downloadUrl: string }>> {
    return apiService.post('/api/export/attendance', {
      startDate,
      endDate,
      format,
    });
  },

  async getMonthlyReport(
    year: number, 
    month: number, 
    userId?: string
  ): Promise<ApiResponse<MonthlyReport[]>> {
    const params = userId ? `?userId=${userId}` : '';
    return apiService.get(`/manager/reports/monthly/${year}/${month}${params}`);
  },

  async getDepartmentStats(
    startDate: string, 
    endDate: string
  ): Promise<ApiResponse<{
    totalEmployees: number;
    presentToday: number;
    lateToday: number;
    absentToday: number;
    averageHours: number;
  }>> {
    return apiService.get(`/manager/stats?start=${startDate}&end=${endDate}`);
  },

  // Request approval endpoints
  async getPendingLeaveRequests(): Promise<ApiResponse<any[]>> {
    return apiService.get('/api/requests/leave?status=pending');
  },

  async getPendingTimeModificationRequests(): Promise<ApiResponse<any[]>> {
    return apiService.get('/api/requests/time-modification?status=pending');
  },

  async approveLeaveRequest(requestId: string, comment?: string): Promise<ApiResponse<any>> {
    return apiService.patch(`/api/requests/leave/${requestId}`, { status: 'approved', comment });
  },

  async rejectLeaveRequest(requestId: string, comment: string): Promise<ApiResponse<any>> {
    return apiService.patch(`/api/requests/leave/${requestId}`, { status: 'rejected', comment });
  },

  async approveTimeModificationRequest(requestId: string, comment?: string): Promise<ApiResponse<any>> {
    return apiService.patch(`/api/requests/time-modification/${requestId}`, { status: 'approved', comment });
  },

  async rejectTimeModificationRequest(requestId: string, comment: string): Promise<ApiResponse<any>> {
    return apiService.patch(`/api/requests/time-modification/${requestId}`, { status: 'rejected', comment });
  },
};
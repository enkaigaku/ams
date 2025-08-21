import { apiService } from './api';
import type { TimeRecord, User, AlertItem, MonthlyReport, ApiResponse } from '../types';

export const managerService = {
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
    return apiService.post('/manager/export', {
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
};
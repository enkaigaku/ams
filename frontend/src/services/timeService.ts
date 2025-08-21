import { apiService } from './api';
import type { TimeRecord, ClockAction, ApiResponse, AttendanceStats } from '../types';

export const timeService = {
  async clockAction(action: ClockAction): Promise<ApiResponse<TimeRecord>> {
    return apiService.post('/time/clock', action);
  },

  async getTodayRecord(): Promise<ApiResponse<TimeRecord | null>> {
    const today = new Date().toISOString().split('T')[0];
    return apiService.get(`/time/records/${today}`);
  },

  async getTimeRecords(
    startDate: string, 
    endDate: string
  ): Promise<ApiResponse<TimeRecord[]>> {
    return apiService.get(`/time/records?start=${startDate}&end=${endDate}`);
  },

  async getMonthlyRecords(
    year: number, 
    month: number
  ): Promise<ApiResponse<TimeRecord[]>> {
    return apiService.get(`/time/records/monthly?year=${year}&month=${month}`);
  },

  async getAttendanceStats(
    startDate: string, 
    endDate: string
  ): Promise<ApiResponse<AttendanceStats>> {
    return apiService.get(`/time/stats?start=${startDate}&end=${endDate}`);
  },

  async updateTimeRecord(
    recordId: string, 
    data: Partial<TimeRecord>
  ): Promise<ApiResponse<TimeRecord>> {
    return apiService.put(`/time/records/${recordId}`, data);
  },

  async deleteTimeRecord(recordId: string): Promise<ApiResponse<void>> {
    return apiService.delete(`/time/records/${recordId}`);
  },
};
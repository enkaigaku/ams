import { apiService } from './api';
import type { TimeRecord, ClockAction, ApiResponse, AttendanceStats } from '../types';

export const timeService = {
  async clockIn(timestamp: string, location?: { lat: number; lng: number }): Promise<ApiResponse<TimeRecord>> {
    return apiService.post('/time/clock-in', { timestamp, location });
  },

  async clockOut(timestamp: string, location?: { lat: number; lng: number }): Promise<ApiResponse<TimeRecord>> {
    return apiService.post('/time/clock-out', { timestamp, location });
  },

  async startBreak(timestamp: string): Promise<ApiResponse<TimeRecord>> {
    return apiService.post('/time/break-start', { timestamp });
  },

  async endBreak(timestamp: string): Promise<ApiResponse<TimeRecord>> {
    return apiService.post('/time/break-end', { timestamp });
  },

  // Legacy method for backward compatibility
  async clockAction(action: ClockAction): Promise<ApiResponse<TimeRecord>> {
    switch (action.type) {
      case 'clock_in':
        return this.clockIn(action.timestamp, action.location);
      case 'clock_out':
        return this.clockOut(action.timestamp, action.location);
      case 'break_start':
        return this.startBreak(action.timestamp);
      case 'break_end':
        return this.endBreak(action.timestamp);
      default:
        throw new Error(`Unknown action type: ${action.type}`);
    }
  },

  async getTodayRecord(): Promise<ApiResponse<TimeRecord | null>> {
    return apiService.get('/time/today');
  },

  async getTimeRecords(
    startDate: string, 
    endDate: string
  ): Promise<ApiResponse<TimeRecord[]>> {
    return apiService.get(`/time/history?startDate=${startDate}&endDate=${endDate}`);
  },

  async getMonthlyRecords(
    year: number, 
    month: number
  ): Promise<ApiResponse<TimeRecord[]>> {
    return apiService.get(`/time/history?year=${year}&month=${month}`);
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
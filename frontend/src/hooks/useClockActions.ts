import { useState } from 'react';
import { useTimeStore } from '../stores/timeStore';
import { timeService } from '../services/timeService';
import type { ClockAction } from '../types';

export const useClockActions = () => {
  const [isLoading, setIsLoading] = useState(false);
  const { setTodayRecord, setClockingState } = useTimeStore();

  const performClockAction = async (action: ClockAction) => {
    setIsLoading(true);
    setClockingState(true);
    
    try {
      const response = await timeService.clockAction(action);
      if (response.success && response.data) {
        setTodayRecord(response.data);
        return response.data;
      } else {
        throw new Error(response.error || 'Clock action failed');
      }
    } catch (error) {
      console.error('Clock action failed:', error);
      throw error;
    } finally {
      setIsLoading(false);
      setClockingState(false);
    }
  };

  const clockIn = async (location?: { lat: number; lng: number }) => {
    return performClockAction({
      type: 'clock_in',
      timestamp: new Date().toISOString(),
      location,
    });
  };

  const clockOut = async (location?: { lat: number; lng: number }) => {
    return performClockAction({
      type: 'clock_out',
      timestamp: new Date().toISOString(),
      location,
    });
  };

  const startBreak = async () => {
    return performClockAction({
      type: 'break_start',
      timestamp: new Date().toISOString(),
    });
  };

  const endBreak = async () => {
    return performClockAction({
      type: 'break_end',
      timestamp: new Date().toISOString(),
    });
  };

  return {
    clockIn,
    clockOut,
    startBreak,
    endBreak,
    isLoading,
  };
};
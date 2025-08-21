import { create } from 'zustand';
import type { TimeRecord, ClockAction } from '../types';

interface TimeStore {
  todayRecord: TimeRecord | null;
  isClockingIn: boolean;
  lastAction: ClockAction | null;
  
  setTodayRecord: (record: TimeRecord | null) => void;
  setClockingState: (isClocking: boolean) => void;
  setLastAction: (action: ClockAction | null) => void;
  
  getCurrentStatus: () => 'clocked_out' | 'clocked_in' | 'on_break';
  canClockIn: () => boolean;
  canClockOut: () => boolean;
  canStartBreak: () => boolean;
  canEndBreak: () => boolean;
}

export const useTimeStore = create<TimeStore>((set, get) => ({
  todayRecord: null,
  isClockingIn: false,
  lastAction: null,

  setTodayRecord: (record) => set({ todayRecord: record }),
  setClockingState: (isClocking) => set({ isClockingIn: isClocking }),
  setLastAction: (action) => set({ lastAction: action }),

  getCurrentStatus: () => {
    const { todayRecord } = get();
    if (!todayRecord) return 'clocked_out';
    
    if (todayRecord.clockIn && !todayRecord.clockOut) {
      if (todayRecord.breakStart && !todayRecord.breakEnd) {
        return 'on_break';
      }
      return 'clocked_in';
    }
    
    return 'clocked_out';
  },

  canClockIn: () => {
    const { todayRecord } = get();
    return !todayRecord?.clockIn;
  },

  canClockOut: () => {
    const { todayRecord } = get();
    return Boolean(todayRecord?.clockIn && !todayRecord?.clockOut);
  },

  canStartBreak: () => {
    const { todayRecord } = get();
    return Boolean(
      todayRecord?.clockIn && 
      !todayRecord?.clockOut && 
      !todayRecord?.breakStart
    );
  },

  canEndBreak: () => {
    const { todayRecord } = get();
    return Boolean(
      todayRecord?.breakStart && 
      !todayRecord?.breakEnd
    );
  },
}));
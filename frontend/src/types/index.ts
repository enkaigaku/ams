export interface User {
  id: string;
  employeeId: string;
  name: string;
  email?: string;
  role: 'EMPLOYEE' | 'MANAGER';
  isActive: boolean;
  department: string;
  departmentId?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface TimeRecord {
  id: string;
  userId: string;
  date: string;
  clockIn?: string;
  clockOut?: string;
  breakStart?: string;
  breakEnd?: string;
  totalHours?: number;
  status: 'PRESENT' | 'ABSENT' | 'LATE' | 'EARLY_LEAVE';
  notes?: string;
}

export interface LeaveRequest {
  id: string;
  userId: string;
  type: 'ANNUAL' | 'SICK' | 'PERSONAL' | 'SPECIAL' | 'MATERNITY' | 'PATERNITY' | 'PAID';
  startDate: string;
  endDate: string;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  approvedBy?: string;
  approvedAt?: string;
  createdAt: string;
  comment?: string;
}

export interface TimeModificationRequest {
  id: string;
  userId: string;
  date: string;
  originalClockIn?: string;
  originalClockOut?: string;
  requestedClockIn?: string;
  requestedClockOut?: string;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  approvedBy?: string;
  approvedAt?: string;
  createdAt: string;
  comment?: string;
}

export interface Department {
  id: string;
  name: string;
  managerId: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface ClockAction {
  type: 'clock_in' | 'clock_out' | 'break_start' | 'break_end';
  timestamp: string;
  location?: {
    lat: number;
    lng: number;
  };
}

export interface AttendanceStats {
  totalDays: number;
  presentDays: number;
  lateDays: number;
  absentDays: number;
  totalHours: number;
  averageHours: number;
}

export interface MonthlyReport {
  userId: string;
  month: string;
  year: number;
  records: TimeRecord[];
  stats: AttendanceStats;
}

export interface AlertItem {
  id: string;
  type: 'late' | 'absent' | 'missing_clock_out';
  userId: string;
  userName: string;
  date: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}
import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { ja } from 'date-fns/locale';
import { 
  UsersIcon, 
  ClockIcon, 
  ExclamationTriangleIcon,
  DocumentTextIcon,
  ChartBarIcon,
  CheckCircleIcon,
  XCircleIcon
} from '@heroicons/react/24/outline';

import { 
  Card, 
  CardHeader, 
  CardContent,
  Badge,
  LoadingSpinner,
  Button 
} from '../../components/ui';
import { useAuthStore } from '../../stores/authStore';
import { managerService } from '../../services/managerService';
import { requestService } from '../../services/requestService';
import type { TimeRecord, User, AlertItem, LeaveRequest, TimeModificationRequest } from '../../types';

const ManagerDashboard: React.FC = () => {
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(true);
  const [teamMembers, setTeamMembers] = useState<User[]>([]);
  const [todayAttendance, setTodayAttendance] = useState<TimeRecord[]>([]);
  const [alerts, setAlerts] = useState<AlertItem[]>([]);
  const [pendingLeaveRequests, setPendingLeaveRequests] = useState<LeaveRequest[]>([]);
  const [pendingTimeRequests, setPendingTimeRequests] = useState<TimeModificationRequest[]>([]);
  const [stats, setStats] = useState({
    totalEmployees: 0,
    presentToday: 0,
    lateToday: 0,
    absentToday: 0,
    averageHours: 0,
  });

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const today = format(new Date(), 'yyyy-MM-dd');
      const startOfMonth = format(new Date(new Date().getFullYear(), new Date().getMonth(), 1), 'yyyy-MM-dd');
      const endOfMonth = format(new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0), 'yyyy-MM-dd');

      const [
        teamResponse,
        attendanceResponse,
        alertsResponse,
        leaveRequestsResponse,
        timeRequestsResponse,
        statsResponse,
      ] = await Promise.all([
        managerService.getTeamMembers(),
        managerService.getTeamAttendance(today),
        managerService.getAlerts(10),
        requestService.getLeaveRequests('pending'),
        requestService.getTimeModificationRequests('pending'),
        managerService.getDepartmentStats(startOfMonth, endOfMonth),
      ]);

      if (teamResponse.success && teamResponse.data) {
        setTeamMembers(teamResponse.data);
      }
      if (attendanceResponse.success && attendanceResponse.data) {
        setTodayAttendance(attendanceResponse.data);
      }
      if (alertsResponse.success && alertsResponse.data) {
        setAlerts(alertsResponse.data);
      }
      if (leaveRequestsResponse.success && leaveRequestsResponse.data) {
        setPendingLeaveRequests(leaveRequestsResponse.data);
      }
      if (timeRequestsResponse.success && timeRequestsResponse.data) {
        setPendingTimeRequests(timeRequestsResponse.data);
      }
      if (statsResponse.success && statsResponse.data) {
        setStats(statsResponse.data);
      }
    } catch (error) {
      console.error('Failed to load dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getAttendanceStatus = (record: TimeRecord) => {
    if (record.clockIn && record.clockOut) {
      return { text: '出勤済み', color: 'success' as const };
    } else if (record.clockIn) {
      return { text: '勤務中', color: 'info' as const };
    } else {
      return { text: '未出勤', color: 'default' as const };
    }
  };

  const calculateWorkingHours = (record: TimeRecord): string => {
    if (!record.clockIn) return '0:00';
    
    const clockIn = new Date(record.clockIn);
    const clockOut = record.clockOut ? new Date(record.clockOut) : new Date();
    
    let totalMinutes = Math.floor((clockOut.getTime() - clockIn.getTime()) / (1000 * 60));
    
    // Subtract break time
    if (record.breakStart && record.breakEnd) {
      const breakStart = new Date(record.breakStart);
      const breakEnd = new Date(record.breakEnd);
      const breakMinutes = Math.floor((breakEnd.getTime() - breakStart.getTime()) / (1000 * 60));
      totalMinutes -= breakMinutes;
    }
    
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    
    return `${hours}:${minutes.toString().padStart(2, '0')}`;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="text-center">
        <h1 className="text-2xl font-bold text-white mb-2">
          管理者ダッシュボード
        </h1>
        <p className="text-gray-300">
          {user?.name}さん | {format(new Date(), 'yyyy年M月d日（E）', { locale: ja })}
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card>
          <CardContent className="p-6 text-center">
            <div className="flex items-center justify-center mb-2">
              <UsersIcon className="h-8 w-8 text-primary-600" />
            </div>
            <div className="text-2xl font-bold text-white">{stats.totalEmployees}</div>
            <div className="text-sm text-gray-400">総従業員数</div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 text-center">
            <div className="flex items-center justify-center mb-2">
              <CheckCircleIcon className="h-8 w-8 text-green-600" />
            </div>
            <div className="text-2xl font-bold text-white">{stats.presentToday}</div>
            <div className="text-sm text-gray-400">本日出勤</div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 text-center">
            <div className="flex items-center justify-center mb-2">
              <ExclamationTriangleIcon className="h-8 w-8 text-yellow-600" />
            </div>
            <div className="text-2xl font-bold text-white">{stats.lateToday}</div>
            <div className="text-sm text-gray-400">遅刻者数</div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 text-center">
            <div className="flex items-center justify-center mb-2">
              <XCircleIcon className="h-8 w-8 text-red-600" />
            </div>
            <div className="text-2xl font-bold text-white">{stats.absentToday}</div>
            <div className="text-sm text-gray-400">欠勤者数</div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Today's Attendance */}
        <Card>
          <CardHeader>
            <h3 className="text-lg font-medium flex items-center">
              <ClockIcon className="h-5 w-5 mr-2" />
              本日の出勤状況
            </h3>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 max-h-64 overflow-y-auto">
              {todayAttendance.length === 0 ? (
                <p className="text-center text-gray-500 py-4">出勤記録がありません</p>
              ) : (
                todayAttendance.map((record) => {
                  const member = teamMembers.find(m => m.id === record.userId);
                  const status = getAttendanceStatus(record);
                  
                  return (
                    <div key={record.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <div className="flex-1">
                        <p className="font-medium text-gray-900">{member?.name || 'Unknown'}</p>
                        <p className="text-sm text-gray-600">
                          {record.clockIn ? format(new Date(record.clockIn), 'HH:mm') : '--:--'} - 
                          {record.clockOut ? format(new Date(record.clockOut), 'HH:mm') : '--:--'}
                        </p>
                      </div>
                      <div className="flex items-center space-x-2">
                        <Badge variant={status.color} size="sm">
                          {status.text}
                        </Badge>
                        <span className="text-sm font-medium text-gray-700">
                          {calculateWorkingHours(record)}
                        </span>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </CardContent>
        </Card>

        {/* Alerts */}
        <Card>
          <CardHeader>
            <h3 className="text-lg font-medium flex items-center">
              <ExclamationTriangleIcon className="h-5 w-5 mr-2" />
              アラート ({alerts.filter(a => !a.isRead).length}件未読)
            </h3>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 max-h-64 overflow-y-auto">
              {alerts.length === 0 ? (
                <p className="text-center text-gray-500 py-4">アラートはありません</p>
              ) : (
                alerts.map((alert) => (
                  <div 
                    key={alert.id} 
                    className={`p-3 rounded-lg border ${
                      alert.isRead ? 'bg-gray-50 border-gray-200' : 'bg-yellow-50 border-yellow-200'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="text-sm font-medium text-gray-900">{alert.message}</p>
                        <p className="text-xs text-gray-500 mt-1">
                          {format(new Date(alert.createdAt), 'MM/dd HH:mm')}
                        </p>
                      </div>
                      {!alert.isRead && (
                        <div className="w-2 h-2 bg-yellow-400 rounded-full ml-2 mt-1"></div>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Pending Approvals */}
      <Card>
        <CardHeader>
          <h3 className="text-lg font-medium flex items-center">
            <DocumentTextIcon className="h-5 w-5 mr-2" />
            承認待ち申請 ({pendingLeaveRequests.length + pendingTimeRequests.length}件)
          </h3>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Leave Requests */}
            <div>
              <h4 className="font-medium text-gray-900 mb-3">休暇申請 ({pendingLeaveRequests.length}件)</h4>
              <div className="space-y-2">
                {pendingLeaveRequests.slice(0, 5).map((request) => {
                  const member = teamMembers.find(m => m.id === request.userId);
                  return (
                    <div key={request.id} className="p-3 bg-gray-50 rounded-lg">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <p className="font-medium text-sm">{member?.name || 'Unknown'}</p>
                          <p className="text-xs text-gray-600">
                            {format(new Date(request.startDate), 'MM/dd')} - {format(new Date(request.endDate), 'MM/dd')}
                          </p>
                        </div>
                        <Badge variant="warning" size="sm">
                          {request.type === 'paid_leave' ? '有給' : 
                           request.type === 'sick_leave' ? '病欠' : '私用'}
                        </Badge>
                      </div>
                    </div>
                  );
                })}
                {pendingLeaveRequests.length === 0 && (
                  <p className="text-sm text-gray-500 text-center py-2">承認待ちの休暇申請はありません</p>
                )}
              </div>
            </div>

            {/* Time Modification Requests */}
            <div>
              <h4 className="font-medium text-gray-900 mb-3">打刻修正申請 ({pendingTimeRequests.length}件)</h4>
              <div className="space-y-2">
                {pendingTimeRequests.slice(0, 5).map((request) => {
                  const member = teamMembers.find(m => m.id === request.userId);
                  return (
                    <div key={request.id} className="p-3 bg-gray-50 rounded-lg">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <p className="font-medium text-sm">{member?.name || 'Unknown'}</p>
                          <p className="text-xs text-gray-600">
                            {format(new Date(request.date), 'MM/dd')}
                          </p>
                        </div>
                        <Badge variant="info" size="sm">打刻修正</Badge>
                      </div>
                    </div>
                  );
                })}
                {pendingTimeRequests.length === 0 && (
                  <p className="text-sm text-gray-500 text-center py-2">承認待ちの打刻修正申請はありません</p>
                )}
              </div>
            </div>
          </div>

          {(pendingLeaveRequests.length > 0 || pendingTimeRequests.length > 0) && (
            <div className="flex justify-center mt-4">
              <Button onClick={() => window.location.href = '/manager/approvals'}>
                すべての申請を確認
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Button
          size="lg"
          className="h-16"
          onClick={() => window.location.href = '/manager/team'}
        >
          <UsersIcon className="h-6 w-6 mr-2" />
          チーム状況
        </Button>
        
        <Button
          size="lg"
          variant="secondary"
          className="h-16"
          onClick={() => window.location.href = '/manager/approvals'}
        >
          <DocumentTextIcon className="h-6 w-6 mr-2" />
          承認管理
        </Button>
        
        <Button
          size="lg"
          variant="secondary"
          className="h-16"
          onClick={() => window.location.href = '/manager/reports'}
        >
          <ChartBarIcon className="h-6 w-6 mr-2" />
          レポート
        </Button>
      </div>
    </div>
  );
};

export default ManagerDashboard;
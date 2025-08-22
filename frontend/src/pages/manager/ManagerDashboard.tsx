import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { ja } from 'date-fns/locale';
import { 
  Users, 
  UserCheck, 
  Clock3,
  UserX,
  AlertTriangle,
  FileText,
  BarChart3,
  CheckCircle,
  XCircle,
  Clock,
  Calendar
} from 'lucide-react';

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
    <div className="min-h-screen bg-background p-4 sm:p-6 lg:p-8">
      {/* Header Section */}
      <section className="mb-8 animate-fade-in">
        <div className="flex items-center gap-4 mb-4">
          <div className="w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center border-2 border-primary/20">
            <BarChart3 className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-foreground">管理者ダッシュボード</h1>
            <p className="text-muted-foreground">
              {user?.name}さん | {format(new Date(), 'yyyy年M月d日（E）', { locale: ja })}
            </p>
          </div>
        </div>
      </section>

      {/* Quick Stats - 4 Column Grid */}
      <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-card border border-border rounded-xl shadow-md p-6 flex items-center gap-4 hover:shadow-lg transition-all duration-300 animate-fade-in-up">
          <Users className="h-10 w-10 text-chart-1" />
          <div>
            <p className="text-sm text-muted-foreground">総従業員数</p>
            <p className="text-3xl font-bold text-foreground">{stats.totalEmployees}</p>
          </div>
        </div>
        <div className="bg-card border border-border rounded-xl shadow-md p-6 flex items-center gap-4 hover:shadow-lg transition-all duration-300 animate-fade-in-up">
          <UserCheck className="h-10 w-10 text-chart-2" />
          <div>
            <p className="text-sm text-muted-foreground">本日出勤</p>
            <p className="text-3xl font-bold text-foreground">{stats.presentToday}</p>
          </div>
        </div>
        <div className="bg-card border border-border rounded-xl shadow-md p-6 flex items-center gap-4 hover:shadow-lg transition-all duration-300 animate-fade-in-up">
          <Clock3 className="h-10 w-10 text-chart-4" />
          <div>
            <p className="text-sm text-muted-foreground">遅刻者数</p>
            <p className="text-3xl font-bold text-foreground">{stats.lateToday}</p>
          </div>
        </div>
        <div className="bg-card border border-border rounded-xl shadow-md p-6 flex items-center gap-4 hover:shadow-lg transition-all duration-300 animate-fade-in-up">
          <UserX className="h-10 w-10 text-destructive" />
          <div>
            <p className="text-sm text-muted-foreground">欠勤者数</p>
            <p className="text-3xl font-bold text-foreground">{stats.absentToday}</p>
          </div>
        </div>
      </section>

      {/* Calendar and Pending Actions */}
      <section className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
        <div className="lg:col-span-2 bg-card border border-border rounded-xl shadow-md p-6 animate-fade-in-up">
          <h3 className="text-lg font-semibold mb-4 flex items-center text-foreground">
            <Calendar className="h-5 w-5 mr-2 text-primary" />
            チーム勤怠カレンダー
          </h3>
          <div className="text-center py-16 text-muted-foreground">
            <p>[フルインタラクティブチームカレンダーがここに表示されます]</p>
          </div>
        </div>
        <div className="bg-card border border-border rounded-xl shadow-md p-6 animate-fade-in-up">
          <h3 className="text-lg font-semibold mb-4 text-foreground">承認待ちアクション</h3>
          <div className="space-y-4">
            <div className="p-3 rounded-md flex justify-between items-center border border-border hover:bg-accent transition-colors">
              <div>
                <p className="font-medium text-foreground">田中 一郎 - 休暇申請</p>
                <p className="text-sm text-muted-foreground">病気休暇</p>
              </div>
              <div className="flex gap-2">
                <button className="bg-primary text-primary-foreground px-3 py-1 text-sm rounded-md hover:bg-primary/90 transition-colors">承認</button>
                <button className="bg-secondary text-secondary-foreground px-3 py-1 text-sm rounded-md border border-border hover:bg-accent transition-colors">却下</button>
              </div>
            </div>
            <div className="p-3 rounded-md flex justify-between items-center border border-border hover:bg-accent transition-colors">
              <div>
                <p className="font-medium text-foreground">佐藤 花子 - 勤怠修正</p>
                <p className="text-sm text-muted-foreground">打刻忘れ</p>
              </div>
              <button className="bg-secondary text-secondary-foreground px-3 py-1 text-sm rounded-md border border-border hover:bg-accent transition-colors">確認</button>
            </div>
            <div className="p-3 rounded-md flex justify-between items-center border border-border hover:bg-accent transition-colors">
              <div>
                <p className="font-medium text-foreground">鈴木 太郎 - 休暇申請</p>
                <p className="text-sm text-muted-foreground">夏季休暇</p>
              </div>
              <div className="flex gap-2">
                <button className="bg-primary text-primary-foreground px-3 py-1 text-sm rounded-md hover:bg-primary/90 transition-colors">承認</button>
                <button className="bg-secondary text-secondary-foreground px-3 py-1 text-sm rounded-md border border-border hover:bg-accent transition-colors">却下</button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Team Attendance Overview */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <div className="bg-card border border-border rounded-xl shadow-md p-6 animate-fade-in-up">
          <h3 className="text-lg font-semibold mb-4 flex items-center text-foreground">
            <Users className="h-5 w-5 mr-2 text-primary" />
            本日の出勤状況
          </h3>
          <div className="space-y-3">
            {todayAttendance.slice(0, 5).map((record) => {
              const member = teamMembers.find(m => m.id === record.userId);
              const status = getAttendanceStatus(record);
              return (
                <div key={record.id} className="flex items-center justify-between p-3 rounded-lg border border-border hover:bg-accent transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 bg-primary/10 rounded-full flex items-center justify-center">
                      <UserCheck className="h-4 w-4 text-primary" />
                    </div>
                    <div>
                      <p className="font-medium text-foreground">{member?.name || 'Unknown'}</p>
                      <p className="text-sm text-muted-foreground">
                        {record.clockIn ? format(new Date(record.clockIn), 'HH:mm') : '未出勤'} - 
                        {record.clockOut ? format(new Date(record.clockOut), 'HH:mm') : '勤務中'}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge variant={status.color} className="text-xs">
                      {status.text}
                    </Badge>
                    <span className="text-sm font-medium text-foreground">
                      {calculateWorkingHours(record)}
                    </span>
                  </div>
                </div>
              );
            })}
            {todayAttendance.length === 0 && (
              <div className="text-center py-8">
                <p className="text-muted-foreground">本日の出勤記録がありません</p>
              </div>
            )}
          </div>
        </div>

        <div className="bg-card border border-border rounded-xl shadow-md p-6 animate-fade-in-up">
          <h3 className="text-lg font-semibold mb-4 flex items-center text-foreground">
            <AlertTriangle className="h-5 w-5 mr-2 text-chart-4" />
            重要な通知 ({alerts.length}件)
          </h3>
          <div className="space-y-3">
            {alerts.slice(0, 5).map((alert, index) => (
              <div key={index} className="flex items-start gap-3 p-3 rounded-lg border border-border hover:bg-accent transition-colors">
                <div className="w-2 h-2 bg-chart-4 rounded-full mt-2 flex-shrink-0"></div>
                <div className="flex-1">
                  <p className="font-medium text-foreground text-sm">{alert.title}</p>
                  <p className="text-sm text-muted-foreground">{alert.message}</p>
                  <p className="text-xs text-muted-foreground mt-1">
                    {format(new Date(alert.createdAt), 'MM/dd HH:mm')}
                  </p>
                </div>
              </div>
            ))}
            {alerts.length === 0 && (
              <div className="text-center py-8">
                <p className="text-muted-foreground">新しい通知はありません</p>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Quick Action Cards */}
      <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <button
          onClick={() => window.location.href = '/manager/team'}
          className="bg-card border border-border rounded-xl shadow-md p-6 flex flex-col items-center gap-4 hover:shadow-lg hover:bg-accent transition-all duration-300 animate-fade-in-up group"
        >
          <div className="w-16 h-16 bg-chart-2/10 rounded-full flex items-center justify-center group-hover:bg-chart-2/20 transition-colors">
            <Users className="h-8 w-8 text-chart-2" />
          </div>
          <div className="text-center">
            <h4 className="font-semibold text-foreground">チーム管理</h4>
            <p className="text-sm text-muted-foreground">メンバーの勤怠状況を確認</p>
          </div>
        </button>

        <button
          onClick={() => window.location.href = '/manager/approvals'}
          className="bg-card border border-border rounded-xl shadow-md p-6 flex flex-col items-center gap-4 hover:shadow-lg hover:bg-accent transition-all duration-300 animate-fade-in-up group"
        >
          <div className="w-16 h-16 bg-chart-4/10 rounded-full flex items-center justify-center group-hover:bg-chart-4/20 transition-colors">
            <FileText className="h-8 w-8 text-chart-4" />
          </div>
          <div className="text-center">
            <h4 className="font-semibold text-foreground">承認管理</h4>
            <p className="text-sm text-muted-foreground">
              {pendingLeaveRequests.length + pendingTimeRequests.length}件の申請待ち
            </p>
          </div>
        </button>

        <button
          onClick={() => window.location.href = '/manager/reports'}
          className="bg-card border border-border rounded-xl shadow-md p-6 flex flex-col items-center gap-4 hover:shadow-lg hover:bg-accent transition-all duration-300 animate-fade-in-up group"
        >
          <div className="w-16 h-16 bg-chart-1/10 rounded-full flex items-center justify-center group-hover:bg-chart-1/20 transition-colors">
            <BarChart3 className="h-8 w-8 text-chart-1" />
          </div>
          <div className="text-center">
            <h4 className="font-semibold text-foreground">レポート</h4>
            <p className="text-sm text-muted-foreground">勤怠データの分析と出力</p>
          </div>
        </button>
      </section>
    </div>
  );
};

export default ManagerDashboard;
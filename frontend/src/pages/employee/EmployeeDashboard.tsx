import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { ja } from 'date-fns/locale';
import { 
  Clock, 
  Play, 
  Square, 
  Pause,
  CheckCircle,
  CalendarCheck,
  Clock3,
  CalendarOff
} from 'lucide-react';

import { 
  Button, 
  Card, 
  CardHeader, 
  CardContent, 
  Badge, 
  LoadingSpinner 
} from '../../components/ui';
import { useAuthStore } from '../../stores/authStore';
import { useTimeStore } from '../../stores/timeStore';
import { timeService } from '../../services/timeService';
import type { ClockAction } from '../../types';

const EmployeeDashboard: React.FC = () => {
  const { user } = useAuthStore();
  const { 
    todayRecord, 
    isClockingIn, 
    setTodayRecord, 
    setClockingState,
    getCurrentStatus,
    canClockIn,
    canClockOut,
    canStartBreak,
    canEndBreak
  } = useTimeStore();
  
  const [currentTime, setCurrentTime] = useState(new Date());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    loadTodayRecord();
  }, []);

  const loadTodayRecord = async () => {
    setLoading(true);
    try {
      const response = await timeService.getTodayRecord();
      if (response.success) {
        setTodayRecord(response.data || null);
      }
    } catch (error) {
      console.error('Failed to load today record:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleClockAction = async (type: ClockAction['type']) => {
    setClockingState(true);
    try {
      const action: ClockAction = {
        type,
        timestamp: new Date().toISOString(),
      };

      const response = await timeService.clockAction(action);
      if (response.success && response.data) {
        setTodayRecord(response.data);
      }
    } catch (error) {
      console.error('Clock action failed:', error);
    } finally {
      setClockingState(false);
    }
  };

  const getStatusDisplay = () => {
    const status = getCurrentStatus();
    switch (status) {
      case 'clocked_in':
        return { text: '勤務中', color: 'success' as const };
      case 'on_break':
        return { text: '休憩中', color: 'warning' as const };
      case 'clocked_out':
        return { text: '退勤済み', color: 'default' as const };
      default:
        return { text: '未出勤', color: 'default' as const };
    }
  };

  const calculateWorkingHours = () => {
    if (!todayRecord?.clockIn) return '0:00';
    
    const clockIn = new Date(todayRecord.clockIn);
    const clockOut = todayRecord.clockOut ? new Date(todayRecord.clockOut) : new Date();
    
    let totalMinutes = Math.floor((clockOut.getTime() - clockIn.getTime()) / (1000 * 60));
    
    // Subtract break time if applicable
    if (todayRecord.breakStart && todayRecord.breakEnd) {
      const breakStart = new Date(todayRecord.breakStart);
      const breakEnd = new Date(todayRecord.breakEnd);
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

  const statusDisplay = getStatusDisplay();

  return (
    <div className="min-h-screen bg-background p-4 sm:p-6 lg:p-8">
      {/* Welcome Section */}
      <section className="mb-8 animate-fade-in">
        <h2 className="text-3xl font-bold text-foreground mb-2">おかえりなさい、{user?.name}さん</h2>
        <p className="text-muted-foreground">
          {format(currentTime, 'yyyy年M月d日', { locale: ja })} の勤怠状況です
        </p>
      </section>

      {/* Real-time Clock */}
      <section className="mb-8">
        <div className="bg-card border border-border rounded-2xl shadow-md p-8 text-center animate-fade-in-up">
          <div className="text-4xl font-bold text-foreground mb-2 font-mono">
            {format(currentTime, 'HH:mm:ss')}
          </div>
          <div className="flex items-center justify-center mb-4">
            <Badge variant={statusDisplay.color} className="text-sm px-3 py-1">
              {statusDisplay.text}
            </Badge>
          </div>
          {todayRecord?.clockIn && (
            <div className="text-sm text-muted-foreground space-y-1">
              <p>勤務時間: <span className="font-semibold text-foreground">{calculateWorkingHours()}</span></p>
              <p>出勤: {format(new Date(todayRecord.clockIn), 'HH:mm')}</p>
            </div>
          )}
        </div>
      </section>

      {/* Stats Cards */}
      <section className="grid grid-cols-1 sm:grid-cols-3 gap-6 mb-8">
        <div className="bg-card border border-border rounded-xl shadow-md p-6 flex items-center gap-4 hover:shadow-lg transition-all duration-300 animate-fade-in-up">
          <CalendarCheck className="h-10 w-10 text-chart-2" />
          <div>
            <p className="text-sm text-muted-foreground">今月出勤日数</p>
            <p className="text-2xl font-bold text-foreground">22</p>
          </div>
        </div>
        <div className="bg-card border border-border rounded-xl shadow-md p-6 flex items-center gap-4 hover:shadow-lg transition-all duration-300 animate-fade-in-up">
          <Clock3 className="h-10 w-10 text-chart-4" />
          <div>
            <p className="text-sm text-muted-foreground">遅刻回数</p>
            <p className="text-2xl font-bold text-foreground">1</p>
          </div>
        </div>
        <div className="bg-card border border-border rounded-xl shadow-md p-6 flex items-center gap-4 hover:shadow-lg transition-all duration-300 animate-fade-in-up">
          <CalendarOff className="h-10 w-10 text-chart-5" />
          <div>
            <p className="text-sm text-muted-foreground">有給取得</p>
            <p className="text-2xl font-bold text-foreground">3</p>
          </div>
        </div>
      </section>

      {/* Action Buttons */}
      <section className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-8">
        <button
          onClick={() => handleClockAction('clock_in')}
          disabled={!canClockIn() || isClockingIn}
          className="bg-chart-2 hover:bg-chart-2/90 disabled:opacity-50 text-white font-semibold py-6 px-6 rounded-xl transition-all duration-200 flex items-center justify-center gap-3 text-lg transform hover:translate-y-[-2px] hover:shadow-lg active:translate-y-0 disabled:transform-none animate-fade-in-up"
        >
          <Play className="h-6 w-6" />
          出勤
        </button>

        <button
          onClick={() => handleClockAction('clock_out')}
          disabled={!canClockOut() || isClockingIn}
          className="bg-destructive hover:bg-destructive/90 disabled:opacity-50 text-destructive-foreground font-semibold py-6 px-6 rounded-xl transition-all duration-200 flex items-center justify-center gap-3 text-lg transform hover:translate-y-[-2px] hover:shadow-lg active:translate-y-0 disabled:transform-none animate-fade-in-up"
        >
          <Square className="h-6 w-6" />
          退勤
        </button>

        <button
          onClick={() => handleClockAction('break_start')}
          disabled={!canStartBreak() || isClockingIn}
          className="bg-secondary hover:bg-secondary/80 text-secondary-foreground font-semibold py-6 px-6 rounded-xl transition-all duration-200 flex items-center justify-center gap-3 text-lg border border-border transform hover:translate-y-[-2px] hover:shadow-lg active:translate-y-0 disabled:transform-none animate-fade-in-up"
        >
          <Pause className="h-6 w-6" />
          休憩開始
        </button>

        <button
          onClick={() => handleClockAction('break_end')}
          disabled={!canEndBreak() || isClockingIn}
          className="bg-secondary hover:bg-secondary/80 text-secondary-foreground font-semibold py-6 px-6 rounded-xl transition-all duration-200 flex items-center justify-center gap-3 text-lg border border-border transform hover:translate-y-[-2px] hover:shadow-lg active:translate-y-0 disabled:transform-none animate-fade-in-up"
        >
          <CheckCircle className="h-6 w-6" />
          休憩終了
        </button>
      </section>

      {/* Today's Record Details */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-card border border-border rounded-xl shadow-md p-6 animate-fade-in-up">
          <h3 className="text-lg font-semibold mb-4 flex items-center text-foreground">
            <Clock className="h-5 w-5 mr-2 text-primary" />
            本日の勤怠詳細
          </h3>
          {todayRecord ? (
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-muted-foreground mb-1">出勤時刻</p>
                <p className="font-semibold text-foreground">
                  {todayRecord.clockIn ? format(new Date(todayRecord.clockIn), 'HH:mm') : '未打刻'}
                </p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground mb-1">退勤時刻</p>
                <p className="font-semibold text-foreground">
                  {todayRecord.clockOut ? format(new Date(todayRecord.clockOut), 'HH:mm') : '未打刻'}
                </p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground mb-1">休憩開始</p>
                <p className="font-semibold text-foreground">
                  {todayRecord.breakStart ? format(new Date(todayRecord.breakStart), 'HH:mm') : '未取得'}
                </p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground mb-1">休憩終了</p>
                <p className="font-semibold text-foreground">
                  {todayRecord.breakEnd ? format(new Date(todayRecord.breakEnd), 'HH:mm') : '未取得'}
                </p>
              </div>
            </div>
          ) : (
            <div className="text-center py-8">
              <p className="text-muted-foreground">本日の勤怠記録がありません</p>
            </div>
          )}
        </div>

        <div className="bg-card border border-border rounded-xl shadow-md p-6 animate-fade-in-up">
          <h3 className="text-lg font-semibold mb-4 text-foreground">最近の申請</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center py-2 border-b border-border">
              <span className="text-sm text-foreground">年次有給休暇 (2024/08/15)</span>
              <Badge className="bg-chart-2/10 text-chart-2 border-chart-2/20">承認済み</Badge>
            </div>
            <div className="flex justify-between items-center py-2 border-b border-border">
              <span className="text-sm text-foreground">勤怠修正 (2024/08/12)</span>
              <Badge className="bg-chart-2/10 text-chart-2 border-chart-2/20">承認済み</Badge>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-sm text-foreground">夏季休暇 (2024/08/20-22)</span>
              <Badge className="bg-chart-4/10 text-chart-4 border-chart-4/20">待機中</Badge>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default EmployeeDashboard;
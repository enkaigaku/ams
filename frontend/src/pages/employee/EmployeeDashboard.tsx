import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { ja } from 'date-fns/locale';
import { 
  ClockIcon, 
  PlayIcon, 
  StopIcon, 
  PauseIcon,
  CheckCircleIcon 
} from '@heroicons/react/24/outline';

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
    <div className="p-6 space-y-6">
      <div className="text-center">
        <h1 className="text-2xl font-bold text-white mb-2">
          おかえりなさい、{user?.name}さん
        </h1>
        <p className="text-lg text-gray-300">
          {format(currentTime, 'yyyy年M月d日 HH:mm:ss', { locale: ja })}
        </p>
      </div>

      {/* Status Card */}
      <Card>
        <CardContent className="text-center py-8">
          <div className="mb-4">
            <Badge variant={statusDisplay.color} size="md" className="text-lg px-4 py-2">
              {statusDisplay.text}
            </Badge>
          </div>
          
          {todayRecord?.clockIn && (
            <div className="space-y-2 text-sm text-gray-300">
              <p>出勤時刻: {format(new Date(todayRecord.clockIn), 'HH:mm')}</p>
              {todayRecord.clockOut && (
                <p>退勤時刻: {format(new Date(todayRecord.clockOut), 'HH:mm')}</p>
              )}
              <p>勤務時間: {calculateWorkingHours()}</p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Action Buttons */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <Button
          size="lg"
          className="h-20 text-lg"
          onClick={() => handleClockAction('clock_in')}
          disabled={!canClockIn() || isClockingIn}
          loading={isClockingIn}
        >
          <PlayIcon className="h-6 w-6 mr-2" />
          出勤
        </Button>

        <Button
          size="lg"
          variant="danger"
          className="h-20 text-lg"
          onClick={() => handleClockAction('clock_out')}
          disabled={!canClockOut() || isClockingIn}
          loading={isClockingIn}
        >
          <StopIcon className="h-6 w-6 mr-2" />
          退勤
        </Button>

        <Button
          size="lg"
          variant="secondary"
          className="h-20 text-lg"
          onClick={() => handleClockAction('break_start')}
          disabled={!canStartBreak() || isClockingIn}
          loading={isClockingIn}
        >
          <PauseIcon className="h-6 w-6 mr-2" />
          休憩開始
        </Button>

        <Button
          size="lg"
          variant="secondary"
          className="h-20 text-lg"
          onClick={() => handleClockAction('break_end')}
          disabled={!canEndBreak() || isClockingIn}
          loading={isClockingIn}
        >
          <CheckCircleIcon className="h-6 w-6 mr-2" />
          休憩終了
        </Button>
      </div>

      {/* Today's Summary */}
      <Card>
        <CardHeader>
          <h3 className="text-lg font-medium flex items-center text-white">
            <ClockIcon className="h-5 w-5 mr-2" />
            本日の勤怠
          </h3>
        </CardHeader>
        <CardContent>
          {todayRecord ? (
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-gray-400">出勤時刻</p>
                <p className="font-medium text-white">
                  {todayRecord.clockIn ? format(new Date(todayRecord.clockIn), 'HH:mm') : '未打刻'}
                </p>
              </div>
              <div>
                <p className="text-gray-400">退勤時刻</p>
                <p className="font-medium text-white">
                  {todayRecord.clockOut ? format(new Date(todayRecord.clockOut), 'HH:mm') : '未打刻'}
                </p>
              </div>
              <div>
                <p className="text-gray-400">休憩開始</p>
                <p className="font-medium text-white">
                  {todayRecord.breakStart ? format(new Date(todayRecord.breakStart), 'HH:mm') : '未取得'}
                </p>
              </div>
              <div>
                <p className="text-gray-400">休憩終了</p>
                <p className="font-medium text-white">
                  {todayRecord.breakEnd ? format(new Date(todayRecord.breakEnd), 'HH:mm') : '未取得'}
                </p>
              </div>
            </div>
          ) : (
            <p className="text-center text-gray-400">本日の勤怠記録がありません</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default EmployeeDashboard;
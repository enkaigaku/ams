import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { ja } from 'date-fns/locale';
import { 
  PlayIcon, 
  StopIcon, 
  PauseIcon,
  CheckCircleIcon,
  MapPinIcon
} from '@heroicons/react/24/outline';

import { Button, Card, CardContent, Badge } from '../../components/ui';
import { useTimeStore } from '../../stores/timeStore';
import { timeService } from '../../services/timeService';
import type { ClockAction } from '../../types';

const ClockPage: React.FC = () => {
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
  const [location, setLocation] = useState<{ lat: number; lng: number } | null>(null);

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    // Get current location for clock-in verification
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLocation({
            lat: position.coords.latitude,
            lng: position.coords.longitude
          });
        },
        (error) => {
          console.warn('Location access denied:', error);
        }
      );
    }
  }, []);

  const handleClockAction = async (type: ClockAction['type']) => {
    setClockingState(true);
    try {
      const action: ClockAction = {
        type,
        timestamp: new Date().toISOString(),
        location: location || undefined,
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

  const getMainAction = () => {
    if (canClockIn()) {
      return {
        text: '出勤',
        icon: PlayIcon,
        variant: 'primary' as const,
        action: () => handleClockAction('clock_in'),
        description: '出勤時刻を記録します'
      };
    }
    
    if (canStartBreak()) {
      return {
        text: '休憩開始',
        icon: PauseIcon,
        variant: 'secondary' as const,
        action: () => handleClockAction('break_start'),
        description: '休憩開始時刻を記録します'
      };
    }
    
    if (canEndBreak()) {
      return {
        text: '休憩終了',
        icon: CheckCircleIcon,
        variant: 'secondary' as const,
        action: () => handleClockAction('break_end'),
        description: '休憩終了時刻を記録します'
      };
    }
    
    if (canClockOut()) {
      return {
        text: '退勤',
        icon: StopIcon,
        variant: 'danger' as const,
        action: () => handleClockAction('clock_out'),
        description: '退勤時刻を記録します'
      };
    }

    return null;
  };

  const getStatusBadge = () => {
    const status = getCurrentStatus();
    switch (status) {
      case 'clocked_in':
        return <Badge variant="success" size="md" className="text-base px-4 py-2">勤務中</Badge>;
      case 'on_break':
        return <Badge variant="warning" size="md" className="text-base px-4 py-2">休憩中</Badge>;
      case 'clocked_out':
        return <Badge variant="default" size="md" className="text-base px-4 py-2">退勤済み</Badge>;
      default:
        return <Badge variant="default" size="md" className="text-base px-4 py-2">未出勤</Badge>;
    }
  };

  const mainAction = getMainAction();

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-blue-50 p-4">
      <div className="max-w-md mx-auto space-y-6">
        {/* Time Display */}
        <Card className="text-center bg-white/80 backdrop-blur-sm">
          <CardContent className="py-8">
            <div className="text-4xl font-bold text-foreground mb-2">
              {format(currentTime, 'HH:mm:ss')}
            </div>
            <div className="text-lg text-muted-foreground mb-4">
              {format(currentTime, 'M月d日（E）', { locale: ja })}
            </div>
            {getStatusBadge()}
          </CardContent>
        </Card>

        {/* Main Clock Action */}
        {mainAction && (
          <Card className="bg-white/80 backdrop-blur-sm">
            <CardContent className="py-8 text-center">
              <Button
                size="lg"
                variant={mainAction.variant}
                onClick={mainAction.action}
                disabled={isClockingIn}
                loading={isClockingIn}
                className="w-full h-24 text-2xl font-bold rounded-2xl"
              >
                <mainAction.icon className="h-8 w-8 mr-3" />
                {mainAction.text}
              </Button>
              <p className="text-sm text-muted-foreground mt-3">
                {mainAction.description}
              </p>
            </CardContent>
          </Card>
        )}

        {/* Location Status */}
        {location && (
          <Card className="bg-white/80 backdrop-blur-sm">
            <CardContent className="py-4 text-center">
              <div className="flex items-center justify-center text-sm text-muted-foreground">
                <MapPinIcon className="h-4 w-4 mr-1" />
                位置情報取得済み
              </div>
            </CardContent>
          </Card>
        )}

        {/* Today's Summary */}
        {todayRecord && (
          <Card className="bg-white/80 backdrop-blur-sm">
            <CardContent className="py-4">
              <h3 className="font-medium text-foreground mb-3 text-center">本日の記録</h3>
              <div className="space-y-2 text-sm">
                {todayRecord.clockIn && (
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">出勤</span>
                    <span className="font-medium">
                      {format(new Date(todayRecord.clockIn), 'HH:mm')}
                    </span>
                  </div>
                )}
                {todayRecord.breakStart && (
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">休憩開始</span>
                    <span className="font-medium">
                      {format(new Date(todayRecord.breakStart), 'HH:mm')}
                    </span>
                  </div>
                )}
                {todayRecord.breakEnd && (
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">休憩終了</span>
                    <span className="font-medium">
                      {format(new Date(todayRecord.breakEnd), 'HH:mm')}
                    </span>
                  </div>
                )}
                {todayRecord.clockOut && (
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">退勤</span>
                    <span className="font-medium">
                      {format(new Date(todayRecord.clockOut), 'HH:mm')}
                    </span>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        )}

        {/* Quick Actions */}
        <div className="grid grid-cols-2 gap-3">
          {canClockOut() && (
            <Button
              variant="danger"
              size="lg"
              onClick={() => handleClockAction('clock_out')}
              disabled={isClockingIn}
              className="h-16"
            >
              <StopIcon className="h-5 w-5 mr-2" />
              退勤
            </Button>
          )}
          
          {(canStartBreak() || canEndBreak()) && (
            <Button
              variant="secondary"
              size="lg"
              onClick={() => handleClockAction(canStartBreak() ? 'break_start' : 'break_end')}
              disabled={isClockingIn}
              className="h-16"
            >
              {canStartBreak() ? (
                <>
                  <PauseIcon className="h-5 w-5 mr-2" />
                  休憩開始
                </>
              ) : (
                <>
                  <CheckCircleIcon className="h-5 w-5 mr-2" />
                  休憩終了
                </>
              )}
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ClockPage;
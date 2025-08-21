import React, { useState, useEffect } from 'react';
import { format, startOfMonth, endOfMonth, eachDayOfInterval, isSameMonth, isToday, parseISO } from 'date-fns';
import { ja } from 'date-fns/locale';
import { ChevronLeftIcon, ChevronRightIcon, CalendarIcon } from '@heroicons/react/24/outline';

import { Button, Card, CardHeader, CardContent, Badge, LoadingSpinner } from '../../components/ui';
import { timeService } from '../../services/timeService';
import type { TimeRecord } from '../../types';

const AttendanceHistory: React.FC = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [records, setRecords] = useState<TimeRecord[]>([]);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadMonthlyRecords();
  }, [currentDate]);

  const loadMonthlyRecords = async () => {
    setLoading(true);
    try {
      const year = currentDate.getFullYear();
      const month = currentDate.getMonth() + 1;
      const response = await timeService.getMonthlyRecords(year, month);
      
      if (response.success && response.data) {
        setRecords(response.data);
      }
    } catch (error) {
      console.error('Failed to load monthly records:', error);
    } finally {
      setLoading(false);
    }
  };

  const navigateMonth = (direction: 'prev' | 'next') => {
    const newDate = new Date(currentDate);
    if (direction === 'prev') {
      newDate.setMonth(currentDate.getMonth() - 1);
    } else {
      newDate.setMonth(currentDate.getMonth() + 1);
    }
    setCurrentDate(newDate);
    setSelectedDate(null);
  };

  const getRecordForDate = (date: Date): TimeRecord | undefined => {
    const dateStr = format(date, 'yyyy-MM-dd');
    return records.find(record => record.date === dateStr);
  };

  const getStatusBadge = (record: TimeRecord | undefined) => {
    if (!record) {
      return <Badge variant="default" size="sm">未記録</Badge>;
    }
    
    switch (record.status) {
      case 'present':
        return <Badge variant="success" size="sm">出勤</Badge>;
      case 'late':
        return <Badge variant="warning" size="sm">遅刻</Badge>;
      case 'absent':
        return <Badge variant="danger" size="sm">欠勤</Badge>;
      case 'early_leave':
        return <Badge variant="warning" size="sm">早退</Badge>;
      default:
        return <Badge variant="default" size="sm">未記録</Badge>;
    }
  };

  const calculateWorkingHours = (record: TimeRecord): string => {
    if (!record.clockIn || !record.clockOut) return '0:00';
    
    const clockIn = new Date(record.clockIn);
    const clockOut = new Date(record.clockOut);
    let minutes = Math.floor((clockOut.getTime() - clockIn.getTime()) / (1000 * 60));
    
    // Subtract break time
    if (record.breakStart && record.breakEnd) {
      const breakStart = new Date(record.breakStart);
      const breakEnd = new Date(record.breakEnd);
      const breakMinutes = Math.floor((breakEnd.getTime() - breakStart.getTime()) / (1000 * 60));
      minutes -= breakMinutes;
    }
    
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}:${mins.toString().padStart(2, '0')}`;
  };

  const monthStart = startOfMonth(currentDate);
  const monthEnd = endOfMonth(currentDate);
  const calendarDays = eachDayOfInterval({ start: monthStart, end: monthEnd });

  const selectedRecord = selectedDate ? getRecordForDate(selectedDate) : null;

  return (
    <div className="p-6 max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center">
          <CalendarIcon className="h-6 w-6 mr-2" />
          勤怠履歴
        </h1>
        
        <div className="flex items-center space-x-2">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigateMonth('prev')}
          >
            <ChevronLeftIcon className="h-4 w-4" />
          </Button>
          
          <div className="text-lg font-medium min-w-[120px] text-center">
            {format(currentDate, 'yyyy年M月', { locale: ja })}
          </div>
          
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigateMonth('next')}
          >
            <ChevronRightIcon className="h-4 w-4" />
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Calendar */}
        <Card>
          <CardHeader>
            <h3 className="text-lg font-medium">カレンダー</h3>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="flex justify-center py-8">
                <LoadingSpinner size="md" />
              </div>
            ) : (
              <>
                {/* Weekday headers */}
                <div className="grid grid-cols-7 gap-1 mb-2">
                  {['日', '月', '火', '水', '木', '金', '土'].map((day) => (
                    <div key={day} className="p-2 text-center text-sm font-medium text-gray-500">
                      {day}
                    </div>
                  ))}
                </div>
                
                {/* Calendar days */}
                <div className="grid grid-cols-7 gap-1">
                  {calendarDays.map((date) => {
                    const record = getRecordForDate(date);
                    const isSelected = selectedDate && format(date, 'yyyy-MM-dd') === format(selectedDate, 'yyyy-MM-dd');
                    const isTodayDate = isToday(date);
                    
                    return (
                      <button
                        key={format(date, 'yyyy-MM-dd')}
                        onClick={() => setSelectedDate(date)}
                        className={`
                          p-2 text-sm rounded-md border transition-colors
                          ${isSelected 
                            ? 'bg-primary-100 border-primary-300' 
                            : 'border-gray-200 hover:bg-gray-50'
                          }
                          ${isTodayDate ? 'font-bold text-primary-600' : ''}
                          ${!isSameMonth(date, currentDate) ? 'text-gray-300' : ''}
                        `}
                      >
                        <div className="font-medium">{format(date, 'd')}</div>
                        {record && (
                          <div className="mt-1 flex justify-center">
                            <div className={`w-2 h-2 rounded-full ${
                              record.status === 'present' ? 'bg-green-500' :
                              record.status === 'late' ? 'bg-yellow-500' :
                              record.status === 'absent' ? 'bg-red-500' :
                              'bg-gray-300'
                            }`} />
                          </div>
                        )}
                      </button>
                    );
                  })}
                </div>
              </>
            )}
          </CardContent>
        </Card>

        {/* Selected Date Details */}
        <Card>
          <CardHeader>
            <h3 className="text-lg font-medium">
              {selectedDate ? format(selectedDate, 'M月d日（E）', { locale: ja }) : '日付詳細'}
            </h3>
          </CardHeader>
          <CardContent>
            {selectedDate ? (
              selectedRecord ? (
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <span className="text-gray-600">状態</span>
                    {getStatusBadge(selectedRecord)}
                  </div>
                  
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="text-gray-500 mb-1">出勤時刻</p>
                      <p className="font-medium">
                        {selectedRecord.clockIn 
                          ? format(parseISO(selectedRecord.clockIn), 'HH:mm')
                          : '未打刻'
                        }
                      </p>
                    </div>
                    
                    <div>
                      <p className="text-gray-500 mb-1">退勤時刻</p>
                      <p className="font-medium">
                        {selectedRecord.clockOut 
                          ? format(parseISO(selectedRecord.clockOut), 'HH:mm')
                          : '未打刻'
                        }
                      </p>
                    </div>
                    
                    <div>
                      <p className="text-gray-500 mb-1">休憩開始</p>
                      <p className="font-medium">
                        {selectedRecord.breakStart 
                          ? format(parseISO(selectedRecord.breakStart), 'HH:mm')
                          : '未取得'
                        }
                      </p>
                    </div>
                    
                    <div>
                      <p className="text-gray-500 mb-1">休憩終了</p>
                      <p className="font-medium">
                        {selectedRecord.breakEnd 
                          ? format(parseISO(selectedRecord.breakEnd), 'HH:mm')
                          : '未取得'
                        }
                      </p>
                    </div>
                  </div>
                  
                  <div className="pt-4 border-t">
                    <div className="flex justify-between">
                      <span className="text-gray-600">勤務時間</span>
                      <span className="font-bold text-lg">
                        {calculateWorkingHours(selectedRecord)}
                      </span>
                    </div>
                  </div>
                  
                  {selectedRecord.notes && (
                    <div className="pt-4 border-t">
                      <p className="text-gray-500 mb-1">備考</p>
                      <p className="text-sm">{selectedRecord.notes}</p>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center text-gray-500 py-8">
                  この日の勤怠記録はありません
                </div>
              )
            ) : (
              <div className="text-center text-gray-500 py-8">
                カレンダーから日付を選択してください
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Monthly Summary */}
      <Card>
        <CardHeader>
          <h3 className="text-lg font-medium">月間サマリー</h3>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
            <div>
              <p className="text-2xl font-bold text-primary-600">
                {records.filter(r => r.status === 'present').length}
              </p>
              <p className="text-sm text-gray-600">出勤日数</p>
            </div>
            <div>
              <p className="text-2xl font-bold text-yellow-600">
                {records.filter(r => r.status === 'late').length}
              </p>
              <p className="text-sm text-gray-600">遅刻回数</p>
            </div>
            <div>
              <p className="text-2xl font-bold text-red-600">
                {records.filter(r => r.status === 'absent').length}
              </p>
              <p className="text-sm text-gray-600">欠勤日数</p>
            </div>
            <div>
              <p className="text-2xl font-bold text-green-600">
                {records.reduce((total, record) => {
                  const hours = calculateWorkingHours(record);
                  const [h, m] = hours.split(':').map(Number);
                  return total + h + (m / 60);
                }, 0).toFixed(1)}h
              </p>
              <p className="text-sm text-gray-600">総勤務時間</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default AttendanceHistory;
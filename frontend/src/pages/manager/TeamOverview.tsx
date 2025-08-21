import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { ja } from 'date-fns/locale';
import { 
  UsersIcon, 
  ChevronLeftIcon,
  ChevronRightIcon,
  UserIcon
} from '@heroicons/react/24/outline';

import { 
  Card, 
  CardContent,
  Badge,
  LoadingSpinner,
  Button 
} from '../../components/ui';
import { managerService } from '../../services/managerService';
import type { TimeRecord, User } from '../../types';

const TeamOverview: React.FC = () => {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [teamMembers, setTeamMembers] = useState<User[]>([]);
  const [attendanceData, setAttendanceData] = useState<TimeRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  useEffect(() => {
    loadData();
  }, [selectedDate]);

  const loadData = async () => {
    setLoading(true);
    try {
      const dateStr = format(selectedDate, 'yyyy-MM-dd');
      const [teamResponse, attendanceResponse] = await Promise.all([
        managerService.getTeamMembers(),
        managerService.getTeamAttendance(dateStr),
      ]);

      if (teamResponse.success && teamResponse.data) {
        setTeamMembers(teamResponse.data);
      }
      if (attendanceResponse.success && attendanceResponse.data) {
        setAttendanceData(attendanceResponse.data);
      }
    } catch (error) {
      console.error('Failed to load team data:', error);
    } finally {
      setLoading(false);
    }
  };

  const navigateDate = (direction: 'prev' | 'next') => {
    const newDate = new Date(selectedDate);
    if (direction === 'prev') {
      newDate.setDate(selectedDate.getDate() - 1);
    } else {
      newDate.setDate(selectedDate.getDate() + 1);
    }
    setSelectedDate(newDate);
  };

  const getAttendanceForMember = (memberId: string): TimeRecord | undefined => {
    return attendanceData.find(record => record.userId === memberId);
  };

  const getStatusDisplay = (record: TimeRecord | undefined) => {
    if (!record) {
      return { text: '未出勤', color: 'default' as const, hours: '0:00' };
    }

    const status = record.status;
    let hours = '0:00';
    
    if (record.clockIn) {
      const clockIn = new Date(record.clockIn);
      const clockOut = record.clockOut ? new Date(record.clockOut) : new Date();
      let minutes = Math.floor((clockOut.getTime() - clockIn.getTime()) / (1000 * 60));
      
      if (record.breakStart && record.breakEnd) {
        const breakStart = new Date(record.breakStart);
        const breakEnd = new Date(record.breakEnd);
        const breakMinutes = Math.floor((breakEnd.getTime() - breakStart.getTime()) / (1000 * 60));
        minutes -= breakMinutes;
      }
      
      const h = Math.floor(minutes / 60);
      const m = minutes % 60;
      hours = `${h}:${m.toString().padStart(2, '0')}`;
    }

    switch (status) {
      case 'present':
        return { 
          text: record.clockOut ? '退勤済み' : '勤務中', 
          color: record.clockOut ? 'success' as const : 'info' as const,
          hours 
        };
      case 'late':
        return { text: '遅刻', color: 'warning' as const, hours };
      case 'absent':
        return { text: '欠勤', color: 'danger' as const, hours: '0:00' };
      case 'early_leave':
        return { text: '早退', color: 'warning' as const, hours };
      default:
        return { text: '未出勤', color: 'default' as const, hours: '0:00' };
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center">
          <UsersIcon className="h-6 w-6 mr-2" />
          チーム出勤状況
        </h1>

        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigateDate('prev')}
            >
              <ChevronLeftIcon className="h-4 w-4" />
            </Button>
            
            <div className="text-lg font-medium min-w-[140px] text-center">
              {format(selectedDate, 'yyyy年M月d日（E）', { locale: ja })}
            </div>
            
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigateDate('next')}
            >
              <ChevronRightIcon className="h-4 w-4" />
            </Button>
          </div>

          <div className="flex items-center bg-gray-100 rounded-lg p-1">
            <button
              onClick={() => setViewMode('grid')}
              className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                viewMode === 'grid' 
                  ? 'bg-white text-gray-900 shadow-sm' 
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              グリッド
            </button>
            <button
              onClick={() => setViewMode('list')}
              className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                viewMode === 'list' 
                  ? 'bg-white text-gray-900 shadow-sm' 
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              リスト
            </button>
          </div>
        </div>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-primary-600">
              {teamMembers.length}
            </div>
            <div className="text-sm text-gray-600">総メンバー数</div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-green-600">
              {attendanceData.filter(r => r.status === 'present').length}
            </div>
            <div className="text-sm text-gray-600">出勤者数</div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-yellow-600">
              {attendanceData.filter(r => r.status === 'late').length}
            </div>
            <div className="text-sm text-gray-600">遅刻者数</div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-red-600">
              {teamMembers.length - attendanceData.length}
            </div>
            <div className="text-sm text-gray-600">欠勤者数</div>
          </CardContent>
        </Card>
      </div>

      {/* Team Members Display */}
      {viewMode === 'grid' ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {teamMembers.map((member) => {
            const attendance = getAttendanceForMember(member.id);
            const status = getStatusDisplay(attendance);
            
            return (
              <Card key={member.id} className="hover:shadow-md transition-shadow">
                <CardContent className="p-6">
                  <div className="flex items-center space-x-3 mb-4">
                    <div className="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center">
                      <UserIcon className="h-6 w-6 text-gray-600" />
                    </div>
                    <div className="flex-1">
                      <h3 className="font-medium text-gray-900">{member.name}</h3>
                      <p className="text-sm text-gray-600">{member.department}</p>
                    </div>
                    <Badge variant={status.color} size="sm">
                      {status.text}
                    </Badge>
                  </div>

                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-gray-500">出勤時刻:</span>
                      <span className="font-medium">
                        {attendance?.clockIn 
                          ? format(new Date(attendance.clockIn), 'HH:mm')
                          : '--:--'
                        }
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-500">退勤時刻:</span>
                      <span className="font-medium">
                        {attendance?.clockOut 
                          ? format(new Date(attendance.clockOut), 'HH:mm')
                          : '--:--'
                        }
                      </span>
                    </div>
                    <div className="flex justify-between border-t pt-2">
                      <span className="text-gray-500">勤務時間:</span>
                      <span className="font-bold text-primary-600">{status.hours}</span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      ) : (
        <Card>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      従業員
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      出勤時刻
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      退勤時刻
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      勤務時間
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      状態
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {teamMembers.map((member) => {
                    const attendance = getAttendanceForMember(member.id);
                    const status = getStatusDisplay(attendance);
                    
                    return (
                      <tr key={member.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center mr-3">
                              <UserIcon className="h-4 w-4 text-gray-600" />
                            </div>
                            <div>
                              <div className="text-sm font-medium text-gray-900">{member.name}</div>
                              <div className="text-sm text-gray-500">{member.department}</div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {attendance?.clockIn 
                            ? format(new Date(attendance.clockIn), 'HH:mm')
                            : '--:--'
                          }
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {attendance?.clockOut 
                            ? format(new Date(attendance.clockOut), 'HH:mm')
                            : '--:--'
                          }
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-primary-600">
                          {status.hours}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <Badge variant={status.color} size="sm">
                            {status.text}
                          </Badge>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default TeamOverview;
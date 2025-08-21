import React, { useState, useEffect } from 'react';
import { format, startOfMonth, endOfMonth } from 'date-fns';
import { 
  ChartBarIcon,
  ArrowDownTrayIcon,
  CalendarIcon,
  UsersIcon,
  ClockIcon,
  DocumentChartBarIcon
} from '@heroicons/react/24/outline';

import { 
  Card, 
  CardHeader, 
  CardContent,
  Button,
  Input,
  LoadingSpinner,
  Badge
} from '../../components/ui';
import { managerService } from '../../services/managerService';
import type { MonthlyReport, User } from '../../types';

const ReportsPage: React.FC = () => {
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [startDate, setStartDate] = useState(format(startOfMonth(new Date()), 'yyyy-MM-dd'));
  const [endDate, setEndDate] = useState(format(endOfMonth(new Date()), 'yyyy-MM-dd'));
  const [selectedUserId, setSelectedUserId] = useState<string>('');
  const [teamMembers, setTeamMembers] = useState<User[]>([]);
  const [monthlyReports, setMonthlyReports] = useState<MonthlyReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    loadData();
  }, [selectedYear, selectedMonth, selectedUserId]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [teamResponse, reportsResponse] = await Promise.all([
        managerService.getTeamMembers(),
        managerService.getMonthlyReport(selectedYear, selectedMonth, selectedUserId || undefined),
      ]);

      if (teamResponse.success && teamResponse.data) {
        setTeamMembers(teamResponse.data);
      }
      if (reportsResponse.success && reportsResponse.data) {
        setMonthlyReports(reportsResponse.data);
      }
    } catch (error) {
      console.error('Failed to load reports data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleExportCSV = async () => {
    setExporting(true);
    try {
      const response = await managerService.exportTeamReport(startDate, endDate, 'csv');
      if (response.success && response.data?.downloadUrl) {
        // Create a temporary link to download the file
        const link = document.createElement('a');
        link.href = response.data.downloadUrl;
        link.download = `attendance-report-${format(new Date(), 'yyyyMMdd')}.csv`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }
    } catch (error) {
      console.error('Failed to export CSV:', error);
      alert('CSVエクスポートに失敗しました');
    } finally {
      setExporting(false);
    }
  };

  const getMemberName = (userId: string): string => {
    const member = teamMembers.find(m => m.id === userId);
    return member?.name || 'Unknown User';
  };

  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: 5 }, (_, i) => currentYear - i);
  const months = Array.from({ length: 12 }, (_, i) => i + 1);

  const totalStats = monthlyReports.reduce(
    (acc, report) => ({
      totalDays: acc.totalDays + (report.stats?.totalDays || 0),
      presentDays: acc.presentDays + (report.stats?.presentDays || 0),
      lateDays: acc.lateDays + (report.stats?.lateDays || 0),
      absentDays: acc.absentDays + (report.stats?.absentDays || 0),
      totalHours: acc.totalHours + (report.stats?.totalHours || 0),
    }),
    { totalDays: 0, presentDays: 0, lateDays: 0, absentDays: 0, totalHours: 0 }
  );

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center">
          <ChartBarIcon className="h-6 w-6 mr-2" />
          レポート・エクスポート
        </h1>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <h3 className="text-lg font-medium">フィルター設定</h3>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">年</label>
              <select
                value={selectedYear}
                onChange={(e) => setSelectedYear(Number(e.target.value))}
                className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              >
                {years.map(year => (
                  <option key={year} value={year}>{year}年</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">月</label>
              <select
                value={selectedMonth}
                onChange={(e) => setSelectedMonth(Number(e.target.value))}
                className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              >
                {months.map(month => (
                  <option key={month} value={month}>{month}月</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">従業員</label>
              <select
                value={selectedUserId}
                onChange={(e) => setSelectedUserId(e.target.value)}
                className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="">全従業員</option>
                {teamMembers.map(member => (
                  <option key={member.id} value={member.id}>{member.name}</option>
                ))}
              </select>
            </div>

            <div className="flex items-end">
              <Button onClick={loadData} disabled={loading}>
                {loading ? <LoadingSpinner size="sm" className="mr-2" /> : <ChartBarIcon className="h-4 w-4 mr-2" />}
                更新
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Export Section */}
      <Card>
        <CardHeader>
          <h3 className="text-lg font-medium">データエクスポート</h3>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
            <Input
              label="開始日"
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
            />
            <Input
              label="終了日"
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
            />
            <Button
              onClick={handleExportCSV}
              loading={exporting}
              disabled={exporting}
              className="h-10"
            >
              <ArrowDownTrayIcon className="h-4 w-4 mr-2" />
              CSV出力
            </Button>
          </div>
          <p className="text-sm text-gray-500 mt-2">
            指定した期間の勤怠データをCSV形式でダウンロードします
          </p>
        </CardContent>
      </Card>

      {/* Summary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card>
          <CardContent className="p-6 text-center">
            <div className="flex items-center justify-center mb-2">
              <CalendarIcon className="h-8 w-8 text-primary-600" />
            </div>
            <div className="text-2xl font-bold text-gray-900">{totalStats.presentDays}</div>
            <div className="text-sm text-gray-600">総出勤日数</div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 text-center">
            <div className="flex items-center justify-center mb-2">
              <ClockIcon className="h-8 w-8 text-green-600" />
            </div>
            <div className="text-2xl font-bold text-gray-900">{totalStats.totalHours.toFixed(1)}</div>
            <div className="text-sm text-gray-600">総勤務時間</div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 text-center">
            <div className="flex items-center justify-center mb-2">
              <UsersIcon className="h-8 w-8 text-yellow-600" />
            </div>
            <div className="text-2xl font-bold text-gray-900">{totalStats.lateDays}</div>
            <div className="text-sm text-gray-600">遅刻回数</div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 text-center">
            <div className="flex items-center justify-center mb-2">
              <DocumentChartBarIcon className="h-8 w-8 text-red-600" />
            </div>
            <div className="text-2xl font-bold text-gray-900">{totalStats.absentDays}</div>
            <div className="text-sm text-gray-600">欠勤回数</div>
          </CardContent>
        </Card>
      </div>

      {/* Monthly Reports Table */}
      <Card>
        <CardHeader>
          <h3 className="text-lg font-medium">
            {selectedYear}年{selectedMonth}月のレポート
            {selectedUserId && ` - ${getMemberName(selectedUserId)}`}
          </h3>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex justify-center py-8">
              <LoadingSpinner size="md" />
            </div>
          ) : monthlyReports.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              データがありません
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      従業員
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      出勤日数
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      総勤務時間
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      平均勤務時間
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      遅刻回数
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      欠勤回数
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {monthlyReports.map((report) => (
                    <tr key={report.userId} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="font-medium text-gray-900">
                          {getMemberName(report.userId)}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {report.stats?.presentDays || 0}日
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {(report.stats?.totalHours || 0).toFixed(1)}時間
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {(report.stats?.averageHours || 0).toFixed(1)}時間
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Badge 
                          variant={report.stats?.lateDays ? 'warning' : 'success'}
                          size="sm"
                        >
                          {report.stats?.lateDays || 0}回
                        </Badge>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Badge 
                          variant={report.stats?.absentDays ? 'danger' : 'success'}
                          size="sm"
                        >
                          {report.stats?.absentDays || 0}回
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Export Instructions */}
      <Card>
        <CardHeader>
          <h3 className="text-lg font-medium">エクスポート機能について</h3>
        </CardHeader>
        <CardContent className="text-sm text-gray-600 space-y-2">
          <p>• CSV形式で勤怠データをエクスポートできます</p>
          <p>• 期間を指定して必要なデータのみを出力可能です</p>
          <p>• エクスポートされるデータには以下が含まれます：</p>
          <ul className="ml-6 list-disc">
            <li>従業員情報（名前、部署、社員番号）</li>
            <li>出勤・退勤時刻</li>
            <li>休憩時間</li>
            <li>勤務時間</li>
            <li>出勤状況（出勤・遅刻・欠勤・早退）</li>
          </ul>
        </CardContent>
      </Card>
    </div>
  );
};

export default ReportsPage;
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { 
  HomeIcon, 
  ClockIcon, 
  CalendarIcon, 
  DocumentTextIcon,
  UsersIcon,
  ChartBarIcon,
  Bars3Icon,
  XMarkIcon,
  ArrowRightOnRectangleIcon,
  UserIcon
} from '@heroicons/react/24/outline';

import { useAuthStore } from '../../stores/authStore';
import { Button } from '../ui';

interface AppLayoutProps {
  children: React.ReactNode;
}

const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user, logout, isManager } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const employeeNavItems = [
    { name: 'ダッシュボード', href: '/employee', icon: HomeIcon },
    { name: '打刻', href: '/employee/clock', icon: ClockIcon },
    { name: '勤怠履歴', href: '/employee/history', icon: CalendarIcon },
    { name: '申請', href: '/employee/requests', icon: DocumentTextIcon },
  ];

  const managerNavItems = [
    { name: 'ダッシュボード', href: '/manager', icon: HomeIcon },
    { name: 'チーム状況', href: '/manager/team', icon: UsersIcon },
    { name: '承認待ち', href: '/manager/approvals', icon: DocumentTextIcon },
    { name: 'レポート', href: '/manager/reports', icon: ChartBarIcon },
  ];

  const navItems = isManager() ? managerNavItems : employeeNavItems;

  return (
    <div className="flex h-screen bg-gray-900">
      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div 
          className="fixed inset-0 bg-gray-600 bg-opacity-75 lg:hidden z-20"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <div className={`
        fixed inset-y-0 left-0 z-30 w-64 bg-gray-800 shadow-lg transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:inset-0
        ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        <div className="flex flex-col h-full">
          {/* Logo */}
          <div className="flex items-center justify-between h-16 px-4 bg-blue-600 text-white">
            <h1 className="text-lg font-semibold">打刻管理システム</h1>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setSidebarOpen(false)}
              className="lg:hidden text-white hover:bg-blue-700"
            >
              <XMarkIcon className="h-5 w-5" />
            </Button>
          </div>

          {/* User Info */}
          <div className="p-4 border-b border-gray-700">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <UserIcon className="h-8 w-8 text-gray-400" />
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-white">{user?.name}</p>
                <p className="text-sm text-gray-400">{user?.department}</p>
                <p className="text-xs text-gray-500">
                  {isManager() ? '管理者' : '従業員'}
                </p>
              </div>
            </div>
          </div>

          {/* Navigation */}
          <nav className="flex-1 px-4 py-4 space-y-2">
            {navItems.map((item) => (
              <Link
                key={item.name}
                to={item.href}
                className="flex items-center px-3 py-2 text-sm font-medium text-gray-300 rounded-md hover:text-white hover:bg-gray-700 transition-colors"
                onClick={() => setSidebarOpen(false)}
              >
                <item.icon className="mr-3 h-5 w-5" />
                {item.name}
              </Link>
            ))}
          </nav>

          {/* Logout */}
          <div className="p-4 border-t border-gray-700">
            <Button
              variant="ghost"
              onClick={handleLogout}
              className="w-full justify-start text-gray-300 hover:text-white hover:bg-gray-700"
            >
              <ArrowRightOnRectangleIcon className="mr-3 h-5 w-5" />
              ログアウト
            </Button>
          </div>
        </div>
      </div>

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top bar */}
        <header className="bg-gray-800 shadow-sm border-b border-gray-700 lg:hidden">
          <div className="flex items-center justify-between h-16 px-4">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setSidebarOpen(true)}
              className="text-gray-300 hover:text-white"
            >
              <Bars3Icon className="h-6 w-6" />
            </Button>
            <h1 className="text-lg font-semibold text-white">
              打刻管理システム
            </h1>
            <div className="w-10" />
          </div>
        </header>

        {/* Main content area */}
        <main className="flex-1 overflow-auto bg-gray-900">
          <div className="h-full">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
};

export default AppLayout;
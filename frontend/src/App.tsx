import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Pages
import LoginPage from './pages/auth/LoginPage';

// Employee pages
import EmployeeDashboard from './pages/employee/EmployeeDashboard';
import ClockPage from './pages/employee/ClockPage';
import AttendanceHistory from './pages/employee/AttendanceHistory';
import RequestsPage from './pages/employee/RequestsPage';

// Manager pages
import ManagerDashboard from './pages/manager/ManagerDashboard';
import TeamOverview from './pages/manager/TeamOverview';
import ApprovalQueue from './pages/manager/ApprovalQueue';
import ReportsPage from './pages/manager/ReportsPage';

// Components
import AuthGuard from './components/layout/AuthGuard';
import AppLayout from './components/layout/AppLayout';

// Store
import { useAuthStore } from './stores/authStore';

const App: React.FC = () => {
  const { isAuthenticated, user } = useAuthStore();

  // If not authenticated, show login page
  if (!isAuthenticated || !user) {
    return (
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    );
  }

  return (
    <Router>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<LoginPage />} />

        {/* Protected Employee Routes */}
        <Route
          path="/employee/*"
          element={
            <AuthGuard requiredRole="EMPLOYEE">
              <AppLayout>
                <Routes>
                  <Route index element={<EmployeeDashboard />} />
                  <Route path="clock" element={<ClockPage />} />
                  <Route path="history" element={<AttendanceHistory />} />
                  <Route path="requests" element={<RequestsPage />} />
                </Routes>
              </AppLayout>
            </AuthGuard>
          }
        />

        {/* Protected Manager Routes */}
        <Route
          path="/manager/*"
          element={
            <AuthGuard requiredRole="MANAGER">
              <AppLayout>
                <Routes>
                  <Route index element={<ManagerDashboard />} />
                  <Route path="team" element={<TeamOverview />} />
                  <Route path="approvals" element={<ApprovalQueue />} />
                  <Route path="reports" element={<ReportsPage />} />
                </Routes>
              </AppLayout>
            </AuthGuard>
          }
        />

        {/* Root redirect based on role */}
        <Route
          path="/"
          element={
            user.role === 'MANAGER' 
              ? <Navigate to="/manager" replace />
              : <Navigate to="/employee" replace />
          }
        />

        {/* Catch all redirect */}
        <Route
          path="*"
          element={
            user.role === 'MANAGER' 
              ? <Navigate to="/manager" replace />
              : <Navigate to="/employee" replace />
          }
        />
      </Routes>
    </Router>
  );
};

export default App;

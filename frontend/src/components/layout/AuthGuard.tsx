import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';
import { LoadingSpinner } from '../ui';

interface AuthGuardProps {
  children: React.ReactNode;
  requiredRole?: 'EMPLOYEE' | 'MANAGER';
  fallbackPath?: string;
}

const AuthGuard: React.FC<AuthGuardProps> = ({ 
  children, 
  requiredRole,
  fallbackPath = '/login'
}) => {
  const { isAuthenticated, user } = useAuthStore();
  const location = useLocation();

  // Show loading while checking authentication
  if (!isAuthenticated && !user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated || !user) {
    return (
      <Navigate 
        to={fallbackPath} 
        state={{ from: location }} 
        replace 
      />
    );
  }

  // Check role-based access
  if (requiredRole && user.role !== requiredRole) {
    // Redirect managers to manager dashboard, employees to employee dashboard
    const redirectPath = user.role === 'MANAGER' ? '/manager' : '/employee';
    return <Navigate to={redirectPath} replace />;
  }

  return <>{children}</>;
};

export default AuthGuard;
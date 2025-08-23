import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import { UserCheck, User, Lock, Loader2 } from 'lucide-react';

import { useAuthStore } from '../../stores/authStore';
import { authService } from '../../services/authService';

const loginSchema = z.object({
  employeeId: z.string().min(1, '社員番号を入力してください'),
  password: z.string().min(1, 'パスワードを入力してください'),
});

type LoginFormData = z.infer<typeof loginSchema>;

const LoginPage: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await authService.login(data);
      
      if (response.success && response.data) {
        login(response.data.user, response.data.accessToken, response.data.refreshToken);
        
        // Navigate based on user role
        if (response.data.user.role === 'MANAGER') {
          navigate('/manager');
        } else {
          navigate('/employee');
        }
      } else {
        setError(response.error || 'ログインに失敗しました');
      }
    } catch (err) {
      setError('ログインに失敗しました。再度お試しください。');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-background to-muted px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md animate-fade-in">
        <div className="bg-card border border-border rounded-2xl shadow-2xl p-8 space-y-6 hover:shadow-xl transition-all duration-300">
          {/* Logo/Title Section */}
          <div className="text-center space-y-2">
            <div className="w-16 h-16 mx-auto mb-4 bg-primary/10 rounded-full flex items-center justify-center border-2 border-primary/20">
              <UserCheck className="h-8 w-8 text-primary" />
            </div>
            <h1 className="text-3xl font-bold text-foreground">勤怠管理システム</h1>
          </div>

          {error && (
            <div className="bg-destructive/10 border border-destructive/20 rounded-lg p-4 animate-fade-in">
              <p className="text-destructive text-sm text-center font-medium">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <div className="space-y-4">
              <div>
                <label className="block text-foreground text-sm font-medium mb-2">
                  社員番号
                </label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <input
                    {...register('employeeId')}
                    type="text"
                    placeholder="例: MGR001"
                    disabled={isLoading}
                    autoComplete="username"
                    className={`w-full pl-10 pr-4 py-3 bg-input border border-border rounded-lg text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:border-primary transition-all duration-200 ${
                      errors.employeeId 
                        ? 'border-destructive focus:ring-destructive' 
                        : 'hover:border-primary/50'
                    }`}
                  />
                </div>
                {errors.employeeId && (
                  <p className="mt-2 text-sm text-destructive font-medium animate-fade-in">{errors.employeeId.message}</p>
                )}
              </div>

              <div>
                <label className="block text-foreground text-sm font-medium mb-2">
                  パスワード
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <input
                    {...register('password')}
                    type="password"
                    placeholder="••••••••"
                    disabled={isLoading}
                    autoComplete="current-password"
                    className={`w-full pl-10 pr-4 py-3 bg-input border border-border rounded-lg text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:border-primary transition-all duration-200 ${
                      errors.password 
                        ? 'border-destructive focus:ring-destructive' 
                        : 'hover:border-primary/50'
                    }`}
                  />
                </div>
                {errors.password && (
                  <p className="mt-2 text-sm text-destructive font-medium animate-fade-in">{errors.password.message}</p>
                )}
              </div>
            </div>

            <div className="space-y-4">
              <button
                type="submit"
                disabled={isLoading}
                className="w-full bg-primary hover:bg-primary/90 disabled:opacity-50 text-primary-foreground font-semibold py-3 px-4 rounded-lg transition-all duration-200 flex items-center justify-center transform hover:translate-y-[-2px] hover:shadow-lg active:translate-y-0 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="animate-spin -ml-1 mr-2 h-4 w-4" />
                    ログイン中...
                  </>
                ) : (
                  'ログイン'
                )}
              </button>
              
              <div className="text-center">
                <p className="text-muted-foreground text-xs">
                  営業時間: 平日 7:00-20:00
                </p>
                <p className="text-muted-foreground text-xs mt-1">
                  サポートが必要な場合は管理者にお問い合わせください
                </p>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
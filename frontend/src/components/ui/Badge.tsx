import React from 'react';
import { cn } from '../../utils/cn';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'default' | 'success' | 'warning' | 'danger' | 'info' | 'secondary' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const Badge: React.FC<BadgeProps> = ({ 
  children, 
  variant = 'default', 
  size = 'md', 
  className 
}) => {
  const baseStyles = 'inline-flex items-center justify-center font-medium border transition-colors';
  
  const variants = {
    default: 'bg-muted/50 text-foreground border-border',
    success: 'bg-chart-2/10 text-chart-2 border-chart-2/20',
    warning: 'bg-chart-4/10 text-chart-4 border-chart-4/20',
    danger: 'bg-destructive/10 text-destructive border-destructive/20',
    info: 'bg-chart-3/10 text-chart-3 border-chart-3/20',
    secondary: 'bg-secondary/50 text-secondary-foreground border-border',
    outline: 'bg-transparent text-foreground border-border'
  };

  const sizes = {
    sm: 'px-2 py-0.5 text-xs rounded-md',
    md: 'px-2.5 py-1 text-sm rounded-lg',
    lg: 'px-3 py-1.5 text-sm rounded-xl'
  };

  return (
    <span className={cn(
      baseStyles,
      variants[variant],
      sizes[size],
      className
    )}>
      {children}
    </span>
  );
};

export default Badge;
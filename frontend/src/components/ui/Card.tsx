import React from 'react';
import { cn } from '../../utils/cn';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  padding?: 'none' | 'sm' | 'md' | 'lg';
  hover?: boolean;
}

interface CardHeaderProps {
  children: React.ReactNode;
  className?: string;
}

interface CardContentProps {
  children: React.ReactNode;
  className?: string;
}

interface CardFooterProps {
  children: React.ReactNode;
  className?: string;
}

export const Card: React.FC<CardProps> = ({ 
  children, 
  className, 
  padding = 'md',
  hover = true
}) => {
  const paddingStyles = {
    none: '',
    sm: 'p-3',
    md: 'p-6',
    lg: 'p-8'
  };

  return (
    <div className={cn(
      'bg-card border border-border rounded-xl shadow-md transition-all duration-300 animate-fade-in-up',
      hover && 'hover:shadow-lg',
      paddingStyles[padding],
      className
    )}>
      {children}
    </div>
  );
};

export const CardHeader: React.FC<CardHeaderProps> = ({ children, className }) => {
  return (
    <div className={cn('mb-4 pb-2', className)}>
      {children}
    </div>
  );
};

export const CardContent: React.FC<CardContentProps> = ({ children, className }) => {
  return (
    <div className={cn('text-card-foreground', className)}>
      {children}
    </div>
  );
};

export const CardFooter: React.FC<CardFooterProps> = ({ children, className }) => {
  return (
    <div className={cn('mt-4 pt-4 border-t border-border', className)}>
      {children}
    </div>
  );
};
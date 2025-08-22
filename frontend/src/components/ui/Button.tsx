import React from 'react';
import { cn } from '../../utils/cn';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
  children: React.ReactNode;
}

const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  loading = false,
  className,
  disabled,
  children,
  ...props
}) => {
  const baseStyles = 'inline-flex items-center justify-center font-semibold transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transform hover:translate-y-[-2px] hover:shadow-lg active:translate-y-0 disabled:transform-none';
  
  const variants = {
    primary: 'bg-primary hover:bg-primary/90 text-primary-foreground shadow-md',
    secondary: 'bg-secondary hover:bg-secondary/80 text-secondary-foreground border border-border shadow-md',
    danger: 'bg-destructive hover:bg-destructive/90 text-destructive-foreground shadow-md',
    ghost: 'bg-transparent hover:bg-accent text-foreground',
    outline: 'bg-transparent border border-border hover:bg-accent text-foreground'
  };

  const sizes = {
    sm: 'px-3 py-1.5 text-sm rounded-md min-h-8',
    md: 'px-4 py-2 text-sm rounded-lg min-h-10',
    lg: 'px-6 py-3 text-base rounded-xl min-h-12'
  };

  return (
    <button
      className={cn(
        baseStyles,
        variants[variant],
        sizes[size],
        className
      )}
      disabled={disabled || loading}
      {...props}
    >
      {loading && (
        <svg
          className="animate-spin -ml-1 mr-2 h-4 w-4"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            className="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            strokeWidth="4"
          />
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
      )}
      {children}
    </button>
  );
};

export default Button;
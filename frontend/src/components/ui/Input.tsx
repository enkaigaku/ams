import React, { forwardRef } from 'react';
import { cn } from '../../utils/cn';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helper?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helper, leftIcon, rightIcon, className, type = 'text', ...props }, ref) => {
    const hasError = Boolean(error);
    
    return (
      <div className="w-full">
        {label && (
          <label className="block text-sm font-medium text-muted-foreground mb-1">
            {label}
            {props.required && <span className="text-destructive ml-1">*</span>}
          </label>
        )}
        
        <div className="relative">
          {leftIcon && (
            <div className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground">
              {leftIcon}
            </div>
          )}
          
          <input
            ref={ref}
            type={type}
            className={cn(
              'block w-full px-3 py-2 border rounded-md shadow-sm placeholder-muted-foreground',
              'focus:outline-none focus:ring-2 focus:border-transparent',
              leftIcon ? 'pl-10' : 'pl-3',
              rightIcon ? 'pr-10' : 'pr-3',
              hasError
                ? 'border-destructive/50 focus:ring-destructive'
                : 'border-border focus:ring-primary',
              props.disabled ? 'bg-muted/50 text-muted-foreground cursor-not-allowed' : '',
              className
            )}
            {...props}
          />
          
          {rightIcon && (
            <div className="absolute right-3 top-1/2 transform -translate-y-1/2 text-muted-foreground">
              {rightIcon}
            </div>
          )}
        </div>
        
        {error && (
          <p className="mt-1 text-sm text-destructive">{error}</p>
        )}
        
        {helper && !error && (
          <p className="mt-1 text-sm text-muted-foreground">{helper}</p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export default Input;
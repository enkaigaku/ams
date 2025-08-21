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
          <label className="block text-sm font-medium text-gray-700 mb-1">
            {label}
            {props.required && <span className="text-red-500 ml-1">*</span>}
          </label>
        )}
        
        <div className="relative">
          {leftIcon && (
            <div className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400">
              {leftIcon}
            </div>
          )}
          
          <input
            ref={ref}
            type={type}
            className={cn(
              'block w-full px-3 py-2 border rounded-md shadow-sm placeholder-gray-400',
              'focus:outline-none focus:ring-2 focus:border-transparent',
              leftIcon ? 'pl-10' : 'pl-3',
              rightIcon ? 'pr-10' : 'pr-3',
              hasError
                ? 'border-red-300 focus:ring-red-500'
                : 'border-gray-300 focus:ring-primary-500',
              props.disabled ? 'bg-gray-50 text-gray-500 cursor-not-allowed' : '',
              className
            )}
            {...props}
          />
          
          {rightIcon && (
            <div className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400">
              {rightIcon}
            </div>
          )}
        </div>
        
        {error && (
          <p className="mt-1 text-sm text-red-600">{error}</p>
        )}
        
        {helper && !error && (
          <p className="mt-1 text-sm text-gray-500">{helper}</p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export default Input;
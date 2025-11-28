import React from 'react';

const LoadingSpinner: React.FC = () => {
  return (
    <div className="flex justify-center items-center min-h-[400px]">
      <div className="relative">
        <div className="w-16 h-16 border-4 border-gray-200 border-t-primary-600 rounded-full animate-spin"></div>
        <p className="mt-4 text-sm text-gray-600 text-center">Loading...</p>
      </div>
    </div>
  );
};

export default LoadingSpinner;


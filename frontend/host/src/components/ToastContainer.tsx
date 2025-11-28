import React, { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../store';
import { removeToast } from '../store/slices/uiSlice';

const ToastContainer: React.FC = () => {
  const toasts = useSelector((state: RootState) => state.ui.toasts);
  const dispatch = useDispatch();

  useEffect(() => {
    toasts.forEach((toast) => {
      const duration = toast.duration || 5000;
      setTimeout(() => {
        dispatch(removeToast(toast.id));
      }, duration);
    });
  }, [toasts, dispatch]);

  const getToastStyles = (type: string) => {
    switch (type) {
      case 'success':
        return 'bg-green-50 border-green-400 text-green-800';
      case 'error':
        return 'bg-red-50 border-red-400 text-red-800';
      case 'warning':
        return 'bg-yellow-50 border-yellow-400 text-yellow-800';
      case 'info':
        return 'bg-blue-50 border-blue-400 text-blue-800';
      default:
        return 'bg-gray-50 border-gray-400 text-gray-800';
    }
  };

  return (
    <div className="fixed top-4 right-4 z-50 space-y-2">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`max-w-sm w-full border-l-4 p-4 rounded-md shadow-soft-lg animate-slide-in ${getToastStyles(toast.type)}`}
        >
          <div className="flex items-start justify-between">
            <p className="text-sm font-medium">{toast.message}</p>
            <button
              onClick={() => dispatch(removeToast(toast.id))}
              className="ml-4 text-gray-400 hover:text-gray-600"
            >
              ×
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};

export default ToastContainer;


import React, { Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { RootState } from './store';
import ToastContainer from './components/ToastContainer';
import LoadingSpinner from './components/LoadingSpinner';

// Lazy load micro frontends
const UploadMfe = lazy(() => import('uploadMfe/UploadComponent'));
const GalleryMfe = lazy(() => import('galleryMfe/GalleryComponent'));
const EventsMfe = lazy(() => import('eventsMfe/EventsComponent'));

const App: React.FC = () => {
  const { sidebarOpen } = useSelector((state: RootState) => state.ui);

  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        {/* Header */}
        <header className="bg-white shadow-soft">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
            <div className="flex justify-between items-center">
              <h1 className="text-2xl font-bold text-gray-900">
                Photo Upload System
              </h1>
              <nav className="flex space-x-4">
                <Link
                  to="/"
                  className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-primary-600 hover:bg-gray-50 rounded-md transition-colors"
                >
                  Upload
                </Link>
                <Link
                  to="/gallery"
                  className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-primary-600 hover:bg-gray-50 rounded-md transition-colors"
                >
                  Gallery
                </Link>
                <Link
                  to="/events"
                  className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-primary-600 hover:bg-gray-50 rounded-md transition-colors"
                >
                  Events
                </Link>
              </nav>
            </div>
          </div>
        </header>

        {/* Main Content */}
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Suspense fallback={<LoadingSpinner />}>
            <Routes>
              <Route path="/" element={<UploadMfe />} />
              <Route path="/gallery" element={<GalleryMfe />} />
              <Route path="/events/:photoId?" element={<EventsMfe />} />
            </Routes>
          </Suspense>
        </main>

        {/* Toast Notifications */}
        <ToastContainer />
      </div>
    </Router>
  );
};

export default App;


import React from 'react';
import ReactDOM from 'react-dom/client';
import UploadComponent from './UploadComponent';

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <UploadComponent />
  </React.StrictMode>
);


import React from 'react';
import { BrowserRouter as Router, Routes, Route , Navigate} from 'react-router-dom';
import { Provider } from 'react-redux';
import { store } from './redux/store';
import Header from './components/common/Header/Header';

import Login from './components/Login/Login';
import Register from './components/Register/Register';
import LoanApplicationForm from './components/LoanApplicationForm/LoanApplicationForm';
import Dashboard from './components/Dashboard/Dashboard';

function App() {
  return (
    <div className="App">
      <Provider store={store}>
      <Router>
        <Header />
        <Routes>
          {/* Public routes */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/loan-application" element={<LoanApplicationForm />} />
          <Route path="/dashboard" element={<Dashboard />} />
          {/* 404 route */}
          {/* <Route path="*" element={<NotFound />} /> */}
        </Routes>
      </Router>
    </Provider>
    </div>
  );
}

export default App;

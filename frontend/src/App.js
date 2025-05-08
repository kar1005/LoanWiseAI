import React from 'react';
import { BrowserRouter as Router, Routes, Route , Navigate} from 'react-router-dom';
import { Provider } from 'react-redux';
import { store } from './redux/store';
import Header from './components/common/Header/Header';

import Login from './components/Login/Login';
import Register from './components/Register/Register';
import LoanApplicationForm from './components/LoanApplicationForm/LoanApplicationForm';
import LoanApprovalResult from './components/LoanApprovalResult/LoanApprovalResult';

import Dashboard from './components/Dashboard/Dashboard';


function App() {
  const token = localStorage.getItem('LoanWisetoken');
  return (
    <div className="App">
      <Provider store={store}>
      <Router>
        <Header />
        <Routes>
          {/* Public routes */}
          <Route path="/" element={token ? <Navigate to="/dashboard" replace />: <Navigate to="/login" replace="/"/>} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          {(token&&
            <>
              <Route path="/loan-application" element={<LoanApplicationForm />} />
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/loan-approval/:applicationId" element={<LoanApprovalResult />} />
            </>
          )}:{(
            <Route path="/login" element={<Login />} />
          )}

          {/* 404 route */}
          {/* <Route path="*" element={<NotFound />} /> */}
        </Routes>
      </Router>
    </Provider>
    </div>
  );
}

export default App;
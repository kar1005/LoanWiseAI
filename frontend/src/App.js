import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoanApplicationForm from './components/LoanApplicationForm/LoanApplicationForm';
import LoanApprovalResult from './components/LoanApprovalResult/LoanApprovalResult';
import Dashboard from './components/Dashboard/Dashboard';
import Header from './components/common/Header/Header';
import './App.css';

function App() {
  return (
    <Router>
      <Header />
      <div className="app-container">
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Navigate replace to="/dashboard" />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/loan-application" element={<LoanApplicationForm />} />
            <Route path="/loan-approval/:applicationId" element={<LoanApprovalResult />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
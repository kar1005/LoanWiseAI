import React from 'react';
import './Header.css';
import { useNavigate } from 'react-router-dom';

const Header = () => {
  const navigate = useNavigate();

  const handleApplyClick = () => {
    navigate('/loan-application');
  }
  return (
    <header className="header">
      <div className="logo">Loan<span>Wise</span>AI</div>
      <button className="apply-button" onClick={handleApplyClick}>Apply for Loan</button>
    </header>
  );
};

export default Header;
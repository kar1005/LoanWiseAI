import React from 'react';
import './Header.css';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { logout } from '../../../redux/slices/authSlice';

const Header = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };
  const token = localStorage.getItem('LoanWisetoken');
  
  return (
    <header className="header">
      <div className="logo">Loan<span>Wise</span>AI</div>
      {token && (
        <button className="logout-button" onClick={handleLogout}>Logout</button>
      )}
    </header>
  );
};

export default Header;
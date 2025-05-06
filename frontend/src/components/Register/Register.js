// src/pages/Register.js

import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { register, clearError } from '../../redux/slices/authSlice'; 
import '../../styles/auth.css'; 

const Register = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [formErrors, setFormErrors] = useState({});
  
  const dispatch = useDispatch();
  const navigate = useNavigate();
  
  const { loading, error, isAuthenticated } = useSelector((state) => state.auth);
  
  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard');
    }
    
    // Clear any previous errors when component mounts
    dispatch(clearError());
  }, [isAuthenticated, navigate, dispatch]);
  
  // Handle form input changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
    
    // Clear field-specific error when user starts typing
    if (formErrors[name]) {
      setFormErrors({
        ...formErrors,
        [name]: '',
      });
    }
  };
  
  // Validate form
  const validateForm = () => {
    const errors = {};
    const { name, email, password, confirmPassword } = formData;
    
    if (!name.trim()) {
      errors.name = 'Name is required';
    }
    
    if (!email.trim()) {
      errors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      errors.email = 'Email is invalid';
    }
    
    if (!password) {
      errors.password = 'Password is required';
    } else if (password.length < 6) {
      errors.password = 'Password must be at least 6 characters';
    }
    
    if (password !== confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };
  
  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (validateForm()) {
      const { name, email, password } = formData;
      try {
        // Only send needed data to the API
        const userData = {
          name,
          email,
          password,
        };
        
        await dispatch(register(userData)).unwrap();
        // Successful registration will redirect via the useEffect
      } catch (error) {
        // Additional error handling if needed
      }
    }
  };
  
  return (
    <div className="auth-container">
      <div className="auth-form">
        <h1 className="auth-title">Create an Account</h1>
        
        {error && <div className="error-message">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name" className="form-label">Name</label>
            <input
              type="text"
              id="name"
              name="name"
              className="form-input"
              placeholder="Enter your full name"
              value={formData.name}
              onChange={handleChange}
            />
            {formErrors.name && <div className="error-message">{formErrors.name}</div>}
          </div>
          
          <div className="form-group">
            <label htmlFor="email" className="form-label">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              className="form-input"
              placeholder="Enter your email"
              value={formData.email}
              onChange={handleChange}
            />
            {formErrors.email && <div className="error-message">{formErrors.email}</div>}
          </div>
          
          <div className="form-group">
            <label htmlFor="password" className="form-label">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              className="form-input"
              placeholder="Create a password"
              value={formData.password}
              onChange={handleChange}
            />
            {formErrors.password && <div className="error-message">{formErrors.password}</div>}
          </div>
          
          <div className="form-group">
            <label htmlFor="confirmPassword" className="form-label">Confirm Password</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              className="form-input"
              placeholder="Confirm your password"
              value={formData.confirmPassword}
              onChange={handleChange}
            />
            {formErrors.confirmPassword && (
              <div className="error-message">{formErrors.confirmPassword}</div>
            )}
          </div>
          
          <button 
            type="submit" 
            className="auth-button"
            disabled={loading}
          >
            {loading ? (
              <>
                Creating Account
                <span className="loading-spinner"></span>
              </>
            ) : (
              'Register'
            )}
          </button>
        </form>
        
        <div className="auth-footer">
          Already have an account?{' '}
          <Link to="/login" className="auth-link">Login</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
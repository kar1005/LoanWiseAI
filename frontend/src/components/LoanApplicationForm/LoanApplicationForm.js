import React, { useState } from 'react';
import './LoanApplicationForm.css';

const LoanApplicationForm = () => {
  const [formData, setFormData] = useState({
    loanAmount: '',
    loanPurpose: '',
    age: '',
    annualIncome: '',
    aadharCard: null,
    panCard: null,
    incomeTaxReturn: null,
    bankStatements: null
  });

  const [errors, setErrors] = useState({});

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleFileChange = (e) => {
    const { name, files } = e.target;
    setFormData({
      ...formData,
      [name]: files[0]
    });
  };

  const validateForm = () => {
    const newErrors = {};
    
    // Validate loan amount
    if (!formData.loanAmount) {
      newErrors.loanAmount = 'Loan amount is required';
    } else if (isNaN(formData.loanAmount) || formData.loanAmount <= 0) {
      newErrors.loanAmount = 'Please enter a valid loan amount';
    }
    
    // Validate loan purpose
    if (!formData.loanPurpose) {
      newErrors.loanPurpose = 'Loan purpose is required';
    }
    
    // Validate age
    if (!formData.age) {
      newErrors.age = 'Age is required';
    } else if (isNaN(formData.age) || formData.age < 18) {
      newErrors.age = 'You must be at least 18 years old';
    }
    
    // Validate annual income
    if (!formData.annualIncome) {
      newErrors.annualIncome = 'Annual income is required';
    } else if (isNaN(formData.annualIncome) || formData.annualIncome <= 0) {
      newErrors.annualIncome = 'Please enter a valid income amount';
    }
    
    // Validate required documents
    if (!formData.aadharCard) {
      newErrors.aadharCard = 'Aadhar card is required';
    }
    
    if (!formData.panCard) {
      newErrors.panCard = 'PAN card is required';
    }
    
    if (!formData.incomeTaxReturn) {
      newErrors.incomeTaxReturn = 'Income tax return is required';
    }
    
    if (!formData.bankStatements) {
      newErrors.bankStatements = 'Bank statements are required';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (validateForm()) {
      // Process form submission
      console.log('Form submitted:', formData);
      alert('Form submitted successfully!');
      // Here you would typically send the data to your backend
    }
  };

  return (
    <div className="loan-form-container">
      <h2>Loan Application Form</h2>
      <form onSubmit={handleSubmit} className="loan-form">
        <div className="form-section">
          <h3>Personal Details</h3>
          
          <div className="form-group">
            <label htmlFor="loanAmount">Loan Amount (₹)*</label>
            <input
              type="number"
              id="loanAmount"
              name="loanAmount"
              value={formData.loanAmount}
              onChange={handleInputChange}
              className={errors.loanAmount ? 'error' : ''}
            />
            {errors.loanAmount && <span className="error-message">{errors.loanAmount}</span>}
          </div>
          
          <div className="form-group">
            <label htmlFor="loanPurpose">Loan Purpose*</label>
            <select
              id="loanPurpose"
              name="loanPurpose"
              value={formData.loanPurpose}
              onChange={handleInputChange}
              className={errors.loanPurpose ? 'error' : ''}
            >
              <option value="">Select Purpose</option>
              <option value="home">Home Loan</option>
              <option value="education">Education Loan</option>
              <option value="personal">Personal Loan</option>
              <option value="business">Business Loan</option>
              <option value="vehicle">Vehicle Loan</option>
              <option value="other">Other</option>
            </select>
            {errors.loanPurpose && <span className="error-message">{errors.loanPurpose}</span>}
          </div>
          
          <div className="form-group">
            <label htmlFor="age">Age*</label>
            <input
              type="number"
              id="age"
              name="age"
              value={formData.age}
              onChange={handleInputChange}
              className={errors.age ? 'error' : ''}
            />
            {errors.age && <span className="error-message">{errors.age}</span>}
          </div>
          
          <div className="form-group">
            <label htmlFor="annualIncome">Annual Income (₹)*</label>
            <input
              type="number"
              id="annualIncome"
              name="annualIncome"
              value={formData.annualIncome}
              onChange={handleInputChange}
              className={errors.annualIncome ? 'error' : ''}
            />
            {errors.annualIncome && <span className="error-message">{errors.annualIncome}</span>}
          </div>
        </div>
        
        <div className="form-section">
          <h3>Required Documents</h3>
          
          <div className="form-group">
            <label htmlFor="aadharCard">Aadhar Card*</label>
            <input
              type="file"
              id="aadharCard"
              name="aadharCard"
              onChange={handleFileChange}
              className={errors.aadharCard ? 'error' : ''}
              accept=".pdf,.jpg,.jpeg,.png"
            />
            {errors.aadharCard && <span className="error-message">{errors.aadharCard}</span>}
            <small>Accepted formats: PDF, JPG, PNG</small>
          </div>
          
          <div className="form-group">
            <label htmlFor="panCard">PAN Card*</label>
            <input
              type="file"
              id="panCard"
              name="panCard"
              onChange={handleFileChange}
              className={errors.panCard ? 'error' : ''}
              accept=".pdf,.jpg,.jpeg,.png"
            />
            {errors.panCard && <span className="error-message">{errors.panCard}</span>}
            <small>Accepted formats: PDF, JPG, PNG</small>
          </div>
          
          <div className="form-group">
            <label htmlFor="incomeTaxReturn">Income Tax Return*</label>
            <input
              type="file"
              id="incomeTaxReturn"
              name="incomeTaxReturn"
              onChange={handleFileChange}
              className={errors.incomeTaxReturn ? 'error' : ''}
              accept=".pdf"
            />
            {errors.incomeTaxReturn && <span className="error-message">{errors.incomeTaxReturn}</span>}
            <small>Accepted format: PDF</small>
          </div>
          
          <div className="form-group">
            <label htmlFor="bankStatements">Bank Statements (Last 6 Months)*</label>
            <input
              type="file"
              id="bankStatements"
              name="bankStatements"
              onChange={handleFileChange}
              className={errors.bankStatements ? 'error' : ''}
              accept=".pdf,.xls,.xlsx"
            />
            {errors.bankStatements && <span className="error-message">{errors.bankStatements}</span>}
            <small>Accepted formats: PDF, XLS, XLSX</small>
          </div>
        </div>
        
        <div className="form-actions">
          <button type="submit" className="submit-btn">Submit Application</button>
          <button type="reset" className="reset-btn">Reset Form</button>
        </div>
        
        <div className="disclaimer">
          <p>* Indicates required fields</p>
          <p>All your information will be kept confidential and secure.</p>
        </div>
      </form>
    </div>
  );
};

export default LoanApplicationForm;
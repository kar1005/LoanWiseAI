import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './LoanApplicationForm.css';
import loanApplicationService from '../../services/loanApplicationService';

const LoanApplicationForm = () => {
    const navigate = useNavigate();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [files, setFiles] = useState({
        aadharCard: null,
        panCard: null,
        incomeProof: null,
        bankStatements: null
    });// Set your backend URL here
    
    const [formData, setFormData] = useState({
        userId: localStorage.getItem('userId') ,
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        dateOfBirth: '',
        address: '',
        city: '',
        state: '',
        zipCode: '',
        loanAmount: '',
        loanPurpose: '',
        loanTerm: 12,
        employmentStatus: '',
        employer: '',
        jobTitle: '',
        annualIncome: '',
        monthlyExpenses: '',
        creditScore: '',
        existingLoans: 'No',
        status: 'PENDING'
    });
    
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
    };
    
    const handleFileChange = (e) => {
        const { name, files } = e.target;
        setFiles({
            ...files,
            [name]: files[0]
        });
    };
    
    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError('');
        
        try {

            if (Object.values(files).some(file => file !== null)) {
                const formDataFiles = new FormData();
                const documentTypes = [];
                
                // Add files to FormData
                Object.entries(files).forEach(([type, file]) => {
                    if (file) {
                        formDataFiles.append('files', file);
                        documentTypes.push(type);
                    }
                });
                documentTypes.forEach(type => {
                    formDataFiles.append('documentTypes', type);
                });
            }
            const applicationResponse = await loanApplicationService.submitApplication(formData);
            const applicationId = applicationResponse.id;
            
            
            // Navigate to results page
            navigate(`/loan-approval/${applicationId}`);
            
        } catch (err) {
            setError('Failed to submit loan application. ' + 
                (err.response?.data || err.message));
            setIsSubmitting(false);
        }
    };
    
    return (
        <div className="loan-form-container">
            <h1>Loan Application</h1>
            
            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}
            
            <form onSubmit={handleSubmit}>
                <div className="form-section">
                    <h2>Personal Information</h2>
                    
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="firstName">First Name</label>
                            <input
                                type="text"
                                id="firstName"
                                name="firstName"
                                value={formData.firstName}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="lastName">Last Name</label>
                            <input
                                type="text"
                                id="lastName"
                                name="lastName"
                                value={formData.lastName}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                    </div>
                    
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="email">Email</label>
                            <input
                                type="email"
                                id="email"
                                name="email"
                                value={formData.email}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="phone">Phone Number</label>
                            <input
                                type="tel"
                                id="phone"
                                name="phone"
                                value={formData.phone}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                    </div>
                    
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="dateOfBirth">Date of Birth</label>
                            <input
                                type="date"
                                id="dateOfBirth"
                                name="dateOfBirth"
                                value={formData.dateOfBirth}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="address">Address</label>
                        <input
                            type="text"
                            id="address"
                            name="address"
                            value={formData.address}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="city">City</label>
                            <input
                                type="text"
                                id="city"
                                name="city"
                                value={formData.city}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="state">State</label>
                            <input
                                type="text"
                                id="state"
                                name="state"
                                value={formData.state}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="zipCode">ZIP Code</label>
                            <input
                                type="text"
                                id="zipCode"
                                name="zipCode"
                                value={formData.zipCode}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                    </div>
                </div>
                
                <div className="form-section">
                    <h2>Loan Details</h2>
                    
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="loanAmount">Loan Amount ($)</label>
                            <input
                                type="number"
                                id="loanAmount"
                                name="loanAmount"
                                value={formData.loanAmount}
                                onChange={handleInputChange}
                                min="1000"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="loanTerm">Loan Term (months)</label>
                            <select
                                id="loanTerm"
                                name="loanTerm"
                                value={formData.loanTerm}
                                onChange={handleInputChange}
                                required
                            >
                                <option value="12">12 months</option>
                                <option value="24">24 months</option>
                                <option value="36">36 months</option>
                                <option value="48">48 months</option>
                                <option value="60">60 months</option>
                            </select>
                        </div>
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="loanPurpose">Loan Purpose</label>
                        <select
                            id="loanPurpose"
                            name="loanPurpose"
                            value={formData.loanPurpose}
                            onChange={handleInputChange}
                            required
                        >
                            <option value="">Select purpose</option>
                            <option value="Home Improvement">Home Improvement</option>
                            <option value="Debt Consolidation">Debt Consolidation</option>
                            <option value="Education">Education</option>
                            <option value="Medical Expenses">Medical Expenses</option>
                            <option value="Business">Business</option>
                            <option value="Vehicle Purchase">Vehicle Purchase</option>
                            <option value="Other">Other</option>
                        </select>
                    </div>
                </div>
                
                <div className="form-section">
                    <h2>Employment & Financial Information</h2>
                    
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="employmentStatus">Employment Status</label>
                            <select
                                id="employmentStatus"
                                name="employmentStatus"
                                value={formData.employmentStatus}
                                onChange={handleInputChange}
                                required
                            >
                                <option value="">Select status</option>
                                <option value="Full-time">Full-time</option>
                                <option value="Part-time">Part-time</option>
                                <option value="Self-employed">Self-employed</option>
                                <option value="Unemployed">Unemployed</option>
                                <option value="Retired">Retired</option>
                                <option value="Student">Student</option>
                            </select>
                        </div>
                    </div>
                    
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="employer">Employer Name</label>
                            <input
                                type="text"
                                id="employer"
                                name="employer"
                                value={formData.employer}
                                onChange={handleInputChange}
                                required={formData.employmentStatus !== 'Unemployed' && 
                                          formData.employmentStatus !== 'Student' &&
                                          formData.employmentStatus !== 'Retired'}
                                disabled={formData.employmentStatus === 'Unemployed' || 
                                          formData.employmentStatus === 'Student' || 
                                          formData.employmentStatus === 'Retired'}
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="jobTitle">Job Title</label>
                            <input
                                type="text"
                                id="jobTitle"
                                name="jobTitle"
                                value={formData.jobTitle}
                                onChange={handleInputChange}
                                required={formData.employmentStatus !== 'Unemployed' && 
                                          formData.employmentStatus !== 'Student' &&
                                          formData.employmentStatus !== 'Retired'}
                                disabled={formData.employmentStatus === 'Unemployed' || 
                                          formData.employmentStatus === 'Student' || 
                                          formData.employmentStatus === 'Retired'}
                            />
                        </div>
                    </div>
                    
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="annualIncome">Annual Income ($)</label>
                            <input
                                type="number"
                                id="annualIncome"
                                name="annualIncome"
                                value={formData.annualIncome}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="monthlyExpenses">Monthly Expenses ($)</label>
                            <input
                                type="number"
                                id="monthlyExpenses"
                                name="monthlyExpenses"
                                value={formData.monthlyExpenses}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                    </div>
                    
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="creditScore">Credit Score (if known)</label>
                            <input
                                type="number"
                                id="creditScore"
                                name="creditScore"
                                value={formData.creditScore}
                                onChange={handleInputChange}
                                min="300"
                                max="850"
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="existingLoans">Do you have any existing loans?</label>
                            <select
                                id="existingLoans"
                                name="existingLoans"
                                value={formData.existingLoans}
                                onChange={handleInputChange}
                                required
                            >
                                <option value="Yes">Yes</option>
                                <option value="No">No</option>
                            </select>
                        </div>
                    </div>
                </div>
                
                <div className="form-section">
                    <h2>Document Upload</h2>
                    <p className="document-info">Please upload the following documents to expedite your loan approval process.</p>
                    
                    <div className="form-group">
                        <label htmlFor="identityProof">Aadhar Card</label>
                        <input
                            type="file"
                            id="aadharCard"
                            name="aadharCard"
                            onChange={handleFileChange}
                            accept=".pdf,.jpg,.jpeg,.png"
                            required
                        />
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="addressProof">PAN Card</label>
                        <input
                            type="file"
                            id="panCard"
                            name="panCard"
                            onChange={handleFileChange}
                            accept=".pdf,.jpg,.jpeg,.png"
                            required
                        />
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="incomeProof">Income Tax Return</label>
                        <input
                            type="file"
                            id="incomeProof"
                            name="incomeProof"
                            onChange={handleFileChange}
                            accept=".pdf,.jpg,.jpeg,.png"
                            required
                        />
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="bankStatements">Bank Statements (Last 6 months)</label>
                        <input
                            type="file"
                            id="bankStatements"
                            name="bankStatements"
                            onChange={handleFileChange}
                            accept=".pdf,.jpg,.jpeg,.png"
                            required
                        />
                    </div>
                </div>
                
                <div className="form-section">
                    <div className="terms-container">
                        <label className="checkbox-label">
                            <input type="checkbox" required />
                            I agree to the Terms and Conditions and Privacy Policy.
                        </label>
                    </div>
                    
                    <div className="submit-container">
                        <button type="submit" className="submit-btn" disabled={isSubmitting}>
                            {isSubmitting ? 'Submitting...' : 'Submit Application'}
                        </button>
                    </div>
                </div>
            </form>
        </div>
    );
};

export default LoanApplicationForm;
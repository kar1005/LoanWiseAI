import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useParams, Link } from 'react-router-dom';
import './LoanApprovalResult.css';

const LoanApprovalResult = () => {
    const { applicationId } = useParams();
    const [loading, setLoading] = useState(true);
    const [application, setApplication] = useState(null);
    const [validationLog, setValidationLog] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchApplicationData = async () => {
            try {
                // Get application details
                const appResponse = await axios.get(`/api/loan/${applicationId}`);
                setApplication(appResponse.data);

                // Get validation results
                const validationResponse = await axios.get(`/api/loan/${applicationId}/validation-result`);
                setValidationLog(validationResponse.data);
                
                setLoading(false);
            } catch (err) {
                setError('Failed to load loan application details. ' + 
                    (err.response?.data || err.message));
                setLoading(false);
            }
        };

        fetchApplicationData();
    }, [applicationId]);

    const handleRequestApproval = async () => {
        setLoading(true);
        try {
            const response = await axios.post(`/api/loan/${applicationId}/request-approval`);
            setApplication(response.data.application);
            setValidationLog(response.data.validationLog);
            setLoading(false);
        } catch (err) {
            setError('Failed to process loan approval. ' + 
                (err.response?.data || err.message));
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="loan-result-container">
                <div className="loading-spinner">
                    <div className="spinner"></div>
                    <p>Processing loan application...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="loan-result-container">
                <div className="error-container">
                    <h2>Error</h2>
                    <p>{error}</p>
                    <Link to="/dashboard" className="btn">Return to Dashboard</Link>
                </div>
            </div>
        );
    }

    return (
        <div className="loan-result-container">
            <h1>Loan Application Status</h1>
            
            {application && (
                <div className="application-details">
                    <h2>Application Details</h2>
                    <div className="info-card">
                        <div className="info-row">
                            <span>Application ID:</span>
                            <span>{application.id}</span>
                        </div>
                        <div className="info-row">
                            <span>Full Name:</span>
                            <span>{application.firstName} {application.lastName}</span>
                        </div>
                        <div className="info-row">
                            <span>Loan Amount:</span>
                            <span>${application.loanAmount.toLocaleString()}</span>
                        </div>
                        <div className="info-row">
                            <span>Loan Purpose:</span>
                            <span>{application.loanPurpose}</span>
                        </div>
                        <div className="info-row">
                            <span>Employment Status:</span>
                            <span>{application.employmentStatus}</span>
                        </div>
                        <div className="info-row">
                            <span>Annual Income:</span>
                            <span>${application.annualIncome.toLocaleString()}</span>
                        </div>
                        <div className="info-row">
                            <span>Application Status:</span>
                            <span className={`status ${application.status}`}>{application.status}</span>
                        </div>
                    </div>
                </div>
            )}
            
            {!validationLog && application && application.status === 'PENDING' && (
                <div className="action-container">
                    <p>Your application has been submitted but not yet processed.</p>
                    <button className="btn primary" onClick={handleRequestApproval}>
                        Request Loan Approval
                    </button>
                </div>
            )}
            
            {validationLog && (
                <div className="validation-results">
                    <h2>Validation Results</h2>
                    <div className={`result-card ${validationLog.approved ? 'approved' : 'rejected'}`}>
                        <div className="result-icon">
                            {validationLog.approved ? (
                                <i className="fas fa-check-circle"></i>
                            ) : (
                                <i className="fas fa-times-circle"></i>
                            )}
                        </div>
                        <div className="result-content">
                            <h3>{validationLog.approved ? 'Approved' : 'Rejected'}</h3>
                            <p>{validationLog.message}</p>
                            <div className="validation-date">
                                Processed on: {new Date(validationLog.validationDate).toLocaleString()}
                            </div>
                        </div>
                    </div>
                    
                    {validationLog.approved && (
                        <div className="next-steps">
                            <h3>Next Steps</h3>
                            <p>Congratulations! Your loan has been approved. One of our representatives will contact you shortly to discuss the next steps in the loan disbursement process.</p>
                        </div>
                    )}
                    
                    {!validationLog.approved && (
                        <div className="next-steps">
                            <h3>What to do now?</h3>
                            <p>Your loan application has been rejected based on our verification process. You may contact our customer support for more information or submit a new application with updated information.</p>
                        </div>
                    )}
                </div>
            )}
            
            <div className="action-buttons">
                <Link to="/dashboard" className="btn">Return to Dashboard</Link>
                {validationLog && !validationLog.approved && (
                    <Link to="/loan-application" className="btn primary">Apply Again</Link>
                )}
            </div>
        </div>
    );
};

export default LoanApprovalResult;
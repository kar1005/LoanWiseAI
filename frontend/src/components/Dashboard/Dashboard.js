import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import './Dashboard.css';

const Dashboard = () => {
    const [loading, setLoading] = useState(true);
    const [applications, setApplications] = useState([]);
    const [error, setError] = useState(null);
    
    // Mock user ID for demo purposes
    const userId = localStorage.getItem('userId') || 1;
    
    useEffect(() => {
        const fetchApplications = async () => {
            try {
                const response = await axios.get(`/api/loan/user/${userId}`);
                setApplications(response.data);
                setLoading(false);
            } catch (err) {
                setError('Error fetching loan applications');
                setLoading(false);
            }
        };
        
        fetchApplications();
    }, [userId]);
    
    if (loading) {
        return (
            <div className="dashboard-container">
                <div className="loading-spinner">
                    <div className="spinner"></div>
                    <p>Loading your applications...</p>
                </div>
            </div>
        );
    }
    
    return (
        <div className="dashboard-container">
            <h1>Your Loan Dashboard</h1>
            
            <div className="dashboard-actions">
                <Link to="/loan-application" className="action-btn">
                    <i className="fas fa-plus-circle"></i>
                    Apply for New Loan
                </Link>
            </div>
            
            {error && <div className="error-message">{error}</div>}
            
            <div className="applications-section">
                <h2>Your Loan Applications</h2>
                
                {applications.length === 0 ? (
                    <div className="no-applications">
                        <p>You don't have any loan applications yet.</p>
                        <p>Click "Apply for New Loan" to get started!</p>
                    </div>
                ) : (
                    <div className="applications-grid">
                        {applications.map(app => (
                            <div key={app.id} className={`application-card ${app.status.toLowerCase()}`}>
                                <div className="card-header">
                                    <h3>Loan #{app.id}</h3>
                                    <span className={`status-badge ${app.status.toLowerCase()}`}>
                                        {app.status}
                                    </span>
                                </div>
                                <div className="card-body">
                                    <div className="info-item">
                                        <span>Amount:</span>
                                        <span>${app.loanAmount.toLocaleString()}</span>
                                    </div>
                                    <div className="info-item">
                                        <span>Purpose:</span>
                                        <span>{app.loanPurpose}</span>
                                    </div>
                                    <div className="info-item">
                                        <span>Term:</span>
                                        <span>{app.loanTerm} months</span>
                                    </div>
                                    <div className="info-item">
                                        <span>Applied:</span>
                                        <span>{new Date(app.createdAt).toLocaleDateString()}</span>
                                    </div>
                                </div>
                                <div className="card-footer">
                                    <Link to={`/loan-approval/${app.id}`} className="view-btn">
                                        View Details
                                    </Link>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Dashboard;
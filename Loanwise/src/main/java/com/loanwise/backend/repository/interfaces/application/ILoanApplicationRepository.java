package com.loanwise.backend.repository.interfaces.application;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.loanwise.backend.models.application.LoanApplication;

@Repository
public interface ILoanApplicationRepository extends MongoRepository<LoanApplication, String> {
    // Add custom query methods if needed
}
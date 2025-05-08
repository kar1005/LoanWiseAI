package com.loanwise.backend.repository.interfaces.application;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.loanwise.backend.models.application.LoanApplication;

@Repository
public interface ILoanApplicationRepository extends MongoRepository<LoanApplication, String> {
    List<LoanApplication> findByApplicantEmail(String email);
    List<LoanApplication> findByStatus(String status);
    List<LoanApplication> findByUserId(String userId);
}
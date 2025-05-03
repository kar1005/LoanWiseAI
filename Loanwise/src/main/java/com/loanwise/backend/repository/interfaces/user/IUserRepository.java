package com.loanwise.backend.repository.interfaces.user;

import com.loanwise.backend.model.User;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.loanwise.backend.models.user.User;

@Repository
public interface IUserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}
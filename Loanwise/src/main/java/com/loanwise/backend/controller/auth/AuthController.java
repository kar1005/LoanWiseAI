package com.loanwise.backend.controller.auth;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loanwise.backend.dto.AuthRequest;
import com.loanwise.backend.dto.AuthResponse;
import com.loanwise.backend.dto.RegisterRequest;
import com.loanwise.backend.models.user.User;
import com.loanwise.backend.repository.interfaces.user.IUserRepository;
import com.loanwise.backend.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("Registration attempt for email: {}", registerRequest.getEmail());
            
            // Validate request
            if (registerRequest.getEmail() == null || registerRequest.getPassword() == null || registerRequest.getName() == null) {
                logger.warn("Registration failed: Missing required fields");
                return ResponseEntity
                        .badRequest()
                        .body(new AuthResponse(false, "Name, email and password are required", null, null));
            }
            
            // Check if user already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                logger.warn("Registration failed: Email already in use: {}", registerRequest.getEmail());
                return ResponseEntity
                        .badRequest()
                        .body(new AuthResponse(false, "Email is already in use", null, null));
            }

            // Create new user
            User user = new User();
            user.setName(registerRequest.getName());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : "USER");

            User savedUser = userRepository.save(user);
            logger.info("User registered successfully: {}", savedUser.getEmail());

            // Generate JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", savedUser.getRole());
            claims.put("userId", savedUser.getId());
            String token = jwtUtil.generateToken(userDetails, claims);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new AuthResponse(true, "User registered successfully", token, 
                            Map.of("id", savedUser.getId(),
                                   "name", savedUser.getName(),
                                   "email", savedUser.getEmail(),
                                   "role", savedUser.getRole())
                    )
            );
        } catch (Exception e) {
            logger.error("Registration error: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Registration failed: " + e.getMessage(), null, null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            logger.info("Login attempt for email: {}", authRequest.getEmail());
            
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
            
            // Generate JWT token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
            User user = userRepository.findByEmail(authRequest.getEmail()).orElseThrow();
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole());
            claims.put("userId", user.getId());
            final String token = jwtUtil.generateToken(userDetails, claims);
            
            logger.info("Login successful for user: {}", authRequest.getEmail());
            return ResponseEntity.ok(
                    new AuthResponse(true, "Login successful", token, 
                            Map.of("id", user.getId(),
                                   "name", user.getName(),
                                   "email", user.getEmail(),
                                   "role", user.getRole())
                    )
            );
        } catch (BadCredentialsException e) {
            logger.warn("Login failed: Invalid credentials for email: {}", authRequest.getEmail());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid email or password", null, null));
        } catch (Exception e) {
            logger.error("Login error: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Login failed: " + e.getMessage(), null, null));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            
            logger.info("Profile request for user: {}", email);
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Don't return the password
            user.setPassword(null);
            
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Profile retrieval error: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to retrieve profile: " + e.getMessage()));
        }
    }
}
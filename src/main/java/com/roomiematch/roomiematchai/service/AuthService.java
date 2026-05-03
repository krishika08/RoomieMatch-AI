package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.LoginRequestDTO;
import com.roomiematch.roomiematchai.dto.LoginResponseDTO;
import com.roomiematch.roomiematchai.dto.UserRequestDTO;
import com.roomiematch.roomiematchai.dto.UserResponseDTO;
import com.roomiematch.roomiematchai.entity.User;
import com.roomiematch.roomiematchai.exception.DuplicateEmailException;
import com.roomiematch.roomiematchai.exception.InvalidCredentialsException;
import com.roomiematch.roomiematchai.repository.UserRepository;
import com.roomiematch.roomiematchai.security.CustomUserDetailsService;
import com.roomiematch.roomiematchai.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    // Supported hostel values
    private static final java.util.Set<String> VALID_HOSTELS = java.util.Set.of(
            "BIDHOLI_BOYS_HOSTEL", "BIDHOLI_GIRLS_HOSTEL"
    );
    private static final java.util.Set<String> VALID_ORGS = java.util.Set.of("UPES");

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public UserResponseDTO register(UserRequestDTO request) {
        log.info("AuthService: Registering new user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
             log.warn("AuthService: Duplicate registration for email: {}", request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }

        // Validate organization
        String org = request.getOrganization() != null ? request.getOrganization().toUpperCase() : "";
        if (!VALID_ORGS.contains(org)) {
            throw new IllegalStateException("Invalid organization. Supported: UPES");
        }

        // Validate hostel
        String hostel = request.getHostel() != null ? request.getHostel().toUpperCase() : "";
        if (!VALID_HOSTELS.contains(hostel)) {
            throw new IllegalStateException("Invalid hostel. Supported: BIDHOLI_BOYS_HOSTEL, BIDHOLI_GIRLS_HOSTEL");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        // Hash the password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setOrganization(org);
        user.setHostel(hostel);
        // Default role is STUDENT (set in entity)

        User savedUser = userRepository.save(user);
        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getOrganization(),
                savedUser.getHostel(),
                savedUser.getSapId()
        );
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        log.info("AuthService: Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Match the raw password against the stored BCrypt hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("AuthService: Successful login for email: {}", request.getEmail());
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponseDTO(token, user.getId(), user.getEmail(), user.getRole().name(), user.getHostel());
    }
}

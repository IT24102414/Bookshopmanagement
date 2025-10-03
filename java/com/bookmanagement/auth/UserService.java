package com.bookmanagement.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;

        public OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User registerUser(String fullName, String email, String password, String phoneNumber, String role) {
        User newUser;
        if ("ADMIN".equalsIgnoreCase(role)) {
            AdminUser admin = new AdminUser();
            admin.setPermissions("MANAGE_ALL"); // Default permissions for admin
            newUser = admin;
        } else {
            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setPhoneNumber(phoneNumber);
            newUser = customer;
        }
        newUser.setEmail(email);
        newUser.setUsername(email); // Use email as username
        newUser.setPassword(passwordEncoder.encode(password));
        return userRepository.save(newUser);
    }

    public Optional<User> validateUser(String email, String rawPassword) {
        Optional<User> userOptional = getUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return userOptional;
            }
        }
        return Optional.empty();
    }

    @Transactional
    public void updateUserProfile(String email, String fullName, String phoneNumber) {
        getUserByEmail(email).ifPresent(user -> {
            if (user instanceof Customer) {
                Customer customer = (Customer) user;
                customer.setFullName(fullName);
                customer.setPhoneNumber(phoneNumber);
                userRepository.save(customer);
            } else if (user instanceof AdminUser) {
                // For admin users, update username (since they don't have fullName/phoneNumber)
                user.setUsername(fullName);
                userRepository.save(user);
            }
        });
    }

    public String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000); // 6-digit OTP
    }

    public void storeOtp(String email, String otp) {
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        otpStore.put(email, new OtpData(otp, expiry));
    }

    public boolean verifyOtp(String email, String otp) {
        OtpData otpData = otpStore.get(email);
        if (otpData != null && !otpData.isExpired() && otpData.getOtp().equals(otp)) {
            otpStore.remove(email); // Remove after successful verification
            return true;
        }
        return false;
    }

    @Transactional
    public boolean resetPassword(String email, String newPassword) {
        Optional<User> userOptional = getUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public String validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "Password is required.";
        }

        // Check minimum length
        if (password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter (A-Z).";
        }

        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter (a-z).";
        }

        // Check for at least one digit
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one digit (0-9).";
        }

        // Check for at least one special character
        if (!password.matches(".*[!@#$%&*()\\-+\\=\\^].*")) {
            return "Password must contain at least one special character (!@#$%&*()-+=^).";
        }

        // Check for no spaces
        if (password.contains(" ")) {
            return "Password must not contain any spaces.";
        }

        return null; // Valid
    }

    public String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required.";
        }

        // Check for spaces
        if (email.contains(" ")) {
            return "Email must not contain spaces.";
        }

        // Comprehensive email regex validation
        // Local part: letters, numbers, and symbols (!#$%&'*-+=/?^_{|}~.)
        // @ symbol required
        // Domain: valid domain with TLD
        String emailRegex = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

        if (!email.matches(emailRegex)) {
            return "Please enter a valid email address format.";
        }

        // Additional check for Gmail requirement
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            return "Email must be a Gmail address (ending with @gmail.com).";
        }

        return null; // Valid
    }

    public String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "Phone number is required.";
        }
        // Remove any spaces or dashes
        String cleanPhone = phoneNumber.replaceAll("[\\s-]", "");
        if (!cleanPhone.matches("^\\+94[0-9]{9}$|^0[0-9]{9}$|^[0-9]{10}$")) {
            return "Phone number must be a valid Sri Lankan mobile number (10 digits, starting with 0 or +94).";
        }
        return null; // Valid
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
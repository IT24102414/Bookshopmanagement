package com.bookmanagement.auth;

import com.bookmanagement.common.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    /**
     * Shows the login page.
     * Tells Spring to look for 'login.html' inside the 'resources/templates/auth/' folder.
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Optional<User> userOptional = userService.validateUser(email, password);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            session.setAttribute("loggedInUser", user);

            // Redirect to the correct dashboard URL
            if (user instanceof AdminUser || user.getEmail().equalsIgnoreCase("theebikaloganathan10@gmail.com")) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/";
            }
        } else {
            model.addAttribute("error", "Invalid email or password");
            // On error, show the login page again
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String fullName, @RequestParam String email, @RequestParam String password, @RequestParam String phoneNumber, RedirectAttributes redirectAttributes) {
        // Validate full name
        if (fullName == null || fullName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Full name is required.");
            return "redirect:/register";
        }
        if (fullName.length() < 2 || fullName.length() > 50) {
            redirectAttributes.addFlashAttribute("error", "Full name must be between 2 and 50 characters.");
            return "redirect:/register";
        }

        // Validate email
        String emailError = userService.validateEmail(email);
        if (emailError != null) {
            redirectAttributes.addFlashAttribute("error", emailError);
            return "redirect:/register";
        }

        // Validate password
        String passwordError = userService.validatePassword(password);
        if (passwordError != null) {
            redirectAttributes.addFlashAttribute("error", passwordError);
            return "redirect:/register";
        }

        // Validate phone number
        String phoneError = userService.validatePhoneNumber(phoneNumber);
        if (phoneError != null) {
            redirectAttributes.addFlashAttribute("error", phoneError);
            return "redirect:/register";
        }

        if (userService.getUserByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "An account with this email already exists.");
            return "redirect:/register";
        }

        // Register as admin if email is theebikaloganathan10@gmail.com or contains "admin" or "test", otherwise customer
        String role = (email.equalsIgnoreCase("theebikaloganathan10@gmail.com") || email.toLowerCase().contains("admin") || email.toLowerCase().contains("test")) ? "ADMIN" : "CUSTOMER";
        userService.registerUser(fullName, email, password, phoneNumber, role);
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        // Get fresh user data from database using email to ensure we have the latest data
        User user = userService.getUserByEmail(loggedInUser.getEmail()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        // Create user data map to avoid using class.simpleName in template
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("username", user.getUsername());
        userData.put("enabled", user.isEnabled());
        userData.put("createdAt", user.getCreatedAt());
        userData.put("userType", user instanceof Customer ? "Customer" : "Admin");
        if (user instanceof Customer) {
            userData.put("fullName", ((Customer) user).getFullName());
            userData.put("phoneNumber", ((Customer) user).getPhoneNumber());
        } else {
            userData.put("fullName", user.getUsername());
        }

        model.addAttribute("user", userData);
        return "auth/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName, @RequestParam String phoneNumber, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Validate phone number for customers
        if (loggedInUser instanceof com.bookmanagement.auth.Customer) {
            String phoneError = userService.validatePhoneNumber(phoneNumber);
            if (phoneError != null) {
                redirectAttributes.addFlashAttribute("error", phoneError);
                return "redirect:/profile";
            }
        }

        userService.updateUserProfile(loggedInUser.getEmail(), fullName, phoneNumber);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --- FORGOT PASSWORD ---

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        if (userService.getUserByEmail(email).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No account found with this email address.");
            return "redirect:/forgot-password";
        }
        String otp = userService.generateOtp();
        userService.storeOtp(email, otp);
         emailService.sendOtpEmail(email, otp);
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("success", "OTP sent to your email. Please check your inbox.");
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword() {
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String email, @RequestParam String otp, @RequestParam String newPassword, RedirectAttributes redirectAttributes) {
        // Validate new password
        String passwordError = userService.validatePassword(newPassword);
        if (passwordError != null) {
            redirectAttributes.addFlashAttribute("error", passwordError);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/reset-password";
        }

        if (userService.verifyOtp(email, otp)) {
            if (userService.resetPassword(email, newPassword)) {
                redirectAttributes.addFlashAttribute("success", "Password reset successfully! Please log in.");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to reset password. Please try again.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP.");
        }
        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:/reset-password";
    }
}
package com.bookmanagement.admin;

import com.bookmanagement.auth.AdminUser;
import com.bookmanagement.auth.User;
import com.bookmanagement.auth.UserService;
import com.bookmanagement.common.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class UserManagementController {

    private final UserService userService;

    @Autowired
    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String showUserManagement(HttpSession session, Model model) {
        // Check if admin is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !(loggedInUser instanceof AdminUser)) {
            return "redirect:/login?error=Access%20denied.%20Admin%20privileges%20required.";
        }

        // Get all users
        List<User> allUsers = userService.getUserRepository().findAll();

        // Calculate stats
        long totalUsers = allUsers.size();
        long adminCount = allUsers.stream()
                .filter(user -> user instanceof AdminUser)
                .count();
        long customerCount = allUsers.stream()
                .filter(user -> user instanceof com.bookmanagement.auth.Customer)
                .count();

        // Create user data maps to avoid using class.simpleName in template
        List<Map<String, Object>> userData = allUsers.stream().map(user -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("email", user.getEmail());
            data.put("username", user.getUsername());
            data.put("enabled", user.isEnabled());
            data.put("userType", user instanceof com.bookmanagement.auth.Customer ? "Customer" : "Admin");
            if (user instanceof com.bookmanagement.auth.Customer) {
                data.put("displayName", ((com.bookmanagement.auth.Customer) user).getFullName());
            } else {
                data.put("displayName", user.getUsername());
            }
            return data;
        }).collect(Collectors.toList());

        model.addAttribute("users", userData);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("customerCount", customerCount);

        return "admin/user-management";
    }

    @PostMapping("/users/delete/{userId}")
    @ResponseBody
    public ApiResponse deleteUser(@PathVariable Long userId, HttpSession session) {
        try {
            // Check if admin is logged in
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser == null || !(loggedInUser instanceof AdminUser)) {
                return new ApiResponse<>(false, "Unauthorized access", null);
            }

            // Prevent admin from deleting themselves
            if (loggedInUser.getId().equals(userId)) {
                return new ApiResponse<>(false, "Cannot delete your own account", null);
            }

            // Delete the user
            userService.getUserRepository().deleteById(userId);
            return new ApiResponse<>(true, "User deleted successfully", null);

        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to delete user: " + e.getMessage(), null);
        }
    }

    @PostMapping("/users/toggle-status/{userId}")
    @ResponseBody
    public ApiResponse toggleUserStatus(@PathVariable Long userId, HttpSession session) {
        try {
            // Check if admin is logged in
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser == null || !(loggedInUser instanceof com.bookmanagement.auth.AdminUser)) {
                return new ApiResponse<>(false, "Unauthorized access", null);
            }

            // Get the user
            User user = userService.getUserRepository().findById(userId).orElse(null);
            if (user == null) {
                return new ApiResponse<>(false, "User not found", null);
            }

            // Toggle enabled status
            user.setEnabled(!user.isEnabled());
            userService.getUserRepository().save(user);

            String status = user.isEnabled() ? "enabled" : "disabled";
            return new ApiResponse<>(true, "User " + status + " successfully", null);

        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to update user status: " + e.getMessage(), null);
        }
    }

    @GetMapping("/users/search")
    @ResponseBody
    public List<User> searchUsers(@RequestParam String query) {
        // Simple search implementation - you can enhance this
        return userService.getUserRepository().findAll().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }
}
package com.bookmanagement.admin;

import com.bookmanagement.auth.AdminUser;
import com.bookmanagement.auth.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !(loggedInUser instanceof AdminUser)) {
            return "redirect:/login?error=Access%20denied.%20Admin%20privileges%20required.";
        }
        return "admin/dashboard";
    }
}

package com.bookmanagement.config;

import com.bookmanagement.auth.AdminUser;
import com.bookmanagement.auth.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");

        // Check if user is logged in and is an admin
        if (loggedInUser == null || !(loggedInUser instanceof AdminUser)) {
            // Redirect to login page with error message
            response.sendRedirect("/login?error=Access%20denied.%20Admin%20privileges%20required.");
            return false; // Stop the request
        }

        return true; // Allow the request to proceed
    }
}
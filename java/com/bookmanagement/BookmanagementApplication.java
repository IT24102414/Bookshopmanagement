package com.bookmanagement;

import com.bookmanagement.auth.User;
import com.bookmanagement.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Optional;

@SpringBootApplication
public class BookmanagementApplication implements CommandLineRunner {

	@Autowired
	private UserService userService;

	public static void main(String[] args) {
		SpringApplication.run(BookmanagementApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Ensure default admin user exists with correct password and type
		String adminEmail = "theebikaloganathan10@gmail.com";

		Optional<User> existingUser = userService.getUserByEmail(adminEmail);
		if (existingUser.isPresent()) {
			User user = existingUser.get();
			// Update password
			user.setPassword(userService.getPasswordEncoder().encode("Admin@123"));
			// Save the updated user (this will update the password)
			userService.getUserRepository().save(user);
			System.out.println("Admin user updated: " + adminEmail + " / Admin@123");
		} else {
			// Create new admin user
			userService.registerUser("Admin User", adminEmail, "Admin@123", "ADMIN");
			System.out.println("Admin user created: " + adminEmail + " / Admin@123");
		}
	}
}

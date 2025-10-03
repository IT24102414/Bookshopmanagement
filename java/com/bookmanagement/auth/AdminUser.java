// src/main/java/com/bookmanagement/auth/AdminUser.java
package com.bookmanagement.auth;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("ADMIN")
public class AdminUser extends User {
    private String permissions;

    public AdminUser() {}

    public AdminUser(String username, String email, String password, String permissions) {
        setUsername(username);
        setEmail(email);
        setPassword(password);
        this.permissions = permissions;
    }
}
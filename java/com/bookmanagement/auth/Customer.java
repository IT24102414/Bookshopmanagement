// src/main/java/com/bookmanagement/auth/Customer.java
package com.bookmanagement.auth;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {
    private String fullName;
    private String shippingAddress;
    private String billingAddress;
    private String phoneNumber;
    private boolean marketingOptIn = false;
    private LocalDateTime lastProfileUpdate;

    public Customer() {}

    public Customer(String username, String email, String password) {
        setUsername(username);
        setEmail(email);
        setPassword(password);
    }
}
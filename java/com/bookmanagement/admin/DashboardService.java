package com.bookmanagement.admin;

import com.bookmanagement.order.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    @Autowired
    private OrderRepository orderRepository;

    public long getTotalOrderCount() {
        return orderRepository.count();
    }
    // Add more metric methods here (e.g., calculate revenue, count users)
}

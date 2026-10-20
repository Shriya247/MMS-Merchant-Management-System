
package com.mms;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/")
    public String home(Model model) {

        // Total merchants
        Integer totalMerchants = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM merchants",
            Integer.class
        );

        // Total payments
        Integer totalPayments = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM payments",
            Integer.class
        );

        // Successful payments
        Integer successfulPayments = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM payments WHERE payment_status = 'SUCCESS'",
            Integer.class
        );

        // Pending payments
        Integer pendingPayments = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM payments WHERE payment_status = 'PENDING'",
            Integer.class
        );

        // Failed payments
        Integer failedPayments = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM payments WHERE payment_status = 'FAILED'",
            Integer.class
        );

        // Total successful payment amount
        Double totalAmount = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(amount), 0) " +
            "FROM payments WHERE payment_status = 'SUCCESS'",
            Double.class
        );

        // Pending settlements
        Integer pendingSettlements = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM settlements " +
            "WHERE settlement_status = 'PENDING'",
            Integer.class
        );

        // Completed settlements
        Integer completedSettlements = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM settlements " +
            "WHERE settlement_status = 'COMPLETED'",
            Integer.class
        );

        // Latest five payments
        List<Map<String, Object>> recentPayments =
            jdbcTemplate.queryForList(
                "SELECT p.payment_id, m.merchant_name, " +
                "p.amount, p.payment_status " +
                "FROM payments p " +
                "JOIN merchants m " +
                "ON p.merchant_id = m.merchant_id " +
                "ORDER BY p.payment_id DESC LIMIT 5"
            );

        model.addAttribute("totalMerchants", totalMerchants);
        model.addAttribute("totalPayments", totalPayments);
        model.addAttribute("successfulPayments", successfulPayments);
        model.addAttribute("pendingPayments", pendingPayments);
        model.addAttribute("failedPayments", failedPayments);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("pendingSettlements", pendingSettlements);
        model.addAttribute("completedSettlements", completedSettlements);
        model.addAttribute("recentPayments", recentPayments);

        return "index";
    }
}

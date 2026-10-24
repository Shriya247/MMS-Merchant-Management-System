
package com.mms;

import java.math.BigDecimal;
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

        // TOTAL MERCHANTS
        Integer totalMerchants = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM merchants",
            Integer.class
        );

        // TOTAL PAYMENTS
        Integer totalPayments = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM payments",
            Integer.class
        );

        // SUCCESSFUL PAYMENTS
        Integer successfulPayments = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM payments " +
            "WHERE payment_status = 'SUCCESS'",
            Integer.class
        );

        // PENDING PAYMENTS
        Integer pendingPayments = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM payments " +
            "WHERE payment_status = 'PENDING'",
            Integer.class
        );

        // FAILED PAYMENTS
        Integer failedPayments = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM payments " +
            "WHERE payment_status = 'FAILED'",
            Integer.class
        );

        // TOTAL SUCCESSFUL PAYMENT AMOUNT
        BigDecimal totalAmount = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(amount), 0) " +
            "FROM payments WHERE payment_status = 'SUCCESS'",
            BigDecimal.class
        );

        // PAYMENT SUCCESS RATE
        double successRate = 0.0;

        if (totalPayments != null && totalPayments > 0
                && successfulPayments != null) {

            successRate =
                (successfulPayments * 100.0) / totalPayments;
        }

        // PENDING SETTLEMENTS
        Integer pendingSettlements = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM settlements " +
            "WHERE settlement_status = 'PENDING'",
            Integer.class
        );

        // COMPLETED SETTLEMENTS
        Integer completedSettlements = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM settlements " +
            "WHERE settlement_status = 'COMPLETED'",
            Integer.class
        );

        // LATEST FIVE PAYMENTS
        List<Map<String, Object>> recentPayments =
            jdbcTemplate.queryForList("""
                SELECT p.payment_id,
                       m.merchant_name,
                       p.amount,
                       p.payment_status
                FROM payments p
                JOIN merchants m
                  ON p.merchant_id = m.merchant_id
                ORDER BY p.payment_id DESC
                LIMIT 5
                """);

        // SEND DATA TO DASHBOARD PAGE
        model.addAttribute("totalMerchants", totalMerchants);
        model.addAttribute("totalPayments", totalPayments);
        model.addAttribute("successfulPayments", successfulPayments);
        model.addAttribute("pendingPayments", pendingPayments);
        model.addAttribute("failedPayments", failedPayments);
        model.addAttribute("totalAmount", totalAmount);

        // NEW ANALYTICS DATA
        model.addAttribute("successRate", successRate);

        model.addAttribute("pendingSettlements", pendingSettlements);
        model.addAttribute("completedSettlements", completedSettlements);
        model.addAttribute("recentPayments", recentPayments);

        return "index";
    }
}

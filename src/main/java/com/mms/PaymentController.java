
package com.mms;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PaymentController {

    private final JdbcTemplate jdbc;

    public PaymentController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // DISPLAY, SEARCH AND FILTER PAYMENTS
    @GetMapping("/payments")
    public String payments(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "ALL") String status,
            Model model) {

        // VALIDATE STATUS FILTER
        if (!status.equals("ALL")
                && !status.equals("PENDING")
                && !status.equals("SUCCESS")
                && !status.equals("FAILED")) {
            status = "ALL";
        }

        String sql = """
            SELECT p.*, m.merchant_name
            FROM payments p
            JOIN merchants m
              ON p.merchant_id = m.merchant_id
            WHERE (
                m.merchant_name LIKE ?
                OR CAST(p.payment_id AS CHAR) LIKE ?
                OR p.payment_method LIKE ?
            )
            """;

        if (!status.equals("ALL")) {
            sql += " AND p.payment_status = ?";
        }

        sql += " ORDER BY p.payment_id DESC";

        String keyword = "%" + search.trim() + "%";

        List<Map<String, Object>> payments;

        if (status.equals("ALL")) {

            payments = jdbc.queryForList(
                sql, keyword, keyword, keyword
            );

        } else {

            payments = jdbc.queryForList(
                sql, keyword, keyword, keyword, status
            );
        }

        // ACTIVE MERCHANTS FOR NEW PAYMENTS
        List<Map<String, Object>> merchants =
            jdbc.queryForList("""
                SELECT merchant_id, merchant_name
                FROM merchants
                WHERE status = 'ACTIVE'
                ORDER BY merchant_name ASC
                """);

        model.addAttribute("payments", payments);
        model.addAttribute("merchants", merchants);
        model.addAttribute("search", search);
        model.addAttribute("status", status);

        return "payments";
    }

    // ADD A NEW PAYMENT
    @PostMapping("/payments/add")
    public String addPayment(
            @RequestParam int merchantId,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod) {

        // CHECK PAYMENT AMOUNT
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "redirect:/payments?error=amount";
        }

        // CHECK PAYMENT METHOD
        paymentMethod = paymentMethod.trim();

        if (paymentMethod.isEmpty()) {
            return "redirect:/payments?error=method";
        }

        // CHECK WHETHER MERCHANT IS ACTIVE
        Integer activeCount = jdbc.queryForObject(
            "SELECT COUNT(*) FROM merchants " +
            "WHERE merchant_id = ? AND status = 'ACTIVE'",
            Integer.class,
            merchantId
        );

        if (activeCount == null || activeCount == 0) {
            return "redirect:/payments?error=merchant";
        }

        // CREATE PAYMENT WITH PENDING STATUS
        jdbc.update("""
            INSERT INTO payments
            (merchant_id, amount, payment_method, payment_status)
            VALUES (?, ?, ?, 'PENDING')
            """,
            merchantId, amount, paymentMethod
        );

        return "redirect:/payments?added=true";
    }

    // MARK A PENDING PAYMENT AS SUCCESSFUL
    @PostMapping("/payments/complete")
    public String completePayment(
            @RequestParam int paymentId) {

        int updated = jdbc.update("""
            UPDATE payments
            SET payment_status = 'SUCCESS'
            WHERE payment_id = ?
              AND payment_status = 'PENDING'
            """, paymentId);

        if (updated == 0) {
            return "redirect:/payments?error=invalidStatus";
        }

        return "redirect:/payments?completed=true";
    }

    // MARK A PENDING PAYMENT AS FAILED
    @PostMapping("/payments/fail")
    public String failPayment(
            @RequestParam int paymentId) {

        int updated = jdbc.update("""
            UPDATE payments
            SET payment_status = 'FAILED'
            WHERE payment_id = ?
              AND payment_status = 'PENDING'
            """, paymentId);

        if (updated == 0) {
            return "redirect:/payments?error=invalidStatus";
        }

        return "redirect:/payments?failed=true";
    }
}


package com.mms;

import java.math.BigDecimal;
import java.util.ArrayList;
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
            @RequestParam(defaultValue = "ALL") String method,
            Model model) {

        // VALIDATE STATUS FILTER
        if (!status.equals("ALL")
                && !status.equals("PENDING")
                && !status.equals("SUCCESS")
                && !status.equals("FAILED")) {
            status = "ALL";
        }

        // VALIDATE PAYMENT METHOD FILTER
        if (!method.equals("ALL")
                && !method.equals("UPI")
                && !method.equals("CARD")
                && !method.equals("NET_BANKING")
                && !method.equals("CASH")) {
            method = "ALL";
        }

        // BASE SQL QUERY
        StringBuilder sql = new StringBuilder("""
            SELECT p.*, m.merchant_name
            FROM payments p
            JOIN merchants m
              ON p.merchant_id = m.merchant_id
            WHERE (
                m.merchant_name LIKE ?
                OR CAST(p.payment_id AS CHAR) LIKE ?
                OR p.payment_method LIKE ?
            )
            """);

        String keyword = "%" + search.trim() + "%";

        List<Object> parameters = new ArrayList<>();

        parameters.add(keyword);
        parameters.add(keyword);
        parameters.add(keyword);

        // FILTER BY PAYMENT STATUS
        if (!status.equals("ALL")) {
            sql.append(" AND p.payment_status = ?");
            parameters.add(status);
        }

        // FILTER BY PAYMENT METHOD
        if (!method.equals("ALL")) {
            sql.append(" AND p.payment_method = ?");
            parameters.add(method);
        }

        sql.append(" ORDER BY p.payment_id DESC");

        // FETCH FILTERED PAYMENTS
        List<Map<String, Object>> payments =
            jdbc.queryForList(
                sql.toString(),
                parameters.toArray()
            );

        // ACTIVE MERCHANTS FOR NEW PAYMENTS
        List<Map<String, Object>> merchants =
            jdbc.queryForList("""
                SELECT merchant_id, merchant_name
                FROM merchants
                WHERE status = 'ACTIVE'
                ORDER BY merchant_name ASC
                """);

        // SEND DATA TO THYMELEAF
        model.addAttribute("payments", payments);
        model.addAttribute("merchants", merchants);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("method", method);

        return "payments";
    }

    // ADD A NEW PAYMENT
    @PostMapping("/payments/add")
    public String addPayment(
            @RequestParam int merchantId,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod) {

        // CHECK PAYMENT AMOUNT
        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "redirect:/payments?error=amount";
        }

        // VALIDATE PAYMENT METHOD
        if (paymentMethod == null) {
            return "redirect:/payments?error=method";
        }

        paymentMethod = paymentMethod.trim()
                                     .toUpperCase();

        if (!paymentMethod.equals("UPI")
                && !paymentMethod.equals("CARD")
                && !paymentMethod.equals("NET_BANKING")
                && !paymentMethod.equals("CASH")) {

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

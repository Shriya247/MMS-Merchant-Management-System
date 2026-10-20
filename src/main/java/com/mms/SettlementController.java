
package com.mms;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SettlementController {

    private final JdbcTemplate jdbc;

    public SettlementController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // DISPLAY SETTLEMENTS
    @GetMapping("/settlements")
    public String settlements(Model model) {

        // FETCH ALL SETTLEMENTS
        List<Map<String, Object>> settlements =
            jdbc.queryForList("""
                SELECT s.*, m.merchant_name
                FROM settlements s
                JOIN merchants m
                  ON s.merchant_id = m.merchant_id
                ORDER BY s.settlement_id DESC
                """);

        // FETCH SUCCESSFUL PAYMENTS NOT YET SETTLED
        List<Map<String, Object>> eligiblePayments =
            jdbc.queryForList("""
                SELECT p.payment_id,
                       p.amount,
                       m.merchant_name
                FROM payments p
                JOIN merchants m
                  ON p.merchant_id = m.merchant_id
                LEFT JOIN settlements s
                  ON p.payment_id = s.payment_id
                WHERE p.payment_status = 'SUCCESS'
                  AND s.settlement_id IS NULL
                ORDER BY p.payment_id DESC
                """);

        // SEND DATA TO THYMELEAF PAGE
        model.addAttribute("settlements", settlements);
        model.addAttribute("eligiblePayments", eligiblePayments);

        return "settlements";
    }

    // CREATE A NEW SETTLEMENT
    @PostMapping("/settlements/add")
    public String addSettlement(
            @RequestParam int paymentId) {

        // CHECK WHETHER PAYMENT IS ELIGIBLE
        List<Map<String, Object>> payments =
            jdbc.queryForList("""
                SELECT p.merchant_id, p.amount
                FROM payments p
                WHERE p.payment_id = ?
                  AND p.payment_status = 'SUCCESS'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM settlements s
                      WHERE s.payment_id = p.payment_id
                  )
                """, paymentId);

        // REJECT INVALID OR ALREADY SETTLED PAYMENTS
        if (payments.isEmpty()) {
            return "redirect:/settlements?error=invalid";
        }

        Map<String, Object> payment = payments.get(0);

        // GET MERCHANT ID
        int merchantId =
            ((Number) payment.get("merchant_id")).intValue();

        // GET PAYMENT AMOUNT
        BigDecimal amount =
            (BigDecimal) payment.get("amount");

        // CREATE SETTLEMENT
        try {

            jdbc.update("""
                INSERT INTO settlements
                (payment_id, merchant_id,
                 settlement_amount, settlement_status)
                VALUES (?, ?, ?, 'COMPLETED')
                """,
                paymentId,
                merchantId,
                amount
            );

        } catch (DuplicateKeyException e) {

            // HANDLE DUPLICATE SETTLEMENT
            return "redirect:/settlements?error=duplicate";
        }

        return "redirect:/settlements?added=true";
    }
}

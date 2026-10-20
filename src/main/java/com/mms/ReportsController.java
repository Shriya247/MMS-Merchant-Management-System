
package com.mms;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportsController {

    private final JdbcTemplate jdbc;

    public ReportsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // DISPLAY REPORTS AND ANALYTICS
    @GetMapping("/reports")
    public String reports(Model model) {

        // TOTAL ACTIVE MERCHANTS
        Integer activeMerchants = jdbc.queryForObject(
            "SELECT COUNT(*) FROM merchants " +
            "WHERE status = 'ACTIVE'",
            Integer.class
        );

        // TOTAL SUCCESSFUL PAYMENTS
        Integer successfulPayments = jdbc.queryForObject(
            "SELECT COUNT(*) FROM payments " +
            "WHERE payment_status = 'SUCCESS'",
            Integer.class
        );

        // TOTAL FAILED PAYMENTS
        Integer failedPayments = jdbc.queryForObject(
            "SELECT COUNT(*) FROM payments " +
            "WHERE payment_status = 'FAILED'",
            Integer.class
        );

        // TOTAL COMPLETED SETTLEMENT AMOUNT
        BigDecimal settledAmount = jdbc.queryForObject(
            "SELECT COALESCE(SUM(settlement_amount), 0) " +
            "FROM settlements " +
            "WHERE settlement_status = 'COMPLETED'",
            BigDecimal.class
        );

        // MERCHANT-WISE PERFORMANCE REPORT
        List<Map<String, Object>> merchantReport =
            jdbc.queryForList("""
                SELECT
                    m.merchant_id,
                    m.merchant_name,
                    COUNT(p.payment_id) AS payment_count,
                    COALESCE(
                        SUM(
                            CASE
                                WHEN p.payment_status = 'SUCCESS'
                                THEN p.amount
                                ELSE 0
                            END
                        ), 0
                    ) AS successful_amount
                FROM merchants m
                LEFT JOIN payments p
                  ON m.merchant_id = p.merchant_id
                GROUP BY
                    m.merchant_id,
                    m.merchant_name
                ORDER BY successful_amount DESC
                """);

        // PAYMENT METHOD ANALYSIS
        List<Map<String, Object>> methodReport =
            jdbc.queryForList("""
                SELECT
                    payment_method,
                    COUNT(*) AS payment_count,
                    COALESCE(
                        SUM(
                            CASE
                                WHEN payment_status = 'SUCCESS'
                                THEN amount
                                ELSE 0
                            END
                        ), 0
                    ) AS successful_amount
                FROM payments
                GROUP BY payment_method
                ORDER BY payment_count DESC
                """);

        // SEND DATA TO REPORTS PAGE
        model.addAttribute(
            "activeMerchants", activeMerchants
        );

        model.addAttribute(
            "successfulPayments", successfulPayments
        );

        model.addAttribute(
            "failedPayments", failedPayments
        );

        model.addAttribute(
            "settledAmount", settledAmount
        );

        model.addAttribute(
            "merchantReport", merchantReport
        );

        model.addAttribute(
            "methodReport", methodReport
        );

        return "reports";
    }
}

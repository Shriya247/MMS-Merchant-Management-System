
package com.mms;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;

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

        // NULL-SAFE VALUES FOR THYMELEAF
        model.addAttribute("activeMerchants",
            activeMerchants != null ? activeMerchants : 0);

        model.addAttribute("successfulPayments",
            successfulPayments != null ? successfulPayments : 0);

        model.addAttribute("failedPayments",
            failedPayments != null ? failedPayments : 0);

        model.addAttribute("settledAmount",
            settledAmount != null ? settledAmount : BigDecimal.ZERO);

        model.addAttribute("merchantReport", merchantReport);
        model.addAttribute("methodReport", methodReport);

        return "reports";
    }

    // DOWNLOAD PAYMENT REPORT AS CSV
    @GetMapping("/reports/download")
    public void downloadReport(HttpServletResponse response)
            throws java.io.IOException {

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(
            "Content-Disposition",
            "attachment; filename=\"MMS_Payment_Report.csv\""
        );

        List<Map<String, Object>> payments =
            jdbc.queryForList("""
                SELECT
                    p.payment_id,
                    m.merchant_name,
                    p.amount,
                    p.payment_method,
                    p.payment_status
                FROM payments p
                JOIN merchants m
                  ON p.merchant_id = m.merchant_id
                ORDER BY p.payment_id DESC
                """);

        var writer = response.getWriter();

        // CSV header
        writer.println(
            "Payment ID,Merchant Name,Amount,Payment Method,Status"
        );

        // CSV rows
        for (Map<String, Object> payment : payments) {
            writer.println(
                csv(payment.get("payment_id")) + "," +
                csv(payment.get("merchant_name")) + "," +
                csv(payment.get("amount")) + "," +
                csv(payment.get("payment_method")) + "," +
                csv(payment.get("payment_status"))
            );
        }

        writer.flush();
    }

    // SAFELY FORMAT VALUES FOR CSV
    private String csv(Object value) {
        String text = value == null ? "" : value.toString();

        // Prevent spreadsheet formula injection
        if (!text.isEmpty() &&
            ("=+-@\t\r\n".indexOf(text.charAt(0)) >= 0)) {
            text = "'" + text;
        }

        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}

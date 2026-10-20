
package com.mms;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MerchantController {

    private final JdbcTemplate jdbc;

    public MerchantController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // DISPLAY ALL MERCHANTS
    @GetMapping("/merchants")
    public String showMerchants(
            @RequestParam(defaultValue = "") String search,
            Model model) {

        List<Map<String, Object>> merchants;

        String keyword = "%" + search.trim() + "%";

        if (search.trim().isEmpty()) {

            merchants = jdbc.queryForList(
                "SELECT * FROM merchants " +
                "ORDER BY merchant_id ASC"
            );

        } else {

            merchants = jdbc.queryForList(
                "SELECT * FROM merchants " +
                "WHERE merchant_name LIKE ? " +
                "OR business_type LIKE ? " +
                "OR CAST(merchant_id AS CHAR) LIKE ? " +
                "ORDER BY merchant_id ASC",
                keyword,
                keyword,
                keyword
            );
        }

        model.addAttribute("merchants", merchants);
        model.addAttribute("search", search);

        return "merchants";
    }

    // ADD A NEW MERCHANT
    @PostMapping("/merchants/add")
    public String addMerchant(
            @RequestParam String merchantName,
            @RequestParam String businessType) {

        merchantName = merchantName.trim();
        businessType = businessType.trim();

        // VALIDATE INPUTS
        if (merchantName.isEmpty() || businessType.isEmpty()) {
            return "redirect:/merchants?error=invalid";
        }

        jdbc.update(
            "INSERT INTO merchants " +
            "(merchant_name, business_type, status) " +
            "VALUES (?, ?, 'ACTIVE')",
            merchantName,
            businessType
        );

        return "redirect:/merchants?added=true";
    }

    // UPDATE MERCHANT DETAILS
    @PostMapping("/merchants/edit")
    public String editMerchant(
            @RequestParam int merchantId,
            @RequestParam String merchantName,
            @RequestParam String businessType) {

        merchantName = merchantName.trim();
        businessType = businessType.trim();

        // VALIDATE INPUTS
        if (merchantName.isEmpty() || businessType.isEmpty()) {
            return "redirect:/merchants?error=invalid";
        }

        int updated = jdbc.update(
            "UPDATE merchants " +
            "SET merchant_name = ?, business_type = ? " +
            "WHERE merchant_id = ?",
            merchantName,
            businessType,
            merchantId
        );

        if (updated == 0) {
            return "redirect:/merchants?error=notFound";
        }

        return "redirect:/merchants?updated=true";
    }

    // ACTIVATE OR DEACTIVATE MERCHANT
    @PostMapping("/merchants/status")
    public String changeStatus(
            @RequestParam int merchantId) {

        int updated = jdbc.update(
            "UPDATE merchants " +
            "SET status = CASE " +
            "WHEN status = 'ACTIVE' THEN 'INACTIVE' " +
            "ELSE 'ACTIVE' END " +
            "WHERE merchant_id = ?",
            merchantId
        );

        if (updated == 0) {
            return "redirect:/merchants?error=notFound";
        }

        return "redirect:/merchants?statusChanged=true";
    }

    // DELETE MERCHANT SAFELY
    @PostMapping("/merchants/delete")
    public String deleteMerchant(
            @RequestParam int merchantId) {

        // CHECK WHETHER MERCHANT HAS PAYMENTS
        Integer paymentCount = jdbc.queryForObject(
            "SELECT COUNT(*) FROM payments " +
            "WHERE merchant_id = ?",
            Integer.class,
            merchantId
        );

        // BLOCK DELETION IF PAYMENT HISTORY EXISTS
        if (paymentCount != null && paymentCount > 0) {
            return "redirect:/merchants?error=hasPayments";
        }

        // DELETE ONLY IF NO RELATED RECORDS EXIST
        try {

            int deleted = jdbc.update(
                "DELETE FROM merchants " +
                "WHERE merchant_id = ?",
                merchantId
            );

            if (deleted == 0) {
                return "redirect:/merchants?error=notFound";
            }

        } catch (DataIntegrityViolationException ex) {

            // PROTECT AGAINST RELATED DATABASE RECORDS
            return "redirect:/merchants?error=hasPayments";
        }

        return "redirect:/merchants?deleted=true";
    }
}

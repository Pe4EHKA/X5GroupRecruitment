package com.x5.recruitment.infrastructure.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Utility class for phone number normalization.
 * Converts phone numbers to E.164 format.
 */
@Slf4j
public class PhoneNormalizer {

    private static final Pattern DIGITS_ONLY = Pattern.compile("[^0-9]");
    private static final Pattern RUSSIA_MOBILE = Pattern.compile("^(\\+?7|8)(\\d{10})$");

    /**
     * Normalize phone number to E.164 format.
     * Currently supports Russian phone numbers.
     * 
     * @param rawPhone Raw phone number string
     * @return Normalized phone in E.164 format (+7XXXXXXXXXX) or null if invalid
     */
    public static String normalizeToE164(String rawPhone) {
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            return null;
        }

        // Remove all non-digit characters
        String digitsOnly = DIGITS_ONLY.matcher(rawPhone.trim()).replaceAll("");

        // Handle Russian phone numbers
        // Expected formats: 7XXXXXXXXXX, 8XXXXXXXXXX, +7XXXXXXXXXX
        if (digitsOnly.length() == 11) {
            if (digitsOnly.startsWith("7") || digitsOnly.startsWith("8")) {
                return "+7" + digitsOnly.substring(1);
            }
        } else if (digitsOnly.length() == 10) {
            // Missing country code, assume Russia
            return "+7" + digitsOnly;
        }

        log.warn("Unable to normalize phone number to E.164: {}", rawPhone);
        return null;
    }

    /**
     * Check if phone number is valid.
     */
    public static boolean isValid(String phoneE164) {
        if (phoneE164 == null) {
            return false;
        }
        // Simple validation: starts with + and has 11-15 digits
        return phoneE164.matches("^\\+\\d{10,14}$");
    }
}

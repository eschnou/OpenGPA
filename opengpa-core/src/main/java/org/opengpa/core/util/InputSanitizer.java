package org.opengpa.core.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

public class InputSanitizer {

    /**
     * Sanitizes text for safe use in HTML and XML contexts.
     * - Removes all HTML tags
     * - Escapes special characters
     * - Trims whitespace
     * - Returns null if input is null
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        // Remove all HTML tags first
        String noHtml = Jsoup.clean(input, Safelist.none());

        // Escape special characters for XML
        // String escaped = StringEscapeUtils.escapeXml11(noHtml);

        // Normalize whitespace
        return noHtml.trim();
    }

    /**
     * Validates if the input contains any suspicious patterns
     * Returns true if input is safe, false otherwise
     */
    public static boolean isValid(String input) {
        if (input == null) {
            return false;
        }

        // Check for common dangerous patterns
        String[] dangerousPatterns = {
                "<script>", "</script>",
                "javascript:", "data:",
                "<!--", "-->",
                "<![CDATA[", "]]>",
                "&lt;script&gt;"
        };

        String lowercaseInput = input.toLowerCase();
        for (String pattern : dangerousPatterns) {
            if (lowercaseInput.contains(pattern.toLowerCase())) {
                return false;
            }
        }

        return true;
    }
}
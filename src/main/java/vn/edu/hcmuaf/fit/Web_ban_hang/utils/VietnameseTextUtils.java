package vn.edu.hcmuaf.fit.Web_ban_hang.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility class for Vietnamese text normalization.
 * Supports fuzzy search by stripping diacritical marks so that
 * users can search "dong ho" and find "đồng hồ".
 */
public class VietnameseTextUtils {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Removes all diacritical marks from a Vietnamese string and converts to
     * lowercase.
     * Examples:
     * "Đồng Hồ" → "dong ho"
     * "Vòng Tay" → "vong tay"
     * "Nhẫn Bạc" → "nhan bac"
     * 
     * @param text input text (may contain Vietnamese diacritics)
     * @return normalized lowercase ASCII-like string
     */
    public static String removeDiacritics(String text) {
        if (text == null || text.isEmpty())
            return "";

        // 'đ' / 'Đ' are exception is not decomposed by NFD, handle manually first
        String replaced = text
                .replace('đ', 'd')
                .replace('Đ', 'D');

        // NFD decomposition splits characters into base + combining marks
        String normalized = Normalizer.normalize(replaced, Normalizer.Form.NFD);

        // Strip all combining diacritical marks
        return DIACRITICS_PATTERN.matcher(normalized).replaceAll("").toLowerCase();
    }

    // check string source contain string query ? for fuzzy search
    public static boolean fuzzyContains(String source, String query) {
        if (source == null || query == null || query.isEmpty())
            return false;
        return removeDiacritics(source).contains(removeDiacritics(query));
    }
}

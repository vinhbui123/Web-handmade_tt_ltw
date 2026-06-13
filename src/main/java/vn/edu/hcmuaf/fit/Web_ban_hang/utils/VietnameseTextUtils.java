package vn.edu.hcmuaf.fit.Web_ban_hang.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;


public class VietnameseTextUtils {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    // loại bỏ dấu tiếng việt
    public static String removeDiacritics(String text) {
        if (text == null || text.isEmpty())
            return "";

        // Đ, đ là trường hợp đặc biệt không được xử lý bởi NFD
        String replaced = text
                .replace('đ', 'd')
                .replace('Đ', 'D');
        String normalized = Normalizer.normalize(replaced, Normalizer.Form.NFD);

        // loại bỏ các dấu thanh trong chuỗi tiếng Việt
        return DIACRITICS_PATTERN.matcher(normalized).replaceAll("").toLowerCase();
    }

    // kiểm tra chuỗi source có chứa chuỗi query không
    public static boolean fuzzyContains(String source, String query) {
        if (source == null || query == null || query.isEmpty())
            return false;
        return removeDiacritics(source).contains(removeDiacritics(query));
    }
}

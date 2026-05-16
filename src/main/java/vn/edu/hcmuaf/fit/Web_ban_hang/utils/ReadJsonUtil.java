package vn.edu.hcmuaf.fit.Web_ban_hang.utils;

import java.io.BufferedReader;
import java.io.IOException;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;

public class ReadJsonUtil {

    private static final Gson gson = new Gson();

    public static String read(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }


    //  Đọc JSON từ request body và parse thành object.
    //  Ví dụ: OrderDTO dto = ReadJsonUtil.parseJson(request, OrderDTO.class); để map cho các class khác
    
    public static <T> T parseJson(HttpServletRequest request, Class<T> map) throws IOException {
        String json = read(request);
        return gson.fromJson(json, map);
    }
}

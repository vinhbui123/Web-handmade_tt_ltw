package vn.edu.hcmuaf.fit.Web_ban_hang.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GHNHelper {

    public static String postJson(String url, String token, int shopId, String jsonBody) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Token", token);
        conn.setRequestProperty("ShopId", String.valueOf(shopId));

        OutputStream os = conn.getOutputStream();
        os.write(jsonBody.getBytes("UTF-8"));
        os.close();

        return readResponse(conn);
    }

    public static String getJson(String url, String token) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Token", token);
        return readResponse(conn);
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder res = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) res.append(inputLine);
        in.close();
        conn.disconnect();
        return res.toString();
    }
}
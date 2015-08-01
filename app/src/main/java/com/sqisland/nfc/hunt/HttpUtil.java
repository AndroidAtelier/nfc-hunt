package com.sqisland.nfc.hunt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * Created by brenda on 6/14/15.
 */
public class HttpUtil {

    public final static String KEY_SERVER_RESPONSE = "server_response";

    public static String buildURL(String args) {
        String base = "http://10.0.1.3:8080/v1/";
        StringBuilder builder = new StringBuilder();
        builder.append(base);
        builder.append(args);
        return builder.toString();
    }

    public static InputStream openHttpConnection(String urlStr) {
        InputStream in = null;
        int resCode = -1;

        try {
            URL url = new URL(urlStr);
            URLConnection urlConn = url.openConnection();

            if (!(urlConn instanceof HttpURLConnection)) {
                throw new IOException("URL is not an Http URL");
            }
            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            resCode = httpConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        }

        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

    /**
     * convenience method to do web call and return the result as JSONObject with key "server_response"
     * @param urlStr
     * @return
     */
    public static JSONObject doRequestFromURLAndGetResponseAsJSONObject(String urlStr) {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(urlStr);
            urlConn = url.openConnection();
            if (urlConn != null)
                urlConn.setReadTimeout(60 * 1000);
            if (urlConn != null && urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                if (bufferedReader != null) {
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                }
            }
            in.close();
        } catch (Exception e) {
            throw new RuntimeException("Exception while calling URL:"+ urlStr, e);
        }

        JSONArray jsonArray = null;
        JSONObject result = new JSONObject();
        try {
            jsonArray = new JSONArray(sb.toString());
            result.put(KEY_SERVER_RESPONSE, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}

package com.myproject.joy.bartapplication;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Vamshik B D on 2/24/2018.
 */

public class HttpHandler {
    private static final String TAG = HttpHandler.class.getSimpleName();

    public HttpHandler() {}

    public String makeServiceCall(String reqUrl) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");

            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            response = convertStreamToString(inputStream);
        } catch (MalformedURLException exception) {
            exception.printStackTrace();
            Log.e(TAG, "MalformedURLException occurred");
        } catch (ProtocolException exception) {
            exception.printStackTrace();
            Log.e(TAG, "ProtocolException occurred");
        } catch (IOException exception) {
            exception.printStackTrace();
            Log.e(TAG, "IOException occurred");
        }  catch (Exception exception) {
            exception.printStackTrace();
            Log.e(TAG, "Exception occurred");
        }
        return response;
    }

    private String convertStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();

        String line = "";

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            Log.e(TAG, "IOException occurred");
        } finally {
            try {
                inputStream.close();
            } catch (IOException exception) {
                exception.printStackTrace();
                Log.e(TAG, "IOException occurred");
            }
        }
        return stringBuilder.toString();
    }

    public int makeServerCall(HttpPost httpPost) {
        HttpClient client = new DefaultHttpClient();
        try {
            HttpResponse resp = client.execute(httpPost);

            return resp.getStatusLine().getStatusCode();
        } catch (IOException exception) {
            exception.printStackTrace();
            return -1;
        }
    }
}
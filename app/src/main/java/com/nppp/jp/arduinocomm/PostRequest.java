package com.nppp.jp.mimcom;


import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class PostRequest extends AsyncTask<String, Void, String> {
    public static final String REQUEST_METHOD = "POST";
    public static final int READ_TIMEOUT = 30000;
    public static final int CONNECTION_TIMEOUT = 30000;

    @Override
    protected String doInBackground(String... params) {
        String stringUrl = params[0];
        String stringData=params[1];
        try
        {
            //Create a URL object holding our url
            URL myUrl = new URL(stringUrl);
            //Create a connection
            HttpURLConnection connection =(HttpURLConnection)
                    myUrl.openConnection();
            //Set methods and timeouts
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            String urlParameters = "data="+stringData;
            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));

            System.out.println("\nSending 'POST' request to URL : " + myUrl);
            System.out.println("Post parameters : " + urlParameters);


            try {

                connection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();
                int status = connection.getResponseCode();
                System.out.println(status);
            } finally {
                connection.disconnect();
            }
            }
                catch(IOException e){
            e.printStackTrace();

        }
        return null;
    }
    protected void onPostExecute(String result){
        super.onPostExecute(result);
    }
}

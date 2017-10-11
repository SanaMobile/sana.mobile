package org.sana.android.service.messaging;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * Created by gil on 7/19/17.
 */

public class ExecuteRegisterIdWithServer extends AsyncTask<HttpRequest, Void, Integer> {
    HttpClient client = new DefaultHttpClient();
    @Override
    protected Integer doInBackground(HttpRequest... params) {
        // TODO Auto-generated method stub
        HttpRequest request = params[0];
        int responseCode = -1;
        HttpResponse response;
        try {
            response = client.execute((HttpUriRequest)request);

            Log.d("REGISTER request", response.toString());
            responseCode = response.getStatusLine().getStatusCode();
        } catch (ClientProtocolException e) {
            // This exception raised
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseCode;
    }
}

package org.sana.net.http.handler;

import com.google.gson.reflect.TypeToken;

import org.sana.net.Response;

import java.lang.reflect.Type;

/**
 *
 */
public class StringHandler extends ApiResponseHandler<Response<String>>{
    public static final String TAG = StringHandler.class.getSimpleName();

    @Override
    public Type getType() {
        final Type type = new TypeToken<Response<String>>(){}.getType();
        return type;
    }
}

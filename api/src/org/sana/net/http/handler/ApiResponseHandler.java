package org.sana.net.http.handler;

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class ApiResponseHandler<T> implements ResponseHandler<T>{

	final static Gson gson = new GsonBuilder()
    	.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    	.setDateFormat("yyyy-MM-dd HH:mm:ss")
    	.create();
	
	public abstract Type getType();
	
	@Override
	public T handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		String json = EntityUtils.toString(response.getEntity());
		Type type = getType();
		return gson.fromJson(json, type);
	}

	public static <K> K fromJson(String json, Type typeOf){
		return gson.fromJson(json, typeOf);
	}
	
}

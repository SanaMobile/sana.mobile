package org.sana.net.http.handler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.sana.core.Patient;
import org.sana.core.Procedure;
import org.sana.net.Response;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ProcedureResponseHandler extends ApiResponseHandler<Response<Collection<Procedure>>>{

	@Override
	public Type getType() {
		Type type = new TypeToken<Response<Collection<Procedure>>>(){}.getType();
		return type;
	}
}

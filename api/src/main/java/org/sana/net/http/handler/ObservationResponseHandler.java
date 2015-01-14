package org.sana.net.http.handler;

import java.lang.reflect.Type;
import java.util.Collection;

import org.sana.core.Observation;
import org.sana.net.Response;

import com.google.gson.reflect.TypeToken;

public class ObservationResponseHandler extends ApiResponseHandler<Response<Collection<Observation>>>{

	public Type getType() {
		final Type type = new TypeToken<Response<Collection<Observation>>>(){}.getType();
		return type;
	}
}


package org.sana.net.http.handler;

import java.lang.reflect.Type;
import java.util.Collection;
import org.sana.core.Concept;
import org.sana.net.Response;

import com.google.gson.reflect.TypeToken;

public class NotificationResponseHandler extends ApiResponseHandler<Response<Collection<Concept>>>{

	public Type getType() {
		final Type type = new TypeToken<Response<Collection<Concept>>>(){}.getType();
		return type;
	}
}


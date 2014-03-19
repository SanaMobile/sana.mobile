package org.sana.net.http.handler;

import java.lang.reflect.Type;
import java.util.Collection;

import org.sana.core.Notification;
import org.sana.net.Response;

import com.google.gson.reflect.TypeToken;

public class InstructionResponseHandler extends ApiResponseHandler<Response<Collection<Notification>>>{

	public Type getType() {
		final Type type = new TypeToken<Response<Collection<Notification>>>(){}.getType();
		return type;
	}
}


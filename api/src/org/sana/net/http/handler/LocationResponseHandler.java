package org.sana.net.http.handler;

import java.lang.reflect.Type;
import java.util.Collection;

import org.sana.core.Location;
import org.sana.net.Response;
import com.google.gson.reflect.TypeToken;

public class LocationResponseHandler extends ApiResponseHandler<Response<Collection<Location>>>{

	@Override
	public Type getType() {
		Type typeOf = new TypeToken<Response<Collection<Location>>>(){}.getType();
		return typeOf;
	}
}

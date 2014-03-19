package org.sana.net.http.handler;

import java.lang.reflect.Type;
import java.util.Collection;

import org.sana.core.Device;
import org.sana.net.Response;

import com.google.gson.reflect.TypeToken;

public class DeviceResponseHandler extends ApiResponseHandler<Response<Collection<Device>>>{

	public Type getType() {
		final Type type = new TypeToken<Response<Collection<Device>>>(){}.getType();
		return type;
	}
}


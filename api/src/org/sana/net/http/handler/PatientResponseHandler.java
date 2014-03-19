package org.sana.net.http.handler;

import java.lang.reflect.Type;
import java.util.Collection;

import org.sana.core.Patient;
import org.sana.net.Response;

import com.google.gson.reflect.TypeToken;

public class PatientResponseHandler extends ApiResponseHandler<Response<Collection<Patient>>>{

	@Override
	public Type getType() {
		Type type = new TypeToken<Response<Collection<Patient>>>(){}.getType();
		return type;
	}
}

package org.sana.net.http.handler;

import java.lang.reflect.Type;
import java.util.Collection;

import org.sana.api.task.EncounterTask;
import org.sana.net.Response;

import com.google.gson.reflect.TypeToken;

public class EncounterResponseHandler extends ApiResponseHandler<Response<Collection<EncounterTask>>>{

	@Override
	public Type getType() {
		Type typeOf = new TypeToken<Response<Collection<EncounterTask>>>(){}.getType();
		return typeOf;
	}
}

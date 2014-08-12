package org.sana.net.http.handler;

import java.io.IOException;
import java.lang.reflect.Type;

import java.util.Collection;

import org.sana.api.task.EncounterTask;
import org.sana.net.Response;

import com.google.gson.reflect.TypeToken;

public class EncounterTaskResponseHandler extends ApiResponseHandler<Response<Collection<EncounterTask>>>{

	public Type getType() {
		Type typeOf = new TypeToken<Response<Collection<EncounterTask>>>(){}.getType();
		return typeOf;
	}
}

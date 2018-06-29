package org.sana.net.http.handler;

import java.lang.reflect.Type;
import java.util.Collection;

import org.sana.core.Procedure;
import org.sana.net.Response;

import com.google.gson.reflect.TypeToken;

public class ProcedureResponseHandler extends ApiResponseHandler<Response<Collection<Procedure>>>{

	@Override
	public Type getType() {
		Type type = new TypeToken<Response<Collection<Procedure>>>(){}.getType();
		return type;
	}
}

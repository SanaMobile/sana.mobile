package org.sana.net.http.handler;

import java.lang.reflect.Type;
import java.util.Collection;

import org.sana.api.task.ObservationTask;
import org.sana.net.Response;

import com.google.gson.reflect.TypeToken;

public class ObservationTaskResponseHandler extends ApiResponseHandler<Response<Collection<ObservationTask>>>{

    public Type getType() {
        final Type type = new TypeToken<Response<Collection<ObservationTask>>>(){}.getType();
        return type;
    }
}

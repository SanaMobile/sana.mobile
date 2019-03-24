package org.sana.net.http.handler;

import com.google.gson.reflect.TypeToken;

import org.sana.core.ProcedureGroup;
import org.sana.net.Response;

import java.lang.reflect.Type;
import java.util.Collection;

public class ProcedureGroupResponseHandler extends ApiResponseHandler<Response<Collection<ProcedureGroup>>> {
    @Override
    public Type getType() {
        Type type = new TypeToken<Response<Collection<ProcedureGroup>>>() {
        }.getType();
        return type;
    }
}

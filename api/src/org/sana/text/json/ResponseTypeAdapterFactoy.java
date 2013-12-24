/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sana nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.text.json;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.sana.net.Response;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * @author Sana Development
 *
 */
public class ResponseTypeAdapterFactoy implements TypeAdapterFactory {


	/* (non-Javadoc)
	 * @see com.google.gson.TypeAdapterFactory#create(com.google.gson.Gson, com.google.gson.reflect.TypeToken)
	 */
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
	    Type type = typeToken.getType();
	    if (typeToken.getRawType() != Response.class
	               || !(type instanceof ParameterizedType)) {
	           return null;
	    }
	   Type msgType = ((ParameterizedType) type).getActualTypeArguments()[0];
	   TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(msgType));
	   return (TypeAdapter<T>) newResponseAdapter(adapter);
	}
	
	private <E> TypeAdapter<Response<E>> newResponseAdapter(final TypeAdapter<E> adapter) 
	{
	   return new TypeAdapter<Response<E>>(){
	       @Override
		   public void write(JsonWriter out, Response<E> value) throws IOException 
		   {
	           if (value == null) {
	              out.nullValue();
	                   return;
	            }
	           out.name("status").value(value.getStatus());
	           out.name("code").value(value.getCode());
	           adapter.write(out, value.getMessage());
	        }

			@Override
			public Response<E> read(JsonReader in) throws IOException {
	             if (in.peek() == JsonToken.NULL) {
		               in.nextNull();
		               return null;
		         }
	             Response<E> result = Response.empty();
	             in.beginObject();
	             while(in.hasNext()){
	            	 JsonToken next = in.peek();
	            	 switch(next){
	            	 case BEGIN_ARRAY:
	            		 
	            		 break;
	            	 case BEGIN_OBJECT:
	            		 break;
	            	 case NAME:
	            		 String name = in.nextName();
	            	 	 if(name.equals("status")){
	            	 		result.setStatus(in.nextString());
	            	 	 } else if(name.equals("code")){
	            	 		int code = in.nextInt();
	            	 		result.setCode(code);
	            	 	 } else if(name.equals("message")){
	            	 		E msg = adapter.read(in);
	            		 	result.setMessage(msg);
	            		 }
	            	 	 break;
	            	 default:
	            		 break;
	            	 }
	             }
	             in.endObject();
	             return result;
			}
	    	
			
	      };
	}
}

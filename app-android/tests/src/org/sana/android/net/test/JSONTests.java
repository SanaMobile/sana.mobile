package org.sana.android.net.test;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

import org.sana.core.Location;
import org.sana.core.Patient;
import org.sana.net.Response;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import android.test.AndroidTestCase;

public class JSONTests extends AndroidTestCase {

	String locationJSON = "{ \"status\": \"SUCCESS\","+
			"\"message\": [ " +
			"{ \"uuid\": \"29da1cb1-a8da-486d-80c8-0fbd4ee3f9ca\", \"name\": \"Test Location\" }, " +
			"{ \"uuid\": \"c8aaebf9-094c-404a-ba06-2dec3dfc3f00\", \"name\": \"Test Location #2\" }, " +
			"{ \"uuid\": \"d4b7efb4-7ade-4700-936c-7bf34b0feef1\", \"name\": \"Unknown Location\" }, " +
			"{ \"uuid\": \"1c34034f-5222-4f70-9300-b8af7cae6cef\", \"name\": \"Sapoty\" }, " +
			"{ \"uuid\": \"c671411f-95e2-4a98-819c-55c0d8e5e00f\", \"name\": \"Saintange\" }, " +
			"{ \"uuid\": \"92fd981c-960b-4881-a4f5-c724f009f67c\", \"name\": \"Bideau\" }, " +
			"{ \"uuid\": \"d474cced-52f8-467f-8ff1-63d205576cc7\", \"name\": \"Bastien\" }, " +
			"{ \"uuid\": \"d038df73-e122-44f2-9f56-7761499981f2\", \"name\": \"Etan Coicu\" }, " +
			"{ \"uuid\": \"5e880ce8-73a7-46e9-82d1-8b335aafb331\", \"name\": \"Decouverte\" }, " +
			"{ \"uuid\": \"64b11bd6-9b1f-4402-b42f-f7a2d40cf0dd\", \"name\": \"Modele\" }, " +
			"{ \"uuid\": \"bc1531c8-a949-404f-ab7d-909ad409cd10\", \"name\": \"Penyen\" }, " +
			"{ \"uuid\": \"914a4a6b-95bd-4f97-993a-a08c3874289c\", \"name\": \"Mayambe\" }, " +
			"{ \"uuid\": \"4224696a-1157-43af-a6cd-b7cddb392544\", \"name\": \"Ca Charles/Morne Charles\" }, " +
			"{ \"uuid\": \"e380810e-c1c1-4258-a493-11335a0087d5\", \"name\": \"Morte\" }, " +
			"{ \"uuid\": \"b7f82dcf-b9a5-45d3-a0f8-6fd526f3f632\", \"name\": \"Nan Bois Pin\" } " +
			"], \"code\": 200 }";
	
	public void testLocation(){
		Gson gson = new GsonBuilder()
	     .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
	     .setDateFormat("yyyy-MM-dd HH:mm:ss")
	     .create();
		Type type = new TypeToken<Response<Collection<Location>>>(){}.getType();
		
		Collection<Location> objs = Collections.EMPTY_LIST;
		try{
				Response<Collection<Location>> response = gson.fromJson(locationJSON, type);
				objs = response.message;
		} catch (Exception e){
			
		}
		assertTrue(objs.size() == 15);
	}
	
}

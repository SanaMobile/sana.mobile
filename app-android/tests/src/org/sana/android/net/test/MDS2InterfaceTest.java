package org.sana.android.net.test;

import java.util.Collection;
import java.util.Collections;
import java.net.URI;

import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.content.Uris;
import org.sana.android.net.MDSInterface2;
import org.sana.android.provider.Concepts;
import org.sana.android.provider.EncounterTasks;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.api.task.EncounterTask;
import org.sana.core.Concept;
import org.sana.core.Location;
import org.sana.core.Patient;
import org.sana.core.Procedure;
import org.sana.net.Response;
import org.sana.net.http.handler.ConceptResponseHandler;
import org.sana.net.http.handler.EncounterResponseHandler;
import org.sana.net.http.handler.LocationResponseHandler;
import org.sana.net.http.handler.PatientResponseHandler;
import org.sana.net.http.handler.ProcedureResponseHandler;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import android.util.Log;

public class MDS2InterfaceTest  extends AndroidTestCase {

	String username = "test";
	String password = "t3st";
	Uri[] uris = new Uri[]{};
	Context mContext;

	String scheme, host, rootPath;
	int port;
	public void setUp(){
		mContext = this.getContext();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		host = preferences.getString(Constants.PREFERENCE_MDS_URL, host);
		// Realistically should never use http
		boolean useSecure = preferences.getBoolean(
				Constants.PREFERENCE_SECURE_TRANSMISSION, true);
		scheme = (useSecure)? "https": "http";
		host = mContext.getString(R.string.host_mds);
		port = 443;
		rootPath = mContext.getString(R.string.path_root);
	}
	
	public void testLocationGet(){
		Uri target = Uri.parse("content://org.sana/core/location");
		Collection<Location> objs = Collections.emptyList();
		try {
		URI uri = Uris.iriToURI(target , scheme, host, port, rootPath );
		Log.d("MDS2InterfaceTest", "GET: " + uri);
		LocationResponseHandler handler = new LocationResponseHandler();

			Response<Collection<Location>> response = MDSInterface2.apiGet(uri, handler);
			Log.i("MDS2InterfaceTest", "LOCATIONS " + response);
			objs = response.message;
			for(Location obj:objs){
				Log.i("MDS2InterfaceTest", obj.name );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(objs.size() > 0);
	}
	
	public void testEncounterTaskGet(){
		Collection<EncounterTask> objs = Collections.emptyList();
		Uri target = EncounterTasks.CONTENT_URI;
		try {
		URI uri = Uris.iriToURI(target , scheme, host, port, rootPath );
		Log.d("MDS2InterfaceTest", "GET: " + uri);
		EncounterResponseHandler handler = new EncounterResponseHandler();
			Response<Collection<EncounterTask>> response = MDSInterface2.apiGet(uri,handler);
			Log.i("MDS2InterfaceTest", "TASKS " + response);
			objs = response.message;
			for(EncounterTask task:objs){
				Log.d("MDS2InterfaceTest", "<EncounterTask " + task.status.current +" " + task.assigned_to.getUsername() + " >");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(objs.size() > 0);
		
	}
	
	public void testConceptGet(){
		ConceptResponseHandler handler = new ConceptResponseHandler();
		Collection<Concept> objs = Collections.emptyList();
		Uri target = Concepts.CONTENT_URI;
		try {
		URI uri = Uris.iriToURI(target , scheme, host, port, rootPath );
			Response<Collection<Concept>> response = MDSInterface2.apiGet(uri,handler);
			Log.i("MDS2InterfaceTest", "CONCEPTS" + response);
			objs = response.message;
			for(Concept obj:objs){
				Log.i("MDS2InterfaceTest", "<Concept "+ obj.name+">" );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(objs.size() > 0);
	}
		
	public void testPatientGet(){
		Uri target = Subjects.CONTENT_URI;
		Log.d("MDS2InterfaceTest", "GET: " + target);
		PatientResponseHandler handler = new PatientResponseHandler();
		Collection<Patient> objs = Collections.emptyList();
		try {
			URI uri = Uris.iriToURI(target , scheme, host, port, rootPath );
			Response<Collection<Patient>> response = MDSInterface2.apiGet(uri, handler);
			Log.i("MDS2InterfaceTest", "PATIENTS" + response);
			objs = response.message;
			for(Patient obj:objs){
				Log.i("MDS2InterfaceTest", "<Patient " + obj.system_id +" >");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(objs.size() > 0);
	}
	
	public void testProcedureGet(){
		Uri target = Procedures.CONTENT_URI;
		Log.d("MDS2InterfaceTest", "GET: " + target);
		ProcedureResponseHandler handler = new ProcedureResponseHandler();
		Response<Collection<Procedure>> response = null;
		Collection<Procedure> objs = Collections.emptyList();
		try {
			URI uri = Uris.iriToURI(target , scheme, host, port, rootPath );
			response = MDSInterface2.apiGet(uri, handler);
			Log.i("MDS2InterfaceTest", "CONCEPTS" + response);
			objs = response.message;
			for(Procedure obj:objs){
				Log.i("MDS2InterfaceTest", "<Procedure "+obj.title +" >" );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(objs.size() > 0);
	}
}

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
package org.sana.android.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;


import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.activity.EncounterList;
import org.sana.android.activity.MainActivity;
import org.sana.android.app.Locales;
import org.sana.android.app.NotificationFactory;
import org.sana.android.content.ModelContext;
import org.sana.android.content.ModelEntity;
import org.sana.android.content.Uris;
import org.sana.android.content.Intents;
import org.sana.android.content.core.PatientWrapper;
import org.sana.android.db.ModelWrapper;
import org.sana.android.net.MDSInterface;
import org.sana.android.net.MDSInterface2;
import org.sana.android.provider.EncounterTasks;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.BaseContract;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.service.QueueManager;
import org.sana.android.util.Logf;
import org.sana.android.util.SanaUtil;

import org.sana.api.task.EncounterTask;
import org.sana.core.Model;
import org.sana.core.Patient;
import org.sana.core.Procedure;
import org.sana.net.Response;
import org.sana.core.Subject;
import org.sana.net.http.HttpTaskFactory;
import org.sana.net.http.handler.EncounterResponseHandler;
import org.sana.net.http.handler.EncounterTaskResponseHandler;

import org.sana.net.http.handler.PatientResponseHandler;
import org.sana.util.DateUtil;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

/**
 * Implementation of the dispatch server as an Android service.
 *
 * @author Sana Development
 *
 */
public class DispatchService extends Service{
    public static final String TAG = DispatchService.class.getSimpleName();

    static final int REQUEST = 0;
    static final int RESPONSE = 1;
    public static final int IN_PROGRESS = 0;
    public static final int COMPLETE = 1;
    public static final int UPLOAD_RESPONSE = 1;
    public static final String RESPONSE_NOTIFICATION_ID = "notification_id";
    public static final String RESPONSE_CODE = "code";

    public static final int NO_BROADCAST = -1;

    public class DispatchCallback implements Handler.Callback{

        PatientResponseHandler pHandler = new PatientResponseHandler();
            /**
             * Message passed should have the following values
             * what - REQUEST(0) or RESPONSE(1)
             * arg1 - startId passed to startService()
             * arg2 - Uri descriptor
             * obj - Uri string of the intent
             * data - additional query/post parameters
             *
             * Request will be sent from initial call to handle message which
             * will trigger a second callback placed on the Handler for the
             * response. Hence, Response may not be handled in the same order as
             * the original request.
             */
            @Override
            public boolean handleMessage(Message msg) {

                return true;
            }
    }

    //TODO Refactor this out.
    abstract static class SyncHandler<T> {
        SyncHandler(){}

        public abstract List<ContentValues> values(Response<T> response);

        public Response<T> fromJson(Message msg){
            Log.d("JSONHandler<T>", msg.obj.toString());
            Type type = new TypeToken<Response<T>>(){}.getType();


            Gson gson = new GsonBuilder()
                     .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                     .setDateFormat("yyyy-MM-dd HH:mm:ss")
                     .create();
            Response<T> response = gson.fromJson(msg.obj.toString(), type);
            Log.d("JSONHandler<T>", msg.obj.toString());
            return response;
        }


        public Response<T> fromJson(String json){
            return null;
        }

        public abstract ContentValues[] values(T t);



    }

    final SyncHandler<List<Procedure>> procedureListHandler = new SyncHandler<List<Procedure>>(){

        @Override
        public List<ContentValues> values(Response<List<Procedure>> response) {

            List<ContentValues> list = new ArrayList<ContentValues>();
            for(Procedure p: response.getMessage()){
                ContentValues vals = new ContentValues();
                vals.put(Procedures.Contract.UUID, p.getUuid());
                vals.put(Procedures.Contract.TITLE, p.getDescription());
                vals.put(Procedures.Contract.AUTHOR, p.getAuthor());
                vals.put(Procedures.Contract.VERSION, p.getVersion());
                vals.put(Procedures.Contract.PROCEDURE, p.getSrc());
                list.add(vals);
            }
            return list;
        }

        @Override
        public ContentValues[] values(List<Procedure> t) {
            // TODO Auto-generated method stub
            return null;
        }


    };

    final SyncHandler<Collection<Patient>> patientListHandler = new SyncHandler<Collection<Patient>>(){
        public  Response<Collection<Patient>> fromJson(Message msg){
            Type type = new TypeToken<Response<List<Patient>>>(){}.getType();
            Gson gson = new GsonBuilder()
                     .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                     .setDateFormat(DispatchService.this.getString(R.string.cfg_format_date_value))
                     .create();
            Response<Collection<Patient>> response = gson.fromJson(msg.obj.toString(), type);
            return response;
        }

        public  Response<Collection<Patient>> fromJson(String json){
            Type type = new TypeToken<Response<List<Patient>>>(){}.getType();
            Gson gson = new GsonBuilder()
                     .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                     .setDateFormat(DispatchService.this.getString(R.string.cfg_format_date_value))
                     .create();
            Response<Collection<Patient>> response = gson.fromJson(json, type);
            return response;

        }

        @Override
        public List<ContentValues> values(Response<Collection<Patient>> response) {
            List<ContentValues> list = new ArrayList<ContentValues>();
            for(Patient p: response.getMessage()){
                Log.d(TAG, p.system_id);
                ContentValues vals = new ContentValues();
                vals.put(Patients.Contract.GIVEN_NAME, p.getGiven_name());
                vals.put(Patients.Contract.FAMILY_NAME, p.getFamily_name());
                vals.put(Patients.Contract.GENDER, p.getGender());
                vals.put(Patients.Contract.LOCATION, p.getLocation().getUuid());
                vals.put(Patients.Contract.UUID, p.getUuid());
                vals.put(Patients.Contract.PATIENT_ID, p.system_id);
                vals.put(Patients.Contract.DOB, p.getDob().toString());
                if(p.getImage() != null && (p.getImage().getPath().endsWith("jpg") || p.getImage().getPath().endsWith("png"))){
                    try{
                        File dir = ModelContext.getExternalFilesDir(Subjects.CONTENT_URI);
                        //String path = media.getAbsolutePath() + "/" +  p.getImage().toASCIIString();
                        File f = new File(dir, p.getImage().toASCIIString());
                        if(f.isDirectory()){
                            Log.d(TAG, "deleting erroneous directory " + f.getAbsolutePath());
                            f.delete();
                        }
                        if(f.exists()){
                            // no need to download again
                            Log.d(TAG, "File exists " + f.getAbsolutePath());
                            if(f.isDirectory())
                                f.delete();
                            else
                                vals.put(Patients.Contract.IMAGE, Uri.fromFile(f).toString());
                        } else {
                            // TODO Add image download here
                            Log.d(TAG, "Need to download file to: " + f.getAbsolutePath());
                            if(f.exists() && f.isDirectory())
                                f.delete();
                            f.getParentFile().mkdirs();
                            boolean result = MDSInterface2.getFile(DispatchService.this, "media/" + p.getImage().toASCIIString(), f);
                            Log.d(TAG, "Download success: " + result);
                            vals.put(Patients.Contract.IMAGE, Uri.fromFile(f).toString());

                        }
                    } catch(Exception e){
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    try{
                        File dir = ModelContext.getExternalFilesDir(Subjects.CONTENT_URI);
                        Log.d(TAG, dir.getAbsolutePath());
                        File f = new File(dir, p.getImage().toASCIIString());
                        if(f.isDirectory() && f.delete()){
                            Log.d(TAG, "deleting erroneous directory " + f.getAbsolutePath());
                        }
                    } catch(Exception e){
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
                list.add(vals);
            }
            return list;
        }

        @Override
        public ContentValues[] values(Collection<Patient> t) {
            // TODO Auto-generated method stub
            return null;
        }
    };

    final SyncHandler<Collection<EncounterTask>> encounterTasksHandler = new SyncHandler<Collection<EncounterTask>>(){

        @Override
        public List<ContentValues> values(Response<Collection<EncounterTask>> response) {
            List<ContentValues> values = new ArrayList<ContentValues>(response.message.size());
            for(EncounterTask task:response.message){
                ContentValues value = new ContentValues();
                value.put(EncounterTasks.Contract.UUID , task.uuid);
                value.put(EncounterTasks.Contract.DUE_DATE , DateUtil.format
                        (task.due_on));
                value.put(EncounterTasks.Contract.PROCEDURE , task.procedure.uuid);
                value.put(EncounterTasks.Contract.SUBJECT , task.subject.uuid );
                value.put(EncounterTasks.Contract.ENCOUNTER, task.encounter.uuid);
                value.put(EncounterTasks.Contract.OBSERVER , task.assigned_to.uuid);
                value.put(EncounterTasks.Contract.STATUS , task.getStatus());
                values.add(value);
            }
            return values;
        }

        @Override
        public ContentValues[] values(Collection<EncounterTask> t) {
            ContentValues[] values = new ContentValues[t.size()];
            Iterator<EncounterTask> iterator =  t.iterator();
            int index = 0;
            while(iterator.hasNext()){
                EncounterTask task = iterator.next();
                ContentValues value = new ContentValues();
                value.put(EncounterTasks.Contract.UUID , task.uuid);
                value.put(EncounterTasks.Contract.DUE_DATE , DateUtil.format
                        (task.due_on));
                value.put(EncounterTasks.Contract.PROCEDURE , task.procedure.uuid);
                value.put(EncounterTasks.Contract.SUBJECT , task.subject.uuid );
                if(task.encounter != null)
                    value.put(EncounterTasks.Contract.ENCOUNTER, task.encounter.uuid);
                value.put(EncounterTasks.Contract.OBSERVER , task.assigned_to.uuid);
                value.put(EncounterTasks.Contract.STATUS , task.getStatus());
                values[index] = value;
                index++;
            }
            return values;
        }
    };
    EncounterTaskResponseHandler eTaskHandler = new EncounterTaskResponseHandler();
    static final int create = 0x00000001;
    static final int read = 0x00000010;
    static final int update = 0x00000100;
    static final int delete = 0x000001000;

    static final int CREATE = new Intent(Intents.ACTION_CREATE).filterHashCode();
    static final int READ = new Intent(Intents.ACTION_READ).filterHashCode();
    static final int UPDATE = new Intent(Intents.ACTION_UPDATE).filterHashCode();
    static final int DELETE = new Intent(Intents.ACTION_DELETE).filterHashCode();

    static final IntentFilter filter = new IntentFilter();
    static{
        filter.addAction(Intents.ACTION_CREATE);
        filter.addAction(Intents.ACTION_READ);
        filter.addAction(Intents.ACTION_UPDATE);
        filter.addAction(Intents.ACTION_DELETE);
        filter.addDataScheme("content");
        filter.addDataAuthority("org.sana.provider", null);
    }

    static final IntentFilter pkgFilter = new IntentFilter();
    static{
        pkgFilter.addAction(Intents.ACTION_READ);
        pkgFilter.addAction(Intents.ACTION_UPDATE);
        pkgFilter.addDataScheme("package");
    }

    public static final String PKG = "application/vnd.android.package-archive";
    public static final int PKG_MASK = 0x00000000;
    static final AtomicInteger sNotificationCount = new AtomicInteger(0);

    // Callback we pass to the Handler Thread to execute the requests.
    protected Handler.Callback getHandlerCallback(){
        return new Handler.Callback() {

            /*
             * (non-Javadoc)
             * @see android.os.Handler.Callback#handleMessage(android.os.Message)
             */
            /**
             * Message passed should have the following values
             * what - REQUEST(0) or RESPONSE(1)
             * arg1 - startId passed to startService()
             * arg2 - Uri descriptor
             * obj - Uri string of the intent
             * data - additional query/post parameters
             *
             * Request will be sent from initial call to handle message which
             * will trigger a second callback placed on the Handler for the
             * response. Hence, Response may not be handled in the same order as
             * the original request.
             */
            @Override
            public boolean handleMessage(Message msg) {
                if(msg != null)
                    Logf.D(TAG, "handleMessage()", String.format(
                        "Message what: %d, arg1: %d, arg2: %d",
                        msg.what,msg.arg1,msg.arg2));
                else{
                    Logf.W(TAG, "handleMessage()", "Null message. Was it supposed to be?");
                    return true;
                }
                DispatchService.this.handleRequestStart(1,msg.what);
                int what = msg.what;
                int arg1 = msg.arg1;
                int arg2 = msg.arg2;
                Object obj = (msg.obj != null)? msg.obj: null;
                Bundle data = msg.getData();
                if(data != null)
                    data = new Bundle(data);
                int startId = msg.arg1;
                int mode = msg.arg2;
                // set up for results
                ContentValues[] values = null;
                int index = 0;
                Cursor c = null;

                // Result broadcast Content
                String bcastMessage = "";
                String bcastMessages = null;
                int bcastCode = NO_BROADCAST;
                try {
                    SharedPreferences preferences = PreferenceManager
                            .getDefaultSharedPreferences(DispatchService.this);
                    String username = preferences.getString(
                            Constants.PREFERENCE_EMR_USERNAME, Constants.DEFAULT_USERNAME);
                    String password = preferences.getString(
                            Constants.PREFERENCE_EMR_PASSWORD, Constants.DEFAULT_PASSWORD);
                    switch (msg.arg2) {
                    case REQUEST:
                        Log.i(TAG, "...handleMessage(Message) + Got a " +
                                "REQUEST");
                        HttpUriRequest request = null;
                        HttpResponse httpResponse = null;
                        String responseString = null;
                        HttpClient client = HttpTaskFactory.CLIENT_FACTORY.produce();

                        Intent intent = Intent.parseUri(msg.obj.toString(), 0);
                        String action = intent.getAction();
                        int flags = intent.getFlags();
                        // set the request method.
                        String method = "GET";
                        if (action.contains("CREATE"))
                            method = "POST";
                        else if (action.contains("READ"))
                            method = "GET";
                        else if (action.contains("UPDATE"))
                            method = "PUT";
                        else if (action.contains("DELTE"))
                            method = "DELETE";

                        Log.d(TAG, "...handleMessage()" +
                                String.format("Method: %s", method));
                        Uri msgUri = intent.getData();
                        Log.d(TAG,"..." + msgUri.getSchemeSpecificPart());
                        String path = "";
                        String query = "";
                        if(msgUri != null){
                            path = msgUri.getPath();
                            query = msgUri.getEncodedQuery();
                        }

                        if(!Uris.isEmpty(msgUri)){
                            // Broadcast that something is happening
                            broadcastResult(msgUri,
                                Response.Code.CONTINUE.code,
                                R.string.general_uploading);
                        }

                        URI uri = MDSInterface2.getURI(DispatchService.this, path, query);

                        Logf.D(TAG, "handleMessage()", "method: " + method
                                + ", uri: " + uri);

                        // Set up for results
                        ContentValues update = new ContentValues();


                        switch (msg.what) {
                        case Uris.ITEM_FILE:
                            if(MDSInterface2.getFile(DispatchService.this, msgUri)){
                                Log.d(TAG, "....File download success: " + msgUri);
                            } else {
                                Log.d(TAG, "....File download fail: " + msgUri);
                            }
                            bcastCode = NO_BROADCAST;
                            break;
                        case Uris.ENCOUNTER_DIR:
                            // TODO implement as a query from msg.data
                            break;
                        case Uris.ENCOUNTER_UUID:
                        case Uris.ENCOUNTER_ITEM:

                            // TODO Allows GET or POST
                            if (method.equals("GET"))
                                request = new HttpGet(uri);
                            else if (method.equals("POST")) {
                                try{
                                boolean encounterPost = MDSInterface2
                                        .postProcedureToDjangoServer(
                                                intent.getData(),
                                                DispatchService.this);
                                // Send notification to notification bar

                                // Notification intent
                                Intent notifyIntent = new Intent(
                                        DispatchService.this,
                                        EncounterList.class);
                                // Sets the Activity to start in a new, empty
                                // task
                                notifyIntent
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                if (encounterPost) {
                                    update.put(
                                            Encounters.Contract.UPLOAD_STATUS,
                                            QueueManager.UPLOAD_STATUS_SUCCESS);
                                    DispatchService.this.getContentResolver()
                                            .update(intent.getData(), update,
                                                    null, null);
                                    //DispatchService.this.notify(
                                    //      R.string.upload_success,
                                    //      notifyIntent);
                                    //DispatchService.this.notifyForeground(
                                    //      UPLOAD_RESPONSE,
                                    //      R.string.upload_success,
                                    //      notifyIntent);
                                    Locales.updateLocale(DispatchService.this, getString(R.string.force_locale));
                                    bcastMessage = getString(R.string.upload_success);
                                    bcastCode = 200;
                                } else {
                                    update.put(
                                            Encounters.Contract.UPLOAD_STATUS,
                                            QueueManager.UPLOAD_STATUS_FAILURE);
                                    DispatchService.this.getContentResolver()
                                            .update(intent.getData(), update,
                                                    null, null);

                                    addFailedToQueue(what, arg1, arg2, obj, data, msgUri);
                                    //DispatchService.this.notifyForeground(
                                    //      UPLOAD_RESPONSE,
                                    //      R.string.upload_fail,
                                    //      notifyIntent);

                                    Locales.updateLocale(DispatchService.this, getString(R.string.force_locale));
                                    bcastMessage = getString(R.string.upload_fail);
                                    bcastCode = 400;
                                }
                                }
                                catch (Exception e){
                                    addFailedToQueue(what, arg1, arg2, obj, data, msgUri);
                                    Log.e(TAG, "POST failed: " + msgUri);
                                    Log.e(TAG,"...." + e.getMessage());
                                    Locales.updateLocale(DispatchService.this, getString(R.string.force_locale));
                                    bcastMessage = getString(R.string.upload_fail);
                                    bcastCode = 400;
                                }

                            }
                            break;
                        case Uris.OBSERVATION_DIR:
                            // TODO implement as a query from msg.data
                            break;
                        case Uris.OBSERVATION_UUID:
                            // TODO Allows GET or POST
                            if (method.equals("GET"))
                                request = new HttpGet(uri);
                            else if (method.equals("POST")) {
                                request = new HttpPost(uri);
                            }
                            break;
                        case Uris.PROCEDURE_DIR:
                            // TODO Allows GET only for updating procedures from
                            // repository
                            //uri = MDSInterface2.getURI(DispatchService.this, );
                            // only allows get
                            request = new HttpGet(uri);
                            break;
                        case Uris.PROCEDURE_UUID:
                            // TODO Allows GET. This should pull raw xml
                            if (method.equals("GET"))
                                request = new HttpGet(uri);
                            break;
                        case Uris.SUBJECT_DIR:

                            try {
                                //List<ContentValues> content = new ArrayList<ContentValues>();
                                PatientResponseHandler pHandler = new PatientResponseHandler();
                                Response<Collection<Patient>> patientListResponse = MDSInterface2.apiGet(uri,username,password,
                                    pHandler);
                                bcastCode = createOrUpdateSubjects(patientListResponse.message, startId);
                                bcastMessage = "";
                                Log.d(TAG, "" +Uris.SUBJECT_DIR+"...code " + bcastCode);
                            } catch (Exception e) {
                                    Log.e(TAG,"...." + e.getMessage());
                                    Locales.updateLocale(DispatchService.this, getString(R.string.force_locale));
                                    //bcastMessage = e.getMessage();
                                    bcastCode = 400;
                            }
                            break;
                        case Uris.SUBJECT_UUID:
                        case Uris.SUBJECT_ITEM:
                            Patient patient = PatientWrapper.get
                                    (DispatchService.this, msgUri);
                            PatientResponseHandler pHandler = new PatientResponseHandler();
                            Response<Collection<Patient>> patientResponse =
                                    null;
                            Log.d(TAG, "...method=" + method + ", " +
                                    "data=" + msgUri);
                            if (method.equals("GET"))
                                request = new HttpGet(uri);
                            else if (method.equals("POST")) {
                                patientResponse = MDSInterface2.postPatient(
                                            DispatchService.this, patient,
                                            username,password, pHandler);
                                Log.d(TAG, "...response: code=" +
                                        patientResponse.getCode() + ", data=" +
                                        patientResponse.getMessage());
                                bcastCode = createOrUpdateSubjects(
                                        patientResponse.message, startId);
                                // If successful create, we need to swap the
                                // client side uuid out with the server side
                                // value
                                if(bcastCode == 200) {
                                    bcastCode = 201;
                                    List<Patient> pList = new
                                            ArrayList<Patient>
                                            (patientResponse.getMessage());
                                    Patient p = pList.get(0);
                                    String uuid = ModelWrapper.getUuid(msgUri,
                                            DispatchService.this.getContentResolver());
                                    if(!uuid.equalsIgnoreCase(p.getUuid())){
                                        int isTemp = intent.getFlags()
                                                & Intents.FLAG_REPLACE;
                                        if(isTemp == 0){
                                            DispatchService.this
                                                    .getContentResolver()
                                                    .delete(msgUri,null,null);
                                        }
                                        Uri u = Uris.withAppendedUuid
                                                (Subjects.CONTENT_URI, p.getUuid());
                                        bcastMessage = u.toString();
                                    } else {
                                        bcastMessage = msgUri.toString();
                                    }
                                }
                                Log.d(TAG, "...method=" + method + ", " +
                                        "data=" + msgUri);
                            } else if (method.equals("PUT")) {
                                patientResponse = MDSInterface2.updatePatient(
                                        DispatchService.this, patient,
                                        username,password, pHandler);
                                Log.d(TAG, "...response: code=" + patientResponse
                                        .getCode() + ", data=" + patientResponse.getMessage());
                                bcastCode = patientResponse.getCode();
                                if(patientResponse.code == 200) {
                                    bcastMessage = msgUri.toString();
                                }
                            }
                            break;
                        case Uris.ENCOUNTER_TASK_DIR:
                            if (method.equals("GET")){
                                EncounterTaskResponseHandler handler = new EncounterTaskResponseHandler();
                                Collection<EncounterTask> objs = Collections.emptyList();
                                try {
                                    Response<Collection<EncounterTask>> response = MDSInterface2.apiGet(uri,username,password,handler);
                                    objs = response.message;
                                    Log.i(TAG, "GET EncounterTask: Returned " +
                                            "n=" + objs.size());
                                    bcastCode = createOrUpdateEncounterTasks(response.message, startId);

                                } catch (Exception e) {
                                    Log.w(TAG, "GET failed: " + uri
                                            .toASCIIString());
                                    Log.w(TAG,"...." + e.getMessage());
                                    e.printStackTrace();
                                    Locales.updateLocale(DispatchService.this, getString(R.string.force_locale));
                                    bcastMessage = e.getMessage();//getString(R.string.upload_fail);
                                    bcastCode = 400;
                                }
                            }
                            break;
                        case Uris.ENCOUNTER_TASK_ITEM:
                        case Uris.ENCOUNTER_TASK_UUID:
                            try{
                                if (method.equals("PUT")) {
                                    Log.i(TAG, "....Updating task: " + msgUri);
                                    Bundle form = data.getBundle("form");
                                    Log.d(TAG, "....form size: " + ((form != null)? form.size():"NULL FORM"));
                                    Response<Collection<EncounterTask>> e = MDSInterface2.syncUpdate(DispatchService.this,
                                            msgUri, username, password, form, null, eTaskHandler);
                                    Log.d(TAG, "....UPDATE " + e.status +" --> " + e.message);
                                    if(e.code != 200)
                                        addFailedToQueue(what, arg1, arg2, obj, data, msgUri);
                                    else
                                        bcastMessage = getString(R.string.upload_success);

                                }
                            } catch(Exception e){
                                addFailedToQueue(what, arg1, arg2, obj, data, msgUri);
                                Log.e(TAG, "PUT failed: " + msgUri);
                                Log.e(TAG,"...." + e.getMessage());
                                Locales.updateLocale(DispatchService.this, getString(R.string.force_locale));
                                bcastMessage = e.getMessage();
                                bcastCode = 400;
                            }
                            break;
                        case Uris.PACKAGE_DIR:
                            Logf.D(TAG, "handleMessage(Message)",
                                    "PACKAGE update request");
                            uri = MDSInterface2.getURI(DispatchService.this,DispatchService.this
                                    .getString(R.string.path_app));
                            request = new HttpGet(uri);
                            break;
                        default:
                        }
                        Log.d(TAG, "...REQUEST: " + msgUri);
                        Log.d(TAG, "...   code="+ bcastCode);

                        if(bcastCode != NO_BROADCAST)
                            broadcastResult(intent.getData(),bcastCode,bcastMessage);
                        break;

                    case RESPONSE:
                        Logf.I(TAG, "handleResponse(Message)",
                                "Got a RESPONSE: " + msg.obj);
                        Response<?> response;
                        List<ContentValues> content = new ArrayList<ContentValues>();
                        if (msg.obj == null) {
                            Logf.W(TAG, "handleResponse(Message)",
                                    "NULL RESPONSE");
                            break;
                        }
                        switch (msg.what) {
                        case Uris.ENCOUNTER_DIR:
                            // TODO implement as a query from msg.data
                            break;
                        case Uris.ENCOUNTER_ITEM:
                        case Uris.ENCOUNTER_UUID:
                            Logf.D(TAG, String.valueOf(msg.obj));
                            break;
                        case Uris.OBSERVATION_DIR:
                            // TODO implement as a query from msg.data
                            break;
                        case Uris.OBSERVATION_ITEM:
                        case Uris.OBSERVATION_UUID:
                            // TODO Allows GET or POST
                            break;
                        case Uris.PROCEDURE_DIR:
                            /*
                             Response<List<Procedure>> p =
                             procedureListHandler.fromJson(msg);
                             content = procedureListHandler.values(p);
                             for(ContentValues v: content){
                             ModelWrapper.insertOrUpdate(Procedures.CONTENT_URI, v, getContentResolver());

                             }
                             */
                            break;
                        case Uris.PROCEDURE_ITEM:
                        case Uris.PROCEDURE_UUID:
                            // TODO Allows GET. This should pull raw xml
                            break;
                        case Uris.SUBJECT_DIR:
                            /*
                            for (ContentValues v : content) {
                                ModelWrapper.insertOrUpdate(
                                        Subjects.CONTENT_URI, v,
                                        getContentResolver());
                            }
                            */
                            // getContentResolver().notifyChange(Subjects.CONTENT_URI,
                            // null);
                            break;
                        case Uris.SUBJECT_ITEM:
                        case Uris.SUBJECT_UUID:
                            // TODO
                            Response<Patient> patientResponse;
                            break;
                        case Uris.PACKAGE_DIR:
                            Logf.D(TAG, "handleMessage(Message)",
                                    "PACKAGE update response: " + msg.obj);
                            // This will download and install new apk
                            break;
                        default:
                        }
                        break;
                    default:

                    }
                    DispatchService.this.handleRequestComplete(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //stopSelf(msg.arg1);
                return true;
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////
    int mStartMode;                             // indicates how to behave if the service is killed
    final IBinder mBinder = new Binder();       // interface for clients that bind
    boolean mAllowRebind;                       // indicates whether onRebind should be used
    private Looper mServiceLooper;
    private Handler mHandler;
    private boolean initialized = false;
    private NotificationFactory mNotificationFactory;

    private AtomicInteger numNotifications = new AtomicInteger(0);
    private QueueControl failQueue = new QueueControl();

    ////////////////////////////////////////////////////////////////////////////
    //  Begin Overridden methods
    ////////////////////////////////////////////////////////////////////////////
    /*
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        HandlerThread thread = new HandlerThread("dispatcher",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mHandler = new Handler(mServiceLooper, getHandlerCallback());
        if(!initialized)
            initialized = checkInit();
        mNotificationFactory = NotificationFactory.getInstance(this);
        mNotificationFactory.setContentTitle(R.string.network_alert);
      }

    /* (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /* (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()" + ((intent != null)? intent.getAction(): null));
        Log.d(TAG, "..." + intent);
        if (intent != null) {
            // handleCommand(intent);
            // We want this service to continue running until it is explicitly
            // stopped, so return sticky.
            int matchesPackage = pkgFilter.match(getContentResolver(), intent,
                    false, TAG);
            int matchesModel = filter.match(getContentResolver(), intent,
                    false, TAG);
            int what = Uris.getDescriptor(intent.getData());
            Log.d(TAG, String.format("...Message what: %d, match: %d, pkg: %d",
                    what, matchesModel, matchesPackage));

            // msg.what <=> [REQUEST|RESPONSE] and msg.arg1 <=> startId
            if (matchesModel >= 0) {
                Message msg = mHandler.obtainMessage(what,
                        intent.toUri(Intent.URI_INTENT_SCHEME));
                msg.arg1 = startId;
                msg.arg2 = REQUEST;
                Bundle data = intent.getExtras();
                Log.d(TAG,"onStartCommand() --> " + ((data != null)?data.size():"NULL"));
                msg.setData(intent.getExtras());
                mHandler.sendMessage(msg);
            } else if (matchesPackage >= 0) {
                Message msg = mHandler.obtainMessage(what,
                        intent.toUri(Intent.URI_INTENT_SCHEME));
                msg.arg1 = startId;
                msg.arg2 = REQUEST;
                msg.setData(intent.getExtras());
                mHandler.sendMessage(msg);
            } else {
                Log.e(TAG, String.format(
                        "Unrecognized message. what: %d, match: %d", what,
                        matchesPackage));
                //stopSelf(startId);
            }

        } else {
            Log.w(TAG, "onStartCommand(): Null intent. Are we resuming?");
            //stopSelf(startId);
        }
        mStartMode = START_STICKY;
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Logf.D(TAG, "onDestroy()", "...finishing");
        try{
            if(!failQueue.isEmpty()){
                Log.w(TAG, "RESEND queue is not empty");
            }
            failQueue.cancel();
            mNotificationFactory.cancelAll();
        } catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean stopService(Intent intent){
        Logf.I(TAG, "stopService()",
                    (intent != null)? intent.toUri(Intent.URI_INTENT_SCHEME): "null");
        return super.stopService(intent);
        /*
        if(intent != null){
            Uri data = intent.getData();
            int what = -1;
            if(!Uris.isEmpty(data)){
                what = Uris.getDescriptor(data);
                if(failQueue.contains(what)){
                    return true;
                }
            }
        */
        /*
            failQueue.cancel();
            try{
                int notification = intent.getIntExtra(RESPONSE_NOTIFICATION_ID, 0);
                if (notification != 0){
                    mNotificationFactory.cancel(notification);
                    if(sNotificationCount.decrementAndGet() == 0){
                        mNotificationFactory.cancelAll();
                        mNotificationFactory = null;
                        stopForeground(true);
                        return super.stopService(intent);
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        } else {
            mNotificationFactory = null;
            return super.stopService(intent);
        }
        return false;
        */
    }

    private final boolean checkInit(){
        Logf.D(TAG, "initialize()", "Entering");
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int version = 0;
        String dbKey = getString(R.string.cfg_db_init);
        String dbVersion = getString(R.string.cfg_db_version);
        // check whether the db is initialized and create if not
        boolean doInit = preferences.getBoolean(dbKey, false);
        Logf.D(TAG, "initialize()", "dbs initialized: " + doInit);
        if(!doInit){
            getContentResolver().acquireContentProviderClient(Procedures.CONTENT_URI).release();
            getContentResolver().delete(Procedures.CONTENT_URI, null, null);
            SanaUtil.loadDefaultDatabase(getBaseContext());
            preferences.edit().putBoolean(dbKey, true)
                .putInt(dbVersion, getResources().getInteger(R.integer.cfg_db_version_value))
                .putBoolean(dbKey, true)
                .commit();
        }
        return true;
    }

    //TODO Refactor these out
    /**
     * Returns the number of ms until the next sync should occur
     *
     * @param uri
     * @return
     */
    private final long nextSync(Uri uri){
        final String METHOD = "delta(Uri)";

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(DispatchService.this);
        String key = "last_sync";

        switch(Uris.getDescriptor(uri)){
        default:
            key = "last_sync";
        }
        long now = new Date().getTime();
        long tdelta = Long.valueOf(getString(R.string.sync_delta));
        long delta = now - preferences.getLong(key, 0);
        return delta;
    }

    private final void resetSync(Uri uri){
        final String METHOD = "resetSync(Uri)";

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(DispatchService.this);

        String key = null;
        switch(Uris.getDescriptor(uri)){
        default:
                key = "last_sync";
        }
        preferences.edit().putLong(key, new Date().getTime()).commit();
    }

    // The dispatch server URI
    URI uHost = null;
    HttpClient mClient = null;
    HttpHost mHost = null;



    protected void build() throws URISyntaxException{

        URI uri = MDSInterface2.getRoot(this);
        mHost = new HttpHost(uri.getHost(),uri.getPort(),uri.getScheme());

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        AuthScope authScope = new AuthScope(mHost.getHostName(), mHost.getPort());
        Credentials creds = new UsernamePasswordCredentials("username", "password");
        credsProvider.setCredentials(authScope,creds);

        mClient = new DefaultHttpClient();
        ((AbstractHttpClient) mClient).getCredentialsProvider().setCredentials(
                authScope, creds);
    }

    protected final void notifyForeground(int id, int resID, Intent notifyIntent){
        // Pending intent used to launch the notification intent
        PendingIntent actionIntent =
                PendingIntent.getActivity(
                getBaseContext(),
                0,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        // Always force the locale before we send the notification
        Locales.updateLocale(getBaseContext(), getString(R.string.force_locale));
        Notification notification = mNotificationFactory
                .setContentIntent(actionIntent)
                .setContentText(resID)
                .setNumber(numNotifications.incrementAndGet())
                .build();
        sNotificationCount.incrementAndGet();
        startForeground(id, notification);
    }

    protected final void notify(int resID, Intent notifyIntent){
        Log.d(TAG, "notify(...) " + resID +", " + notifyIntent.toUri(Intent.URI_INTENT_SCHEME));
        // Pending intent used to launch the notification intent
        PendingIntent actionIntent =
                PendingIntent.getActivity(
                getBaseContext(),
                0,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        // Always force the locale before we send the notification
        Locales.updateLocale(getBaseContext(), getString(R.string.force_locale));
        mNotificationFactory
            .setContentIntent(actionIntent)
            .setContentText(resID)
            .doNotify();
    }

    protected final void notify(int resID, int code, Intent notifyIntent){
        Log.d(TAG, "notify(...) " + resID + ", " + notifyIntent.toUri(Intent.URI_INTENT_SCHEME));
        // Pending intent used to launch the notification intent
        PendingIntent actionIntent =
                PendingIntent.getActivity(
                getBaseContext(),
                0,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        // Always force the locale before we send the notification
        Locales.updateLocale(getBaseContext(), getString(R.string.force_locale));
        mNotificationFactory
            .setContentIntent(actionIntent)
            .setContentText(resID, code)
            .doNotify();
    }

    protected final void broadcastResult(Uri data, int code, String message){
        Log.i(TAG,"broadcastResult() code=" + code
            + ", uri=" + data
            + ", message=" + message);
        Intent broadcast = new Intent(Response.RESPONSE,data);
        broadcast.putExtra(Response.MESSAGE, message);
        broadcast.putExtra(Response.CODE, code);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(broadcast);
    }

    protected final void broadcastResult(Uri data, int code, int message){
        Locales.updateLocale(DispatchService.this, getString(R.string.force_locale));
        this.broadcastResult(data, code, getString(message));
    }

    protected void handleEncounterTasks(List<EncounterTasks> tasks){

    }

    protected void handleSubject(Patient subject){

    }

    static <T> T readJSONStream(HttpURLConnection url) throws IOException{
        T t = null;
        JsonReader reader = new JsonReader(new InputStreamReader(url.getInputStream()));
        Type type = new TypeToken<T>(){}.getType();
        t = new Gson().fromJson(reader, type);
        return t;
    }

    static <T> T readJSONStream(HttpURLConnection url, Context context) throws IOException{
        T t = null;
        JsonReader reader = new JsonReader(new InputStreamReader(url.getInputStream()));
        final Gson gson = new GsonBuilder()
         .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
         .setDateFormat(context.getString(R.string.cfg_format_date_value))
         .create();
        Type type = new TypeToken<T>(){}.getType();
        return gson.fromJson(reader, type);
    }

    static <T> T readJSONStream(String string, Context context) throws IOException{
        final Gson gson = new GsonBuilder()
         .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
         .setDateFormat(context.getString(R.string.cfg_format_date_value))
         .create();
        Type type = new TypeToken<T>(){}.getType();
        return gson.fromJson(string, type);
    }




    public static String readCharStream(HttpURLConnection url) throws IOException{
        InputStreamReader in = null;
        StringBuilder builder = new StringBuilder();
        char[] buf = new char[80];
        in = new InputStreamReader(url.getInputStream());
        while(in.read(buf) > -1){
            builder.append(buf);
        }
        return builder.toString().trim();

    }

    public static Uri readByteStream(HttpURLConnection url, String output) throws IOException{
        File file = new File(output);
        OutputStream out = null;
        InputStream in = null;
        try{
            in = new BufferedInputStream(url.getInputStream());
            out = (BufferedOutputStream) new BufferedOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[1024];
            while(in.read(buffer) > -1){
                out.write(buffer);
            }
        } finally {
            in.close();
            out.close();
        }
        return Uri.fromFile(new File(output));
    }

    private final void notifyPackageManager(final Context context, Uri apk){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apk, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
        context.startActivity(intent);
    }


    private ContentValues[] toArray(List<ContentValues> values){
        int size = (values != null)? values.size():0;
        ContentValues[] array = new ContentValues[size];
        int index = 0;
        while(index < size){
            array[index] = values.get(index);
            index++;
        }
        return array;
    }

    public final boolean exists(Uri uri, Model obj){
        boolean exists = false;
        Cursor c = null;
        try{
            Uri target = Uri.parse(uri + "/" + obj.uuid);
            c = getContentResolver().query(target,null,null,null,null);
            if(c!= null && c.getCount() == 1){
                exists = true;
            }
        } finally {
            if(c != null) c.close();
        }
        return exists;
    }
    //TODO Handle the modified part
    public Uri getFileIfNotExistsOrNotModified(URI remote, File dir,
        ContentValues vals, int startId)
    {
        Log.i(TAG, "getFileIfNotExists() remote=" + remote.toASCIIString()
                    +", dir=" +dir.getPath()
                    + ", startId=" + startId);
        Uri result = Uri.EMPTY;
        try{
            File local = new File(dir, remote.toASCIIString());
            if(local.isDirectory()){
                Log.w(TAG, "....deleting erroneous directory " + local.getAbsolutePath());
                local.delete();
            }
            if(!local.exists()){
                try{
                    Log.d(TAG, "....Need to download file to: " + local.getAbsolutePath());
                    if(local.isDirectory())
                        local.delete();
                    local.getParentFile().mkdirs();
                    // Try to fetch and add to ContentVals if success
                    result = Uri.fromFile(local);
                    sendFileGetDispatchSelf(result,startId);
                    vals.put(Patients.Contract.IMAGE, result.toString());
                } catch(Exception e){
                    Log.w(TAG, "....Something went wront getting" + remote.toASCIIString());
                    e.printStackTrace();
                    result = Uri.EMPTY;
                }
            } else {
                // no need to download again
                // TODO Check file size/mod time
                Log.d(TAG, "....File exists " + local.getAbsolutePath());
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }


    public final int createOrUpdateSubjects(Collection<Patient> t, int startId) {
        Log.i(TAG, "createOrUpdatePatients(Collection<Patient>,int)");
        int size = (t != null)?t.size():0;
        Log.d(TAG,"...size="+size);
        // return a 404 not found code if size is zero
        if(size == 0)
            return Response.Code.NOT_FOUND.code;

        Uri result = Uri.EMPTY;
        final File dir = ModelContext.getExternalFilesDir(Subjects.CONTENT_URI);

        // Containers for instances that must be inserted or updated
        List<ContentValues> insert = new ArrayList<ContentValues>();
        List<ModelEntity> update = new ArrayList<ModelEntity>();

        // Begin process of iterating over the list
        int index = 0;
        Iterator<Patient> iterator =  t.iterator();
        while(iterator.hasNext()){
            Patient p = iterator.next();
            ContentValues vals = new ContentValues();
            vals.put(Patients.Contract.GIVEN_NAME, p.getGiven_name());
            vals.put(Patients.Contract.FAMILY_NAME, p.getFamily_name());
            vals.put(Patients.Contract.GENDER, p.getGender());
            vals.put(Patients.Contract.LOCATION, p.getLocation().getUuid());
            vals.put(Patients.Contract.PATIENT_ID, p.system_id);
            vals.put(Patients.Contract.DOB, DateUtil.format(p.getDob()));
            ////////////////////////////////////////////////////////////
            // Handle images
            ////////////////////////////////////////////////////////////
            File file = new File(dir, p.getImage().toASCIIString());
            // check that file is valid
            if(file != null
                && (file.getPath().endsWith("jpg") || file.getPath().endsWith("png")))
            {
                getFileIfNotExistsOrNotModified(p.getImage(), dir, vals, startId);
            }

            // Don't add uuid initially
            if(!exists(Subjects.CONTENT_URI, p)){
                vals.put(Patients.Contract.UUID, p.uuid);
                insert.add(vals);
            } else {
                update.add(
                        new ModelEntity(
                            Uris.withAppendedUuid(Subjects.CONTENT_URI, p.uuid),
                            vals));
            }

        }
        // Handle the insert(s)
        int inserted = getContentResolver().bulkInsert(Subjects.CONTENT_URI,
                toArray(insert));
        Log.d(TAG, "....inserted=" + inserted);

        // Handle the update(s)
        int updated = 0;
        for(ModelEntity me:update){
            updated += getContentResolver().update(me.getUri(),
                    me.getEntityValues(),null,null);
        }
        Log.d(TAG, "....updates=" + updated);
        // Successful return a 200 code
        return Response.Code.OK.code;
    }

    public final int createOrUpdateEncounterTasks(Collection<EncounterTask> t, int startId) {
        Log.i(TAG, "createOrUpdateEncounterTasks() size="
                    +((t != null)?t.size():"null"));
        int result = 400;
        ContentValues[] values = null;
        Map<String,Subject> subjects = new HashMap<String,Subject>();
        List<ContentValues> insert = new ArrayList<ContentValues>();
        List<ModelEntity> update = new ArrayList<ModelEntity>();

        Iterator<EncounterTask> iterator =  t.iterator();
            int index = 0;
            while(iterator.hasNext()){
                EncounterTask task = iterator.next();
                ContentValues value = new ContentValues();
                value.put(EncounterTasks.Contract.UUID , task.uuid);
                value.put(EncounterTasks.Contract.DUE_DATE ,
                        DateUtil.format(task.due_on));
                value.put(EncounterTasks.Contract.PROCEDURE , task.procedure.uuid);
                value.put(EncounterTasks.Contract.SUBJECT , task.subject.uuid );
                subjects.put(task.subject.uuid, task.subject);
                if(task.encounter != null)
                    value.put(EncounterTasks.Contract.ENCOUNTER, task.encounter.uuid);
                value.put(EncounterTasks.Contract.OBSERVER , task.assigned_to.uuid);
                value.put(EncounterTasks.Contract.STATUS , task.getStatus());
                if (task.completed != null) {
                    value.put(EncounterTasks.Contract.COMPLETED,
                            DateUtil.format(task.completed));
                }
                if (task.started != null) {
                    value.put(EncounterTasks.Contract.STARTED,
                            DateUtil.format(task.started));
                }
                if(!exists(EncounterTasks.CONTENT_URI, task))
                    insert.add(value);
                else
                    update.add(
                        new ModelEntity(
                            Uris.withAppendedUuid(EncounterTasks.CONTENT_URI, task.uuid),
                            value));
            }

            int inserted = getContentResolver().bulkInsert(EncounterTasks.CONTENT_URI, toArray(insert));
            Log.d(TAG, "....inserted=" + inserted);
            Log.d(TAG, "....updates=" + update.size());
            int updated = 0;
            for(ModelEntity me:update){
                updated += getContentResolver().update(me.getUri(),me.getEntityValues(),null,null);
            }
            Log.d(TAG, "....updated=" + updated);
            //createOrUpdateSubjects(patients.values(), startId);
            result = 200;
            return result;
        }

    final Handler.Callback updateCheckRunnable(Handler handler) throws URISyntaxException{

        Intent intent = new Intent(getString(R.string.intent_action_read));
        intent.setType("application/vnd.android.package-archive");


        URI uri = MDSInterface2.getRoot(DispatchService.this);
        final Message message = handler.obtainMessage(RESPONSE);
        try {
            final URL url = uri.toURL();

            return new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    try {
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        Response<String> response = readJSONStream(connection);
                        message.obj = response;
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
            }};

        } catch (MalformedURLException e1) {
            throw new IllegalArgumentException(e1);
        }
    }

    public final void sendFileGetDispatchSelf(Uri file, int startId){
        Intent intent = new Intent(Intents.ACTION_READ, file);
        Message msg = mHandler.obtainMessage(Uris.ITEM_FILE,
                        intent.toUri(Intent.URI_INTENT_SCHEME));
                msg.arg1 = startId;
                msg.arg2 = REQUEST;
                mHandler.sendMessage(msg);
    }

    final MessageFactory MSG_FACTORY = new MessageFactory();

    public static class MessageFactory{

        public MessageFactory(){}

        public Message obtainRequest(Handler handler, String action, Uri uri){
            Message msg = handler.obtainMessage(REQUEST, action.hashCode(), Uris.getDescriptor(uri));
            return msg;
        }

        public Message obtainRequest(Handler handler, int who, Intent intent){
            int what =  Uris.getDescriptor(intent.getData());
            Message msg = handler.obtainMessage(what,
                    intent.toUri(Intent.URI_INTENT_SCHEME));
            msg.arg1 = intent.filterHashCode();
            msg.arg2 = REQUEST;
            msg.obj = intent.toUri(Intent.URI_INTENT_SCHEME);

            return msg;
        }

        public Message obtainResponse(Message orig, Object obj){
            Message msg = Message.obtain(orig);
            msg.what = RESPONSE;
            msg.obj = obj;
            return msg;
        }

    }

    public final void handleFailResend(MessageHolder message){
        Log.i(TAG, "handleFailedResend() " + message.what);
        Message msg = mHandler.obtainMessage(message.what,
                message.arg1,
                message.arg2,
                message.object);
        if(message.data != null)
            msg.setData(message.data);
        msg.sendToTarget();
    }

    public final void addFailedToQueue(Message message){
        Log.i(TAG, "addFailedToQueue() " + message.what);
        addFailedToQueue(message.what,message.arg1,message.arg2,message.obj,message.getData(),Uri.EMPTY);
    }

    public final void addFailedToQueue(int what, int arg1, int arg2, Object object, Bundle data){
        Log.i(TAG, "addFailedToQueue() " + what);
        addFailedToQueue(what,arg1,arg2,object,data,Uri.EMPTY);
    }

    public final void addFailedToQueue(int what, int arg1, int arg2, Object object, Bundle data, Uri uri){
        Log.i(TAG, "addFailedToQueue(...Uri) " + what);
        MessageHolder holder = new MessageHolder();
        holder.what = what;
        holder.arg1 = arg1;
        holder.arg2 = arg2;
        holder.object = object;
        if(data != null) {
            holder.data = new Bundle(data);
        }
        holder.uri = uri;
        Log.d(TAG,"....Queue size: "  + failQueue.size());
        Log.d(TAG,"....adding 1 item");
        failQueue.add(holder);
        Log.d(TAG,"....Queue size: "  + failQueue.size());
        failQueue.start();
        failQueue.resend();
    }

    public final void handleRequestStart(int id, int code){
        Log.i(TAG, "handleRequestStart():" + id);
        int resID = R.string.general_network_active;

        Intent notifyIntent = new Intent(DispatchService.this,
                                            MainActivity.class);
        //notifyIntent.addCategory(Intent.CATEGORY_HOME);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notify(resID,code,notifyIntent);
    }

    public final void handleRequestStart(int id){
        Log.i(TAG, "handleRequestStart():" + id);
        int resID = R.string.general_network_active;

        Intent notifyIntent = new Intent(DispatchService.this,
                                            MainActivity.class);
        //notifyIntent.addCategory(Intent.CATEGORY_HOME);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notify(resID,notifyIntent);
    }

    public final void handleRequestComplete(int id){
        Log.i(TAG, "handleRequestComplete():" + id);
        int count = sNotificationCount.decrementAndGet();
        int resID = (count > 0)?
            R.string.general_network_active:
            R.string.general_network_inactive;

        Intent notifyIntent = new Intent(DispatchService.this,
                                            MainActivity.class);
        //notifyIntent.addCategory(Intent.CATEGORY_HOME);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        notify(resID,notifyIntent);
    }

    public final void handleQueueEmpty(){
        Intent notifyIntent = new Intent(getApplicationContext(),
                                        EncounterList.class);
        notifyForeground(COMPLETE,
              R.string.upload_fail,
              notifyIntent);
    }

    public final void handleResponse(Message message){

    }

    class MessageHolder implements Comparable<MessageHolder>{

        public MessageHolder(){}

        public MessageHolder(MessageHolder message){}

        public MessageHolder(Message message){
            what = message.what;
            arg1 = message.arg1;
            arg2 = message.arg2;
            object = message.obj;
            if(message.getData() != null)
                data = new Bundle(message.getData());
        }
        Intent intent = null;
        int priority = 0;
        int what = -1;
        int arg1 = -1;
        int arg2 = -1;
        Object object = null;
        Bundle data = new Bundle();
        Uri uri = Uri.EMPTY;

        public int compareTo(MessageHolder another){
            if(this.what == another.what){
                return another.priority - this.priority;
            } else {
                return another.priority - this.priority;
            }
        }

        public boolean equals(MessageHolder message){
            return (what == message.what);
        }
    }

    class QueueControl{
        //LinkedList<Integer> queue = new LinkedList<Integer>();
        LinkedList<Uri> queue = new LinkedList<Uri>();

        //Hashtable<Integer,MessageHolder> pending = new Hashtable<Integer,MessageHolder>();
        Hashtable<Uri,MessageHolder> pending = new Hashtable<Uri,MessageHolder>();
        //Hashtable<Integer,ArrayList<MessageHolder>> pending = Hashtable<Integer,ArrayList<MessageHolder>>();

        private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
        AtomicBoolean halt = new AtomicBoolean(false);
        public void resend(){
            Log.i(TAG, "QueueControl resend()");
            if(halt.get()){
                return;
            }
            final Runnable sender = new Runnable(){
                public void run(){
                    Log.d(TAG, "QueueControl sender.run()");
                    synchronized(queue){
                        Uri head = queue.poll();
                        if(head != null){
                            MessageHolder next = pending.remove(head);
                            handleFailResend(next);
                        }
                    }
                }
            };
            final ScheduledFuture handle =
                scheduler.schedule(sender, 15, TimeUnit.SECONDS);
        }

        public boolean isEmpty(){
            boolean empty = true;
            synchronized(queue){
                empty = (queue.size() > 0);
            }
            return empty;
        }

        public void add(MessageHolder message){
            Log.i(TAG, "QueueControl add()");
            synchronized(queue){
                queue.add(message.uri);
                pending.put(message.uri, message);
            }
            halt.set(false);
        }

        public final void cancel(){
            Log.i(TAG, "QueueControl cancel()");
            halt.set(true);
        }

        public boolean contains(Uri what){
            boolean contains = false;
            synchronized(queue){
                contains = queue.contains(what);
            }
            return contains;
        }

        public int size(){
            int size = -1;
            synchronized(queue){
                size = queue.size();
            }
            return size;
        }

        public MessageHolder[] toArray(){
            MessageHolder[] array = new MessageHolder[pending.size()];
            synchronized(queue){
                int index = 0;
                for(MessageHolder message:pending.values()){
                    array[index] = new MessageHolder(message);
                }
            }
            return array;
        }

        public final void start(){
            Log.i(TAG, "QueueControl start()");
            halt.set(false);
        }

    }
}

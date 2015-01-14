package org.sana.android.app;

import java.util.Collection;
import java.util.PriorityQueue;

import org.sana.android.provider.Encounters;
import org.sana.net.Response;
import org.sana.net.Response.Code;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Manages the messages waiting to be dispatched. This class replaces
 * the Encounter specific QueueManager from version 1. 
 * 
 * @author Sana Development Team
 * @since 2.0
 */
public class MessageQueueManager {
    private static final String TAG = MessageQueueManager.class.getSimpleName();

    // Included for backwards compatibility
    public static final int UPLOAD_STATUS_NOT_IN_QUEUE = -1;
    public static final int UPLOAD_STATUS_WAITING = 1;
    public static final int UPLOAD_STATUS_SUCCESS = 2;
    public static final int UPLOAD_STATUS_IN_PROGRESS = 3;
    public static final int UPLOAD_NO_CONNECTIVITY = 4;
    public static final int UPLOAD_STATUS_FAILURE = 5;
    public static final int UPLOAD_STATUS_CREDENTIALS_INVALID = 6;
    
    public enum State{
        NOT_IN_QUEUE(-1),
        SUCCESS(0),
        FAILURE(1),
        WAITING(2),
        IN_PROGRESS(4);
        
        public final int code;
        State(int code){ this.code = code; }

        public static State fromCode(int code){
            for(State state: State.values()){
                if(state.code == code) return state;
            }
            throw new IllegalArgumentException("Illegal code: " + code);
        }
        
        public static State stateCompat(int code){
            switch(code){
                case UPLOAD_STATUS_NOT_IN_QUEUE:
                    return NOT_IN_QUEUE;
                case UPLOAD_STATUS_WAITING:
                    return WAITING;
                case UPLOAD_STATUS_IN_PROGRESS:
                    return IN_PROGRESS;
                case UPLOAD_STATUS_SUCCESS:
                    return SUCCESS;
                case UPLOAD_STATUS_FAILURE:
                case UPLOAD_STATUS_CREDENTIALS_INVALID:
                case UPLOAD_NO_CONNECTIVITY:
                    return FAILURE;
            default:
                throw new IllegalArgumentException("Invalid code: " + code);
            }
        }
    }
    
    public enum Priority{
        IMMEDIATE(-1),
        NORMAL(0),
        LOW(1);
        
        public final int code;
        Priority(int code){ this.code = code; }
    }
    
    public enum Method{
        UNKNOWN(-1),
        CREATE(0),
        READ(1),
        UPDATE(2),
        DELETE(4);
        
        public final int code;
        Method(int code){ this.code = code; }
        
        public static Method fromCode(int code){
            for(Method method: Method.values()){
                if(method.code == code) return method;
            }
            throw new IllegalArgumentException("Illegal queue state: " + code);
        }
        
        public static Method fromString(String methodStr){
            String[] segs = methodStr.split(".");
            String action = (segs.length > 0)?segs[segs.length-1]: segs[0];
            for(Method method: Method.values()){
                if(action.compareToIgnoreCase(method.toString()) == 0) return method;
            }
            throw new IllegalArgumentException("Illegal method: " + action);
        }
    }
    
    /**
     * Holds the message contents.
     * 
     */
    public static class MessageHolder{
        
        public int version = 2;
        public Priority priority = Priority.NORMAL;
        public int id = -1;
        public Method method  = Method.UNKNOWN;
        public Uri uri = Uri.EMPTY;
        public Bundle form = new Bundle();
        public Bundle files = new Bundle();
        
        public MessageHolder(){}
        
        public MessageHolder(Intent intent){
            
        }
    }
    
    private static final PriorityQueue<Uri> queue = new PriorityQueue<Uri>();
    private static final String[] PROJECTION = { };
    
    /**
     * Initializes the in-memory queue with what is stored in the database.
     */
    public static void initialize(Context c) {
        Cursor cursor = null;
        try {
            // Initialize the queue from the database
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
    
    /**
     * Updates upload status of items currently in the queue
     *  
     * @param c the current context
     * @param queue
     */
    public static void persist(Context c, PriorityQueue<Uri> queue) {
        Log.i(TAG, "updateQueueInDB()");
        
    }
    
    /**
     * Adds an item to the global queue.
     * 
     * @param c the current context
     * @param queue the queue to update from 
     * @param uri the procedure in the queue
     */
    public static void add(Context c, PriorityQueue<Uri> queue, 
            Uri uri) 
    {
        queue.add(uri);
        setStatus(c, uri, UPLOAD_STATUS_WAITING);
        persist(c, queue);
    }
    

    /**
     * Removes an item to the global queue.
     * 
     * @param c the current context
     * @param queue the queue to update from 
     * @param uri the procedure in the queue
     */
    public static boolean remove(Context c, PriorityQueue<Uri> queue, 
            Uri uri) 
    {
        return remove(c, uri, State.NOT_IN_QUEUE.code);
    }
    
    /**
     * Removes an item to the global queue and updates its upload status. 
     * 
     * @param c the current context
     * @param queue the queue to update from 
     * @param uri the procedure in the queue
     * @param newStatus the new upload status
     * @return true if the procedure was in the queue and updated
     */
    public static boolean remove(Context c, Uri uri, int newStatus) 
    {
        if (MessageQueueManager.contains(uri)) {
            queue.remove(uri);
            persist(c, queue);
            setStatus(c, uri, newStatus);
            return true;
        }
        return false;
    }
    
    /**
     * Checks whether a procedure is in the queue
     * 
     * @param queue the queue to check 
     * @param uri the procedure look for
     * @return true if the procedure was in the queue and updated
     */
    public static boolean contains(Uri uri) {
        return queue.contains(uri);
    }
    
    /**
     * Finds the location of procedure is in the queue
     * 
     * @param queue the queue to check 
     * @param uri the procedure look for
     * @return index of the procedure in the queue or -1
     */
    public static int indexOf(Uri uri) {
        if (contains(uri)) {
            int index = 0;
            for (Uri u : queue) {
                if (uri.equals(u)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }
    
    /**
     * Updates the upload status of a procedure.
     * 
     * @param c the current context
     * @param uri the procedure
     * @param status the new status
     */
    public static void setStatus(Context c, Uri uri, 
            int status) 
    {
        ContentValues cv = new ContentValues();
        c.getContentResolver().update(uri, cv, null, null); 
    }
    
    /**
     * Updates the upload status for a list procedures.
     * 
     * @param c the current context
     * @param uris the procedures to update
     * @param status the new status
     */
    public static void setStatus(Context c, 
            Collection<Uri> uris, int status) 
    {
        ContentValues cv = new ContentValues();
        for (Uri uri : uris) {
            c.getContentResolver().update(uri, cv, null, null);
        }
    }

}

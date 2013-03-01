package org.sana.android.service;

import java.util.Collection;
import java.util.PriorityQueue;

import org.sana.android.provider.Encounters;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Manages the items in the queue awaiting upload.
 * 
 * @author Sana Development Team
 *
 */
public class QueueManager {
	private static final String TAG = QueueManager.class.getSimpleName();

	public static final int UPLOAD_STATUS_NOT_IN_QUEUE = -1;
	public static final int UPLOAD_STATUS_WAITING = 1;
	public static final int UPLOAD_STATUS_SUCCESS = 2;
	public static final int UPLOAD_STATUS_IN_PROGRESS = 3;
	public static final int UPLOAD_NO_CONNECTIVITY = 4;
	public static final int UPLOAD_STATUS_FAILURE = 5;
	public static final int UPLOAD_STATUS_CREDENTIALS_INVALID = 6;
	
	private static final String[] PROJECTION = { Encounters.Contract._ID,
		Encounters.Contract.UUID, Encounters.Contract.PROCEDURE,
		Encounters.Contract.UPLOAD_QUEUE };
	
	/**
	 * Initializes the in-memory queue with what is stored in the database.
	 */
	public static PriorityQueue<Uri> initQueue(Context c) {
		PriorityQueue<Uri> queue = new PriorityQueue<Uri>();
		Cursor cursor = null;
		try {
			// Initialize the queue from the database
			Log.i(TAG, "In initQueue - getting queue from database");
			cursor = c.getContentResolver().query(
					Encounters.CONTENT_URI, PROJECTION,
					Encounters.Contract.UPLOAD_QUEUE + " >= 0", null,
					Encounters.QUEUE_SORT_ORDER);
			cursor.moveToFirst();

			int position = 0;
			while (!cursor.isAfterLast()) {
				int savedProcedureId = cursor.getInt(0);
				Uri savedProcedureUri = ContentUris.withAppendedId(
						Encounters.CONTENT_URI, savedProcedureId);
				Log.i(TAG, "Queue item #" + position + " has URI " + savedProcedureUri);
				queue.add(savedProcedureUri);
				cursor.moveToNext();
				position++;
			}
			Log.i(TAG, "Queue has been extracted from database. Here is the "
					+" queue: " + queue);
		} catch (Exception e) {
			Log.e(TAG, "Exception in getting queue from database: "
					+ e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return queue;
	}
	
	/**
	 * Updates upload status of items currently in the queue
	 *  
	 * @param c the current context
	 * @param queue
	 */
	public static void updateQueueInDB(Context c, PriorityQueue<Uri> queue) {
		Log.i(TAG, "Updating queue information in the database");
		Log.i(TAG, "Queue is now: " + queue.toString());
		
		// Reset all saved procedure to have -1 for UPLOAD_QUEUE. This takes
		// everything out of the queue, then we re-add everything that is in the
		// in-memory queue.
		ContentValues cv = new ContentValues();
		cv.put(Encounters.Contract.UPLOAD_QUEUE, -1);
		c.getContentResolver().update(Encounters.CONTENT_URI, cv, 
				null, null);
		
		// TODO(XXX) This loop is inefficient -- O(n^2) when it could be O(n)
		for (Uri procedureUri : queue) {
			cv = new ContentValues();
			int index = queueIndex(queue, procedureUri);
			Log.i(TAG, "In updateQueueInDB, queueIndex(" + procedureUri
					+ ") returns: " + index);
			cv.put(Encounters.Contract.UPLOAD_QUEUE, index);
			c.getContentResolver().update(procedureUri, cv, null, null);
		}
	}
	
	/**
	 * Adds an item to the global queue.
	 * 
	 * @param c the current context
	 * @param queue the queue to update from 
	 * @param procedureUri the procedure in the queue
	 */
	public static void addToQueue(Context c, PriorityQueue<Uri> queue, 
			Uri procedureUri) 
	{
		queue.add(procedureUri);
		setProcedureUploadStatus(c, procedureUri, UPLOAD_STATUS_WAITING);
		updateQueueInDB(c, queue);
	}
	

	/**
	 * Removes an item to the global queue.
	 * 
	 * @param c the current context
	 * @param queue the queue to update from 
	 * @param procedureUri the procedure in the queue
	 */
	public static boolean removeFromQueue(Context c, PriorityQueue<Uri> queue, 
			Uri procedureUri) 
	{
		return removeFromQueue(c, queue, procedureUri, 
				QueueManager.UPLOAD_STATUS_NOT_IN_QUEUE);
	}
	
	/**
	 * Removes an item to the global queue and updates its upload status. 
	 * 
	 * @param c the current context
	 * @param queue the queue to update from 
	 * @param procedureUri the procedure in the queue
	 * @param newStatus the new upload status
	 * @return true if the procedure was in the queue and updated
	 */
	public static boolean removeFromQueue(Context c, PriorityQueue<Uri> queue, 
			Uri procedureUri, int newStatus) 
	{
		if (QueueManager.isInQueue(queue, procedureUri)) {
			queue.remove(procedureUri);
			QueueManager.updateQueueInDB(c, queue);
			QueueManager.setProcedureUploadStatus(c, procedureUri, newStatus);
			return true;
		}
		return false;
	}
	
	/**
	 * Checks whether a procedure is in the queue
	 * 
	 * @param queue the queue to check 
	 * @param procedureUri the procedure look for
	 * @return true if the procedure was in the queue and updated
	 */
	public static boolean isInQueue(PriorityQueue<Uri> queue, Uri procedureUri) {
		return queue.contains(procedureUri);
	}
	
	/**
	 * Finds the location of procedure is in the queue
	 * 
	 * @param queue the queue to check 
	 * @param procedureUri the procedure look for
	 * @return index of the procedure in the queue or -1
	 */
	public static int queueIndex(PriorityQueue<Uri> queue, Uri procedureUri) {
		if (isInQueue(queue, procedureUri)) {
			int index = 0;
			for (Uri uri : queue) {
				if (uri.equals(procedureUri)) {
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
	 * @param procedureUri the procedure
	 * @param status the new status
	 */
	public static void setProcedureUploadStatus(Context c, Uri procedureUri, 
			int status) 
	{
		Log.v(TAG, "Setting upload status for " + procedureUri + " to " + status);
		ContentValues cv = new ContentValues();
		cv.put(Encounters.Contract.UPLOAD_STATUS, status); 
		c.getContentResolver().update(procedureUri, cv, null, null); 
	}
	
	/**
	 * Updates the upload status for a list procedures.
	 * 
	 * @param c the current context
	 * @param procedureUris the procedures to update
	 * @param status the new status
	 */
	public static void setProceduresUploadStatus(Context c, 
			Collection<Uri> procedureUris, int status) 
	{
		ContentValues cv = new ContentValues();
		cv.put(Encounters.Contract.UPLOAD_STATUS, status);
		for (Uri uri : procedureUris) {
			c.getContentResolver().update(uri, cv, null, null);
		}
	}

}

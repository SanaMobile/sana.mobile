package org.sana.android.content;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/**
 *
 */
public class MessageQueueProvider extends ContentProvider {

    public interface Contract{
        String AUTH = "_auth";
        String STATE = "_state";
        String PRIORITY = "_priority";
        String DATA = "_data";
        String SOURCE = "source";
        String TARGET = "target";
        String SENT = "sent";
        String SEND_COUNT = "send_count";
        String EVENT_START = "event_start";
        String EVENT_COMPLETE = "request_complete";
        String RECEIVED = "received";
        String REQUEST_COMPLETE = "request_complete";

    }

    public enum State{
        ERROR(-1),
        CANCELLED(0),
        COMPLETE(1),
        WAITING(2),
        SCHEDULED(4),
        SENDING(8),;
        final int code;
        State(int code){
            this.code = code;
        }
        public int code(){
            return code;
        }
    }
    public enum Priority{
        LOW(8),
        HIGH(1),
        FIRST(0);
        public final long priority;
        Priority(long i){
            this.priority = i;
        }
        public long val(){
            return priority;
        }
    }
    protected static final class MessageQueueHelper extends SQLiteOpenHelper {

        public static final String CREATE = "CREATE TABLE messages (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                Contract.AUTH + " TEXT, " +
                Contract.PRIORITY + " INTEGER, " +
                Contract.STATE + " INTEGER, " +
                Contract.DATA + " TEXT, " +
                Contract.SOURCE + " TEXT, " +
                Contract.TARGET + " TEXT, " +
                Contract.SENT + " INTEGER, " +
                Contract.SEND_COUNT + " INTEGER," +
                Contract.EVENT_START + " INTEGER," +
                Contract.EVENT_COMPLETE + " INTEGER," +
                Contract.RECEIVED + " INTEGER," +
                Contract.REQUEST_COMPLETE + " INTEGER );";

        public MessageQueueHelper(Context context, String name, int version) {
            super(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            // TODO
        }
    }

    public static final String TAG = MessageQueueProvider.class.getSimpleName();
    public static final String AUTHORITY = "org.sana.provider.dispatch";
    public static final String TABLE = "messages";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/message/");
    public static final UriMatcher sUriMatcher = new UriMatcher(0);
    static{
        sUriMatcher.addURI(AUTHORITY, "message/", 1);
        sUriMatcher.addURI(AUTHORITY, "message/#", 2);
    }

    protected static final String DATABASE = "messages.db";
    private MessageQueueHelper mOpenHelper;
    @Override
    public boolean onCreate() {
        mOpenHelper = new MessageQueueHelper(getContext(),DATABASE, 1);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        switch (sUriMatcher.match(uri)) {
            case 1:
                s1 = (TextUtils.isEmpty(s1))? BaseColumns._ID + " _ASC": s1;
                break;
            case 2:
                s = s + BaseColumns._ID + " = " + uri.getLastPathSegment();
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE);
        Cursor c = qb.query(db, strings, s, strings1, null, null,
                s1);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if(!contentValues.containsKey("_priority")){
            contentValues.put("_priority", 1);
        }
        if(!contentValues.containsKey("_state")){
            contentValues.put("_state", 1);
        }
        long id = db.insert(TABLE, null, contentValues);
        getContext().getContentResolver().notifyChange(uri, null);
        Uri result = ContentUris.withAppendedId(uri, id);
        db.close();
        return result;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        switch (sUriMatcher.match(uri)) {
            case 2:
                s = s + BaseColumns._ID + " = " + uri.getLastPathSegment();
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(TABLE, s, strings);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        switch (sUriMatcher.match(uri)) {
            case 2:
                s = s + BaseColumns._ID + " = " + uri.getLastPathSegment();
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int updated = db.update(TABLE, contentValues, s, strings);
        getContext().getContentResolver().notifyChange(uri, null);
        return updated;
    }
}

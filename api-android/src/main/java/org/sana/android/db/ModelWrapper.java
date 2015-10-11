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
package org.sana.android.db;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.sana.android.content.Uris;
import org.sana.android.provider.BaseContract;
import org.sana.api.IModel;
import org.sana.util.DateUtil;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public abstract class ModelWrapper<T extends IModel> extends CursorWrapper
    implements ModelIterable<T>, IModel
{

    public static final String TAG = ModelWrapper.class.getSimpleName();

    public static interface BaseProjection{
        public static String[] ID_PROJECTION = new String[] { BaseContract._ID };
        public static String[] UUID_PROJECTION = new String[] { BaseContract._ID };
    }
    public ModelWrapper(Cursor cursor){
        super(cursor);
    }

    public boolean getBooleanField(int columnIndex){
        boolean field = false;
        field = (getInt(columnIndex) == 1);
        return field;
    }

    public Date getDateField(String field){
        String dateStr = getString(getColumnIndex(field));
        try {
            return DateUtil.parseDate(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("date string=" + dateStr);
        }
    }

    public int getIntField(String field){
        return getInt(getColumnIndex(field));
    }

    public String getStringField(String field){
        return getString(getColumnIndex(field));
    }

    public boolean getBooleanField(String field){
        return getBooleanField(getColumnIndex(field));
    }

    /* (non-Javadoc)
     * @see org.sana.api.IModel#getUuid()
     */
    @Override
    public String getUuid() {
        return getStringField(BaseContract.UUID);
    }

    /* (non-Javadoc)
     * @see org.sana.api.IModel#getCreated()
     */
    @Override
    public Date getCreated() {
        return getDateField(BaseContract.CREATED);
    }

    /* (non-Javadoc)
     * @see org.sana.api.IModel#getModified()
     */
    @Override
    public Date getModified() {
        return getDateField(BaseContract.MODIFIED);
    }

    /*
     * (non-Javadoc)
     * @see org.sana.android.db.ModelIterable#next()
     */
    public T next() {
        if(moveToNext()){
            try{
                return getObject();
            } catch (Exception e){
                throw new NoSuchElementException("Failed to instantiate object ");
            }
        }
        throw new NoSuchElementException("Wrapped cursor at or past last.");
    }

    /**
     * Should create a new instance of <T> from the values in the current row.
     * Note: Only columns declared as part of the projection in the original
     * query will be non null in the instance.
     * @return
     */
    public abstract T getObject();

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return !isLast();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException("Removal not supported");
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        moveToFirst();
        return new ProxyIterator<T>(this);
    }

    public java.util.List<T> toList(ModelWrapper<T> wrapper){
        java.util.List<T> list = new java.util.ArrayList<T>(wrapper.getCount());
        for(T t:wrapper){
            list.add(t);
        }
        return list;
    }

    public static class ProxyIterator<T extends IModel> implements Iterator <T> {

        private ModelWrapper<T> modelWrapper;

        public ProxyIterator(ModelWrapper<T> proxy){
            this.modelWrapper = proxy;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return modelWrapper.hasNext();
        }


        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public T next() {
            return modelWrapper.next();
        }


        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException("Removal not supported");
        }
    }

    public static synchronized String constructSelectionClause(String[] fields){
        StringBuilder selection = new StringBuilder();
        int index = 0;
        for(String field:fields){
            if(index > 0)
                selection.append(" AND ");
            selection.append(field + " = ?");
            index++;
        }
        return selection.toString();
    }

    /**
     * Convenience wrapper which returns a cursor representing a single row
     * selected a single column value.
     *
     * @param contentUri The content style Uri to query
     * @param resolver The resolver which will perform the query.
     * @param field The field, or column, to select by.
     * @return A cursor with a single row.
     * @throws IllegalArgumentException if multiple rows are returned.
     */
    public static synchronized Cursor getOneByField(Uri contentUri, ContentResolver resolver,
            String field, Object object)
    {
        String selection = field + " = ?";
        Cursor cursor = resolver.query(contentUri,null, selection,
                new String[]{ object.toString() }, null);
        if(cursor != null && cursor.getCount() > 1){
            cursor.close();
            throw new IllegalArgumentException("Multiple entries found! Expecting one.");
        }
        return cursor;
    }

    /**
     * Convenience wrapper which returns a cursor representing a single row
     * selected a single column value.
     *
     * @param contentUri The content style Uri to query
     * @param resolver The resolver which will perform the query.
     * @param fields The field, or column, to select by.
     * @param vals The selection argument or, row value, to select by.
     * @return A cursor with a single row.
     * @throws IllegalArgumentException if multiple rows are returned.
     */
    public static synchronized Cursor getOneByFields(Uri contentUri, ContentResolver resolver,
            String[] fields, String[] vals)
    {
        Cursor cursor = ModelWrapper.getAllByFields(contentUri, resolver, fields, vals);
        if(cursor != null && cursor.getCount() > 1){
            cursor.close();
            throw new IllegalArgumentException("Multiple entries found! Expecting one.");
        }
        return cursor;
    }

    /**
     * Convenience wrapper to returns a cursor representing a single row matched
     * by the uuid value.
     *
     * @param contentUri The content style Uri to query
     * @param resolver The resolver which will perform the query.
     * @param uuid The uuid to select by.
     * @return A cursor with a single row.
     * @throws IllegalArgumentException if multiple rows are returned.
     */
    public static synchronized Cursor getOneByUuid(Uri contentUri, ContentResolver resolver,
            String uuid)
    {
        String selection = BaseContract.UUID + " = ?";

        Cursor cursor = resolver.query(contentUri,null, selection,
                new String[]{ uuid }, null);
        if(cursor != null && cursor.getCount() > 1){
            cursor.close();
            throw new IllegalArgumentException("Non unique id! " +contentUri+"/"+uuid);
        }
        return cursor;
    }

    /**
     * Convenience wrapper which returns a cursor representing zero or more rows
     * selected a single column value.
     *
     * @param contentUri The content style Uri to query
     * @param resolver The resolver which will perform the query.
     * @param field The field, or column, to select by.
     * @param object The selection argument or, row value, to select by.
     * @return A cursor with zero or more rows.
     */
    public static synchronized Cursor getAllByField(Uri contentUri, ContentResolver resolver,
            String field, Object object)
    {
        return ModelWrapper.getAllByFieldOrdered(contentUri, resolver, field, object, null);
    }

    /**
     * Convenience wrapper which returns a cursor representing zero or more rows
     * selected a single column value.
     *
     * @param contentUri The content style Uri to query
     * @param resolver The resolver which will perform the query.
     * @param fields The field, or column, to select by.
     * @param vals The selection argument or, row value, to select by.
     * @return A cursor with zero or more rows.
     */
    public static synchronized Cursor getAllByFields(Uri contentUri, ContentResolver resolver,
            String[] fields, String[] vals)
    {
        return ModelWrapper.getAllByFieldsOrdered(contentUri, resolver, fields, vals, null);
    }

    /**
     * Convenience wrapper which returns a cursor representing zero or more rows
     * selected a single column value and ordered. Passing a null field or object
     * will bypass any selection.
     *
     * @param contentUri The content style Uri to query
     * @param resolver The resolver which will perform the query.
     * @param field The field, or column, to select by.
     * @param object The selection argument or, row value, to select by.
     * @paeam order The order to return by.
     * @return A cursor with zero or more rows.
     */
    public static synchronized Cursor getAllByFieldOrdered(Uri contentUri, ContentResolver resolver,
            String field, Object object, String order)
    {
        String selection = null;
        String[] selectionArgs = null;

        if(!TextUtils.isEmpty(field) && object != null){
            selection = field + " = ?";
            selectionArgs = new String[]{ object.toString() } ;
        }
        return resolver.query(contentUri,null, selection, selectionArgs, order);
    }

    /**
     * Convenience wrapper which returns a cursor representing zero or more rows
     * selected a single column value and ordered. Passing a null field or object
     * will bypass any selection.
     *
     * @param contentUri The content style Uri to query
     * @param resolver The resolver which will perform the query.
     * @param fields The field, or column, to select by.
     * @param vals The selection argument or, row values, to select by.
     * @paeam order The order to return by.
     * @return A cursor with zero or more rows.
     */
    public static synchronized Cursor getAllByFieldsOrdered(Uri contentUri, ContentResolver resolver,
            String[] fields, String[] vals, String order)
    {
        StringBuilder selection = new StringBuilder();
        int index = 0;
        for(String field:fields){
            if(index > 0)
                selection.append(" AND ");
            selection.append(field + " = ?");
            index++;
        }
        return resolver.query(contentUri, null, selection.toString(), vals, order);
    }

    /**
     * Convenience wrapper to return a cursor which returns all of the entries
     * ordered by {@link org.sana.android.provider.BaseContract#CREATED CREATED}
     * in ascending order, or, oldest first.
     *
     * @param contentUri The content style Uri to query
     * @param resolver The resolver which will perform the query.
     * @return A cursor with the result or null.
     */
    public static synchronized Cursor getAllByCreatedAsc(Uri contentUri, ContentResolver resolver)
    {
        return ModelWrapper.getAllByFieldOrdered(contentUri, resolver, null, null, BaseContract.CREATED +" ASC");
    }
    /**
     * Convenience wrapper to return a cursor which returns all of the entries
     * ordered by {@link org.sana.android.provider.BaseContract#CREATED CREATED}
     * in descending order, or, newest first.
     *
     * @param contentUri The content style Uri to query
     * @param resolver The resolver which will perform the query.
     * @return A cursor with the result or null.
     */
    public static synchronized Cursor getAllByCreatedDesc(Uri contentUri, ContentResolver resolver)
    {
        final String order = BaseContract.CREATED +" DESC";
        return resolver.query(contentUri,null, null,null, order);
    }

    /**
     * Convenience wrapper to return a cursor which returns all of the entries
     * ordered by {@link org.sana.android.provider.BaseContract#MODIFIED MODIFIED}
     * in ascending order, or, oldest first.
     *
     * @param contentUri The content style Uri to query
     * @param resolver The resolver which will perform the query.
     * @return A cursor with the result or null.
     */
    public static synchronized Cursor getAllByModifiedAsc(Uri contentUri, ContentResolver resolver)
    {
        final String order = BaseContract.MODIFIED +" ASC";
        return resolver.query(contentUri,null, null,null, order);
    }

    /**
     * Convenience wrapper to return a cursor which returns all of the entries
     * ordered by {@link org.sana.android.provider.BaseContract#MODIFIED MODIFIED}
     * in ascending order, or, newest first.
     *
     * @param contentUri The content style Uri to query
     * @param resolver The resolver which will perform the query.
     * @return A cursor with the result or null.
     */
    public static synchronized Cursor getAllByModifiedDesc(Uri contentUri, ContentResolver resolver)
    {
        final String order = BaseContract.MODIFIED +" DESC";
        return resolver.query(contentUri,null, null,null, order);
    }

    /**
     * Creates or updates an entry.
     *
     * @param uri
     * @param values
     * @param resolver
     * @return
     */
    public static synchronized boolean insertOrUpdate(Uri uri, ContentValues values, ContentResolver resolver){
        Cursor c = null;
        boolean exists = false;
        String uuid = (values.containsKey(BaseContract.UUID))?
                values.getAsString(BaseContract.UUID): null;
        if(uuid != null){
            try{
                c = ModelWrapper.getOneByUuid(uri, resolver, uuid);
                if(c != null && c.moveToFirst() && c.getCount() == 1){
                    exists = true;
                }
            } catch(Exception e){
                e.printStackTrace();
            } finally {
                if(c!=null)
                    c.close();
            }
        }
        try{
            if(exists){
                if(values.containsKey(BaseContract.UUID))
                        values.remove(BaseContract.UUID);
                resolver.update(uri, values, null, null);
            } else {
                resolver.insert(uri, values);
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public synchronized static Uri getOneReferenceByFields(Uri contentUri, String[] fields,
            String[] vals, ContentResolver resolver)
    {
        Uri uri = contentUri;
        Cursor c = null;
        try{
            c = ModelWrapper.getOneByFields(contentUri, resolver, fields, vals);
            if(c != null && c.moveToFirst() && c.getCount() == 1){
                long id = c.getLong(c.getColumnIndex(BaseColumns._ID));
                uri = ContentUris.withAppendedId(contentUri, id);
            }
        }catch(Exception e){
            e.printStackTrace();
        } finally { if(c!=null) c.close(); }
        return uri;
    }

    /**
     * Returns whether an item is unique and exists in the database
     * @param resolver
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static synchronized Uri exists(ContentResolver resolver,
            Uri uri,
            String selection,
            String[] selectionArgs)
    {
        Uri result = null;
        Cursor c = null;
        boolean exists = false;
        // allows for appending uuid query
        if(selection == null && selectionArgs == null
                    && !TextUtils.isEmpty(uri.getQuery())){
            String uuid = uri.getQueryParameter(BaseContract.UUID);
            selection = BaseContract.UUID + "=?";
            selectionArgs = new String[]{ uuid };
        }
        try{
            c = resolver.query(uri,
                        BaseProjection.ID_PROJECTION,
                        selection,
                        selectionArgs, null);
            if(c != null && c.moveToFirst() && c.getCount() == 1){
                exists = true;
            }
        } catch(Exception e){
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        } finally {
            if(c!=null) c.close();
        }
        return result;
    }

    /**
     * Returns whether an item is unique and exists in the database
     *
     * @param context
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static synchronized boolean exists(Context context,
                                          Uri uri,
                                          String selection,
                                          String[] selectionArgs)
    {
        Cursor cursor = null;
        boolean exists = false;
        boolean unique = false;

        if(Uris.isEmpty(uri))
            throw new NullPointerException("Empty object Uri.");

        try {
            // Do the query
            cursor = context.getContentResolver().query(uri,
                    BaseProjection.ID_PROJECTION,
                    selection,
                    selectionArgs, null);
            // Check that the cursor returned
            if (cursor != null){
                if (cursor.getCount() == 1){
                    // Count was one so exists and unique
                    exists = true;
                    unique = true;
                } else if (cursor.getCount() > 1) {
                    exists = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null) cursor.close();
        }

        // Handle the existence and uniqueness constraints
        if(exists && !unique) {
            // TODO Class based exception
            throw new IllegalArgumentException("MultipleObjectsReturned");
        }
        return exists;
    }

    /**
     * Validates the existence of an object in the database when passed a
     * Uri with the row id or object uuid as the last path segment.
     *
     * @param context
     * @param uri
     * @return
     */
    public static synchronized boolean exists(Context context, Uri uri)
    {
        switch(Uris.getTypeDescriptor(uri)) {
            case Uris.ITEM_UUID:
            case Uris.ITEM_ID:
                return exists(context,uri,null,null);
            case Uris.ITEMS:
            default:
                throw new IllegalArgumentException("Invalid Uri. Directory type." + uri);
        }
    }

    /**
     * Inserts or updates and returns
     * @param uri
     * @param values
     * @param resolver
     * @return
     */
    public static synchronized Uri getOrCreate(Uri uri,
            ContentValues values,
            ContentResolver resolver)
    {
        Uri result = Uri.EMPTY;
        Cursor c = null;
        int updated = 0;
        boolean created = false;

        switch(Uris.getTypeDescriptor(uri)){
        case Uris.ITEM_UUID:
        case Uris.ITEM_ID:
            if(Uris.isEmpty(exists(resolver, uri, null, null))){
                updated = resolver.update(uri, values, null, null);
            } else {
              throw new IllegalArgumentException("Error updating. Item does not exist: " + uri);
            }
            break;
        case Uris.ITEMS:
            if(uri.getQuery() != null){
                String uuid = uri.getQueryParameter(BaseContract.UUID);
                String selection = BaseContract.UUID + "=?";
                String[] selectionArgs = new String[]{ uuid };
                result = exists(resolver, uri, selection, selectionArgs);
                if(!Uris.isEmpty(result)){
                    resolver.update(uri, values, null, null);
                } else {
                    result = resolver.insert(uri, values);
                }
            } else {
                result = resolver.insert(uri, values);
            }
            break;
        default:
            throw new IllegalArgumentException("Error updating. Unrecognized uri: " + uri);
        }
        return result;
    }

    /**
     * Retrieves the object uuid either from the last path segment, the 'uuid' column
     * of the table, or throws an exception if a directory type Uri.
     * @param uri
     * @param resolver
     * @return
     */
    public static synchronized String getUuid(Uri uri, ContentResolver resolver){
        Log.i(TAG, "getUuid() " + uri);
        int descriptor = Uris.getTypeDescriptor(uri);
        Log.d(TAG,".... descriptor=" + descriptor);
        switch(descriptor){
        case Uris.ITEM_UUID:
            return uri.getLastPathSegment();
        case Uris.ITEM_ID:
            Cursor c = null;
            String uuid = null;
            try{
                c = resolver.query(uri,
                    new String[]{ BaseContract.UUID }, null,null,null);
                if (c!=null && c.moveToFirst())
                    uuid = c.getString(0);
            }catch (Exception e){
                e.printStackTrace();
            } finally {
                if(c != null) c.close();
            }
            return uuid;
        default:
            throw new IllegalArgumentException("Invalid item uri: " + uri);
        }
    }

    /**
     * Retrieves the local row id either from the last path segment or the
     * {@link android.provider.BaseColumns#_ID _ID} column, of the table.
     *
     * @param uri The Uri to get the row id value for
     * @param resolver
     * @return The row id value
     * @throws IllegalArgumentException
     */
    public static synchronized long getRowId(Uri uri, ContentResolver resolver){
        switch(Uris.getTypeDescriptor(uri)){
        case Uris.ITEM_ID:
            return Long.parseLong(uri.getLastPathSegment());
        case Uris.ITEM_UUID:
            long id = -1;
            Cursor c = null;
            try{
                c = resolver.query(uri,
                    new String[]{ BaseContract._ID }, null,null,null);
                if (c.moveToFirst())
                    id = c.getInt(0);
            } finally {
                if(c != null) c.close();
            }
            return id;
        default:
            throw new IllegalArgumentException("Invalid item uri");
        }
    }
}

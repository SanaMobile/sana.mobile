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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.sana.android.provider.BaseContract;
import org.sana.api.IModel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * @author Sana Development
 *
 */
public abstract class TableHelper<T extends IModel> implements  CreateHelper, 
	DeleteHelper, InsertHelper, QueryHelper, SortHelper,UpdateHelper, 
	UpgradeHelper
{
	public static final String TAG = TableHelper.class.getSimpleName();
    protected static String root = "/";

	private final String table;
	private final String fColumn;
	private final String defaultExtension;
	private final Class<T> model;
	private Map<String,String> projection;
	
	protected TableHelper(Class<T> klazz){
		this( klazz, null, null);
	}
	
	protected TableHelper( Class<T> klazz, String fileColumn, String extension){
		this.model = klazz;
		this.table = klazz.getSimpleName().toLowerCase(Locale.US);
		this.fColumn = fileColumn;
		this.defaultExtension = extension;
		projection = new HashMap<String,String>();
	}
	
	private String pluralize(String in){
		String out = in.toLowerCase(Locale.US);
		return out + "s";
	}
	
	/**
	 * Returns the name of the table.
	 *
	 * @return A table name.
	 */
	public String getTable(){
		return table;
	}

    /**
     * Returns a File object representing the base directory for any files
     * stored by the table.
     *
     * @return The global table directory path appended with the table name.
     */
    public File getDir(File base){
        return new File(base,getTable());
    }

    /**
     * Minimalist representation of the columns needed to create a unique
     * filename for an item in the table. Subclasses should override if they
     * require additional columns.
     *
     * @return A String array containing the "_id", "uuid", and helper defined
     *  file column.
     */
    public String[] getFileProjection(){
        return new String[]{ BaseContract._ID, BaseContract.UUID, getFileColumn() };
    }

	/**
	 * Returns the name of the column in this table where a file path is stored.
	 * @return The name of the file column
	 * @throws UnsupportedOperationException if no file column is defined.
	 */
	public String getFileColumn(){
		if(fColumn == null)
			throw new UnsupportedOperationException("No file column defined");
		else
			return fColumn;
	}

	/**
	 * Returns a default extension if this table has a column for storing files.
	 * @return The default extension.
	 * @throws UnsupportedOperationException if no file column is defined.
	 */
	public String getFileExtension(){
		if(fColumn == null)
			throw new UnsupportedOperationException("No file type defined");
		else
			return defaultExtension;
	}

    /**
     * Returns a row based extension if this table has a column for storing
     * file paths. Default implementation calls {@link #getFileExtension()}.
     * Subclasses should override when handling more than one file type.
     *
     * @return The file extension for an item in the row.
     * @throws UnsupportedOperationException if no file column is defined.
     */
    public String getFileExtension(Cursor cursor){
        return getFileExtension();
    }

	/**
	 * Returns the IModel class this table represents.
	 * 
	 * @return
	 */
	public Class<T> getModelClass(){
		return model;
	}
	
	/* (non-Javadoc)
	 * @see org.sana.android.db.SortHelper#onSort(android.net.Uri)
	 */
	@Override
	public String onSort(Uri uri) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.InsertHelper#onInsert(android.net.Uri, android.content.ContentValues)
	 */
	/**
	 * Sets the creation and modification time to the current date time 
	 * formatted as {@link org.sana.api.IModel#DATE_FORMAT}
	 */
	@Override
	public ContentValues onInsert(ContentValues values) {
		ContentValues validValues = new ContentValues();
		String value = new SimpleDateFormat(IModel.DATE_FORMAT, 
				Locale.US).format(new Date());
		if(!values.containsKey(BaseContract.UUID))
			throw new IllegalArgumentException("Can not insert without uuid");
		validValues.put(BaseContract.CREATED, value);
		validValues.put(BaseContract.MODIFIED, value);
		validValues.putAll(values);
		return validValues;
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.UpdateHelper#onUpdate(java.lang.String, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	/**
	 * Sets the modification time to the current date time if not present,
	 * formatted as {@link org.sana.api.IModel#DATE_FORMAT}
	 */
	@Override
	public ContentValues onUpdate(Uri uri, ContentValues values) {
		ContentValues validValues = new ContentValues();
		if(!values.containsKey(BaseContract.MODIFIED)){
			String value = new SimpleDateFormat(IModel.DATE_FORMAT, 
					Locale.US).format(new Date());
			validValues.put(BaseContract.MODIFIED, value);
		}
		validValues.putAll(values);
		return validValues;
	}

	/**
	 * Provides the name projection for a column into the table
	 * 
	 * @param column The label to project
	 * @return A column label within the table
	 */
	public String getProjection(String column){
		return projection.get(column);
	}
	
	protected void setProjection(Map<String,String> projection){
		this.projection= Collections.unmodifiableMap(projection);
	}
	
	@Override
	public int onDelete(SQLiteDatabase db, String selection, String[] selectionArgs){
		return db.delete(getTable(), selection, selectionArgs);
	}
	
	/**
	 * Default Implementation which provides a thin wrapper around {@link android.database.sqlite.SQLiteQueryBuilder#query(SQLiteDatabase, String[], String, String[], String, String, String) SQLiteQueryBuilder.query()}
	 */
	@Override
	public Cursor onQuery(SQLiteDatabase db, String[] projection, 
			String selection, String[] selectionArgs, String sortOrder)
	{
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(getTable());
		return qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	}

    protected File onOpenFileForRow(File base, Cursor cursor){
        if(TextUtils.isEmpty(getFileColumn())){
            throw new IllegalArgumentException("No file column for this table");
        }
        File file = null;
		long id = cursor.getLong(cursor.getColumnIndex(BaseContract._ID));
		String extension = getFileExtension(cursor);
		file = new File(getDir(base), String.format("%s.%s", String.valueOf(id), extension));
        return file;
    }

    public File onOpenFile(File base, Cursor cursor){
        throw new UnsupportedOperationException("Not Implemented");
    }
}

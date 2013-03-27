package org.sana.android.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

/**
 * {@link android.content.ContentProvider ContentProvider} which contains a 
 * single column for storing the string representation of a file 
 * {@link android.net.Uri Uri}. The file column must be returned by the 
 * {@link #getFileColumn() getFileColumn} method. This method 
 * 
 * @author Sana Development
 * 
 */
public abstract class FileContentProvider extends ContentProvider{
	
	/**
	 * Returns the directory where files will be stored. Default behavior is
	 * to return the value of {@link android.content.Context#getDir(String, int) getDir(String,int)}
	 * with the last path segment of the Uri as the <code>name</code> parameter 
	 * passed to {@link android.content.Context#getDir(String, int) 
	 * getDir(String, int)} method.
	 * 
	 * @param A content style uri.
	 * @param mode Operating mode. Use 0 or {@link android.content.Context#MODE_PRIVATE MODE_PRIVATE} 
	 * 	for the default operation, {@link android.content.Context#MODE_WORLD_READABLE MODE_WORLD_READABLE} 
	 *  and {@link android.content.Context#MODE_WORLD_WRITEABLE MODE_WORLD_WRITABLE} to control 
	 *  permissions.
	 * @return The directory where blob data can be read.
	 */
	protected File getFileDirectory(Uri uri, int mode){
		return getContext().getApplicationContext().getDir(uri.getLastPathSegment(), mode);
	}
	
	/**
	 * Generates the file {@link java.io.File File} used as the destination for 
	 * reading and writing blob data when opening a file at a given URI.
	 *   
	 * @param uri The URI which will be used to generate the File name
	 * @return The newly created file.
	 * @throws FileNotFoundException if the file path stored in the 
	 * 	ContentValues object is null or the File does not exist.
	 */
	protected abstract File insertFileHelper(Uri uri, ContentValues values) 
			throws FileNotFoundException;
	
	/**
	 * Deletes the file {@link java.io.File File} used as the destination for 
	 * reading and writing blob data when opening a file at the given URI.
	 *   
	 * @param uri The URI which will have File data deleted
	 * @return The number of files deleted.
	 */	
	protected int deleteFileHelper(Uri uri){
		getContext().getApplicationContext().deleteFile(null);
		return 1;
	}
	
	/**
	 * The column which the file URI is stored in.
	 * 
	 * @return The name of the blob column.
	 */
	protected abstract String getFileColumn();
	
	/**
	 * Opens a file blob stored in the column specified by {@link #getFileColumn()}
	 * 
     * @param uri The URI to be opened.
     * @param mode The file mode.  May be "r" for read-only access,
     * "w" for write-only access (erasing whatever data is currently in
     * the file), "wa" for write-only access to append to any existing data,
     * "rw" for read and write access on any existing data, and "rwt" for read
     * and write access that truncates any existing file.
     * 
     * @return Returns a new ParcelFileDescriptor that can be used by the
     * client to access the file.
	 */
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws 
		FileNotFoundException
	{        
		return openFileHelper(uri, getFileColumn(), mode);
	}
	
 
	/**
     * Convenience method for subclasses that wish to implement the default 
     * {@link android.content.ContentProvider#openFileHelper(Uri,String openFileHelper(Uri,String} 
     * using the column returned by {@link #getFileColumn()}.
     *
     * @param uri The URI to be opened.
     * @param column The column which holds the Uri string.
     * @param mode The file mode.  May be "r" for read-only access,
     * "w" for write-only access (erasing whatever data is currently in
     * the file), "wa" for write-only access to append to any existing data,
     * "rw" for read and write access on any existing data, and "rwt" for read
     * and write access that truncates any existing file.
     *
     * @return Returns a new ParcelFileDescriptor that can be used by the
     * client to access the file.
     */
    protected ParcelFileDescriptor openFileHelper(Uri uri, String column,
            String mode) throws FileNotFoundException 
    {
    	// This is refactored out into a discrete method, as opposed to just 
    	// leaving it as part of the openFile() method, for later use with
    	// ContentProviders using file storage in multiple columns
        Cursor c = query(uri, new String[]{ column }, null, null, null);
        int count = (c != null) ? c.getCount() : 0;
        if (count != 1) {
            // If there is not exactly one result, throw an appropriate
            // exception.
            if (c != null) {
                c.close();
            }
            if (count == 0) {
                throw new FileNotFoundException("No entry for " + uri);
            }
            throw new FileNotFoundException("Multiple items at " + uri);
        }
        
        c.moveToFirst();
        int i = c.getColumnIndex(column);
        String path = (i >= 0 ? c.getString(i) : null);
        c.close();
        if (path == null) {
            throw new FileNotFoundException("Annotations '" + column 
            		+ "' not found.");
        }

        int modeBits;
        // Borrowed from android.content.ContentResolver.modeToMode
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new FileNotFoundException("Bad mode for " + uri + ": "
                    + mode);
        }
        return ParcelFileDescriptor.open(new File(path), modeBits);
    }
}

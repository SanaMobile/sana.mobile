package org.sana.android.media;

import java.io.File;
import java.io.IOException;

import org.sana.R;
import org.sana.android.Constants;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;


/**
 * Representation of an informative media resource. May be image, video, audio, 
 * or text. The resource may be targeted towards specific groups by specifying 
 * the audience.
 * <br/>
 * See {@link #EducationResourceParser()} for XML specification.
 * 
 * @author Sana Development Team
 */
public class EducationResource implements Parcelable, 
	Comparable<EducationResource>
{
	/**
	 * An enumeration of accepted target audiences for media
	 * @author Sana Development Team
	 */
	public enum Audience{
		ALL,
		ERROR,
		PATIENT,
		WORKER;
		
		/**
		 * Returns the lower case value of <b>Audience.name()</b>
		 */
		@Override
		public String toString(){
			return this.name().toLowerCase();
		}
	}
	public static final String TAG = EducationResource.class.getSimpleName();
	
	// xml tags
	
	/** An a list of items. */
	public static final String LIST = "mediaList";
	
	/** The media type. */
	public static final String ITEM = "educationResource";
	
	/** A unique identifier */ 
	public static final String ID = "id";
	
	/** The version of this resource */
	public static final String VERSION = "majorMinorVersion";
	
	/** A title for display. */
	public static final String TITLE = "name";
	
	/** The author of the resource */
	public static final String AUTHOR = "author";
	
	/** A short but descriptive text about the resource. */
	public static final String DESCRIPTION = "description";
	
	/** Informative text in lieu of, or addition to, a media resource. */
	public static final String TEXT = "text";
	
	/** The name of a media file. */
	public static final String FILENAME = "resource";
	
	/** The remote location to get the media from. */
	public static final String DOWNLOAD_URL = "downloadUrl";
	
	/** The media mime type. */
	public static final String MIMETYPE = "mimeType";
	
	/** A hash of the resource */
	public static final String HASH = "hash";

	/** The group this resource is directed towards */
	public static final String AUDIENCE = "audience";
	
	/** Default path to look for media resources */ 
    public static final String DEFAULT_MEDIA_PATH = Constants.PATH_EDUCATION;
    
    /** Default root directory on the SD card */
    public static final String DEFAULT_MEDIA_ROOT = 
    	Environment.getExternalStorageDirectory().getAbsolutePath();
    
    /** Default name of the XML file holding a list of available resources */
    public static final String DEFAULT_MEDIA_XML = "manifest.xml";
    
    public String id = "";
    public String name = "";
    public String version="";
    public String author = "";
    public String description = "";
    public String text = "";
    public String filename = "";
    public String downloadUrl = "";
    public String hash = "";
    public String mimeType = "";
    public Audience audience = Audience.ALL;
    
    /**
     * A new Media instance with all fields initialized to empty strings and a
     * target audience of ALL. 
     */
    public EducationResource(){}
    
    /**
     * A new Media instance read from a Parcel
     * @param in the Parcel to read from
     */
    public EducationResource(Parcel in){
    	id = in.readString();
    	name = in.readString();
    	author = in.readString();
    	description = in.readString();
    	text = in.readString();
    	filename = in.readString();
    	downloadUrl = in.readString();
    	hash = in.readString();
    	mimeType = in.readString();
    	audience = Audience.valueOf(in.readString().toUpperCase());
    }
    
    /**
     * Compares this object to another HelpInfo instance. Comparison order is
     * by Title then audience.
     */
    @Override
    public int compareTo(EducationResource rsrc){
        int beforeTitle = compareTitle(rsrc);
        if (beforeTitle == 0){
        	return compareAudience(rsrc);
        } else {
        	return beforeTitle;
        }
    }
    
    private int compareTitle(EducationResource rsrc){
        return this.name.compareToIgnoreCase(rsrc.name);
    }
    
    private int compareAudience(EducationResource rsrc){
        return this.audience.name().compareToIgnoreCase(rsrc.audience.name());
    }
    
    /**
     * Checks whether this HelpInfo has a valid resource. This is included to
     * facilitate run time checks.
     * 
     * @return True if resource is defined and file exists or if resource is 
     * 		   not defined and text is not empty;
     * @throws IOException If external storage drive is not mounted.
     */
    public boolean hasValidResource() {
    	boolean result = false;
		String mount = Environment.getExternalStorageState();
		if(!mount.equals(Environment.MEDIA_MOUNTED)){
			Log.e(TAG, "Can not open external storage. " + mount);
			return false;
		}
		if(!TextUtils.isEmpty(this.filename)){
			File f = new File(EducationResource.DEFAULT_MEDIA_ROOT 
								+ EducationResource.DEFAULT_MEDIA_PATH +
								this.filename);
			if(!f.exists()){
				// resource not null and file does not exist
				this.text = "Resource not available";
			} else {
				// resource not null and exists
				result = true;
			}
		} else {
			// text only
			result = (TextUtils.isEmpty(this.text))? false: true;
		}
		if (!result)
			this.audience = Audience.ERROR;
		return result;
    }
    
    /**
     * Generates a Uri for this media file.
     * @param root the root path
     * @return
     */
	public Uri uri(String root){
		String mediaPath = root + filename;
		File f = new File(mediaPath);
		Log.d(TAG, f.getAbsolutePath());
		return Uri.fromFile(f);
	}    
	
	/**
     * Generates a Uri for this media file.
     * @param root the root path
     * @return
     */
	public Uri uri(File dir){
		File f = new File(dir,filename);
		Log.d(TAG, f.getAbsolutePath());
		return Uri.fromFile(f);
	}
	
	/**
	 * Formats an input String to a media compatible id
	 * @param input The String to convert
	 * @return A String with whitespace and all non-alphanumeric characters 
	 * 		stripped
	 */
	public static String toId(String input){
		String regex = "[^a-zA-Z0-9]";
		String nid = input.replaceAll(regex, "");
		Log.d(TAG, " New id: " + nid);
		return nid;    		
	}
	
	public static EducationResource error(String attr, String text){
		EducationResource err = new EducationResource();
		err.text = "XML Error: attr: " + attr + ", value: " + text;
		err.filename = null;
		return err;
	}
	
	/**
	 * Displays the resource as a text Dialog
	 * 
	 * @param c
	 * @param media the help info to display as text
	 * @return a new help dialog
	 */
	public static AlertDialog asDialog(Context c, EducationResource media){
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setMessage(media.text).setPositiveButton(
			c.getResources().getString(R.string.general_ok),
			new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		return builder.create();
	}

	/**
	 * Returns the manifest for education resources.available on the external 
	 * storage device.
	 *  
	 * @return A file object.
	 */
	public static File getManifest(){
		return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+Constants.PATH_EDUCATION + Constants.MANIFEST);
	}
	
	/**
	 * Returns the metadata for education resources available on the external 
	 * storage device.
	 *  
	 * @return A file object.
	 */
	public static File getMetadata(){
		return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+Constants.PATH_EDUCATION + Constants.METADATA);
	}
	
	public static File getDir(){
		return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+Constants.PATH_EDUCATION);
		
	}
	
	/**
	 * Creates the necessary directories and files on the external drive
	 */
	public static void intializeDevice(){
		String mount = Environment.getExternalStorageState();
		Log.d(TAG, "Media stat:" + mount);
		if(!mount.equals(Environment.MEDIA_MOUNTED)){
			Log.e(TAG, "Can not initialize sdcard education resource dir.");
			return;
		}

		File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+Constants.PATH_EDUCATION);
		if (f.mkdirs()){
			Log.d(TAG, "Created Sana media directories");
		} else {
			Log.d(TAG, "Sana media directory failed. Already exists:" 
					+ f.exists());
		}
		File nm = new File(f, ".nomedia");
		try {
			if(!nm.exists())
				nm.createNewFile();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/** {@inheritDoc} */
	@Override
	public int describeContents() {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
    	dest.writeString(id);
    	dest.writeString(name);
    	dest.writeString(author);
    	dest.writeString(description);
    	dest.writeString(text);
    	dest.writeString(filename);
    	dest.writeString(downloadUrl);
    	dest.writeString(hash);
    	dest.writeString(mimeType);
    	dest.writeString(audience.toString());
	}
	
	/** Parcelable.Creator implementation for EducationResource object */ 
	public static final Creator<EducationResource> CREATOR = 
			new Creator<EducationResource>(){
		
		/** {@inheritDoc} */
		@Override
		public EducationResource createFromParcel(Parcel source) {
			return new EducationResource(source);
		}
		
		/** {@inheritDoc} */
		@Override
		public EducationResource[] newArray(int size) {
			EducationResource[] result = new EducationResource[size];
			return result;
		}};
}
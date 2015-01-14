package org.sana.android;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.sana.android.db.ImageProvider;
import org.sana.android.db.SanaDB;
import org.sana.android.db.SanaDB.ImageSQLFormat;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Scaling image adapter is used by the image grid for displaying multiple
 * images acquired on the phone on a picture element in a given procedure. The
 * ScalingImageAdapter also maintains the state for the set of selected images.
 * 
 * Images are stored in a database. This adapter exposes these images to the
 * GridView.
 * 
 * @author Sana Dev Team
 */
public class ScalingImageAdapter extends CursorAdapter {
    private static final String TAG = ScalingImageAdapter.class.getSimpleName();
    private static final int IMAGE_WIDTH = 90;
    private static final int IMAGE_HEIGHT = 90;
    
    private int scaleFactor;
    private HashMap<Long, Boolean> selectedImages;
    
    /**
     * Constructs a new Adapter for scaling images
     * 
     * @param ctx the active context
     * @param cur reference to the images 
     * @param scaleFactor initial scale factor
     */
    public ScalingImageAdapter(Context ctx, Cursor cur, int scaleFactor) {
        super(ctx,cur);
        this.scaleFactor = scaleFactor;
        this.selectedImages = new HashMap<Long,Boolean>();
    }
    
    /**
     * Checks whether an image is selected
     * 
     * @param id the id of the image to check
     * @return true if it is selected
     */
    public boolean isSelected(long id) {
    	if(selectedImages.containsKey(id)) {
    		return selectedImages.get(id);
    	}
    	return false;
    }

    /**
     * Selects or deselects an image
     * 
     * @param id the id of the image
     * @param status the new selected state
     * @return true if it is selected
     */
    public void setSelected(long id, boolean status) {
    	Log.i(TAG, "Setting " + id + " selected as " + status);
    	selectedImages.put(id, status);
    }
    
    /**
     * Negates the current selected state
     * 
     * @param selection the item to negate
     */
    public void toggleSelection(long selection) { 
    	setSelected(selection, !isSelected(selection));
    }
    
    private Bitmap bitmapForImageUri(Context context, Uri imageUri) throws 
    	IOException 
    {
    	BitmapFactory.Options bmo = new BitmapFactory.Options();
    	bmo.inSampleSize = scaleFactor;
    	InputStream is = context.getContentResolver().openInputStream(imageUri);
    	Bitmap bitmap = BitmapFactory.decodeStream(is, null, bmo); 
    	is.close();
    	return bitmap;
    }
    
    /**
     * Takes the cursor and returns the URI for the cursor's current row.
     */
    private Uri getImageThumbnailUriFromCursorRow(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(ImageSQLFormat._ID
        		));
        Uri uri = ContentUris.withAppendedId(SanaDB.ImageSQLFormat.CONTENT_URI, 
        		id);
        return ImageProvider.getThumbUri(uri);
    }
    
    /**
     * Makes a new, empty view. (do not bind an image to it or set its id)
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
    	Log.i(TAG, "newView");
        ImageView imageView = new SelectableImageView(context, this);
        imageView.setLayoutParams(new GridView.LayoutParams(IMAGE_WIDTH, 
        		IMAGE_HEIGHT));
        imageView.setAdjustViewBounds(false);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //imageView.setPadding(6, 6, 6, 6);
        imageView.setPadding(6, 12, 6, 12);
        return imageView;
    }

    /**
     * Takes a SelectableImageView, binds an image to it, and sets it image id.
     */
    @Override
    public void bindView(View _view, Context context, Cursor cursor) {
        SelectableImageView view = (SelectableImageView)_view;

        long imageId = cursor.getLong(cursor.getColumnIndex(
        		ImageSQLFormat._ID));
        view.setImageId(imageId);
        
        // Make new images selected by default
        if(!selectedImages.containsKey(imageId)) {
        	selectedImages.put(imageId, true);
        }
        
        Uri thumbUri = getImageThumbnailUriFromCursorRow(cursor);
        view.setImageURI(thumbUri);
        
        //Log.i(TAG, "bindView: " + thumbUri);
        /*new Thread() { 
        	private Context mContext;
        	private Uri imageUri;
        	private SelectableImageView view;
        	
        	void initAndStart(Uri imageUri, SelectableImageView view, Context context) {
        		this.imageUri = imageUri;
        		this.view = view;
        		this.mContext = context;
        		start();
        	}
        	
        	public void run() {
        		try {
        			view.setImageBitmap(bitmapForImageUri(mContext, imageUri));
        		} catch(IOException e) {
        		}
        	}
        }.initAndStart(imageUri, view, context);*/
    }
}
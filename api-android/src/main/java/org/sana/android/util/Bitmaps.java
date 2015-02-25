package org.sana.android.util;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

public class Bitmaps {
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}

    /**
     *
     * @param imagePath
     * @param reqWidth
     * @param reqHeight
     * @return
     * @throws java.io.FileNotFoundException if the file provided by
     *  <code>imagePath</code> does not exist.
     */
	public static Bitmap decodeSampledBitmapFromFile(String imagePath,
       int reqWidth, int reqHeight) throws FileNotFoundException{
        // Check that we have a valid file
        boolean exists = false;
        if(!TextUtils.isEmpty(imagePath)){
            File file = new File(imagePath);
            exists = file.exists();
            file = null;
        }
        if(!exists)
            throw new FileNotFoundException("Bitmap does not exist. path=" +
                    imagePath);
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    options.inPurgeable = true;
	    //options.inInputShareable = true;
	    BitmapFactory.decodeFile(imagePath, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    options.inJustDecodeBounds = false;
	    /*
	    options.inDither = false;
	    options.inScaled = false;
	    options.inPurgeable = true;
	    */
	    Bitmap bm = BitmapFactory.decodeFile(imagePath, options);
	    return bm;
	}
}

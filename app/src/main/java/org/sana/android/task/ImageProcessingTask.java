package org.sana.android.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.sana.android.db.EventDAO;
import org.sana.android.db.ImageProvider;
import org.sana.android.db.SanaDB.ImageSQLFormat;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

/**
 * Task for handling image quirks
 *
 * @author Sana Development Team
 *
 */
public class ImageProcessingTask extends
	AsyncTask<ImageProcessingTaskRequest, Void, Void>
{
	public static final String TAG = ImageProcessingTask.class.getSimpleName();

	private void logDebugInformationAboutCameraIntents(Intent data) {
		// Print some diagnostic information about the Intent sent back
		// from the Camera app so that if a manufacturer does goofy
		// things in the future we have a fighting chance at remotely
		// debugging it.
		if (data != null) {
			Log.i(TAG, "Received intent from Camera application. May need to "
					+"enable workaround. The Intent's Action is: "
					+ data.getAction());

			// The HTC Tattoo's Camera app just sends back a content://
			// Uri pointing to the image in the Intent. It also included
			// a Parcelable android.graphics.Bitmap object in the extras
			// bundle, except it's a tiny image (320x240).
			Uri returnedUri = data.getData();
			if (returnedUri != null) {
				Log.i(TAG, "Received Uri from Camera application: "
						+ returnedUri);
			}

			Bundle b = data.getExtras();
			if (b != null) {
				Log.d(TAG, "Camera intent had bundle.");
				for (String key : b.keySet()) {
					Log.d(TAG, "Camera Intent Bundle has key: " + key);
				}
				// Try to get the bitmap and poke at it.
				Bitmap bitmap = b.getParcelable("data");
				if (bitmap != null) {
					int iWidth = bitmap.getWidth();
					int iHeight = bitmap.getHeight();
					Log.i(TAG, "Intent had a bitmap Parcel in the 'data' extra."
							+" width: " + iWidth + " height: " + iHeight);
				}
			}
		}

	}

	/**
	 * Some Android manufacturers have decided to replace the
	 * Google Camera application with their own, buggy camera
	 * application. This method attempts to smooth over the
	 * differences and undo the damage done by HTC to Android.
	 *
	 * This currently has a workaround for the HTC Tattoo only.
	 *
	 * @return An InputStream to the image we just captured with the Camera app.
	 * @throws FileNotFoundException
	 */
	private InputStream getImageInputStreamWithWorkaround(
			ImageProcessingTaskRequest request) throws FileNotFoundException
	{
		InputStream is = null;
		// Workaround for phones using a custom Camera application like the HTC Tattoo.
		if (request.tempImageFile.exists()) {
			Log.i(TAG, "Temp file exists, no workaround needed.");
			is = new FileInputStream(request.tempImageFile);
		} else if (request.intent != null) {
			Log.i(TAG, "HTC Sense Workaround active.");
			// The Tattoo's Camera app will not store the file in the
			// tempImageFile path. Instead it will return a Uri pointing to the
			// file.
			Uri fileUri = request.intent.getData();
			Log.i(TAG, "HTC Sense Workaround active. File uri is: " + fileUri.toString());
			if (fileUri != null) {
				is = request.c.getContentResolver().openInputStream(fileUri);
			}
		}
		return is;
	}

	/** {@inheritDoc} */
	@Override
	protected Void doInBackground(ImageProcessingTaskRequest... params) {
		ImageProcessingTaskRequest request = params[0];

		if (request == null) {
			Log.e(TAG, "Didn't receive valid ImageProcessingTaskRequest");
			return null;
		}

		File tempImageFile = request.tempImageFile;
		String savedProcedureId = request.savedProcedureId;
		String elementId = request.elementId;
		Context c = request.c;

		logDebugInformationAboutCameraIntents(request.intent);

		// Get the image parameters stored in the Intent
		ContentValues values = new ContentValues();
		values.put(ImageSQLFormat.ENCOUNTER_ID, savedProcedureId);
		values.put(ImageSQLFormat.ELEMENT_ID, elementId);

		Uri imageUri = c.getContentResolver().insert(ImageSQLFormat.CONTENT_URI, values);
		Uri thumbUri = ImageProvider.getThumbUri(imageUri);

		Log.d(TAG, "...Old URI: " + imageUri);
		Log.d(TAG, "...Thumb URI: " + thumbUri);

		try {
			InputStream is = getImageInputStreamWithWorkaround(request);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			options.inSampleSize = 1;
			// Only decode the size, not the image itself. If we
			// decode the full image then we sometimes get Out
			// of Memory errors.
			Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
			//BitmapFactory.decodeFile(tempImageFile.getAbsolutePath(), options); //Works for Android 1.5
			is.close();
			//bmp.recycle();
			int iWidth = options.outWidth;
			int iHeight = options.outHeight;

			Log.i(TAG, "Image capture activity returned bitmap of width "
                    + iWidth + " and height " + iHeight);

			OutputStream os = c.getContentResolver().openOutputStream(imageUri);
            is = getImageInputStreamWithWorkaround(request);
            //Bitmap bmp = BitmapFactory.decodeStream(is);

			final int bufSize = 4096;
			byte[] buffer = new byte[bufSize];
			int bytesRead = 0;
			int bytesWritten = 0;
			while (bytesRead != -1) {
                                bytesWritten += bytesRead;
				os.write(buffer, 0, bytesRead);
				bytesRead = is.read(buffer, 0, bufSize);
			}
			is.close();
			os.flush();
			os.close();
			Log.d(TAG, "...copy: read="+bytesRead + ", written="+bytesWritten);


            // Correct orientation of original file
            try {
				Log.d(TAG, "...correcting orientation");
				ImageProvider.correctOrientation(request.c, imageUri);
				Log.d(TAG, "...correcting orientation success");
            } catch (OutOfMemoryError e){
				Log.e(TAG, "OutOfMemoryError! Orientation correction");
            } catch (Exception e){
                e.printStackTrace();
            }
			int thumbCompression = 50;
			int thumbMaxSize = 100;
			int largestDimension = (iWidth > iHeight) ? iWidth : iHeight;

			Log.i(TAG, "Saving thumbnail for " + imageUri + " with "
					+ thumbCompression + "% quality.");
			// We want a picture with it's largest side 100 pixels.
			int scaleFactor = largestDimension / thumbMaxSize;
			options = new BitmapFactory.Options();
			options.inSampleSize = scaleFactor;

			is = getImageInputStreamWithWorkaround(request);
			Bitmap thumbBitmap = BitmapFactory.decodeStream(is, null, options);

			os = c.getContentResolver().openOutputStream(thumbUri);
			thumbBitmap.compress(Bitmap.CompressFormat.JPEG, thumbCompression,
					os);
			os.flush();
			os.close();
			is.close();
			thumbBitmap.recycle();
            // Correct thumb orientation of original file
            try {
                Log.d(TAG, "...correcting thumb orientation");
                ImageProvider.correctOrientation(request.c, thumbUri);
                Log.d(TAG, "...correcting thumb orientation success");
            } catch (OutOfMemoryError e){
                Log.e(TAG, "OutOfMemoryError! Orientation correction");
            } catch (Exception e){
                e.printStackTrace();
            }
			// Flag the file as saved - does not record image size
			values = new ContentValues();
			values.put(ImageSQLFormat.FILE_VALID, true);
			c.getContentResolver().update(imageUri, values, null, null);

			if (tempImageFile.exists()) {
				Log.d(TAG, "...temp image file exists");
				Log.d(TAG, "...path=" + tempImageFile.getAbsolutePath());
				//tempImageFile.delete();
			}

			Log.i(TAG, "Successfully saved " + imageUri);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "While storing the image, got an exception: "
					+ e.toString());
			EventDAO.logException(c, e);
		} catch (IOException e) {
			Log.e(TAG, "While storing the image, got an exception: "
					+ e.toString());
			EventDAO.logException(c, e);
		}
		return null;
	}

    protected int rotateBitmap(File file, Bitmap bitmap) throws IOException {

        ExifInterface exif = new ExifInterface(file.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);


        return orientation;
    }
}

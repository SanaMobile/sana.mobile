package org.sana.android;

import java.io.IOException;
import java.io.InputStream;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;

import org.sana.R;
/**
 * ImagePreviewDialog for reviewing captured images. 
 * 
 * Quick user hints: Zoom in, Zoom out, center image, and close dialog (back)
 * buttons available.
 * 
 * Also, a user can flick the image or pan it by dragging the image. If the user
 * holds down a finger to the image, it will reset (center and resize). If a 
 * user taps on the image, it will zoom in.
 * 
 * @author Sana Dev Team
 */
public class ImagePreviewDialog extends Dialog implements OnClickListener,
		OnTouchListener, OnGestureListener {

	private static int BITMAP_SAMPLE_SIZE = 1;
	private Button zoomInButton, zoomOutButton, centerButton, endButton, 
		rotateButton, fitButton;
	private ImageView imageView;
	private GestureDetector gestureDetector;
	private float scaledWidth, scaledHeight, transX, transY;
	private float imageWidth, imageHeight;
	private float originalWidth, originalHeight;
	private final float zoomConst = 0.4f;
	private final float initScaledWidth = 0.55f, initScaledHeight = 0.55f;
	
	/**
	 * Default Constructor
	 * @param c
	 */
	public ImagePreviewDialog(Context c) {
		super(c);
		LinearLayout fv = new LinearLayout(c);
		imageView = new ImageView(c);
		zoomInButton = new Button(c);
		zoomInButton.setText(c.getText(R.string.general_zoom_in));
		zoomInButton.setOnClickListener(this);
		zoomOutButton = new Button(c);
		zoomOutButton.setText(c.getText(R.string.general_zoom_out));
		zoomOutButton.setOnClickListener(this);
		centerButton = new Button(c);
		centerButton.setText(c.getText(R.string.general_center));
		centerButton.setOnClickListener(this);
		endButton = new Button(c);
		endButton.setText(c.getText(R.string.general_back));
		endButton.setOnClickListener(this);
		LinearLayout buttonContainer = new LinearLayout(c);
		buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
		buttonContainer.addView(zoomInButton);
		buttonContainer.addView(zoomOutButton);
		buttonContainer.addView(centerButton);
		buttonContainer.addView(endButton);
		imageView.setOnClickListener(this);
		imageView.setOnTouchListener(this);
		gestureDetector = new GestureDetector(this);

//		height is a hack set for the G1 phone
//		imageView.setLayoutParams(new LayoutParams(-1, -1));
				
		fv.addView(buttonContainer);
		fv.addView(imageView);
		fv.setOrientation(LinearLayout.VERTICAL);
		fv.setGravity(Gravity.CENTER_HORIZONTAL);
		fv.setLayoutParams(new LayoutParams(150, -1));
		setTitle("View Photos");
		setContentView(fv);
	}
	
	/** {@inheritDoc} */
	@Override
	public void onClick(View v) {
		if (v == zoomInButton) {
			zoomIn();
		} else if (v == zoomOutButton) {
			zoomOut();
		} else if (v == centerButton) {
			center();
		} else if (v == endButton) {
			this.dismiss();
		}
	}

	/**
	 * Loads a bitmap image into the image preview dialog window.
	 * @param is inputstream of the bitmap image
	 */
	public void showImage(Uri imageUri) throws IOException {
		BitmapFactory.Options bmo = new BitmapFactory.Options();

    	// Get the image size.
    	bmo.inJustDecodeBounds = true;
    	InputStream imageInputStream = 
    		getContext().getContentResolver().openInputStream(imageUri);
    	BitmapFactory.decodeStream(imageInputStream, null, bmo);
    	int width = bmo.outWidth;
    	int height = bmo.outHeight;
    	bmo.inJustDecodeBounds = false;
    	imageInputStream.close();
    	
    	
    	// Load the bitmap and downsample it to 1000 pixels along its largest 
    	// dimension.
    	int maxDimension = (width > height) ? width : height;
    	bmo.inSampleSize = maxDimension / 1000;
    	imageInputStream = getContext().getContentResolver().openInputStream(
    			imageUri);
		Bitmap loadedBitmap = BitmapFactory.decodeStream(imageInputStream, null,
															bmo);
		
		originalWidth = (float) (loadedBitmap.getWidth());
		originalHeight = (float) (loadedBitmap.getHeight());	
		scaledWidth = initScaledWidth;
		scaledHeight = initScaledHeight;
		imageView.setImageBitmap(loadedBitmap);		
		transX = 0f;
		transY = 0f;
		updateSize();
		setImageMatrix();
	}
	
	// for scaling
	void updateSize() {
		imageWidth = originalWidth * scaledWidth;
		imageHeight = originalHeight * scaledHeight;
	}

	// for scaling
	void setImageMatrix() {
		Matrix mtrx = new Matrix();
		mtrx.postScale(scaledWidth, scaledHeight);
		mtrx.postTranslate(transX, transY);
		imageView.setImageMatrix(mtrx);
		imageView.setScaleType(ScaleType.MATRIX);
		imageView.invalidate();
	}

	/**
	 * Pan an image.
	 * @param dx left/right movement amount
	 * @param dy up/down movement amount
	 */
	void translate(float dx, float dy) {
		transX -= dx;
		transY -= dy;
		setImageMatrix();
	}

	/**
	 * Zooms in the image in by a constant factor (zoomConst).
	 */
	void zoomIn() {
		float origImageWidth = imageWidth;
		float origImageHeight = imageHeight;
		scaledWidth += zoomConst;
		scaledHeight += zoomConst;
		updateSize();
		float widthIncrease = imageWidth - origImageWidth;
		float heightIncrease = imageHeight - origImageHeight;
		translate(0.5f * widthIncrease, 0.5f * heightIncrease);	
		//setImageMatrix();
	}

	/**
	 * Zooms out the image in by a constant factor (zoomConst).
	 */
	void zoomOut() {		
		float origImageWidth = imageWidth;
		float origImageHeight = imageHeight;
		if (scaledWidth > zoomConst)
			scaledWidth -= zoomConst;
		if (scaledHeight > zoomConst)
			scaledHeight -= zoomConst;
		updateSize();
		float widthIncrease = imageWidth - origImageWidth;
		float heightIncrease = imageHeight - origImageHeight;
		translate(0.5f * widthIncrease, 0.5f * heightIncrease);
		//setImageMatrix();
	}

	/**
	 * Centers the image in the image preview dialog.
	 */
	void center() {
		transX = (imageView.getWidth() / 2.0f) - (imageWidth / 2.0f);
		transY = (imageView.getHeight() / 2.0f) - (imageHeight / 2.0f);
		updateSize();
		translate(0, 0); 
	}

	/** {@inheritDoc} */
	@Override
	public boolean onTouch(View v, MotionEvent me) {
		if (v == imageView) {
			gestureDetector.onTouchEvent(me);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean onDown(MotionEvent arg0) {
		return false;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float velocityX,
			float velocityY) 
	{
		//Log.i("imagepreview", "fling x:" + velocityX + " y:" + velocityY);
		translate((float) (-velocityX / 8.0), (float) (-velocityY / 8.0));
		return false; 
	}

	/**
	 * Reset image position and size when a finger is held to the screen.
	 */
	public void onLongPress(MotionEvent arg0) {
		// Log.d("imagepreview", "long press");
		scaledWidth = initScaledWidth;
		scaledHeight = initScaledHeight;
		updateSize();
		center();
	}

	/** {@inheritDoc} */
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1,
			float distanceX, float distanceY) 
	{
		//Log.i("imagepreview", "scroll x:" + distanceX + " y:" + distanceY);
		translate((float) (distanceX / 1.0), (float) (distanceY / 1.0));
		return false;
	}
	
	/** {@inheritDoc} */
	@Override
	public void onShowPress(MotionEvent arg0) {
		// Log.d("imagepreview", "show press");
	}

	/**
	 * Zoom in when screen is tapped.
	 */
	public boolean onSingleTapUp(MotionEvent arg0) {
		//Log.d("imagepreview", "single tap");
		zoomIn();
		return false;
	}
}

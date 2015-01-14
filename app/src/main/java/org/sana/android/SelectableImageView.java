package org.sana.android;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.ImageView;

/**
 * A view in which one or more images can be selected
 * 
 * @author markyen
 * 
 */
public class SelectableImageView extends ImageView {
	private static final String TAG = SelectableImageView.class.getSimpleName();
	
	private long imageId = -1;
	private ScalingImageAdapter adapter;
	
	/**
	 * Constructs a new object with a specified adapter
	 * 
	 * @param context
	 * @param adapter the image adapter
	 */
	public SelectableImageView(Context context, ScalingImageAdapter adapter) {
		super(context);
		this.adapter = adapter;
	}
	
	/**
	 * Gets the id of the active image		
	 * @return long value of an image as an id
	 */
	public long getImageId() {
		return imageId;
	}


	/**
	 * Sets the id of the active image		
	 * @return long value of an image as an id
	 */
	public void setImageId(long imageId) {
		this.imageId = imageId;
	}
	
	/**
	 * Shows a highlighted border for selected images
	 * @return true if selected
	 */
	private boolean showBorder() {
		if(imageId != -1) {
			return adapter.isSelected(imageId);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (showBorder()) {
			Rect r_left = new Rect(0,0,8,canvas.getHeight());
			Rect r_top = new Rect(0,0,canvas.getWidth(),8);
			Rect r_right = new Rect(canvas.getWidth()-8,0,canvas.getWidth(),
					canvas.getHeight());
			Rect r_bottom = new Rect(0,canvas.getHeight()-8,canvas.getWidth(),
					canvas.getHeight());
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.YELLOW);
			canvas.drawRect(r_left, paint);
			canvas.drawRect(r_top, paint);
			canvas.drawRect(r_right, paint);
			canvas.drawRect(r_bottom, paint);
		}
	}
}

package org.sana.android.procedure;

import java.io.IOException;
import java.util.ArrayList;

import org.sana.R;
import org.sana.android.ImagePreviewDialog;
import org.sana.android.ScalingImageAdapter;
import org.sana.android.db.SanaDB;
import org.sana.android.db.SanaDB.ImageSQLFormat;
import org.sana.util.UUIDUtil;
import org.w3c.dom.Node;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * PictureElement is a ProcedureElement that allows a user to take photos. It
 * displays thumbnails of images that have been taken, allows a user to bring
 * up the camera to take new ones, and allows a user to click on a thumbnail to
 * bring up a dialog allowing for closer image review and examination.
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use </b>This element is useful in clinical scenarios wherever
 * images will aid in diagnosis.</li>
 * <li><b>Collects </b>Zero or more images returned as a comma separated list of
 * integer picture ids.</li>
 * </ul>
 *
 * @author Sana Development Team
 */
public class PictureElement extends ProcedureElement implements OnClickListener,
	OnItemClickListener, OnItemLongClickListener
{
    public static String TAG = PictureElement.class.getSimpleName();
    public static final String PARAMS_NAME = "keys";
    private ScalingImageAdapter imageAdapter;
    private static final int THUMBNAIL_SCALE_FACTOR = 5;
    private Button cameraButton;
    private GridView imageGrid;
    private ImagePreviewDialog imageReview;
    private Intent imageCaptureIntent;

    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.PICTURE;
    }

    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
        imageGrid = new GridView(c);
        Log.i(TAG, "Looking up for encounter: " + getProcedure().getInstanceUri());
        String procedureId =
        	getProcedure().getInstanceUri().getLastPathSegment();
        Log.w(TAG, "PictureELement: Encounter id " + procedureId);
        String whereStr;
        if(!UUIDUtil.isValid(procedureId))
        	whereStr = ImageSQLFormat.ENCOUNTER_ID + " = ? AND "
				+ ImageSQLFormat.ELEMENT_ID + " = ? AND "
				+ ImageSQLFormat.FILE_VALID + " = ?";
        else
        	whereStr = ImageSQLFormat.ENCOUNTER_ID + " = '?' AND "
				+ ImageSQLFormat.ELEMENT_ID + " = ? AND "
				+ ImageSQLFormat.FILE_VALID + " = ?";

		Cursor cursor = c.getContentResolver().query(
				SanaDB.ImageSQLFormat.CONTENT_URI,
				new String[] { ImageSQLFormat._ID }, whereStr,
				new String[] { procedureId, id, "1" }, null);

		// HAXMODE -- if we don't do this we leak the Cursor
		if (c instanceof Activity) {
			((Activity)c).startManagingCursor(cursor);
		}
        imageAdapter = new ScalingImageAdapter(c, cursor,
        		THUMBNAIL_SCALE_FACTOR);
        imageGrid.setAdapter(imageAdapter);
        imageGrid.setNumColumns(3);
        imageGrid.setVerticalSpacing(5);
        imageGrid.setPadding(5, 0, 0, 0);

        imageGrid.setOnItemClickListener(this);
        imageGrid.setOnItemLongClickListener(this);

        //imageGrid.setTranscriptMode(imageGrid.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        cameraButton = new Button(c);
        cameraButton.setText(R.string.btn_add_photo);
        cameraButton.setOnClickListener(this);

        imageReview = new ImagePreviewDialog(c);
        LinearLayout picContainer = new LinearLayout(c);
        picContainer.setOrientation(LinearLayout.VERTICAL);

        if(question == null) {
            question = c.getString(R.string.question_standard_picture_element);
        }

        //Set question
        TextView tv = new TextView(c);
        tv.setText(String.format("%s: %s", id, question));
        tv.setGravity(Gravity.CENTER);
        tv.setTextAppearance(c, android.R.style.TextAppearance_Medium);

        //Add to layout
        picContainer.addView(tv, new LinearLayout.LayoutParams(-1,-1,0.1f));
        //picContainer.addView(imageView, new LinearLayout.LayoutParams(-1,-1,0.1f));

        //Add button
        picContainer.addView(cameraButton,
        		new LinearLayout.LayoutParams(-1,-1,0.1f));
        picContainer.addView(imageGrid,
        		new LinearLayout.LayoutParams(-1, 210)); //LayoutParams(-1,-1,0.8f));
        picContainer.setWeightSum(1.0f);
        return picContainer;
    }

    /**
     * Sends an Intent to ProcedureRunner with the procedure id and element
     * id as parameters.
     * */
    @Override
	 public void onClick(View v) {
		 if (v == cameraButton) {
			 String procedureId =
				 getProcedure().getInstanceUri().getLastPathSegment(); //which procedure its part of
			 String[] params = {procedureId, id, String.valueOf(imageAdapter.getCount() + 1)};

			 imageCaptureIntent = new Intent(getContext(), ((Activity) getContext()).getClass());
			 imageCaptureIntent.putExtra(PARAMS_NAME, params)
			 			  .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			              .putExtra("intentKey", 0);
			 ((Activity) getContext()).startActivity(imageCaptureIntent);
		 }
	 }

    /** Toggles the image as selected */
    @Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Log.i(TAG, "onItemClick pos:" + position + " id:" + id);
		imageAdapter.toggleSelection(id);
		//v.postInvalidate();
		v.invalidate();
		//SelectableImageView view = (SelectableImageView) v;
		//view.toggleMultiSelected();
		//view.invalidate();
		//iadapter.notifyDataSetChanged();
		//parent.invalidate();

	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
	 */
    /** Opens Image view dialog. */
    @Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
			long id) {
		Log.i(TAG, "" + position);

		long imageId = imageAdapter.getItemId(position);
		Uri imageUri = ContentUris.withAppendedId(ImageSQLFormat.CONTENT_URI,
				imageId);
		try {
			// hack for in-emulator demo, commented out here, but use to load a
			// static image
			// imageReview.showImage(getContext().getContentResolver().openInputStream(Uri.parse("android.resource://org.sana/"
			// + R.drawable.incision2)));
			imageReview.showImage(imageUri);
			imageReview.show();
		} catch (IOException e) {
			Log.e(TAG, "Can't open the image file for uri " + imageUri);
		}
		return false;
	}

    /** {@inheritDoc} */
    @Override
	public void setAnswer(String answer) {
		if(!isViewActive()) {
			this.answer = answer;
		} else {
			// TODO : Fix this so that it works! We have the id # of the picture, and we need to reset 'selected' to match this such that
			// iadapter.getItemId(selected) == the answer we have here.
			String[] ids = answer.split(",");
			for(String id : ids) {
				imageAdapter.setSelected(Long.parseLong(id), true);
			}
		}
	}
    /** {@inheritDoc} */
    @Override
    public String getAnswer() {
        if(!isViewActive())
            return answer;
        ArrayList<String> answerz = new ArrayList<String>();
    	/*for(Entry<Long,SelectableImageView> e : iadapter.getViewMap().entrySet()) {
    		Log.i(TAG, "getAnswer -- item " + e.getKey());
    		if(e.getValue().isMultiSelected()) {
    			answerz.add(Long.toString(e.getKey()));
    			Log.i(TAG, "getAnswer -- item selected" + e.getKey());
    		}
    	}*/
		for (int i = 0; i < imageAdapter.getCount(); i++) {
			Long id = imageAdapter.getItemId(i);
			Log.i(TAG, "Considering element " + id + " for selection.");
			if (imageAdapter.isSelected(id)) {
				Log.i(TAG, "Element " + id + " is selected.");
				answerz.add(Long.toString(id));
			}
		}

    	if (answerz.size() > 0) {
    		StringBuilder csv = new StringBuilder(answerz.get(0));
    		for (int i=1; i<answerz.size(); i++) {
    			csv.append(",");
    			csv.append(answerz.get(i));
    		}
    		Log.i(TAG, "getAnswers is returning " + csv.toString());
    		return csv.toString();
    	} else {
    		Log.i(TAG, "getAnswers is returning blank");
    		return "";
    	}
    }

    /** {@inheritDoc} */
    @Override
    public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\""
        		+ id);
        sb.append("\" question=\"" + question);
        sb.append("\" answer=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }

    /** Default Constructor */
    private PictureElement(String id, String question, String answer,
    		String concept, String figure, String audio) {
        super(id, question, answer, concept, figure, audio);
    }

    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static PictureElement fromXML(String id, String question,
    		String answer, String concept, String figure, String audio,
    		Node node) throws ProcedureParseException
    {
    	return new PictureElement(id, question, answer, concept, figure, audio);
    }



}

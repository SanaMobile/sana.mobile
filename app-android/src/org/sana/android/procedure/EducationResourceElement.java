package org.sana.android.procedure;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.sana.R;
import org.sana.android.media.EducationResource;
import org.sana.android.media.EducationResourceParser;
import org.sana.android.media.EducationResource.Audience;
import org.sana.android.util.SanaUtil;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * A ProcedureElement for displaying help information as part of a procedure
 * such that the returned answer will indicate True/False depending on whether
 * the media was viewed.
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use </b> Wherever any educational resource that can be stored
 * in a file will be useful for presenting to a patient.</li>
 * <li><b>Collects </b> true or false based on whether the resource was viewed</li>
 * </ul>
 *  
 * @author Sana Development Team
 *
 */
public class EducationResourceElement extends ProcedureElement implements 
	OnClickListener 
{
    public static final String TAG = 
    	EducationResourceElement.class.getSimpleName();
	public static final String PARAMS_NAME = "keys";
    private Button mButton;
    private Intent intent;
    private EducationResource media;

    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.EDUCATION_RESOURCE;
    }

    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
		 Log.d(TAG, "");
        mButton = new Button(c);
        mButton.setText(c.getString(R.string.question_standard_view_resource));
        mButton.setOnClickListener(this);
        return encapsulateQuestion(c, mButton);
    }
    
    /** {@inheritDoc} */
    public void onClick(View v) {
    	if (v == mButton) {
    		Log.d(TAG, "Trying to get media");
    		if (media == null){
    			try {
    				String rawStr = getConcept() + question;
    				String path = EducationResource.getMetadata().getAbsolutePath(); 
    				media = EducationResourceElement.getResource(path, rawStr);
    			} catch (FileNotFoundException e) {
    				Log.d(TAG, "No media file");
    				media = null;
    			}
    		}
    		// If null media
    		if (media != null){
    			Log.d(TAG, media.id);
    			// No resource we assume text help only
    			if (TextUtils.isEmpty(media.filename)){
    				String msg = (TextUtils.isEmpty(media.text)) ? 
    						"No Help Available": media.text;
    				SanaUtil.createDialog(getContext(),media.name, msg).show();
    			// Valid resource so we get the uri
    			} else {
    				Uri uri = media.uri(EducationResource.getDir());
    	    		Log.d(TAG, "Opening media: " + uri.toString());
    				intent = new Intent();
    				Log.d(TAG, intent.toUri(Intent.URI_INTENT_SCHEME).toString());
    				intent.setAction(Intent.ACTION_VIEW)
    					.setDataAndType(uri,media.mimeType);
    				try{
    					((Activity) getContext()).startActivity(intent);
    					setAnswer("True");
    				} catch(Exception e){
        	    		Log.e(TAG, e.toString());
        	    		Log.e(TAG, "filename: " + media.filename);
        	    		Log.e(TAG, "mime: " + media.mimeType);
    					setAnswer("False");
    				}
    			}
    			// Error getting help media
    		} else {
    			Log.d(TAG, "No media");
    			setAnswer("False");
    			Toast.makeText(this.getContext(), "Error getting help info", 
    					Toast.LENGTH_SHORT);
    		}
    	}
    }
    
    private EducationResourceElement(String id, String question, String answer, 
    	String concept, String figure, String audio) 
    {
        super(id, question, answer, concept, figure, audio);
    }
    

    /** 
     * Creates the element from an XML procedure definition.
     * 
     * @param id The unique identifier of this element within its procedure.
     * @param question The text that will be displayed to the user as a question
     * @param answer The result of data capture.
     * @param concept A required categorization of the type of data captured.
     * @param figure An optional figure to display to the user.
     * @param audio An optional audio prompt to play for the user. 
     * @param node The source xml node. 
     * @return A new element.
     * @throws ProcedureParseException if there was an error parsing additional 
     * 			information from the Node.
     */
    public static EducationResourceElement fromXML(String id, String question, 
    	String answer, String concept, String figure, String audio, Node node)
		throws ProcedureParseException  
    {
        return new EducationResourceElement(id, question, answer, concept, 
        		figure, audio);
    }
    
    /**
     * Creates a new HelpInfo object from this Element which can be used to 
     * locate and view help text and resources.
     * 
     * @param path The path to media resources
     * @param rawStr Raw identification String
     * @return A new HelpInfo Object
     * @throws FileNotFoundException
     */
    private static EducationResource getResource(String path, String rawStr) 
    	throws FileNotFoundException
    {
		Log.d(TAG, "Getting media: " + path + ", " +rawStr);
		InputStream in = new FileInputStream(path);
    	InputSource source = new InputSource(in);
    	String id = EducationResource.toId(rawStr);
    	EducationResource media = null;
		Log.d(TAG, "Media id to match: " + id);
    	try {
    		EducationResourceParser parser = 
    			EducationResourceParser.newInstance();
    		parser.parse(source);
    		media = parser.findById(id, Audience.PATIENT);
		} catch (ParserConfigurationException e) {
			Log.d(TAG, "Parser config error: " + e.getMessage() );
		} catch (SAXException e) {
			Log.d(TAG, "Error parsing: " + e.getMessage() );
		} catch (IOException e) {
			Log.d(TAG, "IO error: " + e.getMessage() );
		}
    	return media;
    }
}

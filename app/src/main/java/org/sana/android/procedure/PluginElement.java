package org.sana.android.procedure;

import java.net.URISyntaxException;

import org.sana.R;
import org.sana.android.activity.ProcedureRunner;
import org.sana.android.content.core.ObservationWrapper;
import org.sana.android.db.BinaryDAO;
import org.sana.android.db.ModelWrapper;
import org.sana.android.db.SanaDB.BinarySQLFormat;
import org.sana.android.provider.Observations;
import org.sana.android.service.PluginService;
import org.w3c.dom.Node;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * An Element within a Procedure which launches a plug-in, i.e. third
 * party application for data capture as an observation.
 * <br/>
 * <b>Note:<b> The Activity information can usually be obtained by launching the
 * application and viewing the log cat output as in:
 * 
 * <code>I/ActivityManager: Starting activity: Intent { 
 * 		act=android.media.action.VIDEO_CAPTURE 
 * 		cmp=com.google.android.camera/com.android.camera.VideoCamera }</code>
 * <br/>
 * where the xml attributes for the element would be set as follows:
 * <ul type="none">
 * <li><code>action="android.media.action.VIDEO_CAPTURE"</code></li>
 * <li><code>mimeType=""</code></li>
 * </ul> 
 * The <code>act</code> correlates to the <code>name</code> attribute listed 
 *   in an intent filter for the Activity in it's application manifest.
 * The <code>mimeType</code> will appear as <code>typ=</code> in the String 
 * representation of the Intent if available.
 * <p/>
 * <b>Collects: </b>Determined by plug-in activity but will return either a 
 * resource identifier or string.
 * 
 * @author Sana Development Team
 *
 */
public class PluginElement extends ProcedureElement implements 
	OnClickListener
{
	private static final String TAG = PluginElement.class.getSimpleName();
	public static final String PARAMS_NAME = "keys";
	public static final String DELIMITER = ";";
			
	// the plugin action -> intent action string
	protected final String pluginAction;
	// the plugin package -> Activity package 
	protected final String mimeType;
	// launch button for for starting Activity with GET_CONTENT intent
    protected Button mCaptureButton;
	// launch button for for starting Activity with ACTION_VIEW intent
    protected Button mViewButton;
    protected ImageView mPluginIcon;
    protected String mLabel = null;
    protected Bundle mParams;

    /**
     * Constructs a new PluginElement
     * 
     * @param id The value of the "id" attribute
     * @param question The value of the "question" attribute
     * @param answer The value of the "answer" attribute
     * @param concept The value of the "concept" attribute
     * @param figure The value of the "figure" attribute
     * @param audio The value of the "audio" attribute
     * @param pluginAction The value of the "action" attribute. This is the 
     * 		intent-filter action string from the plugin activity. 
     * @param mimeType The content type which the plug-in will collect. Used to
     * 		construct a launch intent. 
     */
	protected PluginElement(String id, String question, String answer,
			String concept, String figure, String audioPrompt, String action,
			String mimeType) 
	{
		this(id, question, answer, concept, figure, audioPrompt, action, new Bundle(), mimeType);
	}
	protected PluginElement(String id, String question, String answer,
			String concept, String figure, String audioPrompt, String action,
			Bundle params,
			String mimeType){

		super(id, question, answer, concept, figure, audioPrompt);
		this.pluginAction = action;
		this.mimeType = mimeType;
		setControlParams(params);
	}
	/**
	 * Fetches the plug-in String for this element
	 * @return the action string
	 */
	public String getAction(){
		return pluginAction;
	}
	
	/**
	 * Fetches the plug-in String for this element
	 * @return the action string
	 */
	public String getMimeType(){
		return mimeType;
	}
	
	public String getControlString(){
		StringBuilder control = new StringBuilder();
		control.append(getAction());
		for(String key: mParams.keySet()){
			control.append(DELIMITER);
			control.append(key + "=" + mParams.getString(key));
		}
		return control.toString();
	}
	
	/**
	 * Gets the Intent which can be used to launch the plug-in activity through
	 * ProcedureRunner
	 * @throws URISyntaxException 
	 */
	protected Intent getRawPluginIntent() {
		Intent pi = new Intent();
		if(!TextUtils.isEmpty(pluginAction)){
			pi = new Intent(pluginAction);
		}
		if(!TextUtils.isEmpty(mimeType))
			pi.setType(mimeType);
		
		Log.d(TAG, "Raw plugin intent: " + pi.toUri(Intent.URI_INTENT_SCHEME));
		return pi;
	}
	
	/**
	 * Gets the Intent which can be used to launch the plug-in activity through
	 * ProcedureRunner
	 * @throws URISyntaxException 
	 */
	protected Intent getViewIntent() {
		Log.d(TAG, "view: " + answer);
		Uri ue = getProcedure().getInstanceUri();
		String encounter = ModelWrapper.getUuid(ue, getContext().getContentResolver());
		Intent intent = null;
		if(TextUtils.isEmpty(answer))
			return intent;
		try{
			intent = new Intent();
			//Uri obs = Uri.parse(answer);
			Uri obs = ObservationWrapper.getReferenceByEncounterAndId(
				getContext().getContentResolver(), encounter,
				id);
			intent = new Intent(getAction() +"_VIEW");
			if(obs != null && !obs.equals(Observations.CONTENT_URI)){
				intent.setDataAndType(obs,mimeType)
					.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			}
			Log.d(TAG, "....encounter" + encounter);
			Log.d(TAG, "....obs" + obs);
			Log.d(TAG, "intent: " + intent);
		} catch (Exception e){
			e.printStackTrace();
		}
		return intent;
	}
	
	/**
	 * Handles clicks on the button responsible for triggering the launch of the
	 * plugin activity
	 */
	@Override 
	public void onClick(View v) {
		 if (v == mCaptureButton) {
			 capture();
		 } else if(v == mViewButton){
			 view();
		 }
	}

	/**
	 * Launches an Intent to capture data captured using this element's plug-in.
	 */
	protected void capture(){
		 try {
			 Uri obs = Uri.withAppendedPath(getProcedure().getInstanceUri(),id);
			 
			 
			 Intent plugin = new Intent(getAction());
			 plugin.putExtras(getControlParams());
			 plugin.putExtra(Observations.Contract.ID, id);
			 plugin.putExtra(Observations.Contract.CONCEPT, concept);
			 Log.d(TAG, "obs: " + obs);
			 Intent i = new Intent(getContext(), ProcedureRunner.class);
			 i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
				.putExtra(ProcedureRunner.INTENT_KEY_STRING, 
						  ProcedureRunner.PLUGIN_INTENT_REQUEST_CODE)
				.putExtra(Observations.Contract.ID, id)
				.putExtra(Intent.EXTRA_INTENT, plugin)
				.setDataAndType(getProcedure().getInstanceUri(),mimeType);
			 ((Activity) getContext()).startActivity(i);
		 } catch (Exception e){
			 Log.e(TAG, "Error starting plugin: " + e.toString());
		 }
	}
	
	/**
	 * Launches an Intent to view the data captured as this element's answer.
	 */
	protected void view(){
		try {
			Intent intent = getViewIntent();
			if(intent != null && intent.getData() != null){
				getContext().startActivity(intent);
			} else {
				Toast.makeText(getContext(),
					 getContext().getString(R.string.msg_err_no_answer), 
					 Toast.LENGTH_SHORT).show();
			}
		 } catch (Exception e){
			 Log.e(TAG, "Empty answer of no media found: " + e.toString());
		 }
	}
	
	protected Bundle getControlParams(){
		Bundle params = new Bundle(mParams);
		return params;
	}
	
	protected String getControlParam(String name){
		return mParams.getString(name);
		
	}
	protected void setControlParams(Bundle params){
		mParams = new Bundle(params);
	}
	
	protected void updateControlParams(Bundle params){
		mParams.putAll(params);
	}
	
	protected void setControlParam(String name, String value){
		mParams.putString(name, value);
		
	}
	
    @Override
    protected void appendOptionalAttributes(StringBuilder sb){
        sb.append("\" action=\"" + getControlString());
        sb.append("\" mimeType=\"" + getMimeType());
    }
    
    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
     public static PluginElement fromXML(String id, String question, 
    		String answer, String concept, String figure, String audio, 
    		Node node) throws ProcedureParseException  
    {
        String controlStr = node.getAttributes().getNamedItem("action")
        				.getNodeValue();
        if(TextUtils.isEmpty(controlStr))
        	throw new ProcedureParseException("Invalid contol string: NULL");
        String[] control = controlStr.split(DELIMITER);
        String action = control[0];
        Bundle params = new Bundle();
        for(int i = 1; i < control.length; i++){
        	String[] param = control[i].split("=");
        	params.putString(param[0], param[1]);
        }
        String mimeType = node.getAttributes().getNamedItem("mimeType")
        				.getNodeValue();
    	return new PluginElement(id, question, answer, concept, figure, audio,
    			action,params,mimeType);
    }
     
	/** {@inheritDoc} */
	@Override
	protected View createView(Context c) {
		Log.d(TAG, "");
		// New Layout
    	LinearLayout container = new LinearLayout(c);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        		LayoutParams.WRAP_CONTENT));
        
        // Plugin Launcher
        View plug = getContentView(c);
        container.addView(plug, new LinearLayout.LayoutParams(
        		LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT,0.1f));
        
        //Add data viewer
        View review = viewDataView(c);
        container.addView(review, new LinearLayout.LayoutParams(
        		LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT,0.1f));
        container.setWeightSum(1.0f);
        return encapsulateQuestion(c, container);
	}
	
	protected View getContentView(Context c){
        //Add Capture button
        mCaptureButton = new Button(c);
        mCaptureButton.setText(c.getString(R.string.general_capture_data));
        mCaptureButton.setOnClickListener(this);
        return mCaptureButton;
	}
	
	protected View viewDataView(Context c){
        mViewButton = new Button(c);
        mViewButton.setText(c.getString(R.string.general_view_data));
        mViewButton.setOnClickListener(this);
        return mViewButton;
	}

	/** {@inheritDoc} */
	@Override
	public ElementType getType() {
		return ProcedureElement.ElementType.PLUGIN;
	}
	
	/** {@inheritDoc} */
	@Override
    public String getAnswer() {
		Log.i(TAG, "getAnswer()");
		Log.d(TAG, "... id "+id + ", answer:" + answer);
    	return answer;
    }
    
	/** Sets the answer String for this element */
	@Override
	public void setAnswer(String answer) {
		Log.i(TAG, "setAnswer()");
		Log.d(TAG, "... set answer:" + answer);
		this.answer = answer;
	}
}

package org.sana.android.procedure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import org.sana.android.db.SanaDB.SoundSQLFormat;
import org.w3c.dom.Node;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

/**
 * SoundElement is a ProcedureElement that asks a question and allows a user to 
 * record a response. This sound is saved to a temp file on the phone's SD card 
 * and then sent to a database on the phone. Once a recording is made, the user 
 * has the option of re-recording.
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use</b> This element is useful in several clinical scenarios 
 * such as recording extra notes or recording the patient cough, etc.</li>
 * <li><b>Collects</b></li>An audio recording to a file represented as a string
 * file name.</li>
 * </ul>
 * 
 * @author Sana Development Team
 */
public class SoundElement extends ProcedureElement implements OnClickListener {

    private Button startRecButt;
    private Button endRecButt;
    private TextView textViewSound;
    private ImageView imageViewSound;
    private MediaRecorder recorder;
    private String path;
    private File tempSoundFile;

    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.SOUND;
    }

    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
        LinearLayout soundContainer = new LinearLayout(c);
        soundContainer.setOrientation(LinearLayout.VERTICAL);
        path = Environment.getExternalStorageDirectory().getAbsolutePath() 
        	+ "/testRecording.3gp";
        
        if(question == null) {
            question = "Record audio:";
        }

        //Set display question
        textViewSound = new TextView(c);
        textViewSound.setText(question);
        textViewSound.setGravity(Gravity.CENTER);
        textViewSound.setTextAppearance(c, 
        		android.R.style.TextAppearance_Medium);
        
        //Set accompanying figure
        imageViewSound = new ImageView(c);
        if(!figure.equals("")){    
	        try{
	        	String imagePath = c.getPackageName() + ":" + figure;
	        	int resID = c.getResources().getIdentifier(imagePath, null, 
	        			null);
	        	imageViewSound.setImageResource(resID);
	        	imageViewSound.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
	        	imageViewSound.setLayoutParams(new Gallery.LayoutParams(
	        			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	        	imageViewSound.setPadding(10,10,10,10);
	        }
	        catch(Exception e){
	        	Log.e(TAG, "Couldn't find resource figure " + e.toString());
	        }
        }
        
        //Add to page
        soundContainer.addView(textViewSound, 
        		new LinearLayout.LayoutParams(-1, -1, 0.1f));
        soundContainer.addView(imageViewSound, 
        		new LinearLayout.LayoutParams(-1, -1, 0.1f));
        
        //Initialize audio control buttons
        //Start Record Button
        startRecButt = new Button(c);
        startRecButt.setText("Start Recording");
        startRecButt.setOnClickListener(this);
        
        //End Record Button (disabled initially)
        endRecButt = new Button(c);
        endRecButt.setText("Stop Recording");
        endRecButt.setEnabled(false);
        endRecButt.setOnClickListener(this);
        
        //Add to page
        soundContainer.addView(startRecButt, 
        		new LinearLayout.LayoutParams(-1, -1, 0.1f));
        soundContainer.addView(endRecButt, 
        		new LinearLayout.LayoutParams(-1, -1, 0.1f));
        
        return soundContainer;
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
    	//Start Record button clicked
        if (v == startRecButt) {
        	
        	textViewSound.setText(question);
        	
            //toggle record buttons
            startRecButt.setEnabled(false);
            endRecButt.setEnabled(true);
            
            String state = android.os.Environment.getExternalStorageState();
            if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
            	Log.e(TAG, "SD Card is not mounted.  It is " + state + "build/intermediates/exploded-aar/com.android.support/support-v4/21.0.3/res");
            }

            // make sure the directory we plan to store the recording in exists
            File directory = new File(path).getParentFile();
            if (!directory.exists() && !directory.mkdirs()) {
            	Log.e(TAG, "Path to file could not be created.");
            }
	            
            //create new recorder
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(path);
            
            //start recording
            try {
				recorder.prepare();
				recorder.start();
				endRecButt.setText("Stop Recording");
			} 
            catch (Exception e) {
				endRecButt.setText("Recording Error");
				Log.e(TAG, "Couldn't setup audio recorder: " + e);
			}
        } 
        //End Record button clicked
        else if (v == endRecButt) {
        	
        	startRecButt.setEnabled(true);
        	
        	// if we are still in the recording state
        	// then allow the user to stop recording
        	if (!(endRecButt.getText().equals("Play Back Recording"))) {
        		
        		// stop recording
        		textViewSound.setText("Recording Complete!");
                recorder.stop();
                recorder.release();
                endRecButt.setText("Play Back Recording");
                
                
                Thread t = new Thread() {
					public void run() {
						  // now write the file to the database
		                ContentValues values = new ContentValues();
		                String procedureId = getProcedure().getInstanceUri()
		                						.getPathSegments().get(1);
		                values.put(SoundSQLFormat.ENCOUNTER_ID, 
		                		procedureId);
		                values.put(SoundSQLFormat.ELEMENT_ID, getId());
		                Uri recording = 
		                	getContext().getContentResolver().insert(
		                			SoundSQLFormat.CONTENT_URI, values);
		       
		                // Make this the answer we return 
		                setAnswer(recording.getPathSegments().get(1));
		                
						try {
							byte[] buffer = new byte[1024];
							OutputStream os =  getContext().getContentResolver()
												.openOutputStream(recording);
							InputStream is = new FileInputStream(path);
		                	os = getContext().getContentResolver()
		                						.openOutputStream(recording);
		                    int bytesRemaining = is.available();
		                    while(bytesRemaining > 0) {
		                    	int read = is.read(buffer);
		                    	os.write(buffer, 0, read);
		                    	bytesRemaining -= read;
		                    }
		
		                    is.close();
							os.flush();
							os.close();
							Log.i(TAG, "Successfully saved audio");
							
						} catch (FileNotFoundException e) {
							Log.e(TAG, "While storing the audio, got an " +
									"exception: " + e.toString());
						} catch (IOException e) {
							Log.e(TAG, "While storing the audio, got an "
									+"exception: " + e.toString());
							Log.i(TAG, e.getStackTrace().toString());
						} 
					}
					    
				};
				t.start();
                
                
//                
//                InputStream is = null;
//                OutputStream os = null;
//                try {
//                	byte[] buffer = new byte[1024];
//                	
//                	//new stuff
//                	tempSoundFile = new File(Environment.getExternalStorageDirectory(), "testRecording.3gp");
//    				Uri tempSoundUri = Uri.fromFile(tempSoundFile);
//                	is = new FileInputStream(path);
//                	os = getContext().getContentResolver().openOutputStream(recording);
//
//                    int bytesRemaining = is.available();
//                    while(bytesRemaining > 0) {
//                    	int read = is.read(buffer);
//                    	os.write(buffer, 0, read);
//                    	bytesRemaining -= read;
//                    }
//
//                    is.close();
//                    os.close();
//                } catch (FileNotFoundException e) {
//					Log.i(TAG, "could not find audio temp file");
//				} catch (IOException e) {
//					Log.i(TAG, "IO exception in writing audio to database " + e.toString());
//					Log.i(TAG, e.getStackTrace().toString());
//				} finally {
//					try {
//						if (os != null)
//							os.close();
//					} catch (IOException e) {
//
//					}
//					try {
//						if (is != null)
//							is.close();
//					} catch (IOException e) {
//
//					}
//
//				}

            // if we are in a completed state
            // then allow audio playback
        	} else {
        		MediaPlayer mp = new MediaPlayer();
        	    try {
					mp.setDataSource(path);
					mp.prepare();
	        	    mp.start();
				} catch (Exception e) {
					return;
				}
				startRecButt.setText("Rerecord");
        	}  
        }    
    }

    /** {@inheritDoc} */
    @Override
    public void setAnswer(String answer) {
    	this.answer = answer;
    }
    

    /** {@inheritDoc} */
    @Override
    public String getAnswer() {
        return answer;
    }
    
    /** {@inheritDoc} */
    @Override
    public void buildXML(StringBuilder sb) {
    	sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" answer=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }
    
    /** Default constructor */
    private SoundElement(String id, String question, String answer, 
    		String concept, String figure, String audio) 
    {
        super(id,question,answer, concept, figure, audio);
    }
    

    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
   public static SoundElement fromXML(String id, String question, 
    		String answer, String concept, String figure, String audio, 
    		Node node)  throws ProcedureParseException 
    {
        return new SoundElement(id, question, answer, concept, figure, audio);
    }

}

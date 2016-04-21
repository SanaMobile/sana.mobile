package org.sana.android.procedure;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import org.sana.R;
import org.sana.android.content.ModelContentProvider;
import org.sana.android.content.Uris;
import org.sana.android.provider.Observations;
import org.sana.android.util.SanaUtil;
import org.sana.core.Encounter;
import org.w3c.dom.Node;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
    public static final String TAG = SoundElement.class.getSimpleName();
    public static final int STATE_READY = 0;
    public static final int STATE_RECORDING = 1;
    public static final int STATE_COMPLETE = 2;
    public static final int STATE_PLAYBACK = 4;
    public static final int STATE_ERROR = 8;
    public static final int NO_LIMIT = -1;

    private Button startRecButt;
    private Button endRecButt;
    private TextView textViewSound;
    private TextView mMessage;
    private ImageView imageViewSound;
    private MediaRecorder recorder;
    private MediaPlayer mPlayer = null;
    private String path;
    private File tempSoundFile;

    private Uri mData = Uri.EMPTY;
    private int state = STATE_READY;
    private int limit = -1;
    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.SOUND;
    }

    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
        LinearLayout soundContainer = null;

        if(question == null) {
            question = c.getString(R.string.label_record_audio);
        }

        if(c instanceof Activity) {
            Activity activity = (Activity) c;
            soundContainer = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.element_sound, null);
            textViewSound = (TextView) soundContainer.findViewById(R.id.question);
            textViewSound.setText(question);
            imageViewSound = (ImageView) soundContainer.findViewById(R.id.image);
            setImage(c, imageViewSound, figure);
            startRecButt = (Button) soundContainer.findViewById(R.id.btn_action_start);
            endRecButt = (Button) soundContainer.findViewById(R.id.btn_action_stop);
            setButtons(startRecButt,endRecButt);
            mMessage = (TextView) soundContainer.findViewById(R.id.error);
            mMessage.setText("");

        } else {
            soundContainer = new LinearLayout(c);
            soundContainer.setOrientation(LinearLayout.VERTICAL);
            createView(c, soundContainer);
        }
        /*
        try {
            path = openTempFile().getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        */
        return soundContainer;
    }

    protected View createView(Context c, ViewGroup soundContainer){
        //Set display question
        textViewSound = new TextView(c);
        textViewSound.setText(question);
        textViewSound.setGravity(Gravity.CENTER);
        textViewSound.setTextAppearance(c,
                android.R.style.TextAppearance_Medium);

        //Set accompanying figure
        imageViewSound = new ImageView(c);
        setImage(c, imageViewSound, figure);

        //Add to page
        soundContainer.addView(textViewSound,
                new LinearLayout.LayoutParams(-1, -1, 0.1f));
        soundContainer.addView(imageViewSound,
                new LinearLayout.LayoutParams(-1, -1, 0.1f));

        //Initialize audio control buttons
        //Start Record Button
        startRecButt = new Button(c);

        //End Record Button (disabled initially)
        endRecButt = new Button(c);

        setButtons(startRecButt,endRecButt);

        //Add to page
        soundContainer.addView(startRecButt,
                new LinearLayout.LayoutParams(-1, -1, 0.1f));
        soundContainer.addView(endRecButt,
                new LinearLayout.LayoutParams(-1, -1, 0.1f));

        return soundContainer;
    }

    protected void setImage(Context c, ImageView imageViewSound, String figure){
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
                imageViewSound.setVisibility(View.GONE);
                Log.e(TAG, "Couldn't find resource figure " + e.toString());
            }
        } else {
            imageViewSound.setVisibility(View.GONE);
        }
    }

    protected void setButtons(Button startRecButt, Button endRecButt){

        startRecButt.setText(R.string.label_start_recording);
        startRecButt.setOnClickListener(this);
        if(!TextUtils.isEmpty(answer)){

            endRecButt.setText(R.string.label_play_recording);
            endRecButt.setEnabled(false);
        } else {
            endRecButt.setText(R.string.label_stop_recording);
            endRecButt.setEnabled(false);
        }
        endRecButt.setOnClickListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
    	//Start Record button clicked
        if (v == startRecButt) {
            try {
                if(state == STATE_PLAYBACK){
                    if(mPlayer.isPlaying())
                    mPlayer.stop();
                    mPlayer.release();
                }
                state = STATE_RECORDING;
                mMessage.setText("");
                recorder = getMediaRecorder(getUri());
				recorder.start();
                // If everything goes well we toggle buttons
                startRecButt.setEnabled(false);
                endRecButt.setEnabled(true);
                textViewSound.setText(question);
				endRecButt.setText(R.string.label_stop_recording);
			} 
            catch (Exception e) {
                mMessage.setText(R.string.label_record_error);
				Log.e(TAG, "Couldn't setup audio recorder: " + e);
                e.printStackTrace();
			}
        } 
        //End Record button clicked
        else if (v == endRecButt) {

        	startRecButt.setEnabled(true);
        	// if we are still in the recording state
        	// then allow the user to stop recording
        	if (state == STATE_RECORDING) {
        		state = STATE_COMPLETE;
        		// stop recording
        		textViewSound.setText(R.string.label_recording_complete);
                recorder.stop();
                recorder.release();
                endRecButt.setText(R.string.label_play_recording);

                final Uri uri = save(getContext(),getEncounter());
                setAnswer(uri.getLastPathSegment());
                saveComplexValue(uri);
            // if we are in a completed state
            // then allow audio playback
        	} else {
                try {
                    mPlayer = getMediaPlayer(getUri());
                    mPlayer.start();
                    state = STATE_PLAYBACK;
				} catch (Exception e) {
					return;
				}
				startRecButt.setText(R.string.label_record_again);
        	}  
        }    
    }

    public Uri getUri(){
        if(!TextUtils.isEmpty(answer)){
            return ContentUris.withAppendedId(Observations.CONTENT_URI, Long.valueOf(answer));
        } else {
            return super.getUri();
        }
    }

    public MediaRecorder getMediaRecorder(Uri dest) throws IOException {
        //create new recorder
        MediaRecorder recorder = new MediaRecorder();
        recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Log.e(TAG, "Recording error: " + what);
                mMessage.setText(R.string.label_record_error);
            }
        });
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        if (Build.VERSION.SDK_INT >= 10) {
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(96000);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        } else {
            // older version of Android, use crappy sounding voice codec
            recorder.setAudioSamplingRate(8000);
            recorder.setAudioEncodingBitRate(12200);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        }
        File file = openTempFile();
        recorder.setOutputFile(file.getPath());
        // set the recording limit if specified
        if(limit > 0){
            recorder.setMaxDuration(limit*1000);
        }
        //start recording
        recorder.prepare();
        return recorder;
    }

    public MediaPlayer getMediaPlayer(Uri source) throws IOException {
        File file = openTempFile();
        MediaPlayer mp = new MediaPlayer();
        mp.setDataSource(file.getPath());
        mp.prepare();
        return mp;
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
        sb.append("\" limit=\"" + limit);
        sb.append("\"/>\n");
    }

    public void saveComplexValue(final Uri uri){
        Thread t = new Thread() {
            public void run() {
                try {
                    byte[] buffer = new byte[1024];
                    InputStream is = new FileInputStream(path);
                    OutputStream os = getContext().getContentResolver()
                            .openOutputStream(uri);
                    int bytesRemaining = is.available();
                    while (bytesRemaining > 0) {
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
                            + "exception: " + e.toString());
                    Log.i(TAG, e.getStackTrace().toString());
                }
            }
        };
    }

    public File openTempFile() throws FileNotFoundException {
        ContentProviderClient client = getContext().getContentResolver()
                .acquireContentProviderClient(Observations.CONTENT_URI);
        ModelContentProvider provider = (ModelContentProvider) client.getLocalContentProvider();
        File file = provider.getCacheFile(Observations.CONTENT_URI);
        client.release();
        return file;
    }

    @Override
    public Uri save(Context context) {
        Uri uri = super.save(context);
        setAnswer(uri.getLastPathSegment());
        return uri;
    }

    /** Default constructor */
    private SoundElement(String id, String question, String answer, 
    		String concept, String figure, String audio) 
    {
        this(id, question, answer, concept, figure, audio, NO_LIMIT);
    }

    private SoundElement(String id, String question, String answer,
                         String concept, String figure, String audio, int limit)
    {
        super(id, question, answer, concept, figure, audio);
        if(!TextUtils.isEmpty((answer))){
            mData = Uris.withAppendedUuid(Observations.CONTENT_URI, answer);
        }
        this.limit = limit;
    }

    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
   public static SoundElement fromXML(String id, String question, 
    		String answer, String concept, String figure, String audio, 
    		Node node)  throws ProcedureParseException 
    {
        String limitStr = SanaUtil.getNodeAttributeOrDefault(node, "limit", "");
        int limit = NO_LIMIT;
        if(!TextUtils.isEmpty(limitStr)){
            try {
                limit = Integer.valueOf(limitStr);
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return new SoundElement(id, question, answer, concept, figure, audio, limit);
    }

}

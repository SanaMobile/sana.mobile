package org.sana.android.media;

import android.content.Context;			
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AudioPlayer implements OnClickListener, OnCompletionListener {
	private static final String TAG = AudioPlayer.class.toString();
	
	private static final String PLAY = "?";
	private static final String PAUSE = "||";
	
	private int resourceId;
	private Button playButton = null;
	private Context mContext = null;
	private MediaPlayer mp = null;
	
	public AudioPlayer(int resourceId) {
		this.resourceId = resourceId;
	}
	
	public void onClick(View v) {
		Log.i(TAG, "Audio prompt play button pressed");
	    try {
	    	if (mp == null) {
	    		play();
	    	} else {
	    		stop();
	    	}
		} catch (Exception e) {
			Log.e(TAG, "Could not prepare media player: " + e.toString());
		}
	}
	
	public void play() {
		if (mp == null) {
			try {
				mp = MediaPlayer.create(mContext, resourceId);
	    		mp.setOnCompletionListener(this);
	    		mp.start();
	    		playButton.setText(PAUSE);
			} catch (Resources.NotFoundException exception) {
				Log.e(TAG, "Resource " + resourceId + " did not exist.");
				exception.printStackTrace();
			}
		}
	}
	
	public void stop() {
		if (mp != null) {
			mp.stop();
			mp.release();
			mp = null;
			playButton.setText(PLAY);
		}
	}
	
	public View createView(Context c) {
		mContext = c; // TODO leak?
		playButton = new Button(c);
		playButton.setText(PLAY);
        playButton.setOnClickListener(this);
        return playButton;
	}

	public void onCompletion(MediaPlayer foo) {
		Log.i(TAG, "Playing complete.");
		stop();
	}
}

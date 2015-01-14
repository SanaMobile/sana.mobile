package org.sana.android.task;

import java.io.File;

import android.content.Context;
import android.content.Intent;

/**
 * A request to process and Image.  
 * 
 * @author Sana Development Team
 *
 */
public class ImageProcessingTaskRequest {
	
	/** A valid context to proces in. */
	public Context c;
	
	/** Holds reference to image data */
	public Intent intent;
	
	/** A temporary storage file */
	public File tempImageFile;
	
	/** The encounter for which the image was recorded */
	public String savedProcedureId;
	
	/** The observation, or procedure element, for which the image was 
	 * collected. 
	 */
	public String elementId;
}

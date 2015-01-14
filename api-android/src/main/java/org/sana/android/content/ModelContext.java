package org.sana.android.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import android.net.Uri;
import android.os.Environment;


/**
 * 
 * @author Sana Development
 *
 */
public class ModelContext {

	public static final String FILES_FORMAT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data%s/files%s";
	
	public static File getExternalFilesDir(Uri uri){
		if(Uris.isEmpty(uri)){
			throw new IllegalArgumentException("Invalid uri: EMPTY");
		}
		String path = "/";
		switch(Uris.getTypeDescriptor(uri)){
		case Uris.ITEM_ID:
		case Uris.ITEM_UUID:
			char sep = "/".charAt(0);
			path = uri.getPath();
			int start = path.lastIndexOf(sep);
			path = path.substring(0, start);
			path = String.format(FILES_FORMAT, "org.sana", path);
			break;
		case Uris.ITEMS:
			path = String.format(FILES_FORMAT, "org.sana", uri.getPath());
		}
		File result =  new File(path);
		// be certain parents exist
		File nomedia = new File(result, ".nomedia");
		touch(nomedia);
		return result;
	}
	
	static final boolean touch(File file){
		try {
			file.mkdirs();
			if(!file.exists()){
				file.createNewFile();
			} else {
				
			}
			return true;
		} catch (IOException e) {
				e.printStackTrace();
				return false;
		}
		
	}
	
}

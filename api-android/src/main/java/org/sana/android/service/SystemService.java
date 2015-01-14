/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sana nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.android.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

/**
 * @author Sana Development
 *
 */
public class SystemService extends IntentService {
	public static final String TAG = SystemService.class.getSimpleName();

	static final int DB_TASK = 0;
	
	public static final int EXPORT_DB = 1 << 3 | DB_TASK;
	public static final int IMPORT_DB = 2 << 3 | DB_TASK; 
	
	public SystemService(String name){
		super(name);
	}
	
	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Uri data = Uri.EMPTY;
		
		switch(intent.getFlags()){
		case(EXPORT_DB):
			// Should be the file Uri we want to write to
			Uri out = intent.getData();

			break;
		case(IMPORT_DB):
			// Should be the file Uri we want to read from
			Uri in = intent.getData();
			
			break;
		default:
			
		}
	}
	
	protected boolean exportDb() throws IOException{
		File dir = new File(Environment.getExternalStorageDirectory(),"export/" + getPackageName() +"/databases");
		boolean result = databaseList().length > 0;
		for(String db: databaseList()){
			FileChannel src = openFileInput(db).getChannel();
			FileChannel dst = new FileOutputStream(new File(dir,db)).getChannel();
			try{
				result = result && (src.transferTo(0, src.size(), dst) > 0);
			} finally {
				dst.close();
			}
		}
		return result;
	}
	
	protected boolean importDb(String dir) throws IOException{
		boolean result = false;
		FilenameFilter filter = new FilenameFilter(){

			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith("*.db");
			}};
		File fdir = new File(dir);
		for(File f: fdir.listFiles(filter) ){
			FileChannel src = new FileInputStream(f).getChannel();
			FileChannel dst = openFileOutput(f.getName(),0).getChannel();
			try{
				result = result && (src.transferTo(0, src.size(), dst) > 0);
			} finally {
				dst.close();
			}
		}
		return result;
	}
}

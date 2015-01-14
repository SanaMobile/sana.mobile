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
package org.sana.android.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * Wrappers around Android Log which provides explicit message formatting and
 * includes loggers which will only print when app is run in debug mode.
 * 
 * @author Sana Development
 *
 */
public final class Logf {

	private Logf(){}
	
	/** Formats the tag, method, and message with semi-colon separated key 
	 * value pairs. 
	 */
	public static final String FORMAT_LONG = "tag=%s;method=%s;message='%s'";
	
	/** Formats the tag and message as semi-colon separated key-value pairs. */
	static final String FORMAT_SHORT = "tag=%s;%message=%s";
	
	/** */
	public static final String FORMAT_LOG = "method=%s;message='%s'";
	
	/** 
	 * Checks whether the application is running in debug mode.
	 * 
	 * @return <code>true</code> if the application is run in debug mode.
	 */
	public static final boolean isDebuggable(){
		return org.sana.api.BuildConfig.DEBUG;
	}

	/** 
	 * Checks whether the application is running in debug mode.
	 * 
	 * @return <code>true</code> if the application is run in debug mode.
	 */
	public static final boolean isDebuggable(Context c){
		String packageName = c.getPackageName();
		int flags = c.getPackageManager().getLaunchIntentForPackage(packageName).getFlags();
		return ((flags & ApplicationInfo.FLAG_DEBUGGABLE) > 0)?true: false;
	}
	
	/** 
	 * Formats the method and message using the {@link #FORMAT_LOG} format.
	 * @param method The method string
	 * @param message The message String.
	 * @return The formatted message.
	 */
	public static final String logFormat(String method, String message){
		return String.format(FORMAT_LOG, method,message);
	}
	
	/**
	 * Formats the log string using the specified format. The format must 
	 * include three replacement strings.
	 * 
	 * @param format The format String to use.
	 * @param tag The tag string.
	 * @param method The method name.
	 * @param message The log message.
	 * @return A formatted log string.
	 */
	public static final String format(String format, String tag, String method, String message){
		return String.format(format, tag, method, message);
	}

	/**
	 * Formats the log string using {@link #FORMAT_LONG}
	 * 
	 * @param tag The tag string.
	 * @param method The method name.
	 * @param message The log message.
	 * @return A formatted log string.
	 */
	public static final String format(String tag, String method, String message){
		return format(FORMAT_LONG, tag, method, message);
	}
	

	/**
	 * Formats the log string using the specified format. The format must 
	 * include three replacement strings.
	 * 
	 * @param format The format String to use.
	 * @param klazz The class to use for the tag.
	 * @param method The method name.
	 * @param message The log message.
	 * @return A formatted log string.
	 */
	public static final String format(String format, Class<?> klazz, String method, String message){
		return format(format, klazz.getSimpleName(), method, message);
	}

	/**
	 * Formats the log string using {@link #FORMAT_LONG}
	 * 
	 * @param klazz The class to use for the tag.
	 * @param method The method name.
	 * @param message The log message.
	 * @return A formatted log string.
	 */
	public static final String format(Class<?> klazz, String method, String message){
		return format(FORMAT_LONG, klazz.getSimpleName(), method, message);
	}
	
	
	/**
	 * Formatted logger which adds an info level Log entry using a classes 
	 * simple name as the tag and only when the app is running in debug mode.
	 * 
	 * @param tag The class to use as a tag.
	 * @param message The log message
	 */
	public static final void I(String tag, String message){
		if(org.sana.api.BuildConfig.DEBUG)
			android.util.Log.i(tag, message);
	}
	
	/**
	 * Formatted logger which adds an info level Log entry.
	 * 
	 * @param tag The log tag.
	 * @param method The name of the method being logged.
	 * @param message The log message
	 */
	public static final void I(String tag, String method, String message){
		I(tag, logFormat(method , message));
	}
	
	public static final void W(String tag, String message){
		if(org.sana.api.BuildConfig.DEBUG)
			android.util.Log.w(tag, message);
	}
	

	/**
	 * Formatted logger which adds a warn level Log entry using a classes 
	 * simple name as the tag.
	 * 
	 * @param klazz The class to use as a tag.
	 * @param method The name of the method being debugged
	 * @param message The log message
	 */
	public static final void W(String tag, String method, String message){
		W(tag, logFormat(method , message));
	}
	
	/**
	 * Formatted logger which only adds a debug level Log entry when the app is 
	 * running has debug mode.
	 *  
	 * @param tag A descriptive tag, typically the class simple name.
	 * @param message An informative message.
	 */
	public static final void D(String tag, String message){
		if(org.sana.api.BuildConfig.DEBUG)
			android.util.Log.d(tag, message);
	}
	
	/**
	 * Formatted logger which only adds a debug level Log entry when app is 
	 * running in debug mode and explicitly includes the method name. 
	 * 
	 * @param tag A descriptive tag, typically the class simple name.
	 * @param method The method where the logging statement occurred
	 * @param message An informative message.
	 */
	public static final void D(String tag, String method, String message){
		D(tag, logFormat(method , message));
	}
	
	/**
	 * Formatted logger which only adds a debug level Log entry when app is 
	 * running in Debug mode using a classes simple name as the tag.
	 * 
	 * @param klazz The class to use as a tag.
	 * @param method The name of the method being debugged
	 * @param message The log message
	 */
	public static final void D(Class<?> klazz, String method, String message){
		D(klazz.getSimpleName(), logFormat(method , message));
	}
	
}

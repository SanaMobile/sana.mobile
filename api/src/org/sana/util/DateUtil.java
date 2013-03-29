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
package org.sana.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.sana.api.IModel;

/**
 * Utility methods for dealing with Date objects.
 * 
 * @author Sana Development
 *
 */
public class DateUtil {
	public static final String TAG = DateUtil.class.getSimpleName();
	
	private static final DateFormat FORMAT = new SimpleDateFormat(IModel.DATE_FORMAT, 
			Locale.US);
	
	/**
	 * Parses a Date string according to {@link org.sana.api.IModel#DATE_FORMAT}
	 * 
	 * @param date The date String to parse
	 * @return A new Date object.
	 * @throws ParseException If the date String can not be parsed.
	 */
	public static Date parseDate(String date) throws ParseException{
		return FORMAT.parse(date);
	}
	
	/**
	 * Parses a Date from a long value.
	 * 
	 * @param date The date value to parse
	 * @return A new Date object.
	 */
	public static Date parseDate(long date){
		return new Date(date);
	}
	
	/**
	 * Formats a Date according to {@link org.sana.api.IModel#DATE_FORMAT}
	 * 
	 * @param date The Date to format
	 * @return A date/time string.
	 */
	public static String format(Date date){
		return FORMAT.format(date);
	}
	
	/**
	 * Converts a Date to a long value as the number of milliseconds since 
	 * January 1, 1970, 00:00:00 GMT.
	 * 
	 * @param date The Date to format
	 * @return A date/time string.
	 */
	public static long formatAsLong(Date date){
		return date.getTime();
	}
	
}

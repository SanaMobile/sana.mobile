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
package org.sana.android.app;

/**
 * Strings used in the Activity component state persistence such as for passing 
 * the application state between components.
 * 
 * @author Sana Development
 *
 */
public final class State {

	private State(){}
	
	public static interface Keys{
		/** Key for use if the state is to be saved under a single key */
		public static final String APP_STATE = "applicationState";
		
		/** The app instance unauthorized key. */
		public static final String INSTANCE_KEY = "instanceKey";
		
		/** The app instance authorization key */
		public static final String SESSION_KEY = "sessionKey";

		/** The current subject. */
		public static final String SUBJECT = "subject";
		
		/** The current encounter */
		public static final String ENCOUNTER = "encounter";
		
		/** The current procedure */
		public static final String PROCEDURE = "procedure";
		
		/** The current observation */
		public static final String OBSERVATION = "observation";
		
		/** The current observer */
		public static final String OBSERVER = "observer";
	}
}

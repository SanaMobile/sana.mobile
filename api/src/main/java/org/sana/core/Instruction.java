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
package org.sana.core;

import org.sana.api.IInstruction;


/**
 * A single step within a procedure as a representation of a Concept.
 * 
 * @author Sana Development
 *
 */
public class Instruction extends Model implements IInstruction{
	// wrapper for mime type
	public Concept concept;
	// mmap to education resource can be complex
	public String help;
	// map to question
	public String hint;
	// message on fail
	public String alert;
	boolean required;
	/* (non-Javadoc)
	 * @see org.sana.core.IInstruction#getConcept()
	 */
	@Override
	public Concept getConcept() {
		return concept;
	}
	/**
	 * Sets the concept for an instance of this class. 
	 *
	 * @param concept the concept to set
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IInstruction#getHelp()
	 */
	@Override
	public String getHelp() {
		return help;
	}
	/**
	 * Sets the help for an instance of this class. 
	 *
	 * @param help the help to set
	 */
	public void setHelp(String help) {
		this.help = help;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IInstruction#getHint()
	 */
	@Override
	public String getHint() {
		return hint;
	}
	/**
	 * Sets the hint for an instance of this class. 
	 *
	 * @param hint the hint to set
	 */
	public void setHint(String hint) {
		this.hint = hint;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IInstruction#getAlert()
	 */
	@Override
	public String getAlert() {
		return alert;
	}
	/**
	 * Sets the alert for an instance of this class. 
	 *
	 * @param alert the alert to set
	 */
	public void setAlert(String alert) {
		this.alert = alert;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IInstruction#isRequired()
	 */
	@Override
	public boolean isRequired() {
		return required;
	}
	/**
	 * Sets the required for an instance of this class. 
	 *
	 * @param required the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

}

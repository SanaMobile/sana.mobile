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
package org.sana.android.content;

/**
 * @author Sana Development
 *
 */
public enum Token {
	
    ITEMS(0),
    ITEM_ID(1),
    ITEM_UUID(3),
    ITEM_FILE(5),
    
    LEXICON(0,1),
    OBSERVABLE(1,1),
    MESSAGING(2,1),
    
    CONCEPTS(0,2),
    INSTRUCTION(1,2),
    PROCEDURE(2,2),
    
    
    ENCOUNTERS(0,2),
    OBSERVATIONS(1,2),
    OBSERVERS(2,2),
    SUBJECTS(3,2),
    
    
    EVENTS(0,2),
    NOTIFICATIONS(1,2);
    
    private final int code; 
    private final int offset;
    Token(int code){
    	this(code,CONTENT);
    }
    Token(int code, int offset){
    	this.code = code;
    	this.offset = offset;
    }
    
    public int code(){
    	return code << (OFFSET*offset);
    }
    
    public int offset(){
    	return offset;
    }
    
    public int addSub(Token other){
    	return other.code << OFFSET & code();
    }
    

	public static final int OFFSET = 4;
	
	public static final int OBJECT = 2;
	public static final int AUTHORITY = 1;
	public static final int CONTENT = 0;
}

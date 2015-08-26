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
package org.sana.api.task;

/**
 * @author Sana Development
 *
 */
public enum Status {
    UNKNOWN(-1),
	ASSIGNED(1),
	REVIEWED(5),
	REJECTED(3),
	COMPLETED(2),
    IN_PROGRESS(4);
    public final int code;
    Status(int code){
        this.code = code;
    }
        
    public static Status fromCode(int code){
        for(Status status: Status.values()){
            if(status.code == code)
                return status;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns a new Status based on a case insensitive comparison to the
     * allowed Status Strings.
     *
     * @param statusStr
     * @return
     */
    public static Status fromString(String statusStr){
        Status status = UNKNOWN;
        for(Status stat:Status.values()){
            if(statusStr.compareToIgnoreCase(stat.toString()) == 0){
                status = stat;
                break;
            }
        }
        return status;
    }
}

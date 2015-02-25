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
package org.sana.net;

import org.sana.util.Arrays;

/**
 * @author Sana Development
 *
 */
public class Response<T> {
    /**
     * The <code>code</code> values returned from MDS. This is
     * essentially a subset of the standard HTTP 1.1 response codes
     * defined in <a href="https://tools.ietf.org/html/rfc2616#section-10">RFC 2616</a>
     *
     * @author Sana
     */
    public enum Code{
        UNKNOWN(-1),
        CONTINUE(100),
        OK(200),
        CREATED(201),
        ACCEPTED(202),
        UPDATED(205),
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        NOT_FOUND(404),
        METHOD_NOT_ALLOWED(405),
        INTERNAL_ERROR(500),
        NOT_IMPLEMENTED(501),
        UNAVAILABLE(503);

        public final int code;

        Code(int code){
            this.code = code;
        }

        public static Code get(int val){
            for(Code c:Code.values()){
                if(c.code == val)
                    return c;
            }
            throw new IllegalArgumentException("Unknown code int value: " + val);
        }

        /**
         * Returns the version 1.x compatible {@see status String which provided
         * only a <strong>SUCCESS</strong> or <strong>FAILURE</strong>
         * message.
         *
         * @return
         */
        public String statusString(){
            if(code < 300)
                return Status.SUCCESS.toString();
            else
                return Status.FAILURE.toString();
        }

    }

    /**
     *  The success or failure status.
     */
    public enum Status{
        SUCCESS,
        FAILURE,
        UNKNOWN;

        /**
         * Returns the {@link org.sana.net.Response#Status Status} from
         * a <code>String</code> value. This method is not case-sensitive.
         *
         * @param val The String value of the status
         * @return The <code>Status</code> object
         * @throws IllegalArgumentException If no matching <code>Status</code>
         *   is found.
         */
        public Status fromString(String val){
            for(Status status:Status.values()){
                if(val.compareToIgnoreCase(status.toString()) == 0) return status;
            }
            throw new IllegalArgumentException("Unrecognized status string. " + val);
        }

    }

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String UNKNOWN = "UNKNOWN";

    public static final String CODE = "_code";
    public static final String STATUS = "_status";
    public static final String MESSAGE = "_message";
    public static final String ERRORS = "_errors";
    public static final String RESPONSE = "org.sana.net.RESPONSE";

    public String status;
    public int code;
    public T message;
    public String[] errors;

    /**
     * A new MDSResult with status of "UNKNOWN", code value of
     * {@link Code#UNKNOWN}, and null message.
     */
    public Response() {
        this(UNKNOWN, -1, null);
    }

    public Response(String status, int code, T message, String[] errors) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.errors = (errors != null)?
            Arrays.copyOf(errors, errors.length):
            new String[0];
    }

    public Response(String status, int code, T message) {
        this(status,code,message, null);
    }

    /**
     * Whether the result is successful.
     * @return true if <code>code >= 200</code> < <code>code < 400 </code>
     */
    public boolean succeeded() {
        return (200 <= code) && (code < 400)  ;
    }

    /**
     * Whether the result is a failure.
     * @return true if <code>code < 200</code> or <code>code >= 400 </code>
     */
    public boolean failed() {
        return (code < 200) || (code >= 400);
    }

    /**
     * The result message body.
     * @return
     */
    public T getMessage() {
        return message;
    }

    public void setMessage(T message){
        this.message = message;
    }

    /**
     * The status code.
     * @return
     */
    public int getCode() {
        return code;
    }

    public void setCode(int code){
        this.code = code;
    }

    public String getStatus(){
        return this.status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public static <K> Response<K> empty(){
        Response<K> e = new Response<K>();
        return e;
    }

    public static Response NOSERVICE = new Response();
}

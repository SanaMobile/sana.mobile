package org.sana.android.procedure;

/**
 * An Exception to throw when parsing a Procedure declared in text form.
 * 
 * @author Sana Development Team
 *
 */
public class ProcedureParseException extends Exception {
    private static final long serialVersionUID = 1L;
    
    /**
     * A new Exception with a message.
     * 
     * @param text the message.
     */
    public ProcedureParseException(String text) {
        super(text);
    }
}
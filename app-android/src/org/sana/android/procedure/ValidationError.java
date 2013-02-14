package org.sana.android.procedure;

/**
 * An Exception to throw when validating fails. 
 * @author Sana Development Team
 *
 */
public class ValidationError extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * A new Exception with a message.
	 * @param s the message
	 */
	public ValidationError(String s) {
		super(s);
	}
}

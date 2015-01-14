package org.sana.android.task;

/**
 * Callback methods to execute on completion of validation checks.
 * 
 * @author Sana Development Team
 *
 */
public interface ValidationListener {
	
	/**
	 * Validation result callback.
	 * 
	 * @param validationResult the validation result.
	 */
	void onValidationComplete(int validationResult);
}

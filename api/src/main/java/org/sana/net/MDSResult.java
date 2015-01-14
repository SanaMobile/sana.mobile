package org.sana.net;

/**
 * Characterizes a result from the MDS. The response is a JSON dictionary with 
 * two keys.
 * <ul type="none">
 * <li><code>status</code> either SUCCESS or FAILURE, depending on whether the 
 * request succeeded</li>
 * <li><code>code</code> the code indicating what the result was</li>
 * <li><code>data</code> : miscellaneous data pertaining to the request</li>
 * </ul>
 * 
 *  @author Sana Development Team
 */
public class MDSResult {
	private static final String SUCCESS_STRING = "SUCCESS";
	private static final String FAILURE_STRING = "FAILURE";
	
	
	private String status;
	private String code;
	private String data;
	private String encounter;
	private String procedure_guid;
	
	/**
	 * A new MDSResult with status of "none" and empty strings for all other
	 * fields.
	 */
	MDSResult() {
		status = "none";
		code = "";
		data = "";
		procedure_guid = "";
	}
	
	/**
	 * Whether the result is successful.
	 * @return true if <code>status</code> equals "SUCCESS" 
	 */
	public boolean succeeded() {
		return SUCCESS_STRING.compareToIgnoreCase(status) == 0;
	}

	/**
	 * Whether the result is a failure.
	 * @return true if <code>status</code> equals "FAILURE" 
	 */
	public boolean failed() {
		return FAILURE_STRING.compareToIgnoreCase(status) == 0;
	}
	
	/**
	 * The result message body.
	 * @return 
	 */
	public String getData() {
		return data;
	}
	
	/**
	 * The status code.
	 * @return
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public String getEncounter() {
		return encounter;
	}

	/**
	 * TODO
	 * @return
	 */
	public String getProcedure_guid() {
		return procedure_guid;
	}

	public static MDSResult NOSERVICE = new MDSResult();
	
}

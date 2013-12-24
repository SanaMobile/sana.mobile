package org.sana.net;

import java.util.Map;
/**
 * Response codes returned from MDS
 * 
 * @author Sana Development Team
 */
public enum MDSCode {
	SUCCEED(0),
	FAIL(5),
	LOGIN_SUCCESSFUL(10),
	LOGIN_FAILED(15),
	REGISTER_SUCCESSFUL(20),
	REGISTER_FAILED(25),
	INVALID_REQUEST(35),
	SAVE_SUCCESSFUL(40),
	SAVE_FAILED(45),
	SUCCESSFUL(50),
	FAILURE(55),
	NO_CODE(999);
	
	MDSCode(int code) {
		MDSCode.
		this.code = code;
		MDSCode.addMapping(code, this);
	}
	
	private int code;
	
	
	public String toString() {
		return "" + this.code;
	}
	
	private static Map<Integer, MDSCode> codeMap;
	
	private static void addMapping(int iCode, MDSCode eCode) {
		codeMap.put(iCode, eCode);
	}
	
	public static MDSCode parseMDSCode(String code) {
		try {
			int iCode = Integer.parseInt(code);
			if (codeMap.containsKey(iCode)) {
				return codeMap.get(iCode);
			}
		} catch (NumberFormatException e) {
		} catch (NullPointerException e) {
		} catch (Exception e) {
		}
		return NO_CODE;
	}
}

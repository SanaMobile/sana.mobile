/**
 * 
 */
package org.sana.core;

import org.sana.api.ISubject;


/**
 * Entity about whom data is collected. Most implementations will want to 
 * extend this class to include more useful annotations such as names, 
 * locations, and other fields which are typically static over the lifetime
 * of the Subject's interaction with the system.
 * 
 * @author Sana Development
 *
 */
public class Subject extends Model implements ISubject{
	
}

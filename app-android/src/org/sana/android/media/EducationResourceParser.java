package org.sana.android.media;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sana.android.media.EducationResource.Audience;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.text.TextUtils;
import android.util.Log;

/**
 * Parses education resources from xml.
 * 
 * @author Sana Development Team
 */
public class EducationResourceParser {
	public static String TAG = EducationResourceParser.class.getSimpleName();
	
	private EducationResourceHandler handler;
	
	/**
	 * Creates a new parser for a education resource info xml resource
	 */
	private EducationResourceParser(){}
	
	/**
	 * Returns a new parser
	 * @return
	 */
	public static EducationResourceParser newInstance(){
		return new EducationResourceParser();
	}
	
	/**
	 * Parses an Input source for education resources
	 * 
	 * @param source The input source to read from
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void parse(InputSource source) throws 
	ParserConfigurationException, SAXException, IOException
	{
		Log.d(TAG, "parse()");
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		XMLReader xr = sp.getXMLReader();
		handler = new EducationResourceHandler();
		xr.setContentHandler(handler);
		xr.parse(source);
	}

	/**
	 * Returns the list of education resources parsed from a resource
	 * 
     * @param audience The target audience
	 * @return
	 */
	public List<EducationResource> infoList(Audience audience){
		return new ArrayList<EducationResource>(handler.infoList(audience));
	}

	/**
	 * Returns the list of media resources parsed from a resource
	 * @return
	 */
	public List<EducationResource> infoList(List<String> ids, Audience audience){
		return new ArrayList<EducationResource>(handler.infoList(ids, audience));
	}
	
	/**
	 * Finds a media resource by its id from a parsed xml source
	 * 
	 * @param id The resource id
     * @param audience The target audience
     * @return
	 */
	public EducationResource findById(String id, Audience audience){
		return handler.findById(id, audience);
	}
	
	/**
	 * Handles SAX parsing events for media resource xml files
	 * 
	 * 	 &ltmedia id=""&gt
	 *       &lttitle&gt&lt/title&gt
	 *       &ltauthor&gt&lt/author&gt
	 *       &lttext&gt&lt/concept&gt
	 *       &ltauthor&gt&lt/question&gt
	 *       &lttext&gt&lt/text&gt
	 *       &ltresource&gt&lt/resource&gt
     *       &ltmimetype&gt&lt/mimetype&gt
     *       &ltaudience&gt&lt/audience&gt
     *   &lt/media&gt
     *   
     *   The following fields must be non null: title, author, text | resource,
     *   	mimetype, audience 
     *   
	 * @author Sana Development Team
	 */
    private static class EducationResourceHandler extends DefaultHandler{
    	public static String TAG = EducationResourceHandler.class.getSimpleName();
    	private boolean inList, inItem, inID, inTitle, inAuthor, inDescription;
    	private boolean inText, inFileName, inUrl, inMime, inHash, inAudience;
    	private EducationResource current;
    	private Map<String, EducationResource> patientResources;
    	private Map<String, EducationResource> workerResources; 
    	private Map<String, EducationResource> errResources; 
    	private EducationResourceHandler(){}
    	
    	/**
    	 * Returns the list of all available education resources
    	 * @return
    	 */
    	public List<EducationResource> infoList(Audience audience){
			Log.d(TAG, "Getting list for: " + audience.toString() );
			ArrayList<EducationResource> list = 
				new ArrayList<EducationResource>();
    		switch(audience){
    		case PATIENT:
    			list.addAll(patientResources.values());
    			break;
    		case WORKER:
    			list.addAll(workerResources.values());
    			break;
    		case ALL:
    			list.addAll(workerResources.values());
				list.addAll(patientResources.values());
				list.addAll(errResources.values());
				break;
			default:
				break;
    		}
    		Collections.sort(list);
    		return list;
    	}
    	
    	/**
    	 * Finds zero or more education resources from available resources 
    	 * filtered by a list of id's
    	 * 
    	 * @param ids The filter list
    	 * @param audience The target audience
    	 * @return A list of matching education resources
    	 */
    	public List<EducationResource> infoList(List<String> ids, 
    			Audience audience)
    	{
    		List<EducationResource> list = new ArrayList<EducationResource>();
    		for(String id: ids){
    			switch(audience){
    			case PATIENT:
        			if (patientResources.containsKey(id)){ 
        				list.add(patientResources.get(id));
        			}
        			break;
    			case WORKER:
        			if (workerResources.containsKey(id)){ 
        				list.add(workerResources.get(id));
        			}
        			break;
    			case ALL:
        			if (workerResources.containsKey(id)){ 
        				list.add(workerResources.get(id));
        			}
        			if (patientResources.containsKey(id)){ 
        				list.add(patientResources.get(id));
        			}
        			break;
    			}
    		}
    		return list;
    	}
    	
    	/**
    	 * Finds a education resource by its id from available resources. 
    	 * 
    	 * @param id The id to look up
    	 * @return A matching education resource or null
    	 */
    	public EducationResource findById(String id, Audience audience){
    		switch(audience){
    		case PATIENT:
				return patientResources.get(id);
    		case WORKER:
				return workerResources.get(id);
			default:
				EducationResource info = patientResources.get(id);
				if (info == null)
					return workerResources.get(id);
				else
					return info;
			}
    	}
    	
    	/**  {@inheritDoc} */
    	@Override 
    	  public void characters(char ch[], int start, int length) { 
    	    String chars = new String(ch, start, length); 
    	    chars = chars.trim(); 
    	    Log.d(TAG, "Read characters: " + chars );
    	    if(inID){
    	    	String scheme = chars.split(":")[0];
    	    	current.id = chars.split(":")[1];
    			try{
    				current.audience = EducationResource.Audience.valueOf(
    									String.valueOf(scheme).toUpperCase());
    			} catch(Exception e) {
    				Log.e(TAG, "Error parsing: "+current +", audience: "+chars);
    				current.audience = Audience.ERROR;
    			}
    	    } else if (inTitle){
    			current.name = chars;
    		} else if (inAuthor){
    			current.author = chars;
    		} else if (inDescription){
    			current.description = chars;
    		} else if (inText){
    			current.text = chars;
    		} else if (inFileName){
    			current.filename = chars;
    		} else if (inUrl){
    			current.downloadUrl = chars;
    		} else if (inMime){
    			current.mimeType = chars;
    		} else if (inHash){
    			current.hash = chars;
    		} else if (inAudience){
    			try{
    				current.audience = EducationResource.Audience.valueOf(
    									String.valueOf(chars).toUpperCase());
    			} catch(Exception e) {
    				Log.e(TAG, "Error parsing: "+current +", audience: "+chars);
    				current.audience = Audience.ERROR;
    			}
    		}
    	} 
    	
    	/**  {@inheritDoc} */
    	@Override 
    	public void startElement(String uri, String localName, String qName, 
    			Attributes attributes) throws SAXException 
    	{ 
    		Log.d(TAG, "startElement(): " + localName );
    		if(localName.equals(EducationResource.LIST)) {
    			workerResources = new Hashtable<String,EducationResource>();
    			patientResources = new Hashtable<String,EducationResource>();
    			errResources = new Hashtable<String,EducationResource>();
    			inList = true;
    		}else if (localName.equals(EducationResource.ITEM)){
    			current = new EducationResource();
    			inItem = true;
    		} else if (localName.equals(EducationResource.ID)){
    			inID = true;
    		} else if (localName.equals(EducationResource.TITLE)){
    			inTitle = true;
    		} else if (localName.equals(EducationResource.AUTHOR)){
    			inAuthor = true;
    		} else if (localName.equals(EducationResource.DESCRIPTION)){
    			inDescription = true;
    		} else if (localName.equals(EducationResource.TEXT)){
    			inText = true;
    		} else if (localName.equals(EducationResource.FILENAME)){
    			inFileName = true;
    		} else if (localName.equals(EducationResource.DOWNLOAD_URL)){
    			inUrl = true;
    		} else if (localName.equals(EducationResource.MIMETYPE)){
    			inMime = true;
    		} else if (localName.equals(EducationResource.HASH)){
    			inHash = true;
    		} else if (localName.equals(EducationResource.AUDIENCE)){
    			inAudience = true;
    		}
    	}
    	

    	/**  {@inheritDoc} */
    	@Override 
    	public void endElement(String uri, String localName, String qName) 
    			throws SAXException 
    	{ 
    		Log.d(TAG, "endElement(): " + localName );
    		if(localName.equals(EducationResource.LIST)) {
    			inList = false;
    		}else if (localName.equals(EducationResource.ITEM)){
    			if(!TextUtils.isEmpty(current.filename)){
    				File f = new File(EducationResource.DEFAULT_MEDIA_ROOT 
    									+ EducationResource.DEFAULT_MEDIA_PATH +
    									current.filename);
    				if(!f.exists())
    					current.audience = Audience.ERROR;
    			}
    			switch(current.audience){
    			case PATIENT:
    				patientResources.put(current.id, current);
    				break;
    			case WORKER:
    				workerResources.put(current.id, current);
    				break;
    			default:
    				errResources.put(current.id, current);
    				break;
    			}
    			current = null;
    			inItem = false;
    		} else if (localName.equals(EducationResource.ID)){
    			inID = false;
    		} else if (localName.equals(EducationResource.TITLE)){
    			inTitle = false;
    		} else if (localName.equals(EducationResource.AUTHOR)){
    			inAuthor = false;
    		} else if (localName.equals(EducationResource.DESCRIPTION)){
    			inDescription = false;
    		} else if (localName.equals(EducationResource.TEXT)){
    			inText = false;
    		} else if (localName.equals(EducationResource.FILENAME)){
    			inFileName = false;
    		} else if (localName.equals(EducationResource.DOWNLOAD_URL)){
    			inUrl = false;
    		} else if (localName.equals(EducationResource.MIMETYPE)){
    			inMime = false;
    		} else if (localName.equals(EducationResource.HASH)){
    			inHash = true;
    		} else if (localName.equals(EducationResource.AUDIENCE)){
    			inAudience = false;
    		}
    	}
    }
}

package org.sana.android.procedure;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.db.PatientInfo;
import org.sana.android.db.PatientValidator;

import org.sana.android.util.EnvironmentUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ViewAnimator;

/**
 * A Procedure is, conceptually, a form that can be made up of a number of 
 * pages, each of which may contain several elements. Since pages may contain 
 * entry criteria (checks that allow the procedure to branch if previous 
 * responses were made a certain way), the methods in the Procedure take care of 
 * checking these criteria.
 * 
 * @author Sana Development Team
 */
public class Procedure {
    public static final String TAG = Procedure.class.getSimpleName();
    
    private View cachedView;
    private Context cachedContext;

    private String onComplete = null;
    private Uri instanceUri = null;
    private String title;
    private String author;
    private String guid;
    private String concept = null;

    private List<ProcedurePage> pages;
    public ListIterator<ProcedurePage> pagesIterator;
    private ProcedurePage currentPage;
    private ViewAnimator viewAnimator;
    private PatientInfo patientInfo = null;

    private String version = "1.0";
    private boolean showQuestionIds = false;

    /**
     * Constructs a new Procedure.
     * 
     * @param title A title string.
     * @param author The author.
     * @param guid A unique identifier within the context of this application
     * 			   instance.
     * @param pages A list of pages contained within this procedure.
     * @param elements A map of all of the Procedure elements referenced by this
     * 		procedure.
     */
    public Procedure(String title, String author, String guid, 
    	List<ProcedurePage> pages, HashMap<String, ProcedureElement> elements) 
    {
        this.pages = new LinkedList<ProcedurePage>();
        //this.pages.addAll(pages);
        for(ProcedurePage pp : pages) {
            pp.setProcedure(this);
            this.pages.add(pp);
        }
        this.title = title;
        this.author = author;
        this.guid = guid;
        pagesIterator = pages.listIterator();
        
        next();
    }

    /**
     * Constructor which provides the onComplete String
     * @param title
     * @param author
     * @param guid
     * @param pages
     * @param elements
     * @param onComplete
     */
    public Procedure(String title, String author, String guid, List<ProcedurePage> pages, HashMap<String,
            ProcedureElement> elements, String onComplete)
    {
        this(title,author,guid,pages,elements);
        this.onComplete = onComplete;
    }

    public void init() {
    }
    
    /**
     * Sets the view referenced by this procedure
     * 
     * @param v the new View
     */
    public void setCachedView(View v){
    	this.cachedView = v;
    }
    
    /**
     * Gets the cached view.
     * @return a View instance.
     */
    public View getCachedView(){
    	return this.cachedView;
    }
    
    /**
     * Sets the Uri of this instance in the database
     * 
     * @param instanceUri the new instance Uri
     */
    public void setInstanceUri(Uri instanceUri) {
        this.instanceUri = instanceUri;
    }
    
    /**
     * Gets the instance Uri for this procedure.
     * @return A resource identifier for this procedure
     */
    public Uri getInstanceUri() {
        return instanceUri;
    }

    /**
     * The current, or active, page.
     * @return 
     */
    public ProcedurePage current() {
        return currentPage;
    }
    
    /**
     * Sets the data for the patient referenced in this procedure.
     * @param pi the new patient data
     */
    public void setPatientInfo(PatientInfo pi) {
    	this.patientInfo = pi;
    }
    
    /**
     * Gets the data for the patient referenced by this procedure
     * @return The patient data.
     */
    public PatientInfo getPatientInfo() {
    	return patientInfo;
    }

    /**
     * Determines whether there is a next page in the sequence. It does 
     * <b>NOT</b> check whether that page should be viewed or not.
     * 
     * @return true if there is at least one page past the current.
     */
    public boolean hasNext() {
        if(pagesIterator == null)
            return false;
        return pagesIterator.hasNext();
    }

    /**
     * Determines whether there is a previous page in the sequence. It does 
     * <b>NOT</b> check whether that page should be viewed or not.
     * 
     * @return true if there is at least one page before the current.
     */
    public boolean hasPrev() {
        if(pagesIterator == null)
            return false;
        if(pagesIterator.previousIndex() == 0) {
            return false;
        }
        return true;
    }

    /**
     * Advances to the next page in the sequence. It does <b>NOT</b> check 
     * whether that page should be viewed or not given user choices.
     */
    public void next() {
        if (hasNext()) {
            currentPage = pagesIterator.next();
            if(viewAnimator != null && cachedContext != null) {
                viewAnimator.setInAnimation(cachedContext,R.anim.slide_from_right);
                viewAnimator.setOutAnimation(cachedContext,R.anim.slide_to_left);
                viewAnimator.showNext();
            }
        }
    }
    
    /**
     * Goes back to the previous page in the sequence. It does <b>NOT</b> check 
     * whether that page should be viewed or not given user choices.
     */
    public void prev() {
        if (hasPrev()) {
            currentPage = pagesIterator.previous();
            if(viewAnimator != null && cachedContext != null) {
                viewAnimator.setInAnimation(cachedContext,R.anim.slide_from_left);
                viewAnimator.setOutAnimation(cachedContext,R.anim.slide_to_right);
                viewAnimator.showPrevious();
            }
        }
    }
    
    /**
     * Determines whether there is a next show-able page in the sequence, given
     * user selections thus far.
     * 
     * @return true if there is one page past the current and that page is 
     * 				visible
     */
    public boolean hasNextShowable() {
        if(pagesIterator == null)
            return false;
        if (!pagesIterator.hasNext())
            return false;
        for (int i = pagesIterator.nextIndex(); i < pages.size(); i++) {
            if (pages.get(i).shouldDisplay())
                return true;
        }
        return false;
    }
    
    /**
     * Determines whether there is a previous show-able page in the sequence, 
     * given user selections thus far.
     * 
     * @return true if there is one page before the current and that page is 
     * 				visible
     */
    public boolean hasPrevShowable() {
        if (pagesIterator == null)
            return false;
        if (!pagesIterator.hasPrevious())
            return false;
        if (pagesIterator.previousIndex() == 0)
        	return false;
        for (int i = pagesIterator.previousIndex(); i >= 0; i--) {
            if (pages.get(i).shouldDisplay())
                return true;
        }
        return false;
    }
            
    /**
     * Advances the current page to the next show-able page in the sequence, 
     * skipping over non-show-able pages, given user selections thus far. It 
     * also updates the procedure view to advance by this same number of pages.
     */
    public void advance() {
        if (!hasNextShowable())
            return;
        ProcedurePage pp = pagesIterator.next();
        viewAnimator.showNext();
        while (hasNext() && !pp.shouldDisplay()) {
            pp = pagesIterator.next();
            viewAnimator.showNext();
        }
        currentPage = pp;
        
        // Fill in default values for data from patient in the database
		PatientValidator.populateSpecialElements(this, patientInfo);
    }
   
    public ProcedurePage advanceNext() {
        if (!hasNext())
            return null;
        currentPage = pagesIterator.next();
        viewAnimator.showNext();
        // Fill in default values for data from patient in the database
		PatientValidator.populateSpecialElements(this, patientInfo);
		return currentPage;
    }
    
    public ProcedurePage advancePrev() {
        if (!hasPrev())
            return null;
        currentPage = pagesIterator.previous();
        viewAnimator.showPrevious();
        // Fill in default values for data from patient in the database
		PatientValidator.populateSpecialElements(this, patientInfo);
		return currentPage;
    }
    /**
     * Regresses the current page to the previous show-able page in the 
     * sequence, skipping over non-show-able pages, given user selections thus 
     * far. It also updates the procedure view to regress by this same number of
     * pages.
     */
    public void back() {
        if (!hasPrevShowable())
            return;
        ProcedurePage pp;
        // this will refer to the current page
        pagesIterator.previous();
        pp = pages.get(pagesIterator.previousIndex());
        viewAnimator.showPrevious();
        while (hasPrev() && !pp.displayForeground()) {
            pagesIterator.previous();
            pp = pages.get(pagesIterator.previousIndex());
            viewAnimator.showPrevious();
        }
        currentPage = pp;
    }
    
    /**
     * Sets the current page index to a specified value as an offset from zero.
     *  
     * @param pageIndex The index of the page to jump to.
     */
    public void jumpToPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pages.size()) {
        	return;
        }
        pagesIterator = pages.listIterator();
        Log.i(TAG, "pageIndex value: " + pageIndex);
        while(pagesIterator.nextIndex() != pageIndex) {
            pagesIterator.next();
        }
        Log.i(TAG, "current index of page: " + getCurrentIndex());
        currentPage = pagesIterator.next();
        Log.i(TAG, "current index of page: " + getCurrentIndex());
        viewAnimator.setInAnimation(null);
        viewAnimator.setOutAnimation(null);
        viewAnimator.setDisplayedChild(pageIndex);
    }
    

    /**
     * Sets the current page index to a specified value as an offset from zero
     * if and only if that page is viewable. If not viewable, the current index 
     * is unchanged
     *  
     * @param pageIndex The index of the page to jump to.
     */
    public void jumpToVisiblePage(int pageIndex) {
    	if (pageIndex < 0 || pageIndex >= pages.size())
    		return;
    	
    	pagesIterator = pages.listIterator();
    	int visibleIndex = 0;
    	int actualIndex = 0;
    	while (pagesIterator.hasNext()) {
    		ProcedurePage page = pagesIterator.next();
    		
    		if (visibleIndex == pageIndex) {
    			currentPage = page;
    	        viewAnimator.setInAnimation(null);
    	        viewAnimator.setOutAnimation(null);
    	        viewAnimator.setDisplayedChild(actualIndex);
    	        break;
    		}
    		
    		if (page.shouldDisplay()) {
    			visibleIndex++;
    		}
    		actualIndex++;
    	}
    }
    
    /**
     * Gets the index value of the current page.
     * 
     * @return The index value of the current page
     */
    public int getCurrentIndex() {
        return pages.indexOf(currentPage);
    }
    

    /**
     * Gets the index value of the current page if visible.
     * 
     * @return The index value of the current page if visible else 0.
     */
    public int getCurrentVisibleIndex() {
    	Iterator<ProcedurePage> pageIterator = pages.iterator();
    	int visibleIndex = 0;
    	while (pageIterator.hasNext()) {
    		ProcedurePage page = pageIterator.next();
    		if (page == currentPage) {
    			return visibleIndex;
    		}
    		if (page.shouldDisplay()) {
    			visibleIndex++;
    		}
    	}
    	return 0;
    }

    /**
     * The mu,ber of pages in this procedure.
     * @return The total number of pages 
     */
    public int getTotalPageCount() {
        return pages.size();
    }
    

    /**
     * The number of viewable pages in this procedure.
     * @return The total number of pages 
     */
    public int getVisiblePageCount() {
    	Iterator<ProcedurePage> pageIterator = pages.iterator();
    	int visibleCount = 0;
    	while (pageIterator.hasNext()) {
    		ProcedurePage page = pageIterator.next();
    		if (page.shouldDisplay()) {
    			visibleCount++;
    		}
    	}
    	return visibleCount;
    }
    
    /**
     * The procedure title.
     * @return The procedure title string.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * The procedure author
     * @return The procedure author string .
     */
    public String getAuthor() {
    	return author;
    }
    
    /**
     * Gets the unique identifier.
     * @return The guid.
     */
    public String getGuid() {
    	return guid;
    }

    public String getVersion(){
        return version;
    }


    public void setVersion(String version){
        this.version = version;
    }

    public String getOnComplete() {
        return onComplete;
    }

    public void setOnComplete(String onComplete) {
        this.onComplete = onComplete;
    }

    public String getConcept(){ return concept; }

    public void setConcept(String concept){ this.concept=concept; }

    public boolean idsShown(){
        return showQuestionIds;
    }

    public void setShowQuestionIds(boolean value){
        this.showQuestionIds = value;
    }

    /**
     * Writes the procedure, including all of its child elements, to an XML 
     * String. 
     * @return The procedure as xml text.
     */
    public String toXML() {
        Log.i(TAG,"Procedure.toXML()");
        StringBuilder sb = new StringBuilder();
        buildXML(sb);
        return sb.toString();
    }
    
    // ugly way to do this
    //TODO use a proper data dictionary for obs value
    public void setValue(String elementId, String value){
    	for(ProcedurePage page:pages){
    		page.setElementValue(elementId, value);
    	}
    }
    // a bit better but still hacky
    public boolean setValue(int pageIndex, String elementId, String value){
    	boolean result = false;
    	if(pageIndex > -1 && pageIndex < pages.size()){
    		pages.get(pageIndex).setElementValue(elementId, value);
    		result = true;
    	} else {
    		Log.w(TAG, "setValue(). Index Out of bounds. " + pageIndex );
    	}
    	return result;
    }
    
    /**
     * Writes the xml for this procedure to a StringBuilder object.
     * @param sb The builder to write to.
     */
    public void buildXML(StringBuilder sb) {
    	sb.append("<Procedure title =\"" + title 
    			+ "\" author =\"" + author 
    			+ "\" guid =\"" + guid 
    			+ "\" version=\"" + version
                + "\" uuid=\"" + guid
                + "\" concept=\"" + guid
                + "\" onComplete=\"" + guid
    			+ "\">\n");
        
        for (ProcedurePage p : pages) {
            p.buildXML(sb);
        }
        sb.append("</Procedure>");
    }
    
    /**
     * A map of the 'id' to 'answer' attributes for all of the data collection
     * points in this procedure.
     * 
     * @return The answers for all of the ProcedureElements mapped to their ids 
     */
    public Map<String, String> toAnswers() {
        HashMap<String,String> answers = new HashMap<String,String>(); 
        for(ProcedurePage pp : pages) {
        	pp.populateAnswers(answers);
        }
        
        return answers;
    }
    
    /**
     * Takes a map of answers and fills them into the elements of this 
     * procedure. Functionally, this is used to return a prior patient encounter
     * to the state it was in when saved
     * @param answersMap The answers for all of the ProcedureElements mapped to 
     * 		their ids 
     */
    public void restoreAnswers(Map<String,String> answersMap) {
    	for (ProcedurePage pp : pages) {
    		pp.restoreAnswers(answersMap);
    	}
    }
    
    /**
     * Produces a new map of element properties to their ids. 
     * 
     * @return a dictionary mapping Element ids to a dictionary containing the 
     * properties for each Element
     */
    public Map<String, Map<String,String>> toElementMap() {
        HashMap<String,Map<String,String>> answers = 
        							new HashMap<String,Map<String,String>>();
        for(ProcedurePage pp : pages) {
        	pp.populateElementMap(answers);
        }
        return answers;
    }
    
    // Constructs a Procedure object from xml text
    private static Procedure fromXML(Node node) throws ProcedureParseException {
        
        if(!node.getNodeName().equals("Procedure")) {
            throw new ProcedureParseException("Procedure got NodeName" 
            		+ node.getNodeName());
        }        
        
        List<ProcedurePage> pages = new ArrayList<ProcedurePage>();
        NodeList nl = node.getChildNodes();
        ProcedurePage page;
        HashMap<String, ProcedureElement> elts = 
        							new HashMap<String, ProcedureElement>();
        for(int i=0; i<nl.getLength(); i++) {
            Node child = nl.item(i);
            if(child.getNodeName().equals("Page")) {
                page = ProcedurePage.fromXML(child, elts);
                elts.putAll(page.getElementMap());
                pages.add(page);
            }
        }
        String title = "Untitled Procedure";
        Node titleNode = node.getAttributes().getNamedItem("title");

        if(titleNode != null) {
        	title = titleNode.getNodeValue();
            Log.i(TAG, "Loading Procedure from XML: " + title);
            
        }
        
        String author = "";
        Node authorNode = node.getAttributes().getNamedItem("author");
        if(authorNode != null) {
        	author = authorNode.getNodeValue();
            Log.i(TAG, "Author of this procedure: " + author);
            
        }
        
        String uuid = "";
        Node guidNode = node.getAttributes().getNamedItem("uuid");
        if(guidNode != null) {
        	uuid = guidNode.getNodeValue();
            Log.i(TAG, "Unique Id of procedure: " + uuid);
        }
        
        String version = "";
        Node n = node.getAttributes().getNamedItem("version");
        if(n != null) {
        	version = n.getNodeValue();
            Log.i(TAG, "Version: " + version);
        }

        String onComplete = "";
        n = node.getAttributes().getNamedItem("on_complete");
        if(n != null) {
            onComplete = n.getNodeValue();
            Log.i(TAG, "Version: " + version);
        }

        String concept = "";
        n = node.getAttributes().getNamedItem("concept");
        if(n != null) {
            concept = n.getNodeValue();
            Log.i(TAG, "Concept: " + version);
        }
        Procedure procedure = new Procedure(title, author, uuid, pages, elts);
        procedure.setVersion(version);
        procedure.setConcept(concept);
        procedure.setOnComplete(onComplete);
        return procedure;
    }
    
    /**
     * Constructs a new Procedure from a raw xml resource.
     * @param c the current Context.
     * @param id The resource identifier
     * @return A new Procedure instance.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws Exception
     */
    public static Procedure fromRawResource(Context c, int id) throws 
    	IOException, ParserConfigurationException, SAXException, Exception 
    {
    	InputStream is = null;
    	is = c.getResources().openRawResource(id);
        Procedure p =  fromXML(new InputSource(is)); 
        return p;
    }
    
    /**
     * Constructs a new Procedure from an xml string.
     * @param xml The xml string to read.
     * @return A new Procedure instance.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws Exception
     */
    public static Procedure fromXMLString(String xml) throws IOException, 
    		ParserConfigurationException, SAXException, ProcedureParseException 
    {
    	return fromXML(new InputSource(new StringReader(xml)));
    }
    
    /**
     * Constructs a new Procedure from an InputSource.
     * @param xml The InputSource to read.
     * @return A new Procedure instance.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws Exception
     */
    public static Procedure fromXML(InputSource xml) throws IOException, 
    	ParserConfigurationException, SAXException, ProcedureParseException 
    {
    	
    	long processingTime = System.currentTimeMillis();
    	
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setNamespaceAware(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document d = db.parse(xml);
        
        NodeList children = d.getChildNodes();
        Node procedureNode = null;
        for(int i=0; i<children.getLength(); i++) {
            Node child = d.getChildNodes().item(i);
            if(child.getNodeName().equals("Procedure")) {
                procedureNode = child;
                break;
            }
        }
        if(procedureNode == null) {
            throw new ProcedureParseException("Can't get procedure");
        }
        Procedure result = fromXML(procedureNode);
        
        processingTime = System.currentTimeMillis() - processingTime;
        Log.i(TAG, "Parsing procedure XML took " + processingTime + " milliseconds.");
        
        return result;
    }
    
	// creates the views for this object and indirectly all of its child pages 
    private View createView(Context c) {
        viewAnimator = new ViewAnimator(c);
        //viewAnimator.setInAnimation(AnimationUtils.loadAnimation(c,R.anim.slide_from_right));
        //viewAnimator.setOutAnimation(AnimationUtils.loadAnimation(c,R.anim.slide_to_left));

        for(ProcedurePage page : pages) {
            viewAnimator.addView(page.toView(c));
        }

        return viewAnimator;
    }
    
    /**
     * Clears any views cached in this object.
     */
    public void clearCachedViews() {
    	cachedView = null;
    	cachedContext = null;
    	
    	for (ProcedurePage pp : pages) {
    		pp.clearCachedView();
    	}
    }
    
    /**
     * A View of this object.
     * @param c the current Context.
     * @return An existing view of this object if cached or a new one.
     */
    public View toView(Context c) {
        Log.i(TAG,"toView(Context)");
        if(cachedView == null || cachedContext != c) {
            Log.d(TAG, "...generating cached view");
            cachedView = createView(c);
            cachedContext = c;
        }
        return cachedView;
    }
    
    /**
     * A list of the summary strings for all child pages. 
     * @return The child pages as a list of summary strings.
     */
    public ArrayList<String> toStringArray() {
        ArrayList<String> stringList= new ArrayList<String>();
        for (ProcedurePage cp : pages) {
        	if(cp.shouldDisplay()) {
        		stringList.add(cp.getSummary());
        	}
        }
        return stringList;
    }
    
	/**
	 * Creates the necessary directories and files on the external drive for 
	 * procedure management.
	 */
	public static void intializeDevice(){
		String mount = Environment.getExternalStorageState();
		Log.d(TAG, "Media stat:" + mount);
		if(!mount.equals(Environment.MEDIA_MOUNTED)){
			Log.e(TAG, "Can not initialize sdcard procedure resource dir.");
			return;
		}
		File p = new File(EnvironmentUtil.getProcedureDirectory());
		File r = new File(Environment.getExternalStorageDirectory() 
				+ Constants.PATH_EDUCATION);
		if (p.mkdirs() && r.mkdirs()){
			Log.d(TAG, "Created Sana procedure directories");
		} else {
			Log.d(TAG, "Sana procedure directory failed. ");
		}
	}
}

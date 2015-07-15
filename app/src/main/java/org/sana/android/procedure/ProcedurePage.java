package org.sana.android.procedure;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.sana.android.activity.EducationResourceList;
import org.sana.android.content.core.ObservationWrapper;
import org.sana.android.db.PatientValidator;
import org.sana.android.media.EducationResource;
import org.sana.android.media.EducationResource.Audience;
import org.sana.android.procedure.ProcedureElement.ElementType;
import org.sana.android.procedure.branching.Criteria;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * ProcedurePage the the object corresponding to a single "page" in a Sana
 * procedure.
 * <p/>
 * Each ProcedurePage can contain several elements, although the Sana style 
 * recommendation is to use just one element per page. Each page is defined as 
 * an XML node in a procedure description.
 * <p/>
 * ProcedurePages may have criteria that must be true in order for the procedure
 * runner to display the page. This is stored in the ProcedurePage object. See 
 * documentation on Criteria for more info about how Criteria work.
 * 
 * @author Sana Development Team
 */
public class ProcedurePage {
	public static final String TAG = ProcedurePage.class.getSimpleName();

	private View cachedView;
	private Context cachedContext;

	List<ProcedureElement> elements;
	
	Procedure procedure;
	Criteria criteria;
    String label = null;
	/**
	 * Constructor for ProcedurePage if no entry criteria are desired for the
	 * page (the page will always display).
	 *
	 * @param elements a list of displayable elements
	 */
	public ProcedurePage(List<ProcedureElement> elements) {
		this.elements = elements;
		this.criteria = new Criteria();
	}

	/** 
	 * Logs the list of elements on this page.
	 */
	public void listElements() {
		Log.i(TAG, "listing all element types on this page");
		for (int i=0; i<elements.size(); i++) {
			Log.i(TAG, elements.get(i).getId());
		}
	}
	
	// clears the cached view
	void clearCachedView() {
		for (ProcedureElement pe : elements) {
			pe.clearCachedView();
		}
	}
	
	/**
	 * Constructor for ProcedurePage where criteria will determine whether
	 * elements are displayed.
	 *
	 * @param elements a list of displayable elements
	 * @param criteria A set of logic conditions for determining visibility.
	 */
	public ProcedurePage(List<ProcedureElement> elements, Criteria criteria) {
		this.elements = elements;
		this.criteria = criteria;
	}

    public String getLabel(){
      return label;
    }

    public void setLabel(String label){
        this.label = label;
    }
	/**
	 * Returns the value of the answer attribute for a contained element.
	 * 
	 * @param key an element id.
	 * @return the value of the element answer.
	 */
	public String getElementValue(String key) {

		String value = "";
        for (ProcedureElement el:elements) {
            if (el.getId().compareTo(key) == 0) {
                value = el.getAnswer();
                break;
            }
        }
		return value;
	}
	
	/**
	 * Sets the parent procedure for this object and all child elements.
	 * 
	 * @param procedure the new parent.
	 */
	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
		for(ProcedureElement pe : elements) {
			pe.setProcedure(procedure);
		}
	}

	// tests whether page has special elements that need further action
	private String[] specialElements = {"patientId", 
			"patientFirstName", "patientLastName", "patientBirthdateDay", 
			"patientBirthdateMonth", "patientBirthdateYear", "patientGender"};

	/**
	 * @return true if one or more elements require non standard processing
	 */
	public boolean hasSpecialElement() {
		for (int i=0; i<elements.size(); i++) {
			for (int j=0; j<specialElements.length; j++) {
				if (elements.get(i).id.equals(specialElements[j])) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @return the element which holds the patient id if it exists.
	 */
	public PatientIdElement getPatientIdElement() {
		PatientIdElement patientid = null;
		for (int i=0; i<elements.size(); i++) {
			if (elements.get(i).getType().equals(ElementType.PATIENT_ID)) {
				patientid = (PatientIdElement)elements.get(i);
				break;
			}
		}
		return patientid;
	}
	
	//TODO rename to getElementById
	/**
	 * Returns a list of all the elements on the page with a specified 
	 * ElementType 
	 * 
	 * @param type the element type to match.
	 */
	public ProcedureElement getElementByType(String type) {
		ProcedureElement p = null;
		List<ProcedureElement> els = elements;
		for (int i=0; i<els.size(); i++) {
			if (els.get(i).getId().equals(type)) {
				p = els.get(i);
			}
		}
		return p;
	}

    public ProcedureElement getElementById(String id) {
        ProcedureElement p = null;
        for (ProcedureElement el:elements) {
            if (el.getId().compareTo(id) == 0) {
                p = el;
                break;
            }
        }
        return p;
    }

    public List<ProcedureElement> getElements(){
        return elements;
    }

    public boolean hasElementWithId(String id) {
        boolean result = false;
        for (ProcedureElement el:elements) {
            if (el.getId().compareTo(id) == 0) {
                result = true;
                break;
            }
        }
        return result;
    }

	public String elementWithConcept(String concept) {
		String result = null;
		for (ProcedureElement el:elements) {
			String compare = concept.replace("_"," ").toUpperCase();
			if (el.getConcept().compareToIgnoreCase(compare) == 0) {
				result = el.getId();
				break;
			}
		}
		return result;
	}

    public List<String> getConcepts(){
        List<String> result = new ArrayList<String>();
        for (ProcedureElement el:elements) {
            result.add(el.getConcept());
        }
        return result;
    }


	/**
	 * @return A list of elements which require non-standard processing.
	 */
	public List<ProcedureElement> getSpecialElements() {
		List<ProcedureElement> els = new ArrayList<ProcedureElement>();
		for (ProcedureElement el : elements) {
			els.add(el);
		}
		return els;
	}
	
	/**
	 * Sets the answer value of a specific element.
	 * 
	 * @param key the element id attribute.
	 * @param value the new answer value.
	 */
	public void setElementValue(String key, String value) {
		for (int i=0; i<elements.size(); i++) {
			ProcedureElement e = elements.get(i);
			if (e.id.equals(key)) {
				e.setAnswer(value);
				return;
			}
		}
	}

	/**
	 * Inspects a ProcedureElement to determine if it needs non-standard 
	 * processing.
	 * @param e the element to inspect.
	 * @return true if the element reequires non-standard processing.
	 */
	public boolean isSpecialElement(ProcedureElement e) {
		for (int i=0; i<specialElements.length; i++) {
			if (e.id.equals(specialElements[i])) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Validates all child elements
	 * 
	 * @return true if all children are valid.
	 * @throws ValidationError
	 */
	public boolean validate() throws ValidationError {
		
		for (ProcedureElement el : elements) {
			if (!el.validate()) {
				return false;
			}
		}
		if (!PatientValidator.validate(procedure, procedure.getPatientInfo())) {
			return false;
		}
		return true;
	}

	/**
	 * Tests whether the criteria are currently met to display this page, given
	 * user selections thus far.
	 */
	public boolean shouldDisplay() {
		return criteria.criteriaMet();
	}

	public boolean displayForeground(){
		boolean show = false;
		for(ProcedureElement el: elements){
			show = el.isViewActive() || show;
		}
		return shouldDisplay() && show;
	}
	
	/**
	 * Maps all of the child elements to a map using their id as a key.
	 * @param elementMap The map to place the elements into.
	 */
	public void populateElements(HashMap<String, ProcedureElement> elementMap) {
		for (ProcedureElement e : elements) {
			elementMap.put(e.getId(), e);
		}
	}

	/**
	 * @return a new map of all child elements with their id as a key.
	 */
	public HashMap<String, ProcedureElement> getElementMap() {
		HashMap<String, ProcedureElement> ret = 
			new HashMap<String, ProcedureElement>();
		populateElements(ret);
		return ret;
	}
	
	/**
	 * Plays an entry audio prompt for each child element.
	 */
	public void playFirstPrompt() {
		for (ProcedureElement e : elements) { 
			if (e.hasAudioPrompt()) {
				e.playAudioPrompt();
				return;
			}
		}
	}
	
	/**
	 * @return The question attribute of the first element.
	 */
	public String getSummary() {
        if(!TextUtils.isEmpty(label)) {
            return label;
        } else if (!elements.isEmpty())
			return elements.get(0).getQuestion();
		return "";
	}

	/**
	 * Returns a view of elements in this procedure.
	 * @param c The application Context.
	 * @return 
	 */
	public View toView(Context c) {
		if(cachedView == null || cachedContext != c) {
			cachedContext = c;
			cachedView = createView(c);
		}
		return cachedView;        
	}
	
	/**
	 * Constructs a new Intent which will launch the EducationResourceList with 
	 * a selection of the media available for the elements on this page.
	 * @param audience CHWs or patients.
	 * @return
	 */
	public Intent educationResources(Audience audience){
		List<String> ids = new ArrayList<String>();
		for(ProcedureElement pe: getElementMap().values()){
			switch(pe.getType()){
			case INVALID:
				break;
			default:
				String rawStr = pe.getConcept() + pe.getQuestion();
				ids.add(EducationResource.toId(rawStr));
			}
		}
		Intent intent = EducationResourceList.getIntent(ids, audience);
		return intent;
	}

	/** A scrollable view of the elements in this procedure */
	private View createView(Context c) {
		// ll contains scroll contains ill
		ScrollView scroll = new ScrollView(c);
		LinearLayout ll = new LinearLayout(c);
		LinearLayout ill = new LinearLayout(c);

		ll.setOrientation(LinearLayout.VERTICAL);
		ill.setOrientation(LinearLayout.VERTICAL);

        List<View> visibleElements = new ArrayList<View>();
		for (ProcedureElement e : elements) {
            View v = e.toView(c);
            if (e.getType() != ElementType.HIDDEN) {
                visibleElements.add(v);
            }
        }
        int size = visibleElements.size();
        float weight = 1.0f / ((size > 0)? size: 1);
        // Only add and set layout parameters
        for(View v: visibleElements){
            LinearLayout subll = new LinearLayout(c);
            subll.addView(v);
            subll.setGravity(Gravity.CENTER);
            ill.addView(subll, new LinearLayout.LayoutParams(-1, -1, weight));
		}
		ill.setWeightSum(1.0f);
		scroll.addView(ill);

		ll.addView(scroll);
        ll.setGravity(Gravity.CENTER);
		return ll;
	}

	/** Creates an XML description of the page and its elements. */
	public String toXML() {
		StringBuilder sb = new StringBuilder();
		buildXML(sb);
		return sb.toString();
	}
	
    /**
     * Writes a string representation of this object to a StringBuilder
     * @param sb the builder to write to.
     */
	public void buildXML(StringBuilder sb) {
		Log.i(TAG, "ProcedurePage.toXML()");
		sb.append("<Page>\n");

		for (ProcedureElement e : elements) {
			e.buildXML(sb);
		}

		sb.append("</Page>\n");
	}

	/**
	 * Restores the state of a procedure by loading a collection of answers. 
	 * @param answersMap A map of previously collected answers.
	 */
	public void restoreAnswers(Map<String,String> answersMap) {
		for(ProcedureElement s : elements) {
            Log.d(TAG, "...checking for id=" + s.getId());
			if(answersMap.containsKey(s.getId())) {
                Log.d(TAG, "...setting answer" + answersMap.get(s.getId()));
				s.setAnswer(answersMap.get(s.getId()));
			}
		}
	}

	/**
	 * Returns a map of the child elements to answer attribute values. 
	 * @return a new answer map.
	 */
	public Map<String,String> toAnswers() {
		HashMap<String,String> answers = new HashMap<String,String>();
		populateAnswers(answers);
		return answers;
	}

	/**
	 * Restores the state of a procedure by loading a collection of answers. 
	 * @param answers A map of previously collected answers.
	 */
	public void populateAnswers(Map<String,String> answers) {
		for(ProcedureElement s : elements) {
			answers.put(s.getId(), s.getAnswer());
		}
	}

	/**
     * Produces a new map of element properties to their ids. 
     * 
     * @return a dictionary mapping Element ids to a dictionary containing the 
     * properties for each Element
     */
	public Map<String,Map<String,String>> toElementMap() {
		HashMap<String,Map<String,String>> elementMap = 
			new HashMap<String,Map<String,String>>();
		populateElementMap(elementMap);
		return elementMap;
	}

	/**
     * Fills in the attributes for an element into a mapping of the element id
     * to its attributes. 
     * @param elementMap map to add to.
     */
	public void populateElementMap(Map<String,Map<String,String>> elementMap) {
		for(ProcedureElement s : elements) {
			Map<String,String> submap = new HashMap<String,String>();
			submap.put("question", s.getQuestion());
			submap.put("answer", s.getAnswer());
			submap.put("type", s.getType().toString());
			submap.put("concept", s.getConcept());
			elementMap.put(s.getId(), submap);
		}
	}

	public List<Intent> hiddenActions(){
		List<Intent> actions = new ArrayList<Intent>();
		for(ProcedureElement s : elements) {
			
    		if(s.getType() == ElementType.HIDDEN && !TextUtils.isEmpty(s.action)){
    			HiddenElement h = (HiddenElement) s;
    			Intent intent = h.getIntent();
    			if(intent != null) actions.add(intent);
    		}	
		}
		return actions;
	}
	
	public boolean hasHiddenActions(){
		boolean actionable = false;
		for(ProcedureElement s : elements){
			actionable = actionable ||(s.getType() == ElementType.HIDDEN && !TextUtils.isEmpty(s.action));
		}
		return actionable;
	}
	
	/**
	 * Create a ProcedurePage from a node in an XML procedure description.
	 */
	public static ProcedurePage fromXML(Node node,
		HashMap<String, ProcedureElement> elts) throws ProcedureParseException 
	{
		//Log.i(TAG, "ProcedurePage.fromXML(" + node.toString() + ")");
		if (!node.getNodeName().equals("Page")) {
			throw new ProcedureParseException("ProcedurePage got NodeName "
					+ node.getNodeName());
		}
		List<ProcedureElement> elements = new ArrayList<ProcedureElement>();
		Criteria criteria = new Criteria();
		NodeList children = node.getChildNodes();
		boolean showIfAlreadyExists = false;
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().equals("Element")) {
				elements.add(ProcedureElement.createElementfromXML(child));
			} else if (child.getNodeName().equals("ShowIf")) {
				//Log.i(TAG, "Page has ShowIf - creating Criteria");
				if (showIfAlreadyExists)
					throw new ProcedureParseException(
					"More than one ShowIf statement!");
				criteria = Criteria.fromXML(child, elts);
				showIfAlreadyExists = true;
			}
		}
		ProcedurePage pp = new ProcedurePage(elements, criteria);
		return pp;
	}
}

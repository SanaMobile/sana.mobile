package org.sana.android.procedure.branching;

import org.sana.BuildConfig;
import org.sana.android.procedure.MultiSelectElement;
import org.sana.android.procedure.ProcedureElement;
import org.sana.android.procedure.ProcedureParseException;
import org.sana.android.procedure.ProcedureElement.ElementType;

import android.util.Log;

/**
 * The Criterion class is a data representation of the XML 
 * <p/>
 * &lt;Criteria type="" elementId="" value=""/&gt;
 * <p/>
 * It holds a reference to the element elementId refers to so that it can 
 * determine user responses. The three Criterion Types (EQUALS, GREATER, LESS) 
 * are logically-complete, since a Criterion object will always sit inside a 
 * Criteria object, which can perform arbitrary boolean logic on it.
 */
public class Criterion {
    public static enum CriterionType {
        EQUALS, GREATER, LESS
    }
    private static final String TAG = "Criterion";
    private CriterionType criterionType;
    private ProcedureElement element;
    private String value;
    
    /**
     * A new Criterion object
     * @param critType The type of Criterion
     * @param elmt The source element in the Procedure
     * @param val The <code>value</code> attribute for this Criterion
     * @throws ProcedureParseException
     */
    public Criterion(CriterionType critType, ProcedureElement elmt, String val) 
    	throws ProcedureParseException 
    {
        this.criterionType = critType;
        this.element = elmt;
        this.value = val;
        if (elmt == null)
            throw new ProcedureParseException("Null element");
        if ((critType == CriterionType.GREATER) || 
        		(critType == CriterionType.LESS)) 
        {
            try {
                Double.parseDouble(val);
            } catch (NumberFormatException e) {
                throw new ProcedureParseException("Cannot compare non-numeric "
                		+"value. Cannot create criterion for element " 
                		+ elmt.getId());
            }
        }
    }
    
    /**
     * Checks if the given Criterion is met, given user responses.
     * 
     * For a Criterion based on a Multi-Select element, if any of the choices
     * evaluates as true, then the Criterion is met.
     * 
     * For blank (unanswered, blank default) elements, criterionMet evaluates 
     * as true.
     */
    public boolean criterionMet() {
        // lookup what the user selected
        String userVal = "";
        try {
            userVal = element.getAnswer();
        } catch (NullPointerException e) {
            // play it safe and show the page
            return false;
        }           
        // check if it is empty
        if ((userVal == "") || (userVal== null)) {
            // empty user response, lets play it safe and show the page
            return false;
        }
        // special case MULTI-SELECT
        if (element.getType() == ElementType.MULTI_SELECT) {
            // We (arbitrarily) handle MultiSelect by seeing if 
            // ANY selection in the "answer" matches "val" 
            // if so, then it evaluates as true
            String[] vals = userVal.split(MultiSelectElement.TOKEN_DELIMITER);
            for (String s : vals) {
                if (criterionMetHelper(s))
                    return true;
            }
            return false;
        } else {
            // test the user's answer against the criterion
            return criterionMetHelper(userVal);
        }
    }
    
    private boolean criterionMetHelper(String userVal) {
    	boolean result = false;
        switch(criterionType) {
        case EQUALS:
            if (value.compareToIgnoreCase(userVal) == 0) {
                result = true;
            }
            break;
        case GREATER:
            try {
                if (Double.parseDouble(userVal) > Double.parseDouble(value))
                    result =  true;
            // show the page if we can't parse
            } catch (NumberFormatException e) {return true;}
            break;
        case LESS:
            try {
                if (Double.parseDouble(userVal) < Double.parseDouble(value))
                    result = true;
            // show the page if we can't parse
            } catch (NumberFormatException e) {return true;}
            break;
        }
        /*
    	if(BuildConfig.DEBUG)
    	Log.d(TAG, "criterionMetHelper(): " + criterionType 
    			+ "(Criterion,User Value): (" +value +"," + userVal + ") "
    			+ " result = " + result);
    	*/
        return result;
    }
}

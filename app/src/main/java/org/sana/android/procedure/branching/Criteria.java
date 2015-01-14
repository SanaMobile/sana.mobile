package org.sana.android.procedure.branching;

import java.util.HashMap;

import org.sana.BuildConfig;
import org.sana.android.procedure.ProcedureElement;
import org.sana.android.procedure.ProcedureParseException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

/**
 * Criteria is the concrete base class for representing arbitrary logic that is
 * used to determine whether a page should be shown or not. Only create a base
 * Criteria object if you always want its crtieriaMet() method to return true.
 * 
 * A complex Criteria object may contain several other Criteria objects within
 * it at different levels of depth. The criteriaMet() method recursively
 * evaluates its contents.
 * 
 * Instantiating a ProcedurePage with the criteria-less constructor is
 * effectively the same as passing in a new Criteria() object into
 * ProcedurePage's regular constructor.
 * 
 */
public class Criteria {
    public static final String TAG = "Criteria";

    /** 
     * An empty criteria set
     */
    public Criteria() {
    }

    /**
     * Defaults to always true. Child classes should override this method.
     * @return true if the criteria is met
     */
    public boolean criteriaMet() {
        return true;
    }

    /**
     * A call to fromXML on a base Criteria type should only be used as a parse
     * entry- point on a ShowIf node. Child classes override this fromXML
     * method, and can be used on inner nodes. The fromXML method will 
     * recursively create a logic tree from the given XML description.
     */
    public static Criteria fromXML(Node node,
            HashMap<String, ProcedureElement> elts)
            throws ProcedureParseException {
        Log.i(TAG, "Criteria.fromXML(" + node.toString() + ")");
        if (!node.getNodeName().equals("ShowIf")) {
            throw new ProcedureParseException("Criteria got NodeName "
                    + node.getNodeName());
        }

        NodeList children = node.getChildNodes();
        if (children.getLength() != 3) {
            throw new ProcedureParseException(
                    "Too many child nodes for a ShowIf: "
                            + children.getLength());
        }
        Node child = children.item(1);
        return Criteria.switchOnCriteria(child, elts);
    }
    /**
     * TODO
     * @param child
     * @param elts
     * @return
     * @throws ProcedureParseException
     */
    public static Criteria switchOnCriteria(Node child,
        HashMap<String, ProcedureElement> elts) throws ProcedureParseException 
    {
        Criteria c = new Criteria();
        if (child.getNodeName().equals("Criteria")) {
        	//if(BuildConfig.DEBUG) Log.d(TAG, "switchOnCriteria(): Child Node Name: " + "Criteria");
            c = LogicBase.fromXML(child, elts);
        } else if (child.getNodeName().equals("and")) {
        	//if(BuildConfig.DEBUG) Log.d(TAG, "switchOnCriteria(): Child Node Name: " + "and");
            c = LogicAnd.fromXML(child, elts);
        } else if (child.getNodeName().equals("or")) {
        	//if(BuildConfig.DEBUG) Log.d(TAG, "switchOnCriteria(): Child Node Name: " + "or");
            c = LogicOr.fromXML(child, elts);
        } else if (child.getNodeName().equals("not")) {
        	//if(BuildConfig.DEBUG) Log.d(TAG, "switchOnCriteria(): Child Node Name: " + "not");
            c = LogicNot.fromXML(child, elts);
        }
        return c;
    }

}

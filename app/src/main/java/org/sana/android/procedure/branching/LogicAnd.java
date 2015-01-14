package org.sana.android.procedure.branching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sana.android.procedure.ProcedureElement;
import org.sana.android.procedure.ProcedureParseException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

/**
 * LogicAnd is a Criteria subclass that serves as a container to hold multiple
 * Criteria. The key difference between LogicAnd and LogicOr is the criteriaMet()
 * method. As the name implies, LogicAnd's criteriaMet() method evaluates if ALL
 * of the Criteria it holds are met.
 */
public class LogicAnd extends Criteria {
    private List<Criteria> criteria;
    
    public LogicAnd(List<Criteria> crit) {
        this.criteria = crit;
    }

    public boolean criteriaMet() {
    	Log.d(TAG, "criterionMet(): AND: Begin:");
    	boolean result = true;
        for(Criteria c : criteria) {
            if (!c.criteriaMet())
                result = result  && false;
        }
    	Log.d(TAG, "criterionMet(): AND: Result: " + result);
        return result;
    }
    
    /**
     * Constructs a LogicAnd Criteria from an XML Node
     * @param node The source Node
     * @param elts a list of child criteria
     * @return
     * @throws ProcedureParseException
     */
    public static LogicAnd fromXML(Node node, HashMap<String, 
    		ProcedureElement> elts) throws ProcedureParseException 
    {
        if(!node.getNodeName().equals("and"))
            throw new ProcedureParseException("LogicAnd got NodeName "
            		+ node.getNodeName());
        NodeList children = node.getChildNodes();
        List<Criteria> crits = new ArrayList<Criteria>();
        for(int i=0; i<children.getLength();i++) {
            Node child = children.item(i);
            if(child.getNodeName().equals("Criteria") || 
                    child.getNodeName().equals("and") || 
                    child.getNodeName().equals("or") || 
                    child.getNodeName().equals("not")) {
                crits.add(Criteria.switchOnCriteria(child, elts));
            }
        }
        if (crits.size() == 0)
            throw new ProcedureParseException("LogicAnd no arguments to <or>");
        else
            return new LogicAnd(crits);
    }
}    
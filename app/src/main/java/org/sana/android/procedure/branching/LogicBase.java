package org.sana.android.procedure.branching;

import java.util.HashMap;

import org.sana.android.procedure.ProcedureElement;
import org.sana.android.procedure.ProcedureParseException;
import org.sana.android.procedure.branching.Criterion.CriterionType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * LogicBase is a Criteria subclass that serves as a container for a single
 * Criterion.
 */      
public  class LogicBase extends Criteria {
	public static final String TAG = LogicBase.class.getSimpleName();
    private Criterion criterion;
    
    public LogicBase(Criterion crit) {
        this.criterion = crit;
    }
    
    public boolean criteriaMet() {
        return criterion.criterionMet();
    }
    
    public static LogicBase fromXML(Node node, HashMap<String, 
    		ProcedureElement> elts) throws ProcedureParseException 
    {
        if(!node.getNodeName().equals("Criteria")) {
            throw new ProcedureParseException("LogicBase got NodeName " 
            		+ node.getNodeName());
        }
        if(node.getChildNodes().getLength() != 0) {
            throw new ProcedureParseException("A single criteria has children.");
        }
        
        NamedNodeMap attributes = node.getAttributes();
        Node typeNode = attributes.getNamedItem("type");
        String type = "";
        CriterionType critType;
        if(typeNode != null) {                
            type = typeNode.getNodeValue();
            if (type.equals("EQUALS"))
                critType = CriterionType.EQUALS;
            else if (type.equals("GREATER"))
                critType = CriterionType.GREATER;
            else if (type.equals("LESS"))
                critType = CriterionType.LESS;
            else
                throw new ProcedureParseException("LogicBase bad type for "
                		+"NodeName " + node.getNodeName());
        } else {
            throw new ProcedureParseException("LogicBase no type for NodeName " 
            		+ node.getNodeName());
        }
        Node elementIdNode = attributes.getNamedItem("id");
        String elementId = "";
        if(elementIdNode != null) {                
            elementId = elementIdNode.getNodeValue();
        } else {
            throw new ProcedureParseException("LogicBase no id for NodeName " 
            		+ node.getNodeName());
        }            
        Node valueNode = attributes.getNamedItem("value");
        String value = "";
        if(valueNode != null) {                
            value = valueNode.getNodeValue();
        } else {
            throw new ProcedureParseException("LogicBase no value for NodeName " 
            		+ node.getNodeName());
        }
        ProcedureElement elt = elts.get(elementId);
        if (elt == null)
            throw new ProcedureParseException("LogicBase cannot resolve element"
            		+"#" + elementId);
        return new LogicBase(new Criterion(critType, elt, value));
    }
}    

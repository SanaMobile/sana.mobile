/**
 * 
 */
package org.sana.core;

import org.sana.api.IRelationship;
import org.sana.api.RelationshipCategory;


/**
 * A classification of the relationship between two Concepts. Uniqueness must 
 * be enforced as the combination of source Concept, target Concept, and 
 * RelationshipCategory must be unique together.
 * 
 * @author Sana Development
 *
 */
public class Relationship extends Model implements IRelationship{

	private Concept fromConcept;
	private Concept toConcept;
	private RelationshipCategory category;
	
	
	/** Default Constructor */
	public Relationship(){}
	
	/* (non-Javadoc)
	 * @see org.sana.core.IRelationship#getFromConcept()
	 */
	@Override
	public Concept getFromConcept() {
		return fromConcept;
	}

	/**
	 * @param fromConcept the fromConcept to set
	 */
	public void setFromConcept(Concept fromConcept) {
		this.fromConcept = fromConcept;
	}

	/* (non-Javadoc)
	 * @see org.sana.core.IRelationship#getToConcept()
	 */
	@Override
	public Concept getToConcept() {
		return toConcept;
	}

	/**
	 * @param toConcept the toConcept to set
	 */
	public void setToConcept(Concept toConcept) {
		this.toConcept = toConcept;
	}

	/* (non-Javadoc)
	 * @see org.sana.core.IRelationship#getCategory()
	 */
	@Override
	public RelationshipCategory getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(RelationshipCategory category) {
		this.category = category;
	}
	
}

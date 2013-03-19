/**
 * 
 */
package org.sana;

/**
 * A classification of the relationship between two Concepts. Uniqueness must 
 * be enforced as the combination of source Concept, target Concept, and 
 * RelationshipCategory must be unique together.
 * 
 * @author Sana Development
 *
 */
public class Relationship extends AbstractModel{

	private Concept fromConcept;
	private Concept toConcept;
	private RelationshipCategory category;
	
	
	/** Default Constructor */
	public Relationship(){}
	
	/**
	 * @return the fromConcept
	 */
	public Concept getFromConcept() {
		return fromConcept;
	}

	/**
	 * @param fromConcept the fromConcept to set
	 */
	public void setFromConcept(Concept fromConcept) {
		this.fromConcept = fromConcept;
	}

	/**
	 * @return the toConcept
	 */
	public Concept getToConcept() {
		return toConcept;
	}

	/**
	 * @param toConcept the toConcept to set
	 */
	public void setToConcept(Concept toConcept) {
		this.toConcept = toConcept;
	}

	/**
	 * @return the category
	 */
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

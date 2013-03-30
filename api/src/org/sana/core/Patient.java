/**
 * 
 */
package org.sana.core;

import java.util.Date;

import org.sana.api.IPatient;


/**
 * A subject in the medical domain.
 * 
 * @author Sana Development
 *
 */
public class Patient extends Subject implements IPatient{

	/* (non-Javadoc)
	 * @see org.sana.core.IPatient#getGiven_name()
	 */
	@Override
	public String getGiven_name() {
		return given_name;
	}
	/**
	 * @param given_name the given_name to set
	 */
	public void setGiven_name(String given_name) {
		this.given_name = given_name;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IPatient#getFamily_name()
	 */
	@Override
	public String getFamily_name() {
		return family_name;
	}
	/**
	 * @param family_name the family_name to set
	 */
	public void setFamily_name(String family_name) {
		this.family_name = family_name;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IPatient#getDob()
	 */
	@Override
	public Date getDob() {
		return dob;
	}
	/**
	 * @param dob the dob to set
	 */
	public void setDob(Date dob) {
		this.dob = dob;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IPatient#getGender()
	 */
	@Override
	public String getGender() {
		return gender;
	}
	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IPatient#getImage()
	 */
	@Override
	public String getImage() {
		return image;
	}
	/**
	 * @param image the image to set
	 */
	public void setImage(String image) {
		this.image = image;
	}
	
	private String given_name;
	private String family_name;
	private Date dob;
	private String gender;
	private String image;
	
	
}

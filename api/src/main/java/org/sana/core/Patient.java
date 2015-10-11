/**
 * 
 */
package org.sana.core;

import java.net.URI;
import java.util.Date;

import org.sana.api.ILocation;
import org.sana.api.IPatient;

import com.google.gson.annotations.Expose;


/**
 * A subject in the medical domain.
 * 
 * @author Sana Development
 *
 */
public class Patient extends Subject implements IPatient{

    @Expose
    public String given_name;
    @Expose
    public String family_name;
    @Expose
    public Date dob;
    @Expose
    public String gender;
    @Expose
    public URI image;
    @Expose
    public Location location;
    @Expose
    public String system_id;
	@Expose
	boolean confirmed = true;
    @Expose
    boolean dobEstimated = false;

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
	public URI getImage() {
		return image;
	}
	/**
	 * @param image the image to set
	 */
	public void setImage(URI image) {
		this.image = image;
	}
	
	/* (non-Javadoc)
	 * @see org.sana.api.IPatient#getLocation()
	 */
	@Override
	public ILocation getLocation() {
		return location;
	}
	
	public void setLocation(Location location){
		this.location = location;
	}


    public String getSystemId(){
        return system_id;
    }

    public void setSystemId(String systemId){
        this.system_id = systemId;
    }

	public boolean getConfirmed(){
		return confirmed;
	}

	public void setConfirmed(boolean confirmed){
		this.confirmed = confirmed;
	}

    public boolean isDobEstimated() {
        return dobEstimated;
    }

    public void setDobEstimated(boolean dobEstimated) {
        this.dobEstimated = dobEstimated;
    }
}

package org.sana.android.db;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * A wrapper around patient information.
 * 
 * @author Sana Development Team
 *
 */
public class PatientInfo implements Parcelable {
	private static final String TAG = PatientInfo.class.toString();
	
	boolean isConfirmed = false;
	private String patientIdentifier = "";
	private String patientFirstName = "";
	private String patientLastName = "";
	private String patientGender = "";
	private Date patientBirthdate = new Date();
	
	/**
	 * Default Constructor
	 */
	public PatientInfo() {
		Log.v(TAG, "PatientInfo()");
	}
	
	/**
	 * Constructs a new PatientInfo object from a parcel
	 * @param p the parcel to read from.
	 */
	private PatientInfo(Parcel p) {
		Log.v(TAG, "PatientInfo(Parcel)");
		readFromParcel(p);
	}
	
	/**
	 * Gets the value of an attribute by name.
	 * @param id the name of the attribute.
	 * @return the value or an empty string.
	 */
	public String getAnswerForId(String id) {
		if ("patientGender".equals(id))
			return patientGender;
		else if ("patientFirstName".equals(id))
			return patientFirstName;
		else if ("patientLastName".equals(id))
			return patientLastName;
		else if ("patientBirthdateYear".equals(id) && patientBirthdate != null)
			return Integer.toString(patientBirthdate.getYear());
		else if ("patientBirthdateMonth".equals(id) && patientBirthdate != null)
			return Integer.toString(patientBirthdate.getMonth());
		else if ("patientBirthdateDay".equals(id) && patientBirthdate != null)
			return Integer.toString(patientBirthdate.getDay());
		return "";
	}
	
	/**
	 * True if this patient identifier is registered with the emr.
	 * @return
	 */
	public boolean isConfirmed() {
		return isConfirmed;
	}
	
	/**
	 * Updates the confirmation status.
	 * @param confirmed the new status.
	 */
	public void setConfirmed(boolean confirmed) {
		isConfirmed = confirmed;
	}

	/**
	 * Gets the patient identifier.
	 * @return an identifier.
	 */
	public String getPatientIdentifier() {
		return patientIdentifier;
	}
	
	/**
	 * Updates the identifier.
	 * @param patientIdentifier the new value.
	 */
	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
	
	/**
	 * Gets the gender.
	 * @return
	 */
	public String getPatientGender() {
		return patientGender;
	}
	
	/**
	 * Updates the gender.
	 * @param patientGender the new value.
	 */
	public void setPatientGender(String patientGender) {
		this.patientGender = patientGender;
	}
	
	/**
	 * Get the given name.
	 * @return
	 */
	public String getPatientFirstName() {
		return patientFirstName;
	}
	
	/**
	 * Updates the given name.
	 * @param patientFirstName the new value.
	 */
	public void setPatientFirstName(String patientFirstName) {
		this.patientFirstName = patientFirstName;
	}
	
	/**
	 * Gets the family name.
	 * @return 
	 */
	public String getPatientLastName() {
		return patientLastName;
	}
	
	/**
	 * Updates the family name.
	 * @param patientLastName the new value.
	 */
	public void setPatientLastName(String patientLastName) {
		this.patientLastName = patientLastName;
	}
	
	/**
	 * Gets the birthdate.
	 * @return
	 */
	public Date getPatientBirthdate() {
		return patientBirthdate;
	}
	
	/**
	 * Updates the birthdate.
	 * @param patientBirthdate the new value.
	 */
	public void setPatientBirthdate(Date patientBirthdate) {
		this.patientBirthdate = patientBirthdate;
	}

	/** {@inheritDoc} */
	@Override
	public int describeContents() {
		return 0;
	}
	
	/** Required for the Parcelable interface. */
	public static final Parcelable.Creator<PatientInfo> CREATOR = new 
		Parcelable.Creator<PatientInfo>() {

		/** {@inheritDoc} */
		@Override
		public PatientInfo createFromParcel(Parcel source) {
			return new PatientInfo(source);
		}

		/** {@inheritDoc} */
		@Override
		public PatientInfo[] newArray(int size) {
			return new PatientInfo[size];
		}
	};
	
	private void readFromParcel(Parcel p) {
		Log.v(TAG, "readFromParcel");

		try {
			boolean[] confirmedArray = p.createBooleanArray();
			isConfirmed = confirmedArray[0];
			patientIdentifier = p.readString();
			patientFirstName = p.readString();
			patientLastName = p.readString();
			patientGender = p.readString();
			patientBirthdate = new Date(p.readString());
		} catch (Exception e) {
			Log.e(TAG, "While reading PatientInfo from Parcel, got exception: " 
					+ e.toString());
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Log.v(TAG, "writeToParcel");
		dest.writeBooleanArray(new boolean[] {isConfirmed});
		dest.writeString(patientIdentifier);
		dest.writeString(patientFirstName);
		dest.writeString(patientLastName);
		dest.writeString(patientGender);
		dest.writeString(patientBirthdate.toString());
	}
	
}

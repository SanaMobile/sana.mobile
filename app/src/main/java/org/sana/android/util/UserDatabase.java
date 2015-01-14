package org.sana.android.util;
import java.util.Date;

import org.sana.android.db.PatientInfo;
import org.sana.android.provider.Patients;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Utilities for handling the user(patient) database
 * 
 * @author Sana Development Team
 *
 */
public class UserDatabase {

    public static final String TAG = UserDatabase.class.toString();
    
    private static final String[] PROJECTION = new String[] {
    	Patients.Contract.DOB,
		Patients.Contract.GIVEN_NAME, 
		Patients.Contract.FAMILY_NAME, 
		Patients.Contract.GENDER,
		Patients.Contract.PATIENT_ID};
    
    /**
     * Inserts data for a list of users.
     * 
     * @param cr resolves the ContentProvider
     * @param users a list of users as '##' separated values 
     */
	public static void addDataToUsers(ContentResolver cr, String users) {
		users = users.trim();
		ContentValues newuser = new ContentValues();

		String[] data = users.split("##");
		
		for (String record : data) {
			record = record.trim();
			if ("".equals(record)) {
				continue;
			}
			Log.i(TAG, "Processing:" + record);
			try {
				String gender = record.substring(record.length() - 1);
				record = record.substring(0, record.length() - 1);
				String[] findname = record.split("[0-9]+");
				String firstname = findname[0];
				String lastname = findname[1];
				String[] findrest = record.split("[A-Za-z]+");
				int birthdate = 0;
				try {
					birthdate = Integer.parseInt(findrest[1]);
				} catch (Exception e) {

				}
				String id = findrest[2];

				Log.i(TAG, "firstname is " + firstname);
				Log.i(TAG, "lastname is " + lastname);
				Log.i(TAG, "birthdate is " + birthdate);
				Log.i(TAG, "gender is " + gender);
				Log.i(TAG, "id is " + id);

				// add new user to database
				newuser.put(Patients.Contract.GIVEN_NAME, firstname);
				newuser.put(Patients.Contract.FAMILY_NAME, lastname);
				newuser.put(Patients.Contract.DOB, birthdate);
				newuser.put(Patients.Contract.PATIENT_ID, id);
				newuser.put(Patients.Contract.GENDER, gender);
				int count = 0;
				Cursor c = null;
				try{
					c= cr.query(Patients.CONTENT_URI, 
						new String[]{ BaseColumns._ID },
						Patients.Contract.PATIENT_ID + " = ?" , 
						new String[]{ id }, null);
					count = ((c != null) && (c.moveToFirst()))? c.getInt(0):0;
				} finally {
					if(c != null) c.close();
				}
				if(count == 0) {
					cr.insert(Patients.CONTENT_URI, newuser);
					Log.i(TAG, "added new patient to database");
				} else {
					cr.update(ContentUris.withAppendedId(Patients.CONTENT_URI, count),
							newuser, null,null);
					Log.i(TAG, "updated patient in database: " + id);
				}
				newuser.clear();
			} catch (Exception e) {
				Log.i(TAG, "Exception while processing:" + record + " : "
						+ e.toString());
			}
		}
	}
	
	private static Date dateFromString(String dumbBirthdate) {
		try {
			String year = dumbBirthdate.substring(0, 4);
			String month = dumbBirthdate.substring(4, 6);
			String day = dumbBirthdate.substring(6, 8);
			
			int iYear = Integer.parseInt(year);
			int iMonth = Integer.parseInt(month);
			int iDay = Integer.parseInt(day);
			
			return new Date(iYear, iMonth, iDay);
		} catch (NumberFormatException e) {
			Log.e(TAG, "Could not parse birthdate from \"" + dumbBirthdate 
					+ "\"");
		}
		return null;
	}
	
	private static String capitalize(String name) {
		if (name.length() == 1) {
			return name.toUpperCase();
		} else if (name.length() > 1) {
			return name.substring(0, 1).toUpperCase() 
				+ name.substring(1).toLowerCase();
		}
		return name;
	}
	
	/**
	 * Retrieves a patient record form the database.
	 * 
	 * @param patientIdentifier the patient identifier to look up.
	 * @param record patient information to use for looking up the patient.
	 * @return a patient as a PatientInfo object.
	 */
	public static PatientInfo getPatientFromMDSRecord(String patientIdentifier, 
			String record) 
	{
		PatientInfo pi = new PatientInfo();
		
		record = record.trim();

		// THIS IS DISGUSTING, DIE DIE DIE
		String gender = record.substring(record.length()-1);
		record = record.substring(0, record.length()-1);
		String[] findname = record.split("[0-9]+");
		String firstname = findname[0];
		String lastname = findname[1];
		String[] findrest = record.split("[A-Za-z]+");
		
		int birthdate = 0;
		try {
			birthdate = Integer.parseInt(findrest[1]);
		} catch (Exception e) {
		}
		
		String birthdayStr = String.valueOf(birthdate);
		Date d = dateFromString(birthdayStr);
		pi.setPatientBirthdate(d);
		pi.setPatientIdentifier(patientIdentifier);
		pi.setPatientFirstName(capitalize(firstname));
		pi.setPatientLastName(capitalize(lastname));
		pi.setPatientGender("m".equals(gender) ? "Male" : "Female");
		pi.setConfirmed(true);
		return pi;
	}
	
	/**
	 * Populates a patient info object from an active cursor
	 * @param cursor a cursor holding a window to patient data
	 * @param patientId the patient identifier
	 * @param pi a PatientInfo container to hold data
	 */
	public static void populateUsingDatabase(Cursor cursor, String patientId, 
			PatientInfo pi) {
		int firstnameind = cursor.getColumnIndex("patient_firstname");
		int lastnameind = cursor.getColumnIndex("patient_lastname");
		int birthdateind = cursor.getColumnIndex("patient_dob");
		int genderind = cursor.getColumnIndex("patient_gender");

		cursor.moveToFirst();

		pi.setPatientIdentifier(patientId);
		String gender = cursor.getString(genderind).trim().toLowerCase();
		Date d = dateFromString(
				cursor.getString(birthdateind).trim().toLowerCase());
		pi.setPatientBirthdate(d);
		
		pi.setPatientFirstName(capitalize(
				cursor.getString(firstnameind).trim().toLowerCase()));
		pi.setPatientLastName(capitalize(
				cursor.getString(lastnameind).trim().toLowerCase()));
		
		pi.setPatientGender("m".equals(gender) ? "Male" : "Female");
		pi.setConfirmed(true);
	}
	
	//checks cached database on phone for patient information
	public static PatientInfo getPatientFromLocalDatabase(Context c, String id) {
		// TODO SQL escaping
		
		Cursor cursor = null;
		try {
			cursor = c.getContentResolver().query(Patients.CONTENT_URI, 
					PROJECTION, "(patient_id=\""+id+"\")", null, null);
			
			if (cursor.getCount() > 0) {
				PatientInfo pi = new PatientInfo();
				populateUsingDatabase(cursor, id, pi);
				return pi;
			}
			
		} catch (Exception e) {
			
		} finally {
			if (cursor != null)
				cursor.close();
		}
		
		return null;
	}

}

package org.sana.android.service;

import org.sana.android.content.core;

// Provides RESTful access to the patients.
interface IPatientService{

	Uri create(in core.PatientParcel t);
	
	core.PatientParcel read(in ParcelUuid uuid);
	
	boolean update(in core.PatientParcel t);
	
	boolean delete(in ParcelUuid uuid);

}
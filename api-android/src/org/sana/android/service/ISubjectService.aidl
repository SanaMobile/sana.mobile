package org.sana.android.service;

import org.sana.android.content.core;

interface ISubjectService{

	Uri create(in core.SubjectParcel t);
	
	core.SubjectParcel read(in ParcelUuid uuid);
	
	boolean update(in core.SubjectParcel t);
	
	boolean delete(in ParcelUuid uuid);

}
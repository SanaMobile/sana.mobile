package org.sana.android.service;

import org.sana.android.content.core;

interface IConceptService{

	Uri create(in core.ConceptParcel t);
	
	core.ConceptParcel read(in ParcelUuid uuid);
	
	boolean update(in core.ConceptParcel t);
	
	boolean delete(in ParcelUuid uuid);

}
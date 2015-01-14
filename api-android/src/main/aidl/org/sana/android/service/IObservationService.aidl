package org.sana.android.service;

import org.sana.android.content.core;

interface IObservationService{

	Uri create(in core.ObservationParcel t);
	
	core.ObservationParcel read(in ParcelUuid uuid);
	
	boolean update(in core.ObservationParcel t);
	
	boolean delete(in ParcelUuid uuid);

}
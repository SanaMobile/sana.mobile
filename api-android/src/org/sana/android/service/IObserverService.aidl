package org.sana.android.service;

import org.sana.android.content.core;

interface IObserverService{

	Uri create(in core.ObserverParcel t);
	
	core.ObserverParcel read(in ParcelUuid uuid);
	
	boolean update(in core.ObserverParcel t);
	
	boolean delete(in ParcelUuid uuid);

}
package org.sana.android.service;

import org.sana.android.content.core;

interface IEventService{

	Uri create(in core.EventParcel t);
	
	core.EventParcel read(in ParcelUuid uuid);
	
	boolean update(in core.EventParcel t);
	
	boolean delete(in ParcelUuid uuid);

}
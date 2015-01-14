package org.sana.android.service;

import org.sana.android.content.core;

interface IEncounterService{

	Uri create(in core.EncounterParcel t);
	
	core.EncounterParcel read(in ParcelUuid uuid);
	
	boolean update(in core.EncounterParcel t);
	
	boolean delete(in ParcelUuid uuid);

}
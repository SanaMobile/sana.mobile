package org.sana.android.service;

import org.sana.android.content.core;

interface IModelService{

	Uri create(in core.ModelParcel t);
	
	core.ModelParcel read(in ParcelUuid uuid);
	
	boolean update(in core.ModelParcel t);
	
	boolean delete(in ParcelUuid uuid);

}
package org.sana.android.service;

import org.sana.android.content.core;

interface IProcedureService{

	Uri create(in core.ProcedureParcel t);
	
	core.ProcedureParcel read(in ParcelUuid uuid);
	
	boolean update(in core.ProcedureParcel t);
	
	boolean delete(in ParcelUuid uuid);

}
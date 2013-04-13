package org.sana.android.service;

import org.sana.android.content.core;

interface INotificationService{

	Uri create(in core.NotificationParcel t);
	
	core.NotificationParcel read(in ParcelUuid uuid);
	
	boolean update(in core.NotificationParcel t);
	
	boolean delete(in ParcelUuid uuid);

}
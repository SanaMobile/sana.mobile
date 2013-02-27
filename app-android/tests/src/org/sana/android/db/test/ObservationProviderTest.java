package org.sana.android.db.test;


import org.sana.android.db.ObservationProvider;
import org.sana.android.provider.Observations;

import android.test.ProviderTestCase2;

public class ObservationProviderTest extends ProviderTestCase2<ObservationProvider>{

	public ObservationProviderTest() {
		super(ObservationProvider.class, Observations.AUTHORITY);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void setUp(){}
	
	@Override
	protected void tearDown(){}
	
	public void testInsert(){}
	
	public void testUpdate(){}
	
	public void testQuery(){}
	
	public void testDelete(){}
	
	public void testInsertFail(){}
	
	public void testUpdateFail(){}
	
	public void testQueryFail(){}
	
	public void testDeleteFail(){}

}

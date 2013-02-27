package org.sana.android.db.test;

import org.sana.android.db.ConceptProvider;
import org.sana.android.provider.Concepts;

import android.test.ProviderTestCase2;

public class ConceptProviderTest extends ProviderTestCase2<ConceptProvider>{

	public ConceptProviderTest() {
		super(ConceptProvider.class, Concepts.AUTHORITY);
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

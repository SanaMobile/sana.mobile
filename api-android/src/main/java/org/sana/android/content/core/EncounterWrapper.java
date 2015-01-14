/**
 * 
 */
package org.sana.android.content.core;

import org.sana.android.db.ModelWrapper;
import org.sana.android.provider.Concepts;
import org.sana.android.provider.Encounters;
import org.sana.api.IConcept;
import org.sana.api.IEncounter;
import org.sana.api.IProcedure;
import org.sana.api.ISubject;
import org.sana.core.Encounter;
import org.sana.core.Observer;
import org.sana.core.Procedure;
import org.sana.core.Subject;

import android.content.ContentResolver;
import android.database.Cursor;

/**
 * @author ewinkler
 *
 */
public class EncounterWrapper extends ModelWrapper<IEncounter> implements IEncounter {

	public EncounterWrapper(Cursor cursor) {
		super(cursor);
	}

	@Override
	public ISubject getSubject() {
		Subject s = new Subject();
		s.setUuid(getString(getColumnIndex(Encounters.Contract.SUBJECT)));
		return s;
	}

	@Override
	public IProcedure getProcedure() {
		return new Procedure(getString(getColumnIndex(Encounters.Contract.PROCEDURE)));
	}

	@Override
	public Observer getObserver() {
		return new Observer(getString(getColumnIndex(Encounters.Contract.PROCEDURE)));
	}

	@Override
	public IEncounter getObject() {
		Encounter e = new Encounter();
		e.created = getCreated();
		e.modified = getModified();
		e.subject = (Subject) getSubject();
		e.observer = getObserver();
		e.procedure = (Procedure) getProcedure();
		return e;
	}

	public static IEncounter getOneByUuid(ContentResolver resolver, String uuid){
		EncounterWrapper wrapper = new EncounterWrapper(ModelWrapper.getOneByUuid(
				Concepts.CONTENT_URI,resolver, uuid));
		IEncounter object = null;
		if(wrapper != null)
		try{ 
			object = wrapper.next();
		} finally {
			wrapper.close();
		}
		return object;
	}
	
}

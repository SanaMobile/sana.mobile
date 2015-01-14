/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sana nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.android.fragment;

import org.sana.R;
import org.sana.android.content.Intents;
import org.sana.android.db.ModelWrapper;
import org.sana.android.db.SanaDB.ImageSQLFormat;
import org.sana.android.provider.Observations;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author Sana Development
 *
 */
public class ObservationListFragment extends ListFragment implements
LoaderManager.LoaderCallbacks<Cursor>
{

	public static class ObservationCursorAdapter extends SimpleCursorAdapter{

		static final String[] from = new String[] {
			Observations.Contract.ID,
			Observations.Contract.CONCEPT,
			Observations.Contract.VALUE };
		
		static final int[] to = 	new int[] { 
			R.id.text_question_id_value, 
			R.id.text_question_concept_value, 
			R.id.text_response_value };

		/**
		 * @param context
		 * @param layout
		 * @param c
		 * @param from
		 * @param to
		 * @param flags
		 */
		public ObservationCursorAdapter(Activity context, Cursor c,
				int flags){
			super(context, R.layout.list_observation_item, c, from, to, flags);
			mContext = context;
		}

		private final Activity mContext;

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			String value = cursor.getString(cursor.getColumnIndex(Observations.Contract.VALUE));
			String id = cursor.getString(cursor.getColumnIndex(Observations.Contract.ID));
			String description = cursor.getString(cursor.getColumnIndex(Observations.Contract.CONCEPT));
			// question id
			TextView question = (TextView) view.findViewById(R.id.text_question_id_value);
			question.setText(id);

			// concept descriptor
			TextView concept = (TextView) view.findViewById(R.id.text_question_concept_value);
			concept.setText(description);

			// String or image response
			TextView txt = (TextView) view.findViewById(R.id.text_response_value);
			ImageView img = (ImageView) view.findViewById(R.id.image_response_value);

			if(description.compareToIgnoreCase(("SX SITE IMAGE")) == 0){
				txt.setVisibility(View.GONE);
				img.setVisibility(View.VISIBLE);
				String[] images = (!TextUtils.isEmpty(value))? value.split(","): new String[0];
				// Only show the first one here
				if(images.length >= 1){
					Uri image = ContentUris.withAppendedId(ImageSQLFormat.CONTENT_URI, Long.valueOf(images[0]));
					img.setImageURI(image);
					img.setTag(image);
				}
			} else {
				img.setVisibility(View.GONE);
				txt.setVisibility(View.VISIBLE);
				txt.setText(value);
			}

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
			View view = mContext.getLayoutInflater().inflate(R.layout.list_observation_item, null);
			return view;
		}
	}
	private static final String[] PROJECTION = {
		Observations.Contract._ID,
		Observations.Contract.PARENT,
		Observations.Contract.ID,
		Observations.Contract.CONCEPT,
		Observations.Contract.VALUE };

	private static final String selection = Observations.Contract.ENCOUNTER
			+ "= ?";
	private static final int URL_LOADER = 0;
	ObservationCursorAdapter mAdapter; 
	private Uri mEncounter = null;

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Intent i = getActivity().getIntent();
		if(i != null)
			mEncounter = i.getData();
		if(mEncounter == null){
			mEncounter = i.getParcelableExtra(Intents.EXTRA_ENCOUNTER);
		}
		mAdapter = new ObservationCursorAdapter( getActivity(), null, 0 );
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(URL_LOADER, null, this);

	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader loader = new CursorLoader(getActivity(),
				Observations.CONTENT_URI, 
				PROJECTION, 
				selection, 
				getSelectionArgs(),
				Observations.Contract.CREATED + " ASC");
		return loader;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
	 */
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case URL_LOADER:
			mAdapter.swapCursor(cursor);
			break;
		}

	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
	 */
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	public String[] getSelectionArgs(){
		String[] selectionArgs = (mEncounter != null)? 
				new String[]{ ModelWrapper.getUuid(mEncounter, getActivity().getContentResolver()) } : null;
				return selectionArgs;
	}

	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Assume it is complex if the thumbnail is visible for now
		View thumb = v.findViewById(R.id.image_response_value);
		if(thumb != null && thumb.getVisibility() != View.GONE){
			Uri uri = (Uri) thumb.getTag();
			String type = getActivity().getContentResolver().getType(uri);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uri, "image/*");
			startActivity(intent);
		}
	}

}

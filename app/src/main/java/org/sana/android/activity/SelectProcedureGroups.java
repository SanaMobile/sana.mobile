/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Sana nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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
package org.sana.android.activity;


import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.sana.R;
import org.sana.android.provider.ProcedureGroups;
import org.sana.android.task.FetchProcedureGroupsTask;
import org.sana.android.task.ProcedureGroupSyncTask;
import org.sana.core.ProcedureGroup;

import java.util.List;

/**
 * A list activity that lets users initiate a sync for procedure groups.
 *
 * @author Sana Development
 *
 */
public class SelectProcedureGroups extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_procedure_groups);

        new FetchProcedureGroupsTask(this, new FetchProcedureGroupsTask.FetchProcedureGroupsCallback() {
            @Override
            public void onProcedureGroupsLoaded(List<ProcedureGroup> procedureGroups) {
                SelectProcedureGroupsAdapter adapter = new SelectProcedureGroupsAdapter(SelectProcedureGroups.this, procedureGroups);
                setListAdapter(adapter);
            }
        }).execute();
    }

    private static class SelectProcedureGroupsAdapter extends ArrayAdapter<ProcedureGroup> {
        private Context mContext;
        private List<ProcedureGroup> mProcedureGroups;

        public SelectProcedureGroupsAdapter(Context context, List<ProcedureGroup> procedureGroups) {
            super(context, 0, procedureGroups);
            mContext = context;
            mProcedureGroups = procedureGroups;
        }

        @Override
        public View getView(int position, View procedureItem, ViewGroup parent) {
            if (procedureItem == null) {
                procedureItem = LayoutInflater.from(mContext).inflate(R.layout.procedure_group_item, parent, false);
            }

            final ProcedureGroup procedureGroup = mProcedureGroups.get(position);

            TextView textTitle = (TextView) procedureItem.findViewById(R.id.text_title);
            textTitle.setText(procedureGroup.getTitle());

            TextView textAuthor = (TextView) procedureItem.findViewById(R.id.text_author);
            textAuthor.setText(procedureGroup.getAuthor());

            TextView textNumProcedures = (TextView) procedureItem.findViewById(R.id.text_num_procedures);
            int numProcedures = procedureGroup.getProcedureNames().size();
            textNumProcedures.setText(String.format(getContext().getString(R.string.procedure_group_num_procedures), numProcedures));

            Button syncButton = (Button) procedureItem.findViewById(R.id.button_sync_procedure_group);
            syncButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    insertProcedureGroup(procedureGroup);

                    // Sync right away to get the new procedures.
                    ProcedureGroupSyncTask procedureGroupSyncTask = new ProcedureGroupSyncTask(getContext());
                    procedureGroupSyncTask.execute();
                }
            });

            return procedureItem;
        }

        private void insertProcedureGroup(ProcedureGroup procedureGroup) {
            final ContentValues cv = new ContentValues();
            cv.put(ProcedureGroups.Contract.UUID, procedureGroup.getUuid());
            cv.put(ProcedureGroups.Contract.TITLE, procedureGroup.getTitle());
            cv.put(ProcedureGroups.Contract.AUTHOR, procedureGroup.getAuthor());
            cv.put(ProcedureGroups.Contract.DESCRIPTION, procedureGroup.getDescription());
            cv.put(ProcedureGroups.Contract.PROCEDURE_NAMES, TextUtils.join(",", procedureGroup.getProcedureNames()));

            int numUpdates = getContext().getContentResolver().update(ProcedureGroups.CONTENT_URI, cv, "uuid = ?", new String[] {procedureGroup.getUuid()});
            if (numUpdates == 0) {
                getContext().getContentResolver().insert(ProcedureGroups.CONTENT_URI, cv);
            }
        }
    }
}

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
package org.sana.android.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import org.sana.R;
import org.sana.android.net.MDSInterface2;
import org.sana.android.provider.ProcedureGroups;
import org.sana.android.provider.Procedures;
import org.sana.android.util.SanaUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Pulls the procedures of procedure groups.
 *
 * @author Sana Development Team
 */
public class ProcedureGroupSyncTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = ProcedureGroupSyncTask.class.getSimpleName();

    private ProgressDialog mProgressDialog;
    private Context mContext;

    public ProcedureGroupSyncTask(Context context) {
        mContext = context;
    }

    private static final String[] PROCEDURE_GROUP_PROJECTION = new String[] {
            ProcedureGroups.Contract.UUID,
            ProcedureGroups.Contract.PROCEDURE_NAMES
    };

    private static final String[] PROCEDURE_PROJECTION = new String[] {
            Procedures.Contract.TITLE,
            Procedures.Contract.VERSION,
    };

    @Override
    protected void onPreExecute() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.procedure_group_sync_loading_label));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Log.i(TAG, "Syncing procedure groups");

        Cursor procedureGroupsCursor = mContext.getContentResolver().query(
                ProcedureGroups.CONTENT_URI,
                PROCEDURE_GROUP_PROJECTION,
                null,
                null,
                null
        );

        while (procedureGroupsCursor.moveToNext()) {
            String groupId = procedureGroupsCursor.getString(procedureGroupsCursor.getColumnIndex(ProcedureGroups.Contract.UUID));
            String[] procedureNames = procedureGroupsCursor.getString(procedureGroupsCursor.getColumnIndex(ProcedureGroups.Contract.PROCEDURE_NAMES)).split(",");

            List<String> procedureTitles = new ArrayList();
            List<String> procedureVersions = new ArrayList();
            for (String procedureName: procedureNames) {
                Cursor proceduresCursor = mContext.getContentResolver().query(
                        Procedures.CONTENT_URI,
                        PROCEDURE_PROJECTION,
                        "title = ?",
                        new String[] {procedureName},
                        null
                );
                if (proceduresCursor.moveToFirst()) {
                    String title = proceduresCursor.getString(proceduresCursor.getColumnIndex(Procedures.Contract.TITLE));
                    String version = proceduresCursor.getString(proceduresCursor.getColumnIndex(Procedures.Contract.VERSION));
                    procedureTitles.add(title);
                    procedureVersions.add(version);
                }

                List<String> procedureXMLs = MDSInterface2.getProcedureXMLsForGroup(mContext, groupId, procedureTitles, procedureVersions);
                for (String procedureXML: procedureXMLs) {
                    SanaUtil.insertProcedureFromXML(mContext, procedureXML);
                }
            }
        }

        return 0;
    }


    @Override
    protected void onPostExecute(Integer result) {
        Log.i(TAG, "Completed sync of procedure groups");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
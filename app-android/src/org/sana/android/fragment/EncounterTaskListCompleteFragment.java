
package org.sana.android.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.sana.R;
import org.sana.android.app.Locales;
import org.sana.android.content.DispatchResponseReceiver;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.content.core.PatientWrapper;
import org.sana.android.db.ModelWrapper;
import org.sana.android.provider.EncounterTasks;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.EncounterTasks.Contract;
import org.sana.android.provider.Subjects;
import org.sana.android.service.impl.DispatchService;
import org.sana.android.util.Bitmaps;
import org.sana.android.util.Logf;
import org.sana.api.IModel;
import org.sana.api.task.EncounterTask;

import org.sana.util.StringUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment displaying all patients.
 * 
 * @author Sana Development Team
 */
public class EncounterTaskListCompleteFragment extends EncounterTaskListFragment{
    public static final String TAG = EncounterTaskListCompleteFragment.class.getSimpleName();


	//TODO try reading from intent
	@Override
    public String getSelectedStatus(){
    	return "Completed";
    }
}

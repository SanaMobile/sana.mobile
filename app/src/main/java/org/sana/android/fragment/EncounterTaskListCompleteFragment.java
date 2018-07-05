
package org.sana.android.fragment;

/**
 * Fragment displaying all patients.
 *
 * @author Sana Development Team
 */
public class EncounterTaskListCompleteFragment extends EncounterTaskListFragment {
    public static final String TAG = EncounterTaskListCompleteFragment.class.getSimpleName();


    //TODO try reading from intent
    @Override
    public String getSelectedStatus() {
        return "Completed";
    }
}

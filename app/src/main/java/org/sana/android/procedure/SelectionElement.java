package org.sana.android.procedure;

import java.util.LinkedHashMap;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;

/**
 * SelectElement is a ProcedureElement that creates a question and provides a
 * selection of one or more choices which will be stored as presented or to a
 * mapped value.
 * <p/>
 *
 * @author Sana Development Team
 */
public abstract class SelectionElement extends ProcedureElement {
    protected static final String TAG = SelectionElement.class.getSimpleName();
    public static final String TOKEN_DELIMITER = ";";

    protected String[] labels;
    protected String[] values;
    protected View mView;
    protected Adapter mAdapter;

    private LinkedHashMap<String,String> valueToLabelMap;
    private LinkedHashMap<String,String> labelToValueMap;

    protected String[] labels() {
        String[] arr = new String[labelToValueMap.keySet().size()];
        int index = 0;
        for(String key: labelToValueMap.keySet()){
            arr[index] = key;
            index++;
        }
        //labelToValueMap.keySet().toArray(arr);
        return arr;
    }

    /**
     * Returns a String array of the choice values.
     *
     * @return the value array
     */
    protected String[] values() {
        String[] arr = new String[valueToLabelMap.keySet().size()];
        labelToValueMap.keySet().toArray(arr);
        return arr;
    }

    /**
     * Appends the <code>choices</code> and <code>values</code> attributes.
     *
     * @param sb The StringBuilder to append to.
     */
    @Override
    protected void appendOptionalAttributes(StringBuilder sb){
        sb.append("\" choices=\"" + TextUtils.join(TOKEN_DELIMITER, labels));
        sb.append("\" values=\"" + TextUtils.join(TOKEN_DELIMITER, values));
    }

    /**
     * Generates the bidirectional mappings for the <code>choices</code> and
     * <code>values</code>
     *
     * @param values The persisted representation of the selectable options
     * @param choices The visible labels for the allowed selections
     */
    protected void mapValues(String[] values, String[] choices) {
        Log.i(TAG, "["+id+"]mapValues() " + values.length +"," +
                "" + choices.length);
        for(int i = 0;i < choices.length;i++) {
            valueToLabelMap.put(values[i], choices[i]);
            labelToValueMap.put(choices[i], values[i]);
        }
    }

    /**
     * Gets the persisted value from the selected label.
     *
     * @param answer The label to look up the value from
     * @return The persisted value.
     */
    protected String getValueFromLabel(String answer) {
        Log.i(TAG,"["  + id +"]getValueFromLabel()");
        String value = labelToValueMap.get(answer);
        Log.d(TAG,"...["  + id +"]value " + value);
        return (value == null)?"":value;
    }

    protected String getLabelFromValue(String answer) {
        Log.i(TAG,"["  + id +"]getLabelFromValue() --> " + answer);
        String choice = valueToLabelMap.get(answer);
        Log.d(TAG,"...["  + id +"] choice = " + choice);
        return (choice == null)?"":choice;
    }

    /**
     * Selection element with no value mapped choices. The choices will be
     * used as the values.
     *
     * @param id The element id within the
     * {@link org.sana.android.procedure.Procedure Procedure}
     * @param question The prompt displayed to the user
     * @param answer The default value
     * @param concept The Concept which provides context for the data
     * @param figure An image resource id
     * @param audio An audio prompt to play
     * @param choices The allowed selections
     */
    protected SelectionElement(String id, String question, String answer,
                               String concept, String figure, String audio, String[] choices)
    {
        this(id,question,answer, concept, figure, audio,choices, choices);
    }

    /**
     * Selection element with value mapped choices. The choices will be
     * used as the values.
     *
     * @param id The element id within the
     * {@link org.sana.android.procedure.Procedure Procedure}
     *
     * @param question The prompt displayed to the user
     * @param answer The default value
     * @param concept The Concept which provides context for the data
     * @param figure An image resource id
     * @param audio An audio prompt to play
     * @param labels The visible labels for the allowed selections
     * @param values The values stored for each selection
     */
    protected SelectionElement(String id, String question, String answer,
                               String concept, String figure, String audio, String[] labels,
                               String[] values)
    {
        super(id,question,answer, concept, figure, audio);
        this.values = (values == null)? new String[]{}: labels;
        this.labels = (labels == null)? values:
                (values.length == labels.length)? labels: values;
        valueToLabelMap = new LinkedHashMap<String,String>(values.length);
        labelToValueMap = new LinkedHashMap<String,String>(values.length);
        mapValues(values,labels);
    }
}

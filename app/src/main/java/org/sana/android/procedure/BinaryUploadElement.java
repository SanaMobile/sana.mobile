package org.sana.android.procedure;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.sana.R;
import org.sana.android.Constants;
import org.w3c.dom.Node;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * BinaryUploadElement is a ProcedureElement that is created when a "BINARYFILE"
 * element is inserted into an XML procedure description. It allows a user to 
 * select a binary file stored on the phone for upload. The path of where the 
 * selectable files reside is set in the Sana settings dialog. By default, the 
 * most recent file modified in the folder is selected. 
 * 
 * A refresh button allows the following example interaction to occur: 
 * <ol>
 * <li>page with binaryuploadelement comes up on phone</li>
 * <li>healthworker uses external ultrasound which automatically stores an MPG 
 * file on the SD card</li>
 * <li> if is Sana still on the screen, the healthworker hits the refresh file 
 * list button to automatically re-source the files on the SD card and 
 * automatically select the file that was just created.
 * </li>
 * </ol>
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use </b> Defined by subclasses.</li>
 * <li><b>Collects </b> name of a binary file to upload.</li>
 * </ul>
 * 
 * @author Sana Dev Team
 */
public class BinaryUploadElement extends ProcedureElement implements 
	OnClickListener, OnItemSelectedListener 
{
    private Button refresh;
    private TextView tvBinary;
    private TextView result;
    private Spinner spin;
    private List<String> sdfiles = new ArrayList<String>();
    File[] filelist;
    private ArrayAdapter<String> adapter;
    private Context context;
    
    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.BINARYFILE;
    }
    

    /** {@inheritDoc} */
    protected View createView(Context c) {
        LinearLayout binaryContainer = new LinearLayout(c);
        binaryContainer.setOrientation(LinearLayout.VERTICAL);
        context = c;
               
        if(question == null) {
            question = c.getString(R.string.question_standard_binary_element);
        }
        tvBinary = new TextView(c);
        tvBinary.setText(question);
        tvBinary.setGravity(Gravity.CENTER);
        tvBinary.setTextAppearance(c, android.R.style.TextAppearance_Medium);
        binaryContainer.addView(tvBinary, new LinearLayout.LayoutParams(-1,-1,
        		0.1f));
        refresh = new Button(c);
        refresh.setText("Refresh file list");
        refresh.setOnClickListener(this);
        spin = new Spinner(c);
        spin.setOnItemSelectedListener(this);
        updateSdList();
        binaryContainer.addView(spin, new LinearLayout.LayoutParams(-1,-1,
        		0.1f));
        binaryContainer.addView(refresh, new LinearLayout.LayoutParams(-1,-1,
        		0.1f));
        result = new TextView(c);
        result.setText("Folder is empty!");
        result.setGravity(Gravity.CENTER);
        result.setTextAppearance(c, android.R.style.TextAppearance_Small);
        binaryContainer.addView(result, new LinearLayout.LayoutParams(-1,-1,
        		0.1f));
        return binaryContainer;
    }
    
    // updates the list of files available
    private void updateSdList() {
    	File folder = new File(PreferenceManager.getDefaultSharedPreferences(
    			context).getString(Constants.PREFERENCE_STORAGE_DIRECTORY, 
    					Constants.DEFAULT_BINARY_FILE_FOLDER));
    	// we may want to add a filename filter here if we want to restrict to 
    	// certain types i.e. mpg files
    	FileFilter nofolders = new FileFilter() {
			public boolean accept(File f) {
				return !f.isDirectory();
			}
    };
    	filelist = folder.listFiles(nofolders);
    	int lastModifiedFileIndex = -1;
    	sdfiles = new ArrayList<String>();
    	if (filelist.length > 0) {
    		lastModifiedFileIndex = 0;
    	        for (int i=0; i < filelist.length; i++) {
    	            sdfiles.add(filelist[i].getName());
    	            if (filelist[i].lastModified() > 
    	            		filelist[lastModifiedFileIndex].lastModified()) 
    	            {
    	            	lastModifiedFileIndex = i;
    	            }
    	        }
    	}
    	adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item,
                sdfiles);
        adapter.setDropDownViewResource(
        		android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        
        if (lastModifiedFileIndex != -1) {
        	spin.setSelection(lastModifiedFileIndex);
        } 
        
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        if (v == refresh) {
        	updateSdList();
        	if (spin.getCount() == 0)
        		result.setText("Folder is empty!");
        } 
    }
    
    /** {@inheritDoc} */
    public void setAnswer(String answer) {
    	this.answer = answer;
    }
    
    /**
     * Gets the text representation of the answer
     * 
     * @return the path of the selected file for upload, or empty string if no 
     * 		   file was selected.
     */
    public String getAnswer() {
    	if(!isViewActive())
    		return answer;
    	
    	if (spin.getCount() > 0)
    		return filelist[spin.getSelectedItemPosition()].getAbsolutePath();
    	else
    		return "";
    }
    
    /** {@inheritDoc} */
    public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" answer=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }
    
    private BinaryUploadElement(String id, String question, String answer, 
    		String concept, String figure, String audio) 
    {
        super(id, question, answer, concept, figure, audio);
    }
    
    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static BinaryUploadElement fromXML(String id, String question, 
    		String answer, String concept, String figure, String audio, 
    		Node node) throws ProcedureParseException 
    {
        return new BinaryUploadElement(id, question, answer, concept, figure, 
        		audio);
    }

    /** {@inheritDoc} */
    @Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		String loadedFileResult = "\nloaded successfully";
		loadedFileResult = spin.getSelectedItem() + loadedFileResult;
    	result.setText(loadedFileResult);
		
	}

    /** {@inheritDoc} */
	public void onNothingSelected(AdapterView<?> arg0) {		
	}
}

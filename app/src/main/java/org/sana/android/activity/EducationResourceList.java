package org.sana.android.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.sana.R;
import org.sana.android.db.SanaDB;
import org.sana.android.db.SanaDB.EducationResourceSQLFormat;
import org.sana.android.media.EducationResource;
import org.sana.android.media.EducationResource.Audience;
import org.sana.android.media.EducationResourceParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


/**
 * Activity which displays a list of available media
 * 
 * @author Sana Development Team
 */
public class EducationResourceList extends ListActivity {
    private static final String TAG = EducationResourceList.class.getSimpleName();
    // set the text color for various audience types
    private static final int error = Color.argb(225, 225, 127, 127);
    private static final int worker = Color.argb(225, 127, 127, 225);
    private static final int patient = Color.argb(225, 193, 127, 225);
    
	/**  {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Future compatibility - we want to use a help info Provider
        Uri intent = getIntent().getData();
        Log.d(TAG, "Intent, Audience: " + getIntent().getStringExtra(
        		"audience"));
        Audience audience = Audience.valueOf(
        		getIntent().getStringExtra("audience").toUpperCase());
        Log.d(TAG, "audience:" + audience);
        ArrayList<EducationResource> items = 
        	getIntent().getParcelableArrayListExtra(SanaDB.EDUCATIONRESOURCE_AUTHORITY);
        if ((items == null) || (items.size() == 0)){
        	String path = EducationResource.DEFAULT_MEDIA_ROOT 
        					+ EducationResource.DEFAULT_MEDIA_PATH 
        					+ EducationResource.DEFAULT_MEDIA_XML;
			Log.d(TAG, "onCreate(): " + path + ", " + audience.toString());
        	items = new ArrayList<EducationResource>(getMedia(path, audience));
        }
        Log.d(TAG, " Media items: " + items.size());
        EducationResourceListAdapter adapter = new EducationResourceListAdapter(this, 
        		R.layout.title_author_row, items);
        setListAdapter(adapter);
    }
    
	/**  {@inheritDoc} */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	EducationResource er = (EducationResource) getListAdapter().getItem((int)id);
        
    	Uri uri = er.uri(EducationResource.getDir());
    	Intent intent = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
        Intent result = (intent != null)? intent: new Intent(Intent.ACTION_VIEW);
    	result.setDataAndType(uri, er.mimeType)
    	      .putExtra("text", er.text);
    	setResult(RESULT_OK, result);
    	Log.d(TAG, result.toUri(Intent.URI_INTENT_SCHEME).toString());
    	if(intent != null)
    		if(intent.getAction().equals(Intent.ACTION_VIEW))
    			startActivity(result);
    	finish();
    }
    
    private void doViewResource(EducationResource er){
    	Uri uri = er.uri(EducationResource.getDir());
    	//TODO make this able to return the value if we want to
    	Intent result = new Intent();
	    result.setAction(Intent.ACTION_VIEW);
	        	result.setDataAndType(uri, er.mimeType);
	        	result.putExtra("text", er.text);
	            setResult(RESULT_OK, result);
	        	Log.d(TAG, "VIEW uri" + uri);
	     startActivity(result);
    }
    
    /**
	 * Constructs a new Intent which can be used to VIEW or PICK available 
	 * EducationResource resource(s).
	 *  
	 * @param action The intent action
	 * @return An Intent which can be used to launch an Activity related to 
	 * 			EducationResource resources
	 */
	public static Intent getIntent(String action, Audience audience){
		Intent intent = new Intent();
		Log.d(TAG, "getIntent(String, Audience): audience: " 
				+ audience.toString());

		ArrayList<EducationResource> items = new ArrayList<EducationResource>();
		try {
			// Parse the xml
			InputStream in = new FileInputStream(EducationResource.getMetadata());
			InputSource source = new InputSource(in);
			EducationResourceParser parser = EducationResourceParser.newInstance();
			parser.parse(source);
			items.addAll(parser.infoList(audience));
			if(items.size() == 0){
				return null;
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File IO error: " + e.getMessage() );
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "Parser config error: " + e.getMessage() );
		} catch (SAXException e) {
			Log.e(TAG, "Error parsing: " + e.getMessage() );
		} catch (IOException e) {
			Log.e(TAG, "IO error: " + e.getMessage() );
		} catch (Exception e) {
			Log.e(TAG, "Unhandled error: " + e.getMessage() );
		}

		intent.setAction(action)
			.setType(EducationResourceSQLFormat.CONTENT_TYPE)
			.setData(EducationResourceSQLFormat.CONTENT_URI)
			.putParcelableArrayListExtra(SanaDB.EDUCATIONRESOURCE_AUTHORITY, items);
		intent.putExtra("audience", audience.toString());
		Log.d(TAG, intent.toUri(Intent.URI_INTENT_SCHEME).toString());
		return intent;
	}
	
	/**
	 * Constructs a new Intent which will launch the EducationResourceList with 
	 * a selection of the info resources available for items in the list.
	 * 
	 * @param ids the list of ids to get the help info for
	 * @param audience the target audience
	 * @return an Intent which will launch an Activity for selecting a 
	 * 	EducationResource resource
	 */
	public static Intent getIntent(List<String> ids, Audience audience){
		Log.d(TAG, "getIntent(List, Audience): list: "+ ids.size() + ", audience: " 
				+ audience.toString());
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK)
			.setType(EducationResourceSQLFormat.CONTENT_TYPE)
			.setData(EducationResourceSQLFormat.CONTENT_URI);
		try {
			// Parse the xml
			InputStream in = new FileInputStream(EducationResource.getMetadata());
			InputSource source = new InputSource(in);
			EducationResourceParser parser = EducationResourceParser.newInstance();
			parser.parse(source);
			ArrayList<EducationResource> items = 
				new ArrayList<EducationResource>(parser.infoList(ids, audience));
			Log.d(TAG, ""+items.size());
			if(items.size() == 0){
				return null;
			}
			intent.putParcelableArrayListExtra(SanaDB.EDUCATIONRESOURCE_AUTHORITY,items);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File IO error: " + e.getMessage() );
		} catch (ParserConfigurationException e) {
			Log.d(TAG, "Parser config error: " + e.getMessage() );
		} catch (SAXException e) {
			Log.d(TAG, "Error parsing: " + e.getMessage() );
		} catch (IOException e) {
			Log.d(TAG, "IO error: " + e.getMessage() );
		}
		intent.putExtra("audience", audience.toString());
		Log.d(TAG, intent.toUri(Intent.URI_INTENT_SCHEME).toString());
		return intent;
	}
	
    /**
     * Returns a list of available help info
     * 
     * @param path Path to the resource list xml file
     * @return
     */
    public static List<EducationResource> getMedia(String path, Audience audience) {
		File f = new File(path);
    	List<EducationResource> media = new ArrayList<EducationResource>();
    	try {
    		InputStream in = new FileInputStream(f);
			media = getMedia(in, audience);
		} catch (IOException e) {
			Log.e(TAG, "IO error: " + e.getMessage() );
		}
    	return media;
    }
    
    /**
     * Returns a list of available EducationResource resources.
     * 
     * @param c the current context
     * @param uri the The resource identifier holding the resource list
     * @return
     */
    public static List<EducationResource> getMedia(Context c, Uri uri, Audience audience) 
    {
    	List<EducationResource> media = new ArrayList<EducationResource>();
    	try {
    		InputStream in = c.getContentResolver().openInputStream(uri);
			media = getMedia(in, audience);
		} catch (IOException e) {
			Log.d(TAG, "IO error: " + e.getMessage() );
		}
    	return media;
    }
    
    /**
     * Returns a list of available EducationResource resources.
     * 
     * @param c the current context
     * @param uri the The resource identifier holding the resource list
     * @return
     */
    public static List<EducationResource> getAll(String path) 
    {
    	return getMedia(path, Audience.ALL);
    }

    /**
     * Returns a list of available EducationResource from an input stream
     * 
     * @param in The stream to read from
     * @return A List of available EducationResource resources filtered by audience
     */
    public static List<EducationResource> getMedia(InputStream in, Audience audience) 
    {
    	List<EducationResource> media = new ArrayList<EducationResource>();
    	try {
			Log.d(TAG, "getMedia(InputStream,Audience): " 
					+ audience.toString());
    		InputSource source = new InputSource(in);
    		EducationResourceParser parser = EducationResourceParser.newInstance();
    		parser.parse(source);
			media.addAll(parser.infoList(audience));
		} catch (ParserConfigurationException e) {
			Log.d(TAG, "Parser config error: " + e.getMessage() );
		} catch (SAXException e) {
			Log.d(TAG, "Error parsing: " + e.getMessage() );
		} catch (IOException e) {
			Log.d(TAG, "IO error: " + e.getMessage() );
		}
    	return media;
    }
    
    /**
     * SAX Based XML handler for parsing EducationResource resources in xml format
     * @author Sana Development Team
     */
    public static class EducationResourceListAdapter extends 
    	ArrayAdapter<EducationResource>
    {
    	public static final String TAG = 
    		EducationResourceListAdapter.class.getSimpleName();

    	private List<EducationResource> objects;
    	
    	/**
    	 * Instantiates a new EducationResourceAdapter with a list of 
    	 * EducationResource
    	 * 
    	 * @param context The Context the new Adapter will be created in
    	 * @param textViewResourceId a View resource
    	 * @param objects a List of EducationResource
    	 */
		public EducationResourceListAdapter(Context context, int textViewResourceId,
				List<EducationResource> objects) 
		{
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}
    	
		/** {@inheritDoc} */
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			View v = convertView;
            if (v == null) {
                LayoutInflater vi = 
                				(LayoutInflater)getContext().getSystemService(
                						Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.title_author_row, null);
            }
            EducationResource m = objects.get(position);
            if (m != null) {
            	Log.d(TAG, "media:" +  m.name);
                TextView tt = (TextView) v.findViewById(R.id.titletext);
                TextView bt = (TextView) v.findViewById(R.id.authortext);
                TextView dt = (TextView) v.findViewById(R.id.descriptiontext);
                if(!m.hasValidResource())
                	tt.setTextColor(error);
        		bt.setTextColor(Color.GRAY);
        		dt.setTextColor(Color.GRAY);
                if (tt != null) {
                	tt.setText(m.name);                            
                } 
                if(bt != null){
                    bt.setText(m.author);                            
                } 
                if(dt != null){
                    dt.setText(m.text);
                } 
            }
            return v;
		}
    }
}

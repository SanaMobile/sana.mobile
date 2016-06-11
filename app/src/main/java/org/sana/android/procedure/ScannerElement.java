package org.sana.android.procedure;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;

import org.sana.R;

/**
 *
 */
public class ScannerElement extends PluginEntryElement {
    public static final String TAG = ScannerElement.class.getSimpleName();

    private BarcodeFormat mBarcodeFormat = null;

    protected ScannerElement(String id, String question, String answer,
                             String concept, String figure,
                             String audioPrompt, String action,
                             String mimeType) {
        super(id, question, answer, concept, figure, audioPrompt, action, mimeType);
    }

    @Override
    protected View createView(Context c) {
        View view = ((Activity) c).getLayoutInflater().inflate(R.layout.element_scanner, null);
        setQuestionView(view);

        return view;
    }

    protected void setQuestionView(View container){
        TextView view = (TextView)container.findViewById(R.id.question);
        String q = question.replace("\\n", "\n");
        view.setText(q);
    }


    protected void setImageView(Context context, View container) {
        ImageView view = (ImageView)container.findViewById(R.id.image);
        int resID = (TextUtils.isEmpty(figure)) ? 0:
                context.getResources().getIdentifier(figure, null, null);
        if(resID != 0){
            view.setImageResource(resID);
            view.setAdjustViewBounds(true);
        } else {
            view.setVisibility(View.GONE);
        }
    }

}

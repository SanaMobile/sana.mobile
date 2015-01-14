package org.sana.android.procedure;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Node;

import android.content.Context;
import android.view.View;
import android.widget.DatePicker;

/**
 * A ProcedureElement for collecting a date value.
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use </b> Defined by subclasses.</li>
 * <li><b>Collects </b>Date value formatted as <code>yyyy/MM/dd</code></li>
 * </ul>
 * 
 * @author Sana Development Team
 */
public class DateElement extends ProcedureElement {

	DatePicker dp = null;
	Date dateAnswer = new Date();

    /** {@inheritDoc} */
	@Override
	public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" question=\"" + question);
        sb.append("\" answer=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }

    /** {@inheritDoc} */
	@Override
	protected View createView(Context c) {
		dp = new DatePicker(c);
		if (dateAnswer != null) {
			dp.init(dateAnswer.getYear() + 1900, dateAnswer.getMonth(), 
					dateAnswer.getDate(), null);
		}
		return encapsulateQuestion(c, dp);
	}

    /** {@inheritDoc} */
	@Override
	public String getAnswer() {
		 if(!isViewActive())
			 return answer;
		 else {
			 dateAnswer = new Date(dp.getYear(), dp.getMonth(), 
					 dp.getDayOfMonth());
			 SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			 return sdf.format(dateAnswer);
		 }
	}

    /** {@inheritDoc} */
	@Override
	public ElementType getType() {
		return ElementType.DATE;
	}

    /** {@inheritDoc} */
	@Override
	public void setAnswer(String answer) {
		dateAnswer = new Date(answer);
		if (isViewActive()) {
			dp.updateDate(dateAnswer.getYear(), dateAnswer.getMonth(), 
					dateAnswer.getDay());
		}
	}
	
	private DateElement(String id, String question, String answer, 
			String concept, String figure, String audio) 
	{
        super(id, question, answer, concept, figure, audio);
    }
    
    /** 
     * Creates the element from an XML procedure definition.
     * 
     * @param id The unique identifier of this element within its procedure.
     * @param question The text that will be displayed to the user as a question
     * @param answer The result of data capture.
     * @param concept A required categorization of the type of data captured.
     * @param figure An optional figure to display to the user.
     * @param audio An optional audio prompt to play for the user. 
     * @param node The source xml node. 
     * @return A new element.
     * @throws ProcedureParseException if an error occurred while parsing 
     * 		additional information from the Node
     */
	public static DateElement fromXML(String id, String question, String answer,
			String concept, String figure, String audio, Node n) throws 
			ProcedureParseException 
	{
		return new DateElement(id, question, answer, concept, figure, audio);
    }

}

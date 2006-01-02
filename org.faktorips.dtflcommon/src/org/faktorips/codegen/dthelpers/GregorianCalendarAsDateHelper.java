package org.faktorips.codegen.dthelpers;

import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.GregorianCalendarAsDateDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;


/**
 * DatatypeHelper for datatype GregorianCalendarAsDate. 
 */
public class GregorianCalendarAsDateHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public GregorianCalendarAsDateHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given boolean datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public GregorianCalendarAsDateHelper(GregorianCalendarAsDateDatatype datatype) {
        super(datatype);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.codegen.DatatypeHelper#newInstance(java.lang.String)
     */
    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();        
        GregorianCalendar date = (GregorianCalendar)new GregorianCalendarDatatype("", false).getValue(value);
        fragment.append("new ");
        fragment.appendClassName(GregorianCalendar.class);
        fragment.append('(');
        fragment.append(date.get(GregorianCalendar.YEAR));
        fragment.append(", ");
        fragment.append(date.get(GregorianCalendar.MONTH));
        fragment.append(", ");
        fragment.append(date.get(GregorianCalendar.DATE));
        fragment.append(')');
        return fragment;
    }

	/* (non-Javadoc)
	 * @see org.faktorips.codegen.dthelpers.AbstractDatatypeHelper#valueOfExpression(java.lang.String)
	 */
	protected JavaCodeFragment valueOfExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();        
        fragment.append('(');
        fragment.appendClassName(GregorianCalendar.class);
        fragment.append(") new ");
        fragment.appendClassName(GregorianCalendarDatatype.class);
        fragment.append("(\"\", false).getValue(");
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

	/* (non-Javadoc)
	 * @see org.faktorips.codegen.DatatypeHelper#nullExpression()
	 */
	public JavaCodeFragment nullExpression() {
		return new JavaCodeFragment("null");
	}
}

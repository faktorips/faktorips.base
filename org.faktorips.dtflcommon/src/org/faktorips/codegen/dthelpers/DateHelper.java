package org.faktorips.codegen.dthelpers;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.valueset.DateRange;

/**
 * 
 * @author Peter Erzberger
 */
public class DateHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public DateHelper() {
        super();
    }

    /**
     * @param datatype
     */
    public DateHelper(DateDatatype datatype) {
        super(datatype);
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.codegen.dthelpers.AbstractDatatypeHelper#valueOfExpression(java.lang.String)
     */
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();        
        fragment.append("new ");
        fragment.appendClassName(DateDatatype.class);
        fragment.append("().getDateValue(");
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.codegen.DatatypeHelper#nullExpression()
     */
    public JavaCodeFragment nullExpression() {
    		return new JavaCodeFragment("null");
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.codegen.DatatypeHelper#newInstance(java.lang.String)
     */
    public JavaCodeFragment newInstance(String value) {
        StringBuffer buf = new StringBuffer();
        buf.append('"').append(value).append('"');
        return valueOfExpression(buf.toString());
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.codegen.DatatypeHelper#getRangeJavaClassName()
     */
    public String getRangeJavaClassName() {
        return DateRange.class.getName();
    }
}

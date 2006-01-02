package org.faktorips.codegen.dthelpers;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Decimal;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.valueset.DecimalRange;

/**
 * DatatypeHelper for datatype Decimal.
 */
public class DecimalHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public DecimalHelper() {
        super();
    }

	/**
	 * Constructs a new helper for the given decimal datatype.
	 * 
	 * @throws IllegalArgumentException
	 *             if datatype is <code>null</code>.
	 */
	public DecimalHelper(DecimalDatatype datatype) {
		super(datatype);
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.codegen.DatatypeHelper#newInstance(java.lang.String)
	 */
	public JavaCodeFragment newInstance(String value) {
		if (StringUtils.isEmpty(value)) {
			return nullExpression();
		}
		JavaCodeFragment fragment = new JavaCodeFragment();
		fragment.appendClassName(Decimal.class);		
		fragment.append(".valueOf(");
		fragment.appendQuoted(value);
		fragment.append(')');
		return fragment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.faktorips.codegen.dthelpers.AbstractDatatypeHelper#valueOfExpression(java.lang.String)
	 */
	protected JavaCodeFragment valueOfExpression(String expression) {
		JavaCodeFragment fragment = new JavaCodeFragment();
		fragment.appendClassName(Decimal.class);		
		fragment.append(".valueOf(");
		fragment.append(expression);
		fragment.append(')');
		return fragment;
	}
	
	/**
	 * Methode der Oberklasse wird ueberschrieben, weil bei diesem Datentyp
	 * valueOf-Methode selbst Null-Expression zurückgeben kann
	 */
	public JavaCodeFragment newInstanceFromExpression(String expression) {	
	    return valueOfExpression(expression);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.faktorips.codegen.DatatypeHelper#nullExpression()
	 */
	public JavaCodeFragment nullExpression() {
		JavaCodeFragment fragment = new JavaCodeFragment();
		fragment.appendClassName(Decimal.class);
		fragment.append(".NULL");
		return fragment;
	}

    /* (non-Javadoc)
     * @see org.faktorips.codegen.DatatypeHelper#getRangeJavaClassName()
     */
    public String getRangeJavaClassName() {
        return DecimalRange.class.getName();
    }
}

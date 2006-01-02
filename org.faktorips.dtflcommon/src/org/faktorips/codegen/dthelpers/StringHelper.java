package org.faktorips.codegen.dthelpers;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.StringDatatype;

/**
 *  
 */
public class StringHelper extends AbstractDatatypeHelper {

	/**
	 * Constructs a new helper for the string datatype.
	 */
	public StringHelper() {
		super();
	}

	/**
	 * Constructs a new helper for the given string datatype.
	 * 
	 * @throws IllegalArgumentException
	 *             if datatype is <code>null</code>.
	 */
	public StringHelper(StringDatatype datatype) {
		super(datatype);
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.codegen.DatatypeHelper#newInstance(java.lang.String)
	 */
	public JavaCodeFragment newInstance(String value) {
		if (value == null) {
			return nullExpression();
		}
		JavaCodeFragment fragment = new JavaCodeFragment();
		fragment.appendQuoted(value);
		return fragment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.faktorips.codegen.DatatypeHelper#newInstanceFromExpression(java.lang.String)
	 */
	public JavaCodeFragment newInstanceFromExpression(String expression) {
		return valueOfExpression(expression);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.faktorips.codegen.dthelpers.AbstractDatatypeHelper#valueOfExpression(java.lang.String)
	 */
	protected JavaCodeFragment valueOfExpression(String expression) {
		if (StringUtils.isEmpty(expression)) {
			return nullExpression();
		}
		JavaCodeFragment fragment = new JavaCodeFragment();
		fragment.append(expression);
		return fragment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.faktorips.codegen.DatatypeHelper#nullExpression()
	 */
	public JavaCodeFragment nullExpression() {
		return new JavaCodeFragment("null");
	}

}

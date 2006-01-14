package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.ArgumentCheck;
import org.apache.commons.lang.StringUtils;

/**
 * Abstract base class for datatype helpers.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractDatatypeHelper implements DatatypeHelper {

	private Datatype datatype;

	/**
	 * Constructs a new helper.
	 */
	public AbstractDatatypeHelper() {
	}

	/**
	 * Constructs a new helper for the given datatype.
	 */
	public AbstractDatatypeHelper(Datatype datatype) {
		ArgumentCheck.notNull(datatype);
		this.datatype = datatype;
	}

	/**
	 * Overridden.
	 */
	public Datatype getDatatype() {
		return datatype;
	}
	
	/**
	 * Overridden Method.
	 * 
	 * @see org.faktorips.codegen.DatatypeHelper#setDatatype(org.faktorips.datatype.Datatype)
	 */
	public void setDatatype(Datatype datatype) {
		this.datatype = datatype;
	}

	/**
	 * Returns a JavaCodeFragment with sourcecode that creates an instance of
	 * the datatype's Java class with the given expression. If the expression is
	 * null the fragment's sourcecode is either the String "null" or the
	 * sourcecode to get an instance of the apropriate null object.
	 * Preconditions: Expression may not be null or empty. When evaluated the
	 * expression must return a string
	 */
	protected abstract JavaCodeFragment valueOfExpression(String expression);

	/**
     * Overridden.
	 */
	public JavaCodeFragment newInstanceFromExpression(String expression) {
		if (StringUtils.isEmpty(expression)) {
			return nullExpression();
		}
		// StringUtils.isEmpty(expression) ? nullExpression() :
		// valueOfExpression(expression)
		JavaCodeFragment fragment = new JavaCodeFragment();
		fragment.appendClassName(StringUtils.class);
		fragment.append(".isEmpty(");		
		fragment.append(expression);
		fragment.append(") ? ");
		fragment.append(nullExpression());
		fragment.append(" : ");
		fragment.append(valueOfExpression(expression));

		return fragment;
	}
	

    /* (non-Javadoc)
     * @see org.faktorips.codegen.DatatypeHelper#getRangeJavaClassName()
     */
    public String getRangeJavaClassName() {
        return null;
    }	
   
}

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.PrimitiveDatatypeHelper;
import org.faktorips.datatype.PrimitiveBooleanDatatype;


/**
 *
 */
public class PrimitiveBooleanHelper extends AbstractDatatypeHelper implements PrimitiveDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public PrimitiveBooleanHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given primitive boolean datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public PrimitiveBooleanHelper(PrimitiveBooleanDatatype datatype) {
        super(datatype);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.codegen.DatatypeHelper#newInstance(java.lang.String)
     */
    public JavaCodeFragment newInstance(String value) {
        Boolean booleanValue = Boolean.valueOf(value);
        if (booleanValue.booleanValue()) {
            return new JavaCodeFragment("true");
        } else {
            return new JavaCodeFragment("false");
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.codegen.PrimitiveDatatypeHelper#toWrapper(org.faktorips.codegen.JavaCodeFragment)
     */
    public JavaCodeFragment toWrapper(JavaCodeFragment expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Boolean.class);
        fragment.append(".valueOf(");
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

	/* (non-Javadoc)
	 * @see org.faktorips.codegen.dthelpers.AbstractDatatypeHelper#valueOfExpression(java.lang.String)
	 */
	protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(expression); 
        return fragment;
	}

	/* (non-Javadoc)
	 * @see org.faktorips.codegen.DatatypeHelper#nullExpression()
	 */
	public JavaCodeFragment nullExpression() {
		// TODO oder doch ne Exception werfen
		return new JavaCodeFragment("false");
	}

}

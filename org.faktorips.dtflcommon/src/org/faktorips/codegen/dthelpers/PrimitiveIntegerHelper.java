package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.PrimitiveDatatypeHelper;
import org.faktorips.datatype.PrimitiveIntegerDatatype;


/**
 * DatatypeHelper for datatype PrimitiveInteger. 
 */
public class PrimitiveIntegerHelper extends AbstractDatatypeHelper implements PrimitiveDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public PrimitiveIntegerHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given primitive integer datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public PrimitiveIntegerHelper(PrimitiveIntegerDatatype datatype) {
        super(datatype);
    }
    
    /** 
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstance(String value) {
        return new JavaCodeFragment(value);
    }

    /** 
     * {@inheritDoc}
     */
    public JavaCodeFragment toWrapper(JavaCodeFragment expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new ");
        fragment.appendClassName(Integer.class);
        fragment.append('(');
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

	/**
     * {@inheritDoc}
	 */
	protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("Integer.parseInt(" + expression + ")");
        return fragment;
	}
	
	/**
     * {@inheritDoc}
	 */
	public JavaCodeFragment nullExpression() {
		// TODO oder doch ne Exception werfen
		return new JavaCodeFragment("0");
	}
	
}

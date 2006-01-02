package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;


/**
 *
 */
public class PrimitiveIntToIntegerCg extends AbstractSingleConversionCg {

    /**
     * @param from
     * @param to
     */
    public PrimitiveIntToIntegerCg() {
        super(Datatype.PRIMITIVE_INT, Datatype.INTEGER);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.codegen.SingleConversionCg#getConversionCode()
     */
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new ");
        fragment.appendClassName(Integer.class);
        fragment.append('(');
        fragment.append(fromValue);
        fragment.append(')');
        return fragment;
    }

}

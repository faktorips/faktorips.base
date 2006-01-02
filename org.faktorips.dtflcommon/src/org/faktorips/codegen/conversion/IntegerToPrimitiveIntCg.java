package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;


/**
 *
 */
public class IntegerToPrimitiveIntCg extends AbstractSingleConversionCg {

    /**
     * @param from
     * @param to
     */
    public IntegerToPrimitiveIntCg() {
        super(Datatype.INTEGER, Datatype.PRIMITIVE_INT);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.codegen.SingleConversionCg#getConversionCode()
     */
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        fromValue.append(".intValue()");
        return fromValue;
    }

}

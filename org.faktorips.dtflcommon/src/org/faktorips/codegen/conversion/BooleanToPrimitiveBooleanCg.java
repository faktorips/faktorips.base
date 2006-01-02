package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;


/**
 *
 */
public class BooleanToPrimitiveBooleanCg extends AbstractSingleConversionCg {

    /**
     * @param from
     * @param to
     */
    public BooleanToPrimitiveBooleanCg() {
        super(Datatype.BOOLEAN, Datatype.PRIMITIVE_BOOLEAN);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.codegen.SingleConversionCg#getConversionCode()
     */
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        fromValue.append(".booleanValue()");
        return fromValue;
    }

}

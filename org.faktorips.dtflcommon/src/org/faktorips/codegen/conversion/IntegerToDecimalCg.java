package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.Decimal;


/**
 *
 */
public class IntegerToDecimalCg extends AbstractSingleConversionCg {

    /**
     * @param from
     * @param to
     */
    public IntegerToDecimalCg() {
        super(Datatype.INTEGER, Datatype.DECIMAL);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.codegen.SingleConversionCg#getConversionCode()
     */
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Decimal.class);
        fragment.append(".valueOf(");
        fragment.append(fromValue);
        fragment.append(')');
        return fragment;
    }

}

package org.faktorips.codegen.dthelpers;

import org.faktorips.datatype.DefaultEnumType;
import org.faktorips.datatype.DefaultEnumValue;
import org.faktorips.datatype.EnumType;

/**
 * Test enum type, to test DefaultEnumTypeHelper.
 */
public class PaymentMode extends DefaultEnumValue {

    public final static PaymentMode ANNUALLY;

    public final static PaymentMode MONTHLY;
    private final static DefaultEnumType enumType;
    
    static {
        enumType = new DefaultEnumType("PaymentMode", PaymentMode.class);
        ANNUALLY = new PaymentMode(enumType, "a", "annually");
        MONTHLY = new PaymentMode(enumType, "m", "monthly");
    }

    public final static PaymentMode getPaymentMode(String id) {
        return (PaymentMode) enumType.getValue(id);
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }

    private PaymentMode(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }
}

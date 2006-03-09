/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

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
        return (PaymentMode) enumType.getEnumValue(id);
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }

    private PaymentMode(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }
}

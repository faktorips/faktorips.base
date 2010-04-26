/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;

/**
 *
 */
public class PrimitiveIntToDecimalCg extends AbstractSingleConversionCg {

    /**
     * @param from
     * @param to
     */
    public PrimitiveIntToDecimalCg() {
        super(Datatype.PRIMITIVE_INT, Datatype.DECIMAL);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.codegen.SingleConversionCg#getConversionCode()
     */
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Decimal.class);
        fragment.append(".valueOf(");
        fragment.append(fromValue);
        fragment.append(", 0)");
        return fragment;
    }

}

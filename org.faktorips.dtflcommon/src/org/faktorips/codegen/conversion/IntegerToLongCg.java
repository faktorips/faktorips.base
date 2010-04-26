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

/**
 *
 */
public class IntegerToLongCg extends AbstractSingleConversionCg {

    /**
     * @param from
     * @param to
     */
    public IntegerToLongCg() {
        super(Datatype.INTEGER, Datatype.LONG);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.codegen.SingleConversionCg#getConversionCode()
     */
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        // new Long(new Integer(1).intValue())
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new ");
        fragment.appendClassName(Long.class);
        fragment.append("(");
        fragment.append(fromValue);
        fragment.append(".intValue())");
        return fragment;
    }

}

/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;

public class IntegerToLongCg extends AbstractSingleConversionCg {

    public IntegerToLongCg() {
        super(Datatype.INTEGER, Datatype.LONG);
    }

    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        // new Long(new Integer(1).intValue())
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new "); //$NON-NLS-1$
        fragment.appendClassName(Long.class);
        fragment.append("("); //$NON-NLS-1$
        fragment.append(fromValue);
        fragment.append(".intValue())"); //$NON-NLS-1$
        return fragment;
    }

}

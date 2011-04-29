/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

public class BooleanToPrimitiveBooleanCg extends AbstractSingleConversionCg {

    public BooleanToPrimitiveBooleanCg() {
        super(Datatype.BOOLEAN, Datatype.PRIMITIVE_BOOLEAN);
    }

    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        fromValue.append(".booleanValue()"); //$NON-NLS-1$
        return fromValue;
    }

}

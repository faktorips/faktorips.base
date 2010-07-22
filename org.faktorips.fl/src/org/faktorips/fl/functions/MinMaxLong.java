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

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;

/**
 *
 */
public class MinMaxLong extends MinMaxNativeTypes {

    public MinMaxLong(String name, String description, boolean isMax) {
        super(name, description, Datatype.LONG, isMax);
    }

    @Override
    protected void writeBody(JavaCodeFragment fragment, CompilationResult first, CompilationResult second) {
        // new Long(Math.max(new Long(1).longValue(), new Long(2).longValue()));
        fragment.append("new Long(Math.");
        fragment.append(functionName);
        fragment.append('(');
        fragment.append(first.getCodeFragment());
        fragment.append(".longValue()");
        fragment.append(", ");
        fragment.append(second.getCodeFragment());
        fragment.append(".longValue()");
        fragment.append("))");
    }
}

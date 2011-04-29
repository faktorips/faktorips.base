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

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;

/**
 *
 */
public class MinMaxDouble extends MinMaxNativeTypes {

    public MinMaxDouble(String name, String description, boolean isMax) {
        super(name, description, Datatype.DOUBLE, isMax);
    }

    @Override
    protected void writeBody(JavaCodeFragment fragment, CompilationResult first, CompilationResult second) {
        // new Double(Math.max(new Double(1).doubleValue(), new Double(2).doubleValue()));
        fragment.append("new Double(Math.");
        fragment.append(functionName);
        fragment.append('(');
        fragment.append(first.getCodeFragment());
        fragment.append(".doubleValue()");
        fragment.append(", ");
        fragment.append(second.getCodeFragment());
        fragment.append(".doubleValue()");
        fragment.append("))");
    }
}

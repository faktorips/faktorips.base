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

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;


/**
 *
 */
public class MinMaxInt extends MinMaxNativeTypes {
    
    public MinMaxInt(String name, String description, boolean isMax) {
        super(name, description, Datatype.PRIMITIVE_INT, isMax);
    }

    protected void writeBody(JavaCodeFragment fragment, CompilationResult first, CompilationResult second) {
        //Math.max(value1, value2)
        fragment.append("Math.");
        fragment.append(functionName);
        fragment.append('(');
        fragment.append(first.getCodeFragment());
        fragment.append(", ");
        fragment.append(second.getCodeFragment());
        fragment.append(")");
    }
}

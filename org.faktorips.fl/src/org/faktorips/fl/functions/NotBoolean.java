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
import org.faktorips.fl.CompilationResultImpl;

/**
 * Boolean function NOT for the wrapper type.
 * 
 * @author Jan Ortmann
 */
public class NotBoolean extends AbstractFlFunction {

    public NotBoolean(String name, String description) {
        super(name, description, Datatype.BOOLEAN, new Datatype[] { Datatype.BOOLEAN });
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        CompilationResultImpl result = (CompilationResultImpl)argResults[0];
        JavaCodeFragment code = result.getCodeFragment();
        JavaCodeFragment newCode = new JavaCodeFragment();
        newCode.append("((");
        newCode.append(code);
        newCode.append(")==null ? (Boolean)null : ");
        newCode.append("Boolean.valueOf(!(" + code + ").booleanValue())");
        newCode.append(')');
        result.setJavaCodeFragment(newCode);
        return result;
    }

}

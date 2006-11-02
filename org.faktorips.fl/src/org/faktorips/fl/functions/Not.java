/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * Boolean function NOT.
 * @author Jan Ortmann
 */
public class Not extends AbstractFlFunction {

    public Not(String name, String description) {
        super(name, description, Datatype.PRIMITIVE_BOOLEAN, new Datatype[]{Datatype.PRIMITIVE_BOOLEAN});
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        JavaCodeFragment code = new JavaCodeFragment("!");
        code.append(argResults[0].getCodeFragment());
        ((CompilationResultImpl)argResults[0]).setJavaCodeFragment(code);
        return argResults[0];
    }

}

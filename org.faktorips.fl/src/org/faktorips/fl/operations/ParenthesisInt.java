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

package org.faktorips.fl.operations;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;

/**
 * Parenthesis <i>()</i> operator for datatype primitive integer.
 */
public class ParenthesisInt extends AbstractUnaryOperation {

    public ParenthesisInt() {
        super(Datatype.PRIMITIVE_INT, "()");
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        JavaCodeFragment parenthesis = new JavaCodeFragment();
        parenthesis.append('(');
        parenthesis.append(arg.getCodeFragment());
        parenthesis.append(')');
        CompilationResultImpl result = new CompilationResultImpl(parenthesis, arg.getDatatype());
        result.addMessages(arg.getMessages());
        return result;
    }

}

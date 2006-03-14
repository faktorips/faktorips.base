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

package org.faktorips.fl.operations;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Minus (-) operator for datatype Integer.
 */
public class MinusInteger extends AbstractUnaryOperation {

    public MinusInteger() {
        super(Datatype.INTEGER, "-"); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.UnaryOperation#generate(org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        if (arg.failed()) {
            return arg;
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append('(');
        fragment.append(arg.getCodeFragment());
        fragment.append("==null?null:new Integer(-1 * "); //$NON-NLS-1$
        fragment.append(arg.getCodeFragment());
        fragment.append(".intValue()))"); //$NON-NLS-1$
        CompilationResultImpl result = new CompilationResultImpl(fragment, Datatype.INTEGER);
        result.addMessages(arg.getMessages());
        return result;
    }

}

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

package org.faktorips.fl.functions;

import java.math.BigDecimal;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.util.ArgumentCheck;

/**
 *
 */
public class WholeNumber extends AbstractFlFunction {

    /**
     */
    public WholeNumber(String name, String description) {
        super(name, description, Datatype.INTEGER, new Datatype[] { Datatype.DECIMAL });
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FlFunction#compile(CompilationResult[])
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        ArgumentCheck.length(argResults, 1);
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new "); //$NON-NLS-1$
        fragment.appendClassName(Integer.class);
        fragment.append('(');
        fragment.append(argResults[0].getCodeFragment());
        fragment.append(".setScale(0, "); //$NON-NLS-1$
        fragment.appendClassName(BigDecimal.class);
        fragment.append(".ROUND_DOWN).intValue())"); //$NON-NLS-1$
        return new CompilationResultImpl(fragment, Datatype.INTEGER);
    }

}

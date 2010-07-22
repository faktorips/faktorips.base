/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.values.Decimal;

/**
 * 
 * @author Jan Ortmann
 */
public class DecimalNullFct extends AbstractFlFunction {

    public DecimalNullFct() {
        super("DECIMALNULL", "", Datatype.DECIMAL, new Datatype[0]);
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        JavaCodeFragment code = new JavaCodeFragment();
        code.appendClassName(Decimal.class);
        code.append(".NULL");
        return new CompilationResultImpl(code, Datatype.DECIMAL);
    }

}

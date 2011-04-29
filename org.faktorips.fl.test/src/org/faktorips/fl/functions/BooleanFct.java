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

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * Function to create a Boolean-Object for testing purposes.
 * 
 * @author Jan Ortmann
 */
public class BooleanFct extends AbstractFlFunction {

    private Boolean value;

    public BooleanFct(String name, Boolean value) {
        super(name, name, Datatype.BOOLEAN, new Datatype[0]);
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        if (value == null) {
            return new CompilationResultImpl("((Boolean)null)", Datatype.BOOLEAN);
        } else {
            return new CompilationResultImpl("new Boolean(" + value + ")", Datatype.BOOLEAN);
        }
    }

}

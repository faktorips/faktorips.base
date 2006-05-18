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
        if (value==null) {
            return new CompilationResultImpl("((Boolean)null)", Datatype.BOOLEAN);
        } else {
            return new CompilationResultImpl("new Boolean(" + value + ")", Datatype.BOOLEAN);
        }
    }

}

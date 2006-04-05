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

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.PrimitiveDatatypeHelper;
import org.faktorips.datatype.Datatype;

/**
 * Abstract base class for all primtive datatypes.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractPrimitiveDatatypeHelper extends AbstractDatatypeHelper implements
        PrimitiveDatatypeHelper {

    public AbstractPrimitiveDatatypeHelper() {
        super();
    }

    public AbstractPrimitiveDatatypeHelper(Datatype datatype) {
        super(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        return valueOfExpression(expression);
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment nullExpression() {
        throw new RuntimeException("Primitive datatype does not support null.");
    }
}

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
import org.faktorips.datatype.Datatype;

/**
 * A helpper class for <code>ArrayOfValueDatatype</code>. ValueOf and newInstance expressions are not
 * supported by this helper. A call to these method returns a "null" fragment.
 * 
 * @author Peter Erzberger
 */
public class ArrayOfValueDatatypeHelper extends AbstractDatatypeHelper {

    public ArrayOfValueDatatypeHelper() {
        super();
    }

    public ArrayOfValueDatatypeHelper(Datatype datatype) {
        super(datatype);
    }

    /**
     * Returns a "null" fragment.
     * 
     * {@inheritDoc}
     */
    protected JavaCodeFragment valueOfExpression(String expression) {
        return nullExpression();
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null");
    }

    /**
     * Returns a "null" fragment.
     * 
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstance(String value) {
        return nullExpression();
    }
}

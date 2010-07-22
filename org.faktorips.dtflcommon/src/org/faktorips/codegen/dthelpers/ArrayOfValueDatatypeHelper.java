/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;

/**
 * A helpper class for <code>ArrayOfValueDatatype</code>. ValueOf and newInstance expressions are
 * not supported by this helper. A call to these method returns a "null" fragment.
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
    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        return nullExpression();
    }

    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null"); //$NON-NLS-1$
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

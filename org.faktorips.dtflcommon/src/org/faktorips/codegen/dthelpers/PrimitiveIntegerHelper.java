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

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.PrimitiveIntegerDatatype;

/**
 * DatatypeHelper for datatype PrimitiveInteger.
 */
public class PrimitiveIntegerHelper extends AbstractPrimitiveDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public PrimitiveIntegerHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given primitive integer datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public PrimitiveIntegerHelper(PrimitiveIntegerDatatype datatype) {
        super(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstance(String value) {
        return new JavaCodeFragment(value);
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment toWrapper(JavaCodeFragment expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new ");
        fragment.appendClassName(Integer.class);
        fragment.append('(');
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("Integer.parseInt(" + expression + ")");
        return fragment;
    }
}

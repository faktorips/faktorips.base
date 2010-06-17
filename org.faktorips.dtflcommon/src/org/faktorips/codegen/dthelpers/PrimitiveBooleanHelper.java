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
import org.faktorips.datatype.PrimitiveBooleanDatatype;

public class PrimitiveBooleanHelper extends AbstractPrimitiveDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public PrimitiveBooleanHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given primitive boolean datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public PrimitiveBooleanHelper(PrimitiveBooleanDatatype datatype) {
        super(datatype);
    }

    public JavaCodeFragment newInstance(String value) {
        Boolean booleanValue = Boolean.valueOf(value);
        if (booleanValue.booleanValue()) {
            return new JavaCodeFragment("true"); //$NON-NLS-1$
        } else {
            return new JavaCodeFragment("false"); //$NON-NLS-1$
        }
    }

    public JavaCodeFragment toWrapper(JavaCodeFragment expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Boolean.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("Boolean.valueOf(" + expression + ").booleanValue()"); //$NON-NLS-1$//$NON-NLS-2$
        return fragment;
    }

}

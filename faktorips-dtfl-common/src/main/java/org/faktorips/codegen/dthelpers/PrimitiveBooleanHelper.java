/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.PrimitiveBooleanDatatype;

/**
 * {@link DatatypeHelper} for {@link PrimitiveBooleanDatatype}.
 */
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

    @Override
    public DatatypeHelper getWrapperTypeHelper() {
        return DatatypeHelper.BOOLEAN;
    }

    @Override
    public String getJavaClassName() {
        return Boolean.TYPE.getName();
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        boolean booleanValue = Boolean.parseBoolean(value);
        if (booleanValue) {
            return new JavaCodeFragment("true"); //$NON-NLS-1$
        } else {
            return new JavaCodeFragment("false"); //$NON-NLS-1$
        }
    }

    @Override
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

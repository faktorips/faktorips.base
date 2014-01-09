/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.PrimitiveIntegerDatatype;

/**
 * {@link DatatypeHelper} for {@link PrimitiveIntegerDatatype}.
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

    public JavaCodeFragment newInstance(String value) {
        return new JavaCodeFragment(value);
    }

    public JavaCodeFragment toWrapper(JavaCodeFragment expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Integer.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("Integer.parseInt(" + expression + ")"); //$NON-NLS-1$//$NON-NLS-2$
        return fragment;
    }

}

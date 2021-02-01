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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.PrimitiveDatatypeHelper;
import org.faktorips.datatype.Datatype;

/**
 * Abstract base class for all primitive datatypes.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractPrimitiveDatatypeHelper extends AbstractDatatypeHelper
        implements PrimitiveDatatypeHelper {

    public AbstractPrimitiveDatatypeHelper() {
        super();
    }

    public AbstractPrimitiveDatatypeHelper(Datatype datatype) {
        super(datatype);
    }

    @Override
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        return valueOfExpression(expression);
    }

    @Override
    public JavaCodeFragment nullExpression() {
        throw new RuntimeException("Primitive datatype does not support null."); //$NON-NLS-1$
    }

    public JavaCodeFragment newEnumValueSetInstance(@SuppressWarnings("unused") JavaCodeFragment valueCollection,
            @SuppressWarnings("unused") JavaCodeFragment containsNullExpression) {

        throw new UnsupportedOperationException("Call the helper of the wrapper type instead."); //$NON-NLS-1$
    }

    public JavaCodeFragment newEnumValueSetInstance(@SuppressWarnings("unused") String[] values,
            @SuppressWarnings("unused") boolean containsNull) {
        throw new UnsupportedOperationException("Call the helper of the wrapper type instead."); //$NON-NLS-1$
    }

    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("String.valueOf("); //$NON-NLS-1$
        fragment.append(fieldName);
        fragment.append(")"); //$NON-NLS-1$
        return fragment;
    }

}

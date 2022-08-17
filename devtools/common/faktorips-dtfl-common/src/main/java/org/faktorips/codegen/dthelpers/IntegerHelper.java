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
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.valueset.IntegerRange;

/**
 * {@link DatatypeHelper} for {@link IntegerDatatype}.
 */
public class IntegerHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public IntegerHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given integer datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public IntegerHelper(IntegerDatatype datatype) {
        super(datatype);
    }

    @Override
    public String getJavaClassName() {
        return Integer.class.getName();
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        if (IpsStringUtils.isEmpty(value)) {
            return nullExpression();
        }
        String trimmedValue = value.trim();
        if (trimmedValue.charAt(0) != '0') {
            return valueOfExpression(trimmedValue);
        }
        // if value starts with a leading zero, we must generate Integer.valueOf("08") as
        // Integer.valueOf(08) won't compile (try it out!)
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Integer.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.appendQuoted(trimmedValue);
        fragment.append(')');
        return fragment;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (IpsStringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Integer.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return IntegerRange.class.getName();
    }

    @Override
    public JavaCodeFragment newRangeInstance(JavaCodeFragment lowerBoundExp,
            JavaCodeFragment upperBoundExp,
            JavaCodeFragment stepExp,
            JavaCodeFragment containsNullExp,
            boolean useTypesafeCollections) {

        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendClassName(getRangeJavaClassName(useTypesafeCollections));
        frag.append(".valueOf("); //$NON-NLS-1$
        frag.append(lowerBoundExp);
        frag.append(", "); //$NON-NLS-1$
        frag.append(upperBoundExp);
        frag.append(", "); //$NON-NLS-1$
        frag.append(stepExp);
        frag.append(", "); //$NON-NLS-1$
        frag.append(containsNullExp);
        frag.append(")"); //$NON-NLS-1$
        return frag;
    }

}

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

import java.util.regex.Pattern;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.PrimitiveLongDatatype;

/**
 * {@link DatatypeHelper} for {@link PrimitiveLongDatatype}.
 */
public class PrimitiveLongHelper extends AbstractPrimitiveDatatypeHelper {

    private static final String L = "L"; //$NON-NLS-1$
    private static final Pattern DIGITS = Pattern.compile("[0-9+-]+"); //$NON-NLS-1$

    /**
     * Constructs a new helper.
     */
    public PrimitiveLongHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given primitive long datatype.
     *
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public PrimitiveLongHelper(PrimitiveLongDatatype datatype) {
        super(datatype);
    }

    @Override
    public DatatypeHelper getWrapperTypeHelper() {
        return DatatypeHelper.LONG;
    }

    @Override
    public String getJavaClassName() {
        return Long.TYPE.getName();
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        return new JavaCodeFragment(longLiteral(value));
    }

    /**
     * Appends a trailing 'L' to the given value if it is a number
     * <p>
     * see {@link Long#parseLong(String, int)} for the length check.
     */
    protected static String longLiteral(String value) {
        String longLiteral = value;
        if (longLiteral != null && longLiteral.length() >= 1) {
            if (DIGITS.matcher(longLiteral).matches()) {
                longLiteral += L;
            }
        }
        return longLiteral;
    }

    @Override
    public JavaCodeFragment toWrapper(JavaCodeFragment expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Long.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("Long.parseLong(" + longLiteral(expression) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        return fragment;
    }

}

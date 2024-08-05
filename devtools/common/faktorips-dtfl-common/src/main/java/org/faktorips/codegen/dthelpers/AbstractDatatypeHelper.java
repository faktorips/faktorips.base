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
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.values.ObjectUtil;
import org.faktorips.valueset.OrderedValueSet;

/**
 * Abstract base class for datatype helpers.
 *
 * @author Jan Ortmann
 */
public abstract class AbstractDatatypeHelper implements DatatypeHelper {

    private Datatype datatype;

    /**
     * Constructs a new helper without initializing the datatype.
     */
    public AbstractDatatypeHelper() {
        // Provides default constructor
    }

    /**
     * Constructs a new helper for the given datatype.
     */
    public AbstractDatatypeHelper(Datatype datatype) {
        ArgumentCheck.notNull(datatype);
        this.datatype = datatype;
    }

    @Override
    public Datatype getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(Datatype datatype) {
        this.datatype = datatype;
    }

    /**
     * This method is supposed to be overridden by subclasses.
     * <p>
     * It is used within the <code>newInstanceFromExpression(String)</code> method. It returns a
     * <code>JavaCodeFragment</code> with sourcecode that creates an instance of the datatype's Java
     * class with the given expression.
     * <p>
     * If the expression is <code>null</code> the fragment's sourcecode is either the String "null"
     * or the sourcecode to get an instance of the appropriate null object. Preconditions:
     * Expression may not be null or empty. When evaluated the expression must return a String.
     */
    protected abstract JavaCodeFragment valueOfExpression(String expression);

    @Override
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        return newInstanceFromExpression(expression, true);
    }

    @Override
    public JavaCodeFragment newInstanceFromExpression(String expression, boolean checkForNull) {
        if (expression == null || expression.length() == 0) {
            return nullExpression();
        }

        String resultingExpression = expression;
        if (resultingExpression.startsWith("(")) { //$NON-NLS-1$
            resultingExpression = '(' + resultingExpression + ')';
        }
        if (!checkForNull) {
            return valueOfExpression(resultingExpression);
        }
        return generateNewInstanceWithStringUtils(resultingExpression);
    }

    private JavaCodeFragment generateNewInstanceWithStringUtils(String expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(IpsStringUtils.class).append(".isEmpty(") //$NON-NLS-1$
                .append(expression).append(") ? "); //$NON-NLS-1$
        fragment.append(nullExpression());
        fragment.append(" : "); //$NON-NLS-1$
        fragment.append(valueOfExpression(expression));
        return fragment;
    }

    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return null;
    }

    @Override
    public JavaCodeFragment newRangeInstance(JavaCodeFragment lowerBoundExp,
            JavaCodeFragment upperBoundExp,
            JavaCodeFragment stepExp,
            JavaCodeFragment containsNullExp,
            boolean useTypesafeCollections) {

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Code sample
     *
     * <pre>
     *  (IEnumValueSet)new DefaultEnumValueSet&lt;&gt;(
     *      true,
     *      GeneratedGender.getGeneratedGender(null),
     *      GeneratedGender.getGeneratedGender(Integer.valueOf(1)),
     *      GeneratedGender.getGeneratedGender(Integer.valueOf(2)));
     * </pre>
     */
    @Override
    public JavaCodeFragment newEnumValueSetInstance(String[] values,
            boolean containsNull,
            boolean useTypesafeCollections) {

        JavaCodeFragment frag = new JavaCodeFragment();
        frag.append("new "); //$NON-NLS-1$
        frag.appendClassName(OrderedValueSet.class);
        frag.append("<>("); //$NON-NLS-1$
        frag.append(containsNull);
        frag.append(", "); //$NON-NLS-1$
        frag.append(newValueInstance(null));
        for (String value : values) {
            frag.append(", "); //$NON-NLS-1$
            if (values.length == 1 && null == value) {
                frag.append(createCastExpression(value));
            } else {
                frag.append(newValueInstance(value));
            }
        }
        frag.appendln(")"); //$NON-NLS-1$
        return frag;
    }

    @Override
    public JavaCodeFragment newEnumValueSetInstance(JavaCodeFragment valueCollection,
            JavaCodeFragment containsNullExpression,
            boolean useTypesafeCollections,
            boolean virtual) {

        JavaCodeFragment frag = new JavaCodeFragment();
        frag.append("new "); //$NON-NLS-1$
        frag.appendClassName(OrderedValueSet.class);
        frag.append("<>"); //$NON-NLS-1$
        frag.append("("); //$NON-NLS-1$
        frag.append(valueCollection);
        frag.append(", "); //$NON-NLS-1$
        frag.append(containsNullExpression);
        frag.append(", "); //$NON-NLS-1$
        frag.append(nullExpression());
        if (virtual) {
            frag.append(", "); //$NON-NLS-1$
            frag.append(Boolean.toString(virtual));
        }
        frag.appendln(")"); //$NON-NLS-1$
        return frag;
    }

    @Override
    public JavaCodeFragment referenceOrSafeCopyIfNeccessary(String expression) {
        if (datatype.isValueDatatype() && ((ValueDatatype)datatype).isMutable()) {
            return newSafeCopy(expression);
        }
        return new JavaCodeFragment(expression);
    }

    /**
     * Helpers for immutable datatypes must override this method to create a copy of the value given
     * in the expression.
     *
     * @param expression The expression of which you want to get the new safe copy code fragment
     *            from
     */
    protected JavaCodeFragment newSafeCopy(String expression) {
        throw new RuntimeException("The DatatypeHelper for datatype " + datatype //$NON-NLS-1$
                + " does not override the method newSafeCopy!"); //$NON-NLS-1$
    }

    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        return new JavaCodeFragment().appendClassName(ObjectUtil.class).append(".isNull(").append(fieldName) //$NON-NLS-1$
                .append(")").append(" ? null : ") //$NON-NLS-1$ //$NON-NLS-2$
                .append(fieldName).append(".toString()"); //$NON-NLS-1$
    }

    /**
     * Returns {@code "null"}.
     *
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null"); //$NON-NLS-1$
    }

    @Override
    public JavaCodeFragment createCastExpression(String bound) {
        JavaCodeFragment frag = new JavaCodeFragment();
        if (IpsStringUtils.isEmpty(bound) && !getDatatype().hasNullObject()) {
            frag.append('(');
            frag.appendClassName(getJavaClassName());
            frag.append(')');
        }
        frag.append(newValueInstance(bound));
        return frag;
    }

}

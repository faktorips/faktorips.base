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
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.Decimal;
import org.faktorips.valueset.DecimalRange;

/**
 * {@link DatatypeHelper} for {@link DecimalDatatype}.
 */
public class DecimalHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public DecimalHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given decimal datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public DecimalHelper(DecimalDatatype datatype) {
        super(datatype);
    }

    @Override
    public String getJavaClassName() {
        return Decimal.class.getName();
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        if (IpsStringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Decimal.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.appendQuoted(value);
        fragment.append(')');
        return fragment;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Decimal.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    /**
     * Methode der Oberklasse wird ueberschrieben, weil bei diesem Datentyp valueOf-Methode selbst
     * Null-Expression zurueckgeben kann
     */
    @Override
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        return valueOfExpression(expression);
    }

    @Override
    public JavaCodeFragment nullExpression() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Decimal.class);
        fragment.append(".NULL"); //$NON-NLS-1$
        return fragment;
    }

    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return DecimalRange.class.getName();
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

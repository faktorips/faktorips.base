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

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.faktorips.values.Money;
import org.faktorips.valueset.MoneyRange;

/**
 * {@link DatatypeHelper} for {@link MoneyDatatype}.
 */
public class MoneyHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public MoneyHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given money datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public MoneyHelper(MoneyDatatype datatype) {
        super(datatype);
    }

    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Money.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.appendQuoted(value);
        fragment.append(')');
        return fragment;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Money.class);
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
        fragment.appendClassName(Money.class);
        fragment.append(".NULL"); //$NON-NLS-1$
        return fragment;
    }

    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return MoneyRange.class.getName();
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

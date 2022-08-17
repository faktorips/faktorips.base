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

import java.math.BigDecimal;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.BigDecimalDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.valueset.BigDecimalRange;

/**
 * {@link DatatypeHelper} for {@link BigDecimalDatatype}.
 * 
 * 
 * @author Jan Ortmann
 */
public class BigDecimalHelper extends AbstractDatatypeHelper {

    public BigDecimalHelper() {
        // Provides default constructor.
    }

    public BigDecimalHelper(BigDecimalDatatype datatype) {
        super(datatype);
    }

    @Override
    public String getJavaClassName() {
        return BigDecimal.class.getName();
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        if (IpsStringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment code = new JavaCodeFragment("new "); //$NON-NLS-1$
        code.appendClassName(BigDecimal.class);
        code.append('(');
        code.appendQuoted(value);
        code.append(')');
        return code;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment code = new JavaCodeFragment("new "); //$NON-NLS-1$
        code.appendClassName(BigDecimal.class);
        code.append('(');
        code.append(expression);
        code.append(')');
        return code;
    }

    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return BigDecimalRange.class.getName();
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

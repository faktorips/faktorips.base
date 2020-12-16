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

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.DoubleDatatype;
import org.faktorips.valueset.DoubleRange;

/**
 * {@link DatatypeHelper} for {@link DoubleDatatype}.
 * 
 * 
 * @author Jan Ortmann
 */
public class DoubleHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public DoubleHelper() {
        super();
    }

    public DoubleHelper(DoubleDatatype datatype) {
        super(datatype);
    }

    @Override
    public String getJavaClassName() {
        return Double.class.getName();
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        return newInstance(expression);
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment ment = new JavaCodeFragment();
        ment.append("new "); //$NON-NLS-1$
        ment.appendClassName(Double.class);
        ment.append('(');
        ment.append(value);
        ment.append(')');
        return ment;
    }

    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return DoubleRange.class.getName();
    }

    @Override
    public JavaCodeFragment newRangeInstance(JavaCodeFragment lowerBoundExp,
            JavaCodeFragment upperBoundExp,
            JavaCodeFragment stepExp,
            JavaCodeFragment containsNullExp,
            boolean useTypesafeCollections) {

        return new JavaCodeFragment().appendClassName(getRangeJavaClassName(useTypesafeCollections))
                .append(".valueOf(") //$NON-NLS-1$
                .append(lowerBoundExp).append(", ") //$NON-NLS-1$
                .append(upperBoundExp).append(", ") //$NON-NLS-1$
                .append(stepExp).append(", ") //$NON-NLS-1$
                .append(containsNullExp).append(")"); //$NON-NLS-1$
    }
}

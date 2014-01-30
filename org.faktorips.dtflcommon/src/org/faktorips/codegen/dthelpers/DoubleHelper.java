/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
    protected JavaCodeFragment valueOfExpression(String expression) {
        return newInstance(expression);
    }

    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new "); //$NON-NLS-1$
        fragment.appendClassName(Double.class);
        fragment.append('(');
        fragment.append(value);
        fragment.append(')');
        return fragment;
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

        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendClassName(getRangeJavaClassName(useTypesafeCollections));
        frag.append(".valueOf("); //$NON-NLS-1$
        frag.append(lowerBoundExp);
        frag.append(", "); //$NON-NLS-1$
        frag.append(upperBoundExp);
        frag.append(", "); //$NON-NLS-1$
        frag.append(containsNullExp);
        frag.append(")"); //$NON-NLS-1$
        return frag;
    }

}

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
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * {@link DatatypeHelper} for {@link BooleanDatatype}.
 */
public class BooleanHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public BooleanHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given boolean datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public BooleanHelper(BooleanDatatype datatype) {
        super(datatype);
    }

    @Override
    public String getJavaClassName() {
        return Boolean.class.getName();
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        if (IpsStringUtils.isEmpty(value)) {
            return nullExpression();
        }
        boolean booleanValue = Boolean.parseBoolean(value);
        if (booleanValue) {
            return new JavaCodeFragment("Boolean.TRUE"); //$NON-NLS-1$
        } else {
            return new JavaCodeFragment("Boolean.FALSE"); //$NON-NLS-1$
        }
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Boolean.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

}

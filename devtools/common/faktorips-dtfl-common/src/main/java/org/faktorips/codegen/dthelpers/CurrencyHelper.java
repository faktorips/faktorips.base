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

import java.util.Currency;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.CurrencyDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * {@link DatatypeHelper} for {@link CurrencyDatatype}.
 */
public class CurrencyHelper extends AbstractDatatypeHelper {

    public CurrencyHelper() {
        super();
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        if (IpsStringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Currency.class);
        fragment.append(".getInstance("); //$NON-NLS-1$
        fragment.appendQuoted(value);
        fragment.append(')');
        return fragment;
    }

    @Override
    public String getJavaClassName() {
        return Currency.class.getName();
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (IpsStringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Currency.class);
        fragment.append(".getInstance("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

}

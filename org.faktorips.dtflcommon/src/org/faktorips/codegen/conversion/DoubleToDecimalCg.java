/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;

public class DoubleToDecimalCg extends AbstractSingleConversionCg {

    public DoubleToDecimalCg() {
        super(Datatype.DOUBLE, Datatype.DECIMAL);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        // Decimal.valueOf(fromValue)
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Decimal.class).append(".valueOf(").append(fromValue).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        return fragment;
    }

}

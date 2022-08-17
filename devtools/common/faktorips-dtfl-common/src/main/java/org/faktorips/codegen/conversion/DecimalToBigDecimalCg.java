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

public class DecimalToBigDecimalCg extends AbstractSingleConversionCg {

    public DecimalToBigDecimalCg() {
        super(Datatype.DECIMAL, Datatype.BIG_DECIMAL);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        // Decimal.valueOf(fromValue)
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(fromValue).append(".bigDecimalValue()"); //$NON-NLS-1$
        return fragment;
    }

}

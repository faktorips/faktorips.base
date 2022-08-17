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

public class PrimitiveIntToDecimalCg extends AbstractSingleConversionCg {

    public PrimitiveIntToDecimalCg() {
        super(Datatype.PRIMITIVE_INT, Datatype.DECIMAL);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Decimal.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(fromValue);
        fragment.append(", 0)"); //$NON-NLS-1$
        return fragment;
    }

}

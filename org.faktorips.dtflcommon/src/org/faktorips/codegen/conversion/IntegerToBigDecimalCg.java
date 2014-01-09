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

package org.faktorips.codegen.conversion;

import java.math.BigDecimal;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;

public class IntegerToBigDecimalCg extends AbstractSingleConversionCg {

    public IntegerToBigDecimalCg() {
        super(Datatype.INTEGER, Datatype.BIG_DECIMAL);
    }

    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(BigDecimal.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(fromValue);
        fragment.append(')');
        return fragment;
    }

}

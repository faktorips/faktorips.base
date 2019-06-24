/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

public class IntegerToLongCg extends AbstractSingleConversionCg {

    public IntegerToLongCg() {
        super(Datatype.INTEGER, Datatype.LONG);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        // Long.valueOf(Integer.valueOf(1).intValue())
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Long.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(fromValue);
        fragment.append(".intValue())"); //$NON-NLS-1$
        return fragment;
    }

}

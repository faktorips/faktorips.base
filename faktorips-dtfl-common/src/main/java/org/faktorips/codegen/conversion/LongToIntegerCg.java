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

public class LongToIntegerCg extends AbstractSingleConversionCg {

    public LongToIntegerCg() {
        super(Datatype.LONG, Datatype.INTEGER);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        // Integer.valueOf(Long.valueOf(1).intValue())
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Integer.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(fromValue);
        fragment.append(".intValue())"); //$NON-NLS-1$
        return fragment;
    }

}

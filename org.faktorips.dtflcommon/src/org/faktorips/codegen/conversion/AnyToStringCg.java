/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;

public class AnyToStringCg extends AbstractSingleConversionCg {

    public AnyToStringCg() {
        super(AnyDatatype.INSTANCE, Datatype.STRING);
    }

    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(String.class);
        fragment.append(".valueOf("); //$NON-NLS-1$ 
        fragment.append(fromValue);
        fragment.append(")"); //$NON-NLS-1$ 
        return fragment;
    }
}

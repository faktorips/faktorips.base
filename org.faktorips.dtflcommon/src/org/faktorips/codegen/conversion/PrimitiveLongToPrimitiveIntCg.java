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

public class PrimitiveLongToPrimitiveIntCg extends AbstractSingleConversionCg {

    public PrimitiveLongToPrimitiveIntCg() {
        super(Datatype.PRIMITIVE_LONG, Datatype.PRIMITIVE_INT);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("(int)"); //$NON-NLS-1$
        fragment.append(fromValue);
        return fragment;
    }

}

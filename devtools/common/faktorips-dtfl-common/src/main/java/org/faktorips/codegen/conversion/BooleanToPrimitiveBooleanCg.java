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

public class BooleanToPrimitiveBooleanCg extends AbstractSingleConversionCg {

    public BooleanToPrimitiveBooleanCg() {
        super(Datatype.BOOLEAN, Datatype.PRIMITIVE_BOOLEAN);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        fromValue.append(".booleanValue()"); //$NON-NLS-1$
        return fromValue;
    }

}

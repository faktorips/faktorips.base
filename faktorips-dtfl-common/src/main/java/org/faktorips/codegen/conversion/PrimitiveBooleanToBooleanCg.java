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
import org.faktorips.codegen.dthelpers.PrimitiveBooleanHelper;
import org.faktorips.datatype.Datatype;

public class PrimitiveBooleanToBooleanCg extends AbstractSingleConversionCg {

    public PrimitiveBooleanToBooleanCg() {
        super(Datatype.PRIMITIVE_BOOLEAN, Datatype.BOOLEAN);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        return new PrimitiveBooleanHelper(Datatype.PRIMITIVE_BOOLEAN).toWrapper(fromValue);
    }

}

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
import org.faktorips.codegen.dthelpers.PrimitiveBooleanHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveBooleanDatatype;

public class PrimitiveBooleanToBooleanCg extends AbstractSingleConversionCg {

    public PrimitiveBooleanToBooleanCg() {
        super(Datatype.PRIMITIVE_BOOLEAN, Datatype.BOOLEAN);
    }

    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        return new PrimitiveBooleanHelper((PrimitiveBooleanDatatype)Datatype.PRIMITIVE_BOOLEAN).toWrapper(fromValue);
    }

}

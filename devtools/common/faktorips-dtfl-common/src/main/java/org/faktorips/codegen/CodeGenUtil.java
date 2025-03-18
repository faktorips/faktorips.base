/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

import org.faktorips.codegen.dthelpers.PrimitiveBooleanHelper;
import org.faktorips.codegen.dthelpers.PrimitiveIntegerHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;

/**
 * A collection of utility methods related to sourcecode generation.
 */
public class CodeGenUtil {

    private CodeGenUtil() {
        // Utility class not to be instantiated.
    }

    public static final JavaCodeFragment convertPrimitiveToWrapper(Datatype type, JavaCodeFragment expression) {
        if (type instanceof PrimitiveBooleanDatatype) {
            return new PrimitiveBooleanHelper((PrimitiveBooleanDatatype)type).toWrapper(expression);
        }
        if (type instanceof PrimitiveIntegerDatatype) {
            return new PrimitiveIntegerHelper((PrimitiveIntegerDatatype)type).toWrapper(expression);
        }
        throw new IllegalArgumentException("Can't convert dataype " + type); //$NON-NLS-1$
    }
}

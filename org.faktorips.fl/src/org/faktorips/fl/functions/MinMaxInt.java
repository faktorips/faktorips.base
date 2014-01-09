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

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;

/**
 *
 */
public class MinMaxInt extends MinMaxNativeTypes {

    public MinMaxInt(String name, String description, boolean isMax) {
        super(name, description, Datatype.PRIMITIVE_INT, isMax);
    }

    @Override
    protected void writeBody(JavaCodeFragment fragment,
            CompilationResult<JavaCodeFragment> first,
            CompilationResult<JavaCodeFragment> second) {
        // Math.max(value1, value2)
        fragment.append("Math.");
        fragment.append(functionName);
        fragment.append('(');
        fragment.append(first.getCodeFragment());
        fragment.append(", ");
        fragment.append(second.getCodeFragment());
        fragment.append(")");
    }
}

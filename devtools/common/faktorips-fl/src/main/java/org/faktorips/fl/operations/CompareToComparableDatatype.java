/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;

/**
 * This is a generic operation for comparable datatypes to. It allows to use greater than, greater
 * or equal, less than and less or equal, according to the operator. Important for this generic
 * operation is, that the given operator is the same as it is used in java code! It is simply
 * transformed to be used with the {@link Comparable#compareTo(Object)} method comparing zero.
 * <p>
 * Example:<br>
 * For the operator '&lt;' we simply generate <em>(lhs.compareTo(rhs) &lt; 0)'</em>
 */
public class CompareToComparableDatatype extends AbstractBinaryJavaOperation {

    public CompareToComparableDatatype(String operator, Datatype type) {
        super(operator, type, type);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.operations.AbstractBinaryJavaOperation#generate(org.faktorips.fl.CompilationResultImpl,
     *          org.faktorips.fl.CompilationResultImpl)
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        JavaCodeFragment result = new JavaCodeFragment();
        result.append("(").append(lhs.getCodeFragment()).append(".compareTo(");
        result.append(rhs.getCodeFragment()).append(") ").append(getOperator()).append(" 0)");
        return new CompilationResultImpl(result, Datatype.PRIMITIVE_BOOLEAN);
    }

}

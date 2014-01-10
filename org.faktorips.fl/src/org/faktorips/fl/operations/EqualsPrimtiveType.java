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

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.util.ArgumentCheck;

/**
 * Operation for the equality check for two decimals.
 */
public class EqualsPrimtiveType extends AbstractBinaryJavaOperation {

    public EqualsPrimtiveType(Datatype primitiveType) {
        super("=", primitiveType, primitiveType); //$NON-NLS-1$
        ArgumentCheck.isTrue(primitiveType.isPrimitive());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        lhs.getCodeFragment().append("=="); //$NON-NLS-1$
        lhs.add(rhs);
        lhs.setDatatype(Datatype.PRIMITIVE_BOOLEAN);
        return lhs;
    }

}

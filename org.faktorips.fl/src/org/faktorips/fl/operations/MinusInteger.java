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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.Operation;

/**
 * Minus (-) operator for datatype Integer.
 */
public class MinusInteger extends AbstractUnaryJavaOperation {

    public MinusInteger() {
        super(Operation.MinusInteger);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.UnaryOperation#generate(org.faktorips.fl.CompilationResult)
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        if (arg.failed()) {
            return arg;
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append('(');
        fragment.append(arg.getCodeFragment());
        fragment.append("==null?null:Integer.valueOf(-1 * "); //$NON-NLS-1$
        fragment.append(arg.getCodeFragment());
        fragment.append(".intValue()))"); //$NON-NLS-1$
        arg.setCodeFragment(fragment);
        return arg;
    }

}

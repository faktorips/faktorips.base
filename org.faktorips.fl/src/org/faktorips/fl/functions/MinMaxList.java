/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.FunctionSignatures;

public class MinMaxList extends AbstractListFunction {

    public MinMaxList(String name, String description, boolean isMax) {
        super(name, description, isMax ? FunctionSignatures.MaxList : FunctionSignatures.MinList);
    }

    @Override
    protected JavaCodeFragment generateReturnFallBackValueCall() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("throw new ");
        fragment.appendClassName(IllegalArgumentException.class);
        fragment.append("(\"List argument is empty or null\")");
        return fragment;
    }
}

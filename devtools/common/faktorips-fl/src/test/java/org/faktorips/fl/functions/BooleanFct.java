/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * Function to create a Boolean-Object for testing purposes.
 * 
 * @author Jan Ortmann
 */
public class BooleanFct extends AbstractFlFunction {

    private Boolean value;

    public BooleanFct(String name, Boolean value) {
        super(name, name, Datatype.BOOLEAN, new Datatype[0]);
        this.value = value;
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        if (value == null) {
            return new CompilationResultImpl("((Boolean)null)", Datatype.BOOLEAN);
        } else {
            return new CompilationResultImpl("new Boolean(" + value + ")", Datatype.BOOLEAN);
        }
    }

}

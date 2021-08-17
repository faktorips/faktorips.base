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
import org.faktorips.util.ArgumentCheck;

/**
 *
 */
public class MinMaxComparableDatatypes extends AbstractFlFunction {

    private boolean isMax;
    private Datatype datatype;

    public MinMaxComparableDatatypes(String name, String description, boolean isMax, Datatype datatype) {
        super(name, description, datatype, new Datatype[] { datatype, datatype });
        this.isMax = isMax;
        this.datatype = datatype;
    }

    /**
     * Max: (p1.compareTo(p2) &gt; 0 ? p1 : p2)
     * <p>
     * Min: (p1.compareTo(p2) &lt; 0 ? p1 : p2)
     */
    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);
        JavaCodeFragment p1 = argResults[0].getCodeFragment();
        JavaCodeFragment p2 = argResults[1].getCodeFragment();
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("(").append(p1).append(".compareTo(").append(p2).append(")");
        if (isMax) {
            fragment.append(" > ");
        } else {
            fragment.append(" < ");
        }
        fragment.append("0 ? ").append(p1).append(" : ").append(p2).append(")");

        CompilationResultImpl result = new CompilationResultImpl(fragment, datatype);
        result.addMessages(argResults[0].getMessages());
        result.addMessages(argResults[1].getMessages());
        return result;
    }

}

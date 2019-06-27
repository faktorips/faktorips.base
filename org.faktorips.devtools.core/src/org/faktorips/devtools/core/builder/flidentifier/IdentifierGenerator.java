/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.fl.CompilationResult;

/**
 * Translates {@link IdentifierNode IdetifierNodes} into a {@link CompilationResult}.
 * 
 * @author widmaier
 */
public interface IdentifierGenerator<T extends CodeFragment> {

    /**
     * Translates a list of {@link IdentifierNode IdetifierNodes}, given the head of the linked
     * list, into a {@link CompilationResult}. The code returned via the {@link CompilationResult}
     * is always a single line of code that is composed of chained statements and/or function calls.
     */
    public CompilationResult<T> generateIdentifiers(IdentifierNode headIdentifierNode);

}

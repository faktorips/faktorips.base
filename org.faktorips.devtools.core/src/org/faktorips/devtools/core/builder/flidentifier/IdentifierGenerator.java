/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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

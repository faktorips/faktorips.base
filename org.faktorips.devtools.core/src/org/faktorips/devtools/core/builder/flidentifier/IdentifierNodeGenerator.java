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

public interface IdentifierNodeGenerator<T extends CodeFragment> {

    /**
     * Builds the code for the given {@link IdentifierNode} and all its successors.
     * 
     * @param identifierNode the {@link IdentifierNode} to generate code for.
     * @param contextCompilationResult the java code that is used as context for the code that will
     *            be generated.
     * @return the {@link CompilationResult} containing code and/or error messages.
     */
    public CompilationResult<T> generateNode(IdentifierNode identifierNode, CompilationResult<T> contextCompilationResult);
}

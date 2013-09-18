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
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.fl.CompilationResult;

public class ParameterIdentifierGenerator<T extends CodeFragment> implements IdentifierGenerator<T> {

    private final IdentifierNodeGeneratorFactory<T> factory;

    public ParameterIdentifierGenerator(IdentifierNodeGeneratorFactory<T> factory) {
        this.factory = factory;
    }

    @Override
    public CompilationResult<T> generateIdentifiers(IdentifierNode headIdentifierNode) {
        IdentifierNodeGenerator<T> nodeGenerator = IdentifierNodeType.getNodeType(headIdentifierNode.getClass())
                .getGeneratorFor(factory);
        return nodeGenerator.generateNode(headIdentifierNode, null);
    }

}

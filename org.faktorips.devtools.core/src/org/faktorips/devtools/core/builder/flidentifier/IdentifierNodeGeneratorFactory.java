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

/**
 * Creates a specific {@link IdentifierNodeGenerator node builder} for each type of
 * {@link IdentifierNode node}.
 * 
 * @author widmaier
 */
public interface IdentifierNodeGeneratorFactory<T extends CodeFragment> {

    public IdentifierNodeGenerator<T> getGeneratorForParameterNode();

    public IdentifierNodeGenerator<T> getGeneratorForAssociationNode();

    public IdentifierNodeGenerator<T> getGeneratorForAttributeNode();

    public IdentifierNodeGenerator<T> getGeneratorForEnumClassNode();

    public IdentifierNodeGenerator<T> getGeneratorForEnumValueNode();

    public IdentifierNodeGenerator<T> getGeneratorForIndexBasedAssociationNode();

    public IdentifierNodeGenerator<T> getGeneratorForQualifiedAssociationNode();

    public IdentifierNodeGenerator<T> getGeneratorForInvalidNode();

}

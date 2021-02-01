/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;

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

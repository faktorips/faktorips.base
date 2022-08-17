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

    IdentifierNodeGenerator<T> getGeneratorForParameterNode();

    IdentifierNodeGenerator<T> getGeneratorForAssociationNode();

    IdentifierNodeGenerator<T> getGeneratorForAttributeNode();

    IdentifierNodeGenerator<T> getGeneratorForEnumClassNode();

    IdentifierNodeGenerator<T> getGeneratorForEnumValueNode();

    IdentifierNodeGenerator<T> getGeneratorForIndexBasedAssociationNode();

    IdentifierNodeGenerator<T> getGeneratorForQualifiedAssociationNode();

    IdentifierNodeGenerator<T> getGeneratorForInvalidNode();

}

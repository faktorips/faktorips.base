/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.plainjava.internal.fl;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGenerator;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.fl.CompilationResult;

/**
 * An {@link IdentifierNodeGeneratorFactory} that generates {@link IdentifierNodeGenerator
 * IdentifierNodeGenerators} which produce no source code but return a {@link CompilationResult}
 * with the matching {@link Datatype}.
 */
class EmptyCodeIdentifierNodeGeneratorFactory
        implements IdentifierNodeGeneratorFactory<JavaCodeFragment> {

    @Override
    public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForParameterNode() {
        return new EmptyCodeIdentifierNodeGenerator(this);
    }

    @Override
    public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForAssociationNode() {
        return new EmptyCodeIdentifierNodeGenerator(this);
    }

    @Override
    public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForAttributeNode() {
        return new EmptyCodeAttributeNodeGenerator(this);
    }

    @Override
    public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForEnumClassNode() {
        return new EmptyCodeIdentifierNodeGenerator(this);
    }

    @Override
    public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForEnumValueNode() {
        return new EmptyCodeIdentifierNodeGenerator(this);
    }

    @Override
    public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForIndexBasedAssociationNode() {
        return new EmptyCodeIdentifierNodeGenerator(this);
    }

    @Override
    public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForQualifiedAssociationNode() {
        return new EmptyCodeIdentifierNodeGenerator(this);
    }

    @Override
    public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForInvalidNode() {
        return new EmptyCodeIdentifierNodeGenerator(this);
    }

}
/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier.ast;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGenerator;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierParser;
import org.faktorips.fl.FunctionResolver;

/**
 * This enum contains an entry for every {@link IdentifierNode} that is used by the
 * {@link IdentifierParser}. Each {@link IdentifierNode} has an integer named
 * <code>proposalSortOrder</code> which indicates in which order they will appear on the UI. A
 * negative sort order implies that proposals of this type should be before any other proposal
 * provided by other sources but the {@link IdentifierParser}, for example functions from
 * {@link FunctionResolver}.
 * 
 * @author dirmeier
 */
public enum IdentifierNodeType {

    PARAMETER(ParameterNode.class, -1) {

        @Override
        public <T extends CodeFragment> IdentifierNodeGenerator<T> getGenerator(
                IdentifierNodeGeneratorFactory<T> factory) {
            return factory.getGeneratorForParameterNode();
        }
    },

    ATTRIBUTE(AttributeNode.class, 2) {

        @Override
        public <T extends CodeFragment> IdentifierNodeGenerator<T> getGenerator(
                IdentifierNodeGeneratorFactory<T> factory) {
            return factory.getGeneratorForAttributeNode();
        }

    },

    ASSOCIATION(AssociationNode.class, 3) {

        @Override
        public <T extends CodeFragment> IdentifierNodeGenerator<T> getGenerator(
                IdentifierNodeGeneratorFactory<T> factory) {
            return factory.getGeneratorForAssociationNode();
        }
    },

    INDEX(IndexNode.class, 4) {

        @Override
        public <T extends CodeFragment> IdentifierNodeGenerator<T> getGenerator(
                IdentifierNodeGeneratorFactory<T> factory) {
            return factory.getGeneratorForIndexBasedAssociationNode();
        }
    },

    QUALIFIER(QualifierNode.class, 5) {

        @Override
        public <T extends CodeFragment> IdentifierNodeGenerator<T> getGenerator(
                IdentifierNodeGeneratorFactory<T> factory) {
            return factory.getGeneratorForQualifiedAssociationNode();
        }
    },

    ENUM_CLASS(EnumClassNode.class, 6) {

        @Override
        public <T extends CodeFragment> IdentifierNodeGenerator<T> getGenerator(
                IdentifierNodeGeneratorFactory<T> factory) {
            return factory.getGeneratorForEnumClassNode();
        }
    },

    ENUM_VALUE(EnumValueNode.class, 7) {

        @Override
        public <T extends CodeFragment> IdentifierNodeGenerator<T> getGenerator(
                IdentifierNodeGeneratorFactory<T> factory) {
            return factory.getGeneratorForEnumValueNode();
        }
    },

    INVALID_IDENTIFIER(InvalidIdentifierNode.class, Integer.MAX_VALUE) {

        @Override
        public <T extends CodeFragment> IdentifierNodeGenerator<T> getGenerator(
                IdentifierNodeGeneratorFactory<T> factory) {
            return factory.getGeneratorForInvalidNode();
        }
    };

    private final Class<? extends IdentifierNode> nodeClass;

    private final int proposalSortOrder;

    /**
     * Instantiates the {@link IdentifierNodeType} with the following parameters
     * 
     * @param nodeClass The subclass of {@link IdentifierNode} that is instantiated for the
     *            specified type
     * @param proposalSortOrder Proposals are sorted in blocks of same type. This number specifies
     *            the position of this proposal type. A negative number implies that the position is
     *            before other functions that may not be provided by the {@link IdentifierParser}.
     * 
     */
    IdentifierNodeType(Class<? extends IdentifierNode> nodeClass, int proposalSortOrder) {
        this.nodeClass = nodeClass;
        this.proposalSortOrder = proposalSortOrder;
    }

    /**
     * Searches for the correct enum value for the specified node class.
     * 
     * @param nodeClass The node class for which you want to have the {@link IdentifierNodeType}
     * @return The {@link IdentifierNodeType} that matches for the given class
     */
    public static IdentifierNodeType getNodeType(Class<? extends IdentifierNode> nodeClass) {
        for (IdentifierNodeType value : values()) {
            if (value.getNodeClass().equals(nodeClass)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Illegal node class " + nodeClass); //$NON-NLS-1$
    }

    /**
     * The <code>proposalSortOrder</code> represents the numerical position of this enum constant. A
     * negative number implies that the position is before other functions that may not be provided
     * by the {@link IdentifierParser}.
     * 
     * @return integer representing numerical position of the IdentifierNodeType,
     */
    public int getProposalSortOrder() {
        return proposalSortOrder;
    }

    public Class<? extends IdentifierNode> getNodeClass() {
        return nodeClass;
    }

    public abstract <T extends CodeFragment> IdentifierNodeGenerator<T> getGenerator(
            IdentifierNodeGeneratorFactory<T> factory);
}

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

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeBuilder;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeBuilderFactory;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierParser;

/**
 * This enum contains an entry for every {@link IdentifierNode} that is used by the
 * {@link IdentifierParser}.
 * 
 * @author dirmeier
 */
public enum IdentifierNodeType {

    ASSOCIATION(AssociationNode.class) {

        @Override
        public <T extends CodeFragment> IdentifierNodeBuilder<T> getBuilderFor(IdentifierNodeBuilderFactory<T> factory) {
            return factory.getBuilderForAssociationNode();
        }
    },

    INDEX_BASED_ASSOCIATION(IndexBasedAssociationNode.class) {

        @Override
        public <T extends CodeFragment> IdentifierNodeBuilder<T> getBuilderFor(IdentifierNodeBuilderFactory<T> factory) {
            return factory.getBuilderForIndexBasedAssociationNode();
        }
    },

    QUALIFIED_ASSOCIATION(QualifiedAssociationNode.class) {

        @Override
        public <T extends CodeFragment> IdentifierNodeBuilder<T> getBuilderFor(IdentifierNodeBuilderFactory<T> factory) {
            return factory.getBuilderForQualifiedAssociationNode();
        }
    },

    ATTRIBUTE(AttributeNode.class) {

        @Override
        public <T extends CodeFragment> IdentifierNodeBuilder<T> getBuilderFor(IdentifierNodeBuilderFactory<T> factory) {
            return factory.getBuilderForAttributeNode();
        }

    },

    PARAMETER(ParameterNode.class) {

        @Override
        public <T extends CodeFragment> IdentifierNodeBuilder<T> getBuilderFor(IdentifierNodeBuilderFactory<T> factory) {
            return factory.getBuilderForParameterNode();
        }
    },

    ENUM_CLASS(EnumClassNode.class) {

        @Override
        public <T extends CodeFragment> IdentifierNodeBuilder<T> getBuilderFor(IdentifierNodeBuilderFactory<T> factory) {
            return factory.getBuilderForEnumClassNode();
        }
    },

    ENUM_VALUE(EnumValueNode.class) {

        @Override
        public <T extends CodeFragment> IdentifierNodeBuilder<T> getBuilderFor(IdentifierNodeBuilderFactory<T> factory) {
            return factory.getBuilderForEnumValueNode();
        }
    },

    INVALID_IDENTIFIER(InvalidIdentifierNode.class) {

        @Override
        public <T extends CodeFragment> IdentifierNodeBuilder<T> getBuilderFor(IdentifierNodeBuilderFactory<T> factory) {
            return factory.getBuilderForInvalidNode();
        }
    };

    private final Class<? extends IdentifierNode> nodeClass;

    private IdentifierNodeType(Class<? extends IdentifierNode> nodeClass) {
        this.nodeClass = nodeClass;
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

    public Class<? extends IdentifierNode> getNodeClass() {
        return nodeClass;
    }

    public abstract <T extends CodeFragment> IdentifierNodeBuilder<T> getBuilderFor(IdentifierNodeBuilderFactory<T> factory);
}

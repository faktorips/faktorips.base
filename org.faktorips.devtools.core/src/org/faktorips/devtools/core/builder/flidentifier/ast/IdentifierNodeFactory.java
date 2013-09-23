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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.flidentifier.Messages;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode.EnumClass;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;

/**
 * The {@link IdentifierNodeFactory} is used to create {@link IdentifierNode identifier nodes}. It
 * handles a bunch of common exceptions and error cases that could occur when creating a new
 * identifier node. In case of exception or error case it will return an
 * {@link InvalidIdentifierNode} instead of the requested node type.
 * 
 * @author dirmeier
 */
public class IdentifierNodeFactory {

    private final String identifierPart;

    private final IIpsProject ipsProject;

    public IdentifierNodeFactory(String identifierPart, IIpsProject ipsProject) {
        this.identifierPart = identifierPart;
        this.ipsProject = ipsProject;
    }

    /**
     * Creates a new {@link ParameterNode} for the specified parameter or an
     * {@link InvalidIdentifierNode} if the {@link Datatype} of the parameter could not be found.
     * 
     * @param parameter The parameter for which you want to have the {@link ParameterNode}
     * @return The new {@link IdentifierNode}
     */
    public IdentifierNode createParameterNode(IParameter parameter) {
        try {
            ParameterNode parameterNode = new ParameterNode(parameter, ipsProject);
            if (parameterNode.getDatatype() == null) {
                return createInvalidNoDatatype(parameter.getDatatype());
            }
            return parameterNode;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return createInvalidDatatypeError(parameter.getDatatype());
        }
    }

    /**
     * Creates a new {@link AttributeNode} for the specified attribute or an
     * {@link InvalidIdentifierNode} if the {@link Datatype} of the attribute could not be found.
     * 
     * @param attribute The attribute for which you want to create an {@link AttributeNode}
     * @param defaultValueAccess <code>true</code> if the identifier is the accessor for the default
     *            value of an policy component type attribute, otherwise false.
     * @param listOfTypes <code>true</code> if the context datatype of the attribute access was a
     *            {@link ListOfTypeDatatype} and hence the result have to be also a
     *            {@link ListOfTypeDatatype}
     * @return The new {@link AttributeNode} or an {@link InvalidIdentifierNode}.
     */
    public IdentifierNode createAttributeNode(IAttribute attribute, boolean defaultValueAccess, boolean listOfTypes) {
        try {
            AttributeNode attributeNode = new AttributeNode(attribute, defaultValueAccess, listOfTypes, ipsProject);
            if (attributeNode.getDatatype() == null) {
                return createInvalidNoDatatype(attribute.getDatatype());
            }
            return attributeNode;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return createInvalidDatatypeError(attribute.getDatatype());
        }
    }

    private IdentifierNode createInvalidNoDatatype(String datatypeName) {
        return createInvalidIdentifier(Message
                .newError(ExprCompiler.UNDEFINED_IDENTIFIER, NLS.bind(
                        Messages.AbstractParameterIdentifierResolver_msgDatatypeCanNotBeResolved, datatypeName,
                        identifierPart)));
    }

    private IdentifierNode createInvalidDatatypeError(String datatypeName) {
        return createInvalidIdentifier(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, NLS.bind(
                Messages.AbstractParameterIdentifierResolver_msgErrorDatatypeResolving, datatypeName, identifierPart)));
    }

    /**
     * Creates a new {@link AssociationNode} for the specified association or an
     * {@link InvalidIdentifierNode} if the target could not be found.
     * 
     * @param association The association for which you want to create an {@link AssociationNode}
     * @param listOfType <code>true</code> if the association is a one to many association and hence
     *            the result have to be also a {@link ListOfTypeDatatype}
     * @return The new {@link AssociationNode} or an {@link InvalidIdentifierNode}.
     */
    public IdentifierNode createAssociationNode(IAssociation association, boolean listOfType) {
        try {
            return new AssociationNode(association, listOfType, ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return createInvalidAssociationTargetNode(association);
        }
    }

    /**
     * Creates a new {@link QualifiedAssociationNode} for the specified association and qualifier or
     * an {@link InvalidIdentifierNode} if the target could not be found.
     * 
     * @param association The association for which you want to create an {@link AssociationNode}
     * @param runtimeID The runtime id of the product component that was found using the qualifier
     * @return The new {@link AssociationNode} or an {@link InvalidIdentifierNode}.
     */
    public IdentifierNode createQualifiedAssociationNode(IAssociation association,
            String qualifier,
            String runtimeID,
            IType policyCmptType,
            boolean listOfTypes) {
        try {
            return new QualifiedAssociationNode(association, qualifier, runtimeID, policyCmptType, listOfTypes,
                    ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return createInvalidAssociationTargetNode(association);
        }
    }

    /**
     * Creates a new {@link IndexBasedAssociationNode} for the specified association and index or an
     * {@link InvalidIdentifierNode} if the target could not be found.
     * 
     * @param association The association for which you want to create an {@link AssociationNode}
     * @param index The index that was specified in the identifier
     * @return The new {@link AssociationNode} or an {@link InvalidIdentifierNode}.
     */
    public IdentifierNode createIndexBasedAssociationNode(IAssociation association, int index) {
        try {
            return new IndexBasedAssociationNode(association, index, ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return createInvalidAssociationTargetNode(association);
        }
    }

    private IdentifierNode createInvalidAssociationTargetNode(IAssociation association) {
        return createInvalidIdentifier(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, NLS.bind(
                Messages.AbstractParameterIdentifierResolver_noAssociationTarget, association.getTarget(),
                identifierPart)));
    }

    /**
     * Create a new {@link EnumClassNode} for the {@link EnumClass} datatype.
     * 
     * @param datatype The {@link EnumClassNode} that was found for the identifier part
     * @return The {@link EnumClassNode}
     */
    public EnumClassNode createEnumClassNode(EnumClass datatype) {
        return new EnumClassNode(datatype);
    }

    /**
     * Create a new {@link EnumValueNode} for the enum value and {@link EnumDatatype} datatype.
     * 
     * @param enumValue the id of the enum value
     * @param enumDatatype The {@link EnumDatatype} that was found in the previous identifier part
     * @return The {@link EnumValueNode}
     */
    public EnumValueNode createEnumValueNode(String enumValue, EnumDatatype enumDatatype) {
        return new EnumValueNode(enumValue, enumDatatype);
    }

    /**
     * Creates a new {@link InvalidIdentifierNode} with the given message
     * 
     * @param message The message that should be provided to the user to help resolving the problem
     * @return The new {@link InvalidIdentifierNode}
     */
    public InvalidIdentifierNode createInvalidIdentifier(Message message) {
        return new InvalidIdentifierNode(message);
    }

}

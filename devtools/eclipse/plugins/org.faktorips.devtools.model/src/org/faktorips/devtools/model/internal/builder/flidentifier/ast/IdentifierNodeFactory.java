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

import java.text.MessageFormat;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.builder.flidentifier.Messages;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.EnumClassNode.EnumClass;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.Message;

/**
 * The {@link IdentifierNodeFactory} is used to create {@link IdentifierNode identifier nodes}. It
 * handles a bunch of common exceptions and error cases that could occur when creating a new
 * identifier node. In case of exception or error case it will return an
 * {@link InvalidIdentifierNode} instead of the requested node type.
 * 
 * @author dirmeier
 */
public class IdentifierNodeFactory {

    private final IIpsProject ipsProject;

    private final TextRegion textRegion;

    public IdentifierNodeFactory(TextRegion textRegion, IIpsProject ipsProject) {
        this.textRegion = textRegion;
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
            ParameterNode parameterNode = new ParameterNode(parameter, textRegion, ipsProject);
            if (parameterNode.getDatatype() == null) {
                return createInvalidNoDatatype(parameter.getDatatype());
            }
            return parameterNode;
        } catch (IpsException e) {
            IpsLog.log(e);
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
        AttributeNode attributeNode = new AttributeNode(attribute, defaultValueAccess, listOfTypes, ipsProject,
                textRegion);
        if (attributeNode.getDatatype() == null) {
            return createInvalidNoDatatype(attribute.getDatatype());
        }
        return attributeNode;
    }

    private IdentifierNode createInvalidNoDatatype(String datatypeName) {
        return createInvalidIdentifier(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, MessageFormat.format(
                Messages.AbstractParameterIdentifierResolver_msgDatatypeCanNotBeResolved, datatypeName,
                textRegion.getTextRegionString())));
    }

    private IdentifierNode createInvalidDatatypeError(String datatypeName) {
        return createInvalidIdentifier(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, MessageFormat.format(
                Messages.AbstractParameterIdentifierResolver_msgErrorDatatypeResolving, datatypeName,
                textRegion.getTextRegionString())));
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
            return new AssociationNode(association, listOfType, textRegion, ipsProject);
        } catch (IpsException e) {
            IpsLog.log(e);
            return createInvalidAssociationTargetNode(association.getTarget());
        }
    }

    /**
     * Creates a new {@link QualifierNode} for the specified association and qualifier or an
     * {@link InvalidIdentifierNode} if the target could not be found.
     * 
     * @param productCmpt the {@link IProductCmpt} that was found for the qualifier.
     * @param qualifier The qualifier found at the identifier
     * @return The new {@link AssociationNode} or an {@link InvalidIdentifierNode}.
     */
    public IdentifierNode createQualifierNode(IProductCmpt productCmpt, String qualifier, boolean listOfTypes) {
        if (checkProductCmpt(productCmpt)) {
            return createInvalidQualifierMessage(qualifier);
        }
        IPolicyCmptType targetType = productCmpt.findPolicyCmptType(ipsProject);
        return new QualifierNode(productCmpt, targetType, listOfTypes, textRegion);
    }

    private InvalidIdentifierNode createInvalidQualifierMessage(String qualifier) {
        return createInvalidIdentifier(Message.newError(ExprCompiler.UNKNOWN_QUALIFIER,
                MessageFormat.format(Messages.AssociationParser_msgErrorAssociationQualifier, qualifier)));
    }

    private boolean checkProductCmpt(IProductCmpt productCmpt) {
        return productCmpt == null || productCmpt.getRuntimeId() == null;
    }

    /**
     * Creates a new {@link IndexNode} for the specified association and index or an
     * {@link InvalidIdentifierNode} if the target could not be found.
     * 
     * @param index The index that was specified in the identifier
     * @param targetType The target type of the association
     * @return The new {@link AssociationNode} or an {@link InvalidIdentifierNode}.
     */
    public IdentifierNode createIndexBasedAssociationNode(int index, IType targetType) {
        return new IndexNode(index, targetType, textRegion);
    }

    private IdentifierNode createInvalidAssociationTargetNode(String targetName) {
        return createInvalidIdentifier(Message.newError(
                ExprCompiler.UNDEFINED_IDENTIFIER,
                MessageFormat.format(Messages.AbstractParameterIdentifierResolver_noAssociationTarget, targetName,
                        textRegion.getTextRegionString())));
    }

    /**
     * Create a new {@link EnumClassNode} for the {@link EnumClass} datatype.
     * 
     * @param datatype The {@link EnumClassNode} that was found for the identifier part
     * @return The {@link EnumClassNode}
     */
    public EnumClassNode createEnumClassNode(EnumClass datatype) {
        return new EnumClassNode(datatype, textRegion);
    }

    /**
     * Create a new {@link EnumValueNode} for the enum value and {@link EnumDatatype} datatype.
     * 
     * @param enumValue the id of the enum value
     * @param enumDatatype The {@link EnumDatatype} that was found in the previous identifier part
     * @return The {@link EnumValueNode}
     */
    public EnumValueNode createEnumValueNode(String enumValue, EnumDatatype enumDatatype) {
        return new EnumValueNode(enumValue, enumDatatype, textRegion);
    }

    /**
     * Creates a new {@link InvalidIdentifierNode} with the given message
     * 
     * @param message The message that should be provided to the user to help resolving the problem
     * @return The new {@link InvalidIdentifierNode}
     */
    public InvalidIdentifierNode createInvalidIdentifier(Message message) {
        return new InvalidIdentifierNode(message, textRegion);
    }

}

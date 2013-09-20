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

public class IdentifierNodeFactory {

    private final String identifierPart;

    private final IIpsProject ipsProject;

    public IdentifierNodeFactory(String identifierPart, IIpsProject ipsProject) {
        this.identifierPart = identifierPart;
        this.ipsProject = ipsProject;
    }

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

    public IdentifierNode createAttributeNode(IAttribute attribute, boolean defaultValueAccess, boolean listOfTypes) {
        try {
            AttributeNode parameterNode = new AttributeNode(attribute, defaultValueAccess, listOfTypes, ipsProject);
            if (parameterNode.getDatatype() == null) {
                return createInvalidNoDatatype(attribute.getDatatype());
            }
            return parameterNode;
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

    public IdentifierNode createAssociationNode(IAssociation association, boolean listOfType) {
        try {
            return new AssociationNode(association, listOfType, ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return createInvalidAssociationTargetNode(association);
        }
    }

    public IdentifierNode createQualifiedAssociationNode(IAssociation association,
            String qualifier,
            String runtimeID,
            IType policyCmptType,
            boolean listOfTypes) {
        try {
            return new QualifiedAssociationNode(association, qualifier, runtimeID, policyCmptType, listOfTypes, ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return createInvalidAssociationTargetNode(association);
        }
    }

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

    public IdentifierNode createEnumClassNode(EnumClass datatype) {
        return new EnumClassNode(datatype);
    }

    public IdentifierNode createEnumValueNode(String enumValue, Datatype enumDatatype) {
        return new EnumValueNode(enumValue, enumDatatype);
    }

    public InvalidIdentifierNode createInvalidIdentifier(Message message) {
        return new InvalidIdentifierNode(message);
    }

}

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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.builder.Messages;
import org.faktorips.devtools.core.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;

public class AttributeParser extends TypeBasedIdentifierParser {

    public static final char VALUE_SUFFIX_SEPARATOR_CHAR = '@';
    public static final String DEFAULT_VALUE_SUFFIX = VALUE_SUFFIX_SEPARATOR_CHAR + "default"; //$NON-NLS-1$
    private final IdentifierFilter identifierFilter;

    public AttributeParser(IExpression expression, IIpsProject ipsProject, IdentifierFilter identifierFilter) {
        super(expression, ipsProject);
        this.identifierFilter = identifierFilter;
    }

    @Override
    public IdentifierNode parseInternal() {
        boolean defaultValueAccess = isDefaultValueAccess(getIdentifierPart());
        List<IAttribute> attributes = findAttributes();
        for (IAttribute anAttribute : attributes) {
            String attributeName = getAttributeName(getIdentifierPart(), defaultValueAccess);
            if (attributeName.equals(anAttribute.getName())) {
                if (isAllowd(anAttribute)) {
                    return createAttributeNode(anAttribute, defaultValueAccess);
                } else {
                    return createInvalidIdentifierNode();
                }
            }
        }
        return null;
    }

    private boolean isDefaultValueAccess(String attributeName) {
        return getContextType() instanceof IPolicyCmptType && attributeName.endsWith(DEFAULT_VALUE_SUFFIX);
    }

    private String getAttributeName(String identifier, boolean defaultValueAccess) {
        if (defaultValueAccess) {
            return identifier.substring(0, identifier.lastIndexOf(VALUE_SUFFIX_SEPARATOR_CHAR));
        } else {
            return identifier;
        }
    }

    private List<IAttribute> findAttributes() {
        List<IAttribute> attributes;
        if (isContextTypeFormulaType()) {
            attributes = getExpression().findMatchingProductCmptTypeAttributes();
        } else {
            try {
                attributes = getContextType().findAllAttributes(getIpsProject());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return attributes;
    }

    private boolean isAllowd(IAttribute anAttribute) {
        return identifierFilter.isIdentifierAllowed(anAttribute);
    }

    private IdentifierNode createAttributeNode(IAttribute anAttribute, boolean defaultValueAccess) {
        try {
            AttributeNode attributeNode = new AttributeNode(anAttribute, defaultValueAccess, isListOfTypeDatatype(),
                    getIpsProject());
            if (attributeNode.getDatatype() == null) {
                return new InvalidIdentifierNode(Message.newError(
                        ExprCompiler.UNDEFINED_IDENTIFIER,
                        NLS.bind(Messages.AbstractParameterIdentifierResolver_msgDatatypeCanNotBeResolved,
                                anAttribute.getDatatype(), getIdentifierPart())));
            } else {
                return attributeNode;
            }
        } catch (CoreException e) {
            return new InvalidIdentifierNode(Message.newError(
                    ExprCompiler.UNDEFINED_IDENTIFIER,
                    NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorDatatypeResolving,
                            anAttribute.getDatatype(), getIdentifierPart())));
        }
    }

    private IdentifierNode createInvalidIdentifierNode() {
        return new InvalidIdentifierNode(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER,
                NLS.bind(Messages.AbstractParameterIdentifierResolver_msgIdentifierNotAllowed, getIdentifierPart())));
    }

}

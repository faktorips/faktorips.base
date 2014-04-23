/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.fl.IdentifierKind;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;

/**
 * This parser tries to match the identifier part to an attribute of the context type. In case of
 * success it will return an {@link AttributeNode} otherwise it returns <code>null</code>.
 * <p>
 * Allows both policy- and the corresponding product attributes if the context type is a policy
 * component type.
 * 
 * @author dirmeier
 */
public class AttributeParser extends TypeBasedIdentifierParser {

    public static final char VALUE_SUFFIX_SEPARATOR_CHAR = '@';

    public static final String DEFAULT_VALUE_SUFFIX = VALUE_SUFFIX_SEPARATOR_CHAR + "default"; //$NON-NLS-1$

    private final IdentifierFilter identifierFilter;

    public AttributeParser(IExpression expression, IIpsProject ipsProject, IdentifierFilter identifierFilter) {
        super(expression, ipsProject);
        this.identifierFilter = identifierFilter;
    }

    @Override
    public List<IdentifierNode> getProposals(String prefix) {
        ArrayList<IdentifierNode> result = new ArrayList<IdentifierNode>();
        List<IAttribute> attributes = findAttributes();
        for (IAttribute attribute : attributes) {
            addIfNotNull(getAttributeProposal(attribute, false, prefix), result);
            addIfNotNull(getAttributeProposal(attribute, true, prefix), result);
        }
        return result;
    }

    private IdentifierNode getAttributeProposal(IAttribute attribute, boolean defaultAccess, String prefix) {
        if (isProposalAllowed(attribute, defaultAccess)) {
            IdentifierNode node = nodeFactory().createAttributeNode(attribute, defaultAccess, isListOfTypeContext());
            if (isMatchingNode(node, prefix)) {
                return node;
            }
        }
        return null;
    }

    private boolean isProposalAllowed(IAttribute attribute, boolean defaultAccess) {
        return (attribute instanceof IPolicyCmptTypeAttribute || !defaultAccess) && isAllowd(attribute, defaultAccess);
    }

    private void addIfNotNull(IdentifierNode node, List<IdentifierNode> result) {
        if (node != null) {
            result.add(node);
        }
    }

    @Override
    public IdentifierNode parseInternal() {
        try {
            return parseToNode();
        } catch (CoreRuntimeException e) {
            IpsPlugin.log(e);
            return nodeFactory().createInvalidIdentifier(
                    Message.newInfo(ExprCompiler.UNDEFINED_IDENTIFIER,
                            Messages.AbstractParameterIdentifierResolver_msgErrorRetrievingAttribute));
        }
    }

    private IdentifierNode parseToNode() {
        boolean defaultValueAccess = isDefaultValueAccess(getIdentifierPart());
        String attributeName = getAttributeName(getIdentifierPart(), defaultValueAccess);
        List<IAttribute> attributes = findAttributes();
        return createNode(defaultValueAccess, attributeName, attributes);
    }

    private IdentifierNode createNode(boolean defaultValueAccess, String attributeName, List<IAttribute> attributes) {
        for (IAttribute anAttribute : attributes) {
            if (attributeName.equals(anAttribute.getName())) {
                if (isAllowd(anAttribute, defaultValueAccess)) {
                    return nodeFactory().createAttributeNode(anAttribute, defaultValueAccess, isListOfTypeContext());
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

    protected List<IAttribute> findAttributes() {
        List<IAttribute> attributes;
        if (isContextTypeFormulaType()) {
            attributes = getExpression().findMatchingProductCmptTypeAttributes();
        } else {
            attributes = getPolicyAndProductAttributesFromIType();
        }
        return attributes;
    }

    private List<IAttribute> getPolicyAndProductAttributesFromIType() {
        List<IAttribute> attributes = new ArrayList<IAttribute>();
        IType contextType = getContextType();
        attributes.addAll(findAllAttributesFor(contextType));
        attributes.addAll(findProductAttributesIfAvailable(contextType));
        return attributes;
    }

    private List<IAttribute> findAllAttributesFor(IType contextType) {
        try {
            return contextType.findAllAttributes(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private List<IAttribute> findProductAttributesIfAvailable(IType contextType) {
        if (contextType instanceof IPolicyCmptType) {
            IProductCmptType productCmptType = findProductCmptType((IPolicyCmptType)contextType);
            if (productCmptType != null) {
                return findAllAttributesFor(productCmptType);
            }
        }
        return Collections.emptyList();
    }

    private IProductCmptType findProductCmptType(IPolicyCmptType policyCmptType) {
        try {
            return policyCmptType.findProductCmptType(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private boolean isAllowd(IAttribute anAttribute, boolean isDefaultIdentifier) {
        return identifierFilter.isIdentifierAllowed(anAttribute,
                IdentifierKind.getDefaultIdentifierOrAttribute(isDefaultIdentifier));
    }

    private IdentifierNode createInvalidIdentifierNode() {
        return nodeFactory().createInvalidIdentifier(
                Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, NLS.bind(
                        Messages.AbstractParameterIdentifierResolver_msgIdentifierNotAllowed, getIdentifierPart())));
    }

}

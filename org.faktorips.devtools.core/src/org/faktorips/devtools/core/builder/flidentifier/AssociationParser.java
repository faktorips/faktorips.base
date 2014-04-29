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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;

/**
 * This parser tires to match the identifier node to an association of the context type. It also
 * handles optional qualifiers or indices. The output will be an {@link AssociationNode} or a
 * subclass of this class.
 */
public class AssociationParser extends TypeBasedIdentifierParser {

    public AssociationParser(ParsingContext parsingContext) {
        super(parsingContext);
    }

    @Override
    public boolean isAllowedType() {
        return super.isAllowedType() && super.getContextType() instanceof IPolicyCmptType;
    }

    @Override
    public IPolicyCmptType getContextType() {
        return (IPolicyCmptType)super.getContextType();
    }

    @Override
    protected IdentifierNode parseInternal() {
        IPolicyCmptType policyCmptType = getContextType();
        try {
            IAssociation association = policyCmptType.findAssociation(getAssociationName(), getIpsProject());
            if (association != null) {
                return createNodeFor(association);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return nodeFactory().createInvalidIdentifier(
                    Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, NLS.bind(
                            Messages.AssociationParser_msgErrorWhileFindAssociation, getIdentifierPart(),
                            getContextType())));
        }
        return null;
    }

    private IdentifierNode createNodeFor(IAssociation association) {
        return nodeFactory().createAssociationNode(association, isListOfTypeContext());
    }

    protected String getAssociationName() {
        return getIdentifierPart();
    }

    @Override
    public List<IdentifierProposal> getProposals(String prefix) {
        if (isAllowedType()) {
            return getProposalsFor(prefix);
        }
        return Collections.emptyList();
    }

    private List<IdentifierProposal> getProposalsFor(String prefix) {
        IdentifierNodeCollector nodeCollector = new IdentifierNodeCollector(this);
        List<IAssociation> allAssociations = getAllAssociations();
        for (IAssociation association : allAssociations) {
            nodeCollector.addMatchingNode(createProposalFor(association), prefix);
        }
        return nodeCollector.getNodes();
    }

    private IdentifierProposal createProposalFor(IAssociation association) {
        return new IdentifierProposal(getText(association), getDescription(association, getParsingContext()
                .getMultiLanguageSupport()));
    }

    private List<IAssociation> getAllAssociations() {
        try {
            return getContextType().findAllAssociations(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getText(IAssociation association) {
        return association.getName();
    }

    public String getDescription(IAssociation association) {
        MultiLanguageSupport multiLanguageSupport = getParsingContext().getMultiLanguageSupport();
        StringBuilder description = new StringBuilder();
        description.append(getText(association));
        description.append(" -> "); //$NON-NLS-1$
        if (isToManyAssociation(association)) {
            description.append(Messages.AssociationParser_ListDatatypeDescriptionPrefix);
        }
        description.append(getUnqualifiedTargetName(association));
        description.append(NAME_DESCRIPTION_SEPERATOR).append(getDescription(association, multiLanguageSupport));
        return description.toString();
    }

    private boolean isToManyAssociation(IAssociation association) {
        return association.is1ToMany();
    }

    private String getUnqualifiedTargetName(IAssociation association) {
        return StringUtil.unqualifiedName(association.getTarget());
    }

}

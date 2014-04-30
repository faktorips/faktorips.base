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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeType;
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

    private static final String INDEX_PROPOSAL = "[0]"; //$NON-NLS-1$

    private static final String QUALIFIER_PROPOSAL = "[\""; //$NON-NLS-1$

    private static final String QUALIFIER_PROPOSAL_LABEL = "[\"...\"]"; //$NON-NLS-1$

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

    private void addAssociationProposals(IAssociation association, String prefix, IdentifierProposalCollector collector) {
        collector.addMatchingNode(getDisplayText(association, StringUtils.EMPTY),
                getText(association, StringUtils.EMPTY), getDescription(association), prefix,
                IdentifierNodeType.ASSOCIATION);
        collector.addMatchingNode(getDisplayText(association, INDEX_PROPOSAL), getText(association, INDEX_PROPOSAL),
                getDescription(association), prefix, IdentifierNodeType.ASSOCIATION);
        collector.addMatchingNode(getDisplayText(association, QUALIFIER_PROPOSAL),
                getText(association, QUALIFIER_PROPOSAL_LABEL), getDescription(association), prefix,
                IdentifierNodeType.ASSOCIATION);
    }

    private String getDisplayText(IAssociation association, String proposal) {
        return association.getName() + proposal;
    }

    private List<IAssociation> getAllAssociations() {
        try {
            return getContextType().findAllAssociations(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private List<IdentifierProposal> getProposalsFor(String prefix) {
        IdentifierProposalCollector collector = new IdentifierProposalCollector();
        List<IAssociation> allAssociations = getAllAssociations();
        for (IAssociation association : allAssociations) {
            addAssociationProposals(association, prefix, collector);
        }
        return collector.getProposals();
    }

    String getText(IAssociation association, String proposal) {
        return association.getName() + proposal + (getAssociationAndTarget(association).toString());

    }

    String getDescription(IAssociation association) {
        MultiLanguageSupport multiLanguageSupport = getParsingContext().getMultiLanguageSupport();
        StringBuilder description = getAssociationAndTarget(association);
        description.append(NAME_DESCRIPTION_SEPERATOR)
                .append(multiLanguageSupport.getLocalizedDescription(association));
        return association.getName() + description.toString();
    }

    private StringBuilder getAssociationAndTarget(IAssociation association) {
        StringBuilder description = new StringBuilder();
        description.append(" -> "); //$NON-NLS-1$
        if (isToManyAssociation(association)) {
            description.append(Messages.AssociationParser_ListDatatypeDescriptionPrefix);
        }
        description.append(getUnqualifiedTargetName(association));
        return description;
    }

    private boolean isToManyAssociation(IAssociation association) {
        return association.is1ToMany();
    }

    private String getUnqualifiedTargetName(IAssociation association) {
        return StringUtil.unqualifiedName(association.getTarget());
    }

}

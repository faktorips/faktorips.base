/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.StringUtil;

/**
 * This parser tires to match the identifier node to an association of the context type. It also
 * handles optional qualifiers or indices. The output will be an {@link AssociationNode} or a
 * subclass of this class.
 */
public class AssociationParser extends TypeBasedIdentifierParser {

    static final String ASSOCIATION_TARGET_SEPERATOR = " -> "; //$NON-NLS-1$

    static final String INDEX_PROPOSAL = "[0]"; //$NON-NLS-1$

    static final String QUALIFIER_PROPOSAL = "[\""; //$NON-NLS-1$

    static final String QUALIFIER_PROPOSAL_LABEL = "[\"...\"]"; //$NON-NLS-1$

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
        IAssociation association = policyCmptType.findAssociation(getAssociationName(), getIpsProject());
        if (association != null) {
            return createNodeFor(association);
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

    private void addAssociationProposals(IAssociation association,
            String prefix,
            IdentifierProposalCollector collector) {
        collector.addMatchingNode(getText(association, StringUtils.EMPTY), getDisplayText(association),
                getDescription(association), prefix, IdentifierNodeType.ASSOCIATION);
        collector.addMatchingNode(getText(association, INDEX_PROPOSAL), getIndexDisplayText(association),
                getIndexDescription(association), prefix, IdentifierNodeType.ASSOCIATION);
        collector.addMatchingNode(getText(association, QUALIFIER_PROPOSAL), getQualifierDisplayText(association),
                getQualifierDescription(association), prefix, IdentifierNodeType.ASSOCIATION);
    }

    private String getText(IAssociation association, String proposalSuffix) {
        return association.getName() + proposalSuffix;
    }

    private List<IAssociation> getAllAssociations() {
        try {
            return getContextType().findAllAssociations(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private List<IdentifierProposal> getProposalsFor(String prefixSuffix) {
        IdentifierProposalCollector collector = new IdentifierProposalCollector();
        List<IAssociation> allAssociations = getAllAssociations();
        for (IAssociation association : allAssociations) {
            addAssociationProposals(association, prefixSuffix, collector);
        }
        return collector.getProposals();
    }

    String getDisplayText(IAssociation association) {
        return getText(association, StringUtils.EMPTY) + getAssociationTarget(association, association.is1ToMany());

    }

    String getIndexDisplayText(IAssociation association) {
        return getText(association, INDEX_PROPOSAL) + getAssociationTarget(association, false);
    }

    private String getQualifierDisplayText(IAssociation association) {
        return getText(association, QUALIFIER_PROPOSAL_LABEL)
                + getAssociationTarget(association, association.is1ToManyIgnoringQualifier());
    }

    private String getAssociationTarget(IAssociation association, boolean oneToMany) {
        String associationTarget = ASSOCIATION_TARGET_SEPERATOR;
        if (oneToMany) {
            associationTarget += NLS.bind(Messages.AssociationParser_ListDatatypeDescriptionPrefix,
                    getUnqualifiedTargetName(association));
        } else {
            associationTarget += getUnqualifiedTargetName(association);
        }
        return associationTarget;
    }

    private String getUnqualifiedTargetName(IAssociation association) {
        return StringUtil.unqualifiedName(association.getTarget());
    }

    String getDescription(IAssociation association) {
        MultiLanguageSupport multiLanguageSupport = getParsingContext().getMultiLanguageSupport();
        StringBuilder description = new StringBuilder();
        description.append(getDisplayText(association)).append("\n\n") //$NON-NLS-1$
                .append(multiLanguageSupport.getLocalizedDescription(association));
        return description.toString();
    }

    String getIndexDescription(IAssociation association) {
        MultiLanguageSupport multiLanguageSupport = getParsingContext().getMultiLanguageSupport();
        StringBuilder description = new StringBuilder();
        description.append(getIndexDisplayText(association)).append("\n\n") //$NON-NLS-1$
                .append(Messages.QualifierAndIndexParser_descriptionIndex).append("\n\n") //$NON-NLS-1$
                .append(multiLanguageSupport.getLocalizedDescription(association));
        return description.toString();
    }

    String getQualifierDescription(IAssociation association) {
        MultiLanguageSupport multiLanguageSupport = getParsingContext().getMultiLanguageSupport();
        StringBuilder description = new StringBuilder();
        description.append(getQualifierDisplayText(association)).append("\n\n") //$NON-NLS-1$
                .append(Messages.QualifierAndIndexParser_descriptionQualifierUndefined).append("\n\n") //$NON-NLS-1$
                .append(multiLanguageSupport.getLocalizedDescription(association));
        return description.toString();
    }

}

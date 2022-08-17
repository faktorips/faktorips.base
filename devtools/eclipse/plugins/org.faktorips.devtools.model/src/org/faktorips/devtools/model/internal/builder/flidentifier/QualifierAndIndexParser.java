/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.contextcollector.ContextProductCmptFinder;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.Message;

/**
 * This parser handles qualifiers and index identifiers. The identifier part always have to end with
 * ']'. The starting '[' is already cut off by the {@link IdentifierParser} when splitting the whole
 * identifier.
 * <p>
 * If the statement begins with " and ends with "] we assume it is a qualifier. If there are no
 * quotation marks it is parsed as an index.
 * 
 * @since 3.11
 * @author dirmeier
 */
public class QualifierAndIndexParser extends TypeBasedIdentifierParser {

    public static final String QUALIFIER_START = "["; //$NON-NLS-1$

    public static final String QUALIFIER_END = "]"; //$NON-NLS-1$

    private static final String QUALIFIER_QUOTATION = "\""; //$NON-NLS-1$

    public QualifierAndIndexParser(ParsingContext parsingContext) {
        super(parsingContext);
    }

    @Override
    public boolean isAllowedType() {
        return super.isAllowedType() && super.getContextType() instanceof IPolicyCmptType;
    }

    @Override
    protected IdentifierNode parseInternal() {
        if (!isValidPreviousNode()) {
            return null;
        }
        if ((isIndex()) && !isListOfTypeContext()) {
            return invalidIndexNode();
        }
        if (isQualifier()) {
            return createQualifierNode();
        } else if (isIndex()) {
            return createIndexNode();
        } else {
            return null;
        }
    }

    private boolean isValidPreviousNode() {
        if ((getPreviousNode() instanceof AssociationNode)
                || (getPreviousNode() instanceof QualifierNode && isIndex())) {
            return true;
        }
        return false;
    }

    private IdentifierNode invalidIndexNode() {
        return nodeFactory().createInvalidIdentifier(Message.newError(ExprCompiler.NO_INDEX_FOR_1TO1_ASSOCIATION, NLS
                .bind(Messages.AbstractParameterIdentifierResolver_noIndexFor1to1Association0, getIdentifierPart())));
    }

    private IdentifierNode createQualifierNode() {
        IProductCmpt productCmpt;
        try {
            productCmpt = findProductCmpt();
            return nodeFactory().createQualifierNode(productCmpt, getQualifier(), isListOfType());
        } catch (IpsException e) {
            IpsLog.log(e);
            return nodeFactory().createInvalidIdentifier(Message.newError(ExprCompiler.UNKNOWN_QUALIFIER, NLS
                    .bind(Messages.QualifierAndIndexParser_errorMsg_errorWhileSearchingProductCmpt, getQualifier())));
        }
    }

    private boolean isListOfType() {
        AssociationNode associationNode = (AssociationNode)getPreviousNode();
        return associationNode.isListContext() || associationNode.getAssociation().is1ToManyIgnoringQualifier();
    }

    private IProductCmpt findProductCmpt() {
        Collection<IIpsSrcFile> foundProductCmpts = findProductCmptByName();
        if (foundProductCmpts != null) {
            for (IIpsSrcFile ipsSrcFile : foundProductCmpts) {
                IProductCmpt foundProductCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
                if (isMatchingProductCmptType(foundProductCmpt)) {
                    return foundProductCmpt;
                }
            }
        }
        return null;
    }

    private Collection<IIpsSrcFile> findProductCmptByName() {
        Collection<IIpsSrcFile> foundProductCmpt = getIpsProject().findProductCmptByUnqualifiedName(getQualifier());
        if (foundProductCmpt.isEmpty()) {
            IProductCmpt foundProductCmptByQName = getIpsProject().findProductCmpt(getQualifier());
            if (foundProductCmptByQName != null) {
                return Arrays.asList(foundProductCmptByQName.getIpsSrcFile());
            } else {
                return Collections.emptyList();
            }
        }
        return foundProductCmpt;
    }

    private boolean isMatchingProductCmptType(IProductCmpt productCmpt) {
        IProductCmptType foundProductCmptType = productCmpt.findProductCmptType(getIpsProject());
        return foundProductCmptType.isSubtypeOrSameType(findProductCmptType(), getIpsProject());

    }

    private IProductCmptType findProductCmptType() {
        return ((IPolicyCmptType)getContextType()).findProductCmptType(getIpsProject());
    }

    private IdentifierNode createIndexNode() {
        try {
            return nodeFactory().createIndexBasedAssociationNode(getIndex(), getContextType());
        } catch (NumberFormatException e) {
            return handleInvalidIndex(e);
        }
    }

    private IdentifierNode handleInvalidIndex(NumberFormatException e) {
        IpsLog.log(e);
        return nodeFactory().createInvalidIdentifier(Message.newError(ExprCompiler.UNKNOWN_QUALIFIER,
                MessageFormat.format(Messages.AssociationParser_msgErrorAssociationQualifierOrIndex,
                        getQualifierOrIndex(),
                        getIdentifierPart())));
    }

    private boolean isQualifier() {
        return getIdentifierPart().startsWith(QUALIFIER_QUOTATION)
                && getIdentifierPart().endsWith(QUALIFIER_QUOTATION + QUALIFIER_END);
    }

    private boolean isIndex() {
        return getIdentifierPart().endsWith(QUALIFIER_END) && !isQualifier();
    }

    private int getIndexOrQualifierEnd() {
        return getIdentifierPart().indexOf(QUALIFIER_END);
    }

    private String getQualifier() {
        return getQualifierOrIndex().substring(1, getQualifierOrIndex().length() - 1);
    }

    private int getIndex() {
        return Integer.parseInt(getQualifierOrIndex());
    }

    private String getQualifierOrIndex() {
        return getIdentifierPart().substring(0, getIndexOrQualifierEnd());
    }

    @Override
    public List<IdentifierProposal> getProposals(String prefix) {
        IdentifierProposalCollector collector = new IdentifierProposalCollector();
        if (getPreviousNode() instanceof AssociationNode || getPreviousNode() instanceof QualifierNode) {
            addIndexProposal(prefix, collector);
        }
        if (getPreviousNode() instanceof AssociationNode) {
            addQualifierProposal(prefix, collector);
        }
        return collector.getProposals();
    }

    private void addIndexProposal(String prefix, IdentifierProposalCollector collector) {
        String text = getIndexText(0);
        collector.addMatchingNode(text, QUALIFIER_START + text, getIndexDescription(), prefix,
                IdentifierNodeType.INDEX);
    }

    private void addQualifierProposal(String prefix, IdentifierProposalCollector collector) {
        List<IProductCmpt> contextProductCmpts = new ContextProductCmptFinder(getParsingContext().getNodes(),
                getExpression(), getIpsProject()).getContextProductCmpts();
        for (IProductCmpt productCmpt : contextProductCmpts) {
            addQualifierProposal(productCmpt, prefix, collector);
        }
    }

    public String getIndexText(int index) {
        return index + QUALIFIER_END;
    }

    public String getIndexDescription() {
        return Messages.QualifierAndIndexParser_descriptionIndex;
    }

    private void addQualifierProposal(IProductCmpt productCmpt, String prefix, IdentifierProposalCollector collector) {
        String text = getQualifierText(productCmpt);
        collector.addMatchingNode(text, QUALIFIER_START + text, getQualifierDescription(productCmpt), prefix,
                IdentifierNodeType.QUALIFIER);
    }

    public String getQualifierText(IProductCmpt productCmpt) {
        return '"' + productCmpt.getName() + '"' + QUALIFIER_END;
    }

    public String getQualifierDescription(IProductCmpt productCmpt) {
        return MessageFormat.format(Messages.QualifierAndIndexParser_descriptionQualifier, productCmpt.getName());
    }

}

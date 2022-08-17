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

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeType;

/**
 * Helper class for filtering proposals. Collects only proposals that match a given prefix.
 * <p>
 * Usage in an {@link AbstractIdentifierNodeParser}:
 * 
 * <pre>
 * IdentifierNodeCollector nodeCollector = new IdentifierNodeCollector(this);
 * nodeCollector.addMatchingNode(createNode(), prefix);
 * return nodeCollector.getNodes();
 * </pre>
 */
class IdentifierProposalCollector {

    private ArrayList<IdentifierProposal> proposals = new ArrayList<>();

    public boolean addMatchingNode(String text, String description, String prefix, IdentifierNodeType nodeType) {
        return addMatchingNode(text, text, description, prefix, nodeType);
    }

    public boolean addMatchingNode(String text,
            String label,
            String description,
            String prefix,
            IdentifierNodeType nodeType) {
        if (isMatchingText(text, prefix)) {
            IdentifierProposal proposal = new IdentifierProposal(text, label, description, prefix, nodeType);
            return proposals.add(proposal);
        } else {
            return false;
        }
    }

    /**
     * Check whether the given node is matching the specified prefix or not.
     * 
     * @param text The text that should match
     * @param prefix The prefix that should match the identifier's text.
     * 
     * @return <code>true</code> if the prefix matches the text, otherwise <code>false</code>.
     */
    protected boolean isMatchingText(String text, String prefix) {
        return StringUtils.startsWithIgnoreCase(text, prefix);
    }

    public ArrayList<IdentifierProposal> getProposals() {
        return proposals;
    }

}

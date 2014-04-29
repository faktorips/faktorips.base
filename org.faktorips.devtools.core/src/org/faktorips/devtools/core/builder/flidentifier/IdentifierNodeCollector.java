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

/**
 * Helper class for filtering proposals. Collects only nodes that match a given prefix.
 * <p>
 * Usage in an {@link AbstractIdentifierNodeParser}:
 * 
 * <pre>
 * IdentifierNodeCollector nodeCollector = new IdentifierNodeCollector(this);
 * nodeCollector.addMatchingNode(createNode(), prefix);
 * return nodeCollector.getNodes();
 * </pre>
 */
class IdentifierNodeCollector {

    private ArrayList<IdentifierProposal> nodes = new ArrayList<IdentifierProposal>();
    private final AbstractIdentifierNodeParser parser;

    public IdentifierNodeCollector(AbstractIdentifierNodeParser parser) {
        this.parser = parser;
    }

    public boolean addMatchingNode(IdentifierProposal node, String prefix) {
        if (node != null && parser.isMatchingNode(node, prefix)) {
            return nodes.add(node);
        } else {
            return false;
        }
    }

    public ArrayList<IdentifierProposal> getNodes() {
        return nodes;
    }

}
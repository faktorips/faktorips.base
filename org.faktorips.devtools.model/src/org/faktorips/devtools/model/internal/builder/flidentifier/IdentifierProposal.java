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

import java.util.Objects;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.util.ArgumentCheck;

/**
 * This class contains the data for a proposal of an identifier as it is provided by the proposal
 * provider in auto completion context in the UI.
 * 
 */
public class IdentifierProposal implements Comparable<IdentifierProposal> {

    private final String text;

    private final String label;

    private final String description;

    private final String prefix;

    private final IdentifierNodeType nodeType;

    public IdentifierProposal(String text, String label, String description, String prefix,
            IdentifierNodeType nodeType) {
        ArgumentCheck.notNull(new Object[] { text, label, description, prefix, nodeType });
        this.text = text;
        this.label = label;
        this.description = description;
        this.prefix = prefix;
        this.nodeType = nodeType;
    }

    /**
     * Returns the text that represents this identifier node. This text is not set by an already
     * parsed expression but is derived from the state of this identifier node. For example an
     * identifier node that represents an attribute may return the name of the attribute.
     * <p>
     * The primary use of this method is to provide the expression text in a content proposal. The
     * text would be inserted when the proposal is selected.
     * 
     * @return The text that represents this identifier node. In other words this is the text that
     *             would lead to to exactly this identifier node if it is parsed by the
     *             {@link IdentifierParser}.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the text that is represented in the proposal list. In most cases this is the same
     * returns {@link #getText()} but in some cases we want to provide additional information in the
     * list.
     */
    public String getLabel() {
        return label;
    }

    /**
     * A brief description of the meaning of this identifier node. The primary use of this method is
     * to provide the description in a content proposal.
     * 
     * @return A short text that describes the use of this identifier node.
     */
    public String getDescription() {
        return description;
    }

    /**
     * The type of the identifier node that would be created by this proposal.
     */
    public IdentifierNodeType getNodeType() {
        return nodeType;
    }

    /**
     * The prefix is the part of the proposal that already was typed in the text. This information
     * is used to replace the existing text instead of inserting the whole String from
     * {@link #getText()}.
     */
    public String getPrefix() {
        return prefix;
    }

    @Override
    public int compareTo(IdentifierProposal o) {
        if (getNodeType().equals(o.getNodeType())) {
            return getText().compareTo(o.getText());
        } else {
            return getNodeType().getProposalSortOrder() - o.getNodeType().getProposalSortOrder();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, label, nodeType, prefix, text);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IdentifierProposal other = (IdentifierProposal)obj;
        return text.equals(other.text) && label.equals(other.label) && description.equals(other.description)
                && prefix.equals(other.prefix) && nodeType == other.nodeType;
    }
}

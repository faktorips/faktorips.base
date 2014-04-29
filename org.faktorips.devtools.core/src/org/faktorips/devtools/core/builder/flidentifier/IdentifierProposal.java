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


public class IdentifierProposal {

    private final String text;

    private final String label;

    private final String description;

    public IdentifierProposal(String text, String description) {
        this(text, text, description);
    }

    public IdentifierProposal(String text, String label, String description) {
        this.text = text;
        this.label = label;
        this.description = description;
    }

    /**
     * Returns the text that represents this identifier node. This text is not set by an already
     * parsed expression but is derived from the state of this identifier node. For example an
     * identifier node that represents an attribute may return the name of the attribute.
     * <p>
     * The primary use of this method is to provide the expression text in a content proposal.
     * 
     * @return The text that represents this identifier node. In other words this is the text that
     *         would lead to to exactly this identifier node if it is parsed by the
     *         {@link IdentifierParser}.
     */
    public String getText() {
        return text;
    }

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

}

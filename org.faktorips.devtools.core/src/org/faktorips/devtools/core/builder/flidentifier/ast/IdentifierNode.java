/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierParser;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.util.TextRegion;

/**
 * An Identifier nodes represents a part of an identifier. Every identifier part have a
 * {@link Datatype} that represents the type of the result. If there is at least one following part,
 * the identifier node also has a successor. Hence the {@link IdentifierNode} is a kind of linked
 * list.
 * 
 * @author dirmeier
 */
public abstract class IdentifierNode {

    public static final String NAME_DESCRIPTION_SEPERATOR = " - "; //$NON-NLS-1$

    private final Datatype datatype;

    private IdentifierNode successor;

    private final TextRegion textRegion;

    protected IdentifierNode(Datatype datatype, TextRegion textRegion) {
        this(datatype, false, textRegion);
    }

    protected IdentifierNode(Datatype datatype, boolean listOfTypes, TextRegion textRegion) {
        this.textRegion = textRegion;
        if (listOfTypes) {
            this.datatype = new ListOfTypeDatatype(datatype);
        } else {
            this.datatype = datatype;
        }
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public IdentifierNode getSuccessor() {
        return successor;
    }

    public void setSuccessor(IdentifierNode successor) {
        this.successor = successor;
    }

    public boolean hasSuccessor() {
        return getSuccessor() != null;
    }

    public boolean isListOfTypeDatatype() {
        return datatype instanceof ListOfTypeDatatype;
    }

    /**
     * The returned TextRegion defines the positions of the text represented by this identifier node
     * within the whole identifier. For example in a identifier "policy.attribute" the first
     * identifier node representing the "policy" has a text region [0-6] and the second identifier
     * node representing the attribute has a text region [7-16].
     * 
     * @return TextRegion
     */
    public TextRegion getTextRegion() {
        return textRegion;
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
    public abstract String getText();

    /**
     * A brief description of the meaning of this identifier node. The primary use of this method is
     * to provide the description in a content proposal.
     * 
     * @return A short text that describes the use of this identifier node.
     */
    public abstract String getDescription(MultiLanguageSupport multiLanguageSupport);

    protected String getNameAndDescription(IIpsElement ipsElement, MultiLanguageSupport multiLanguageSupport) {
        return getNameAndDescription(getName(ipsElement, multiLanguageSupport),
                getDescription(ipsElement, multiLanguageSupport));
    }

    protected String getNameAndDescription(String name, String description) {
        StringBuffer result = new StringBuffer();
        result.append(name);
        if (StringUtils.isNotBlank(description)) {
            result.append(NAME_DESCRIPTION_SEPERATOR).append(description);
        }
        return result.toString();
    }

    protected String getName(IIpsElement ipsElement, MultiLanguageSupport multiLanguageSupport) {
        if (ipsElement instanceof ILabeledElement) {
            return multiLanguageSupport.getLocalizedLabel((ILabeledElement)ipsElement);
        } else {
            return ipsElement.getName();
        }
    }

    protected String getDescription(IIpsElement ipsElement, MultiLanguageSupport multiLanguageSupport) {
        if (ipsElement instanceof IDescribedElement) {
            return multiLanguageSupport.getLocalizedDescription((IDescribedElement)ipsElement);
        } else {
            return null;
        }
    }

}

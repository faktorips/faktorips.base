/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier.ast;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.model.util.TextRegion;

/**
 * An Identifier nodes represents a part of an identifier. Every identifier part have a
 * {@link Datatype} that represents the type of the result. If there is at least one following part,
 * the identifier node also has a successor. Hence the {@link IdentifierNode} is a kind of linked
 * list.
 * 
 * @author dirmeier
 */
public abstract class IdentifierNode {

    private final Datatype datatype;

    private IdentifierNode successor;

    private final TextRegion textRegion;

    protected IdentifierNode(Datatype datatype, TextRegion textRegion) {
        this(datatype, false, textRegion);
    }

    protected IdentifierNode(Datatype datatype, boolean listOfTypes, TextRegion textRegion) {
        this.textRegion = textRegion;
        if (listOfTypes && datatype != null) {
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

}

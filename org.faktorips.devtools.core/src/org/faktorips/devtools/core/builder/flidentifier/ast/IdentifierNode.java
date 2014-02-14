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

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.internal.refactor.TextRegion;

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

    private TextRegion textRegion;

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

    public TextRegion getTextRegion() {
        return textRegion;
    }

}

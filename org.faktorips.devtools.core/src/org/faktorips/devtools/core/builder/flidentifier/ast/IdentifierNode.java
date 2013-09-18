/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;

public abstract class IdentifierNode {

    private final Datatype datatype;

    private IdentifierNode successor;

    public IdentifierNode(Datatype datatype) {
        this(datatype, false);
    }

    public IdentifierNode(Datatype datatype, boolean listOfTypes) {
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

}

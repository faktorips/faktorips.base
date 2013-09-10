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

public abstract class IdentifierNode {

    private final Datatype datatype;

    private final IdentifierNode successor;

    public IdentifierNode(Datatype datatype) {
        this(datatype, null);
    }

    public IdentifierNode(Datatype datatype, IdentifierNode successor) {
        this.datatype = datatype;
        this.successor = successor;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public IdentifierNode getSuccessor() {
        return successor;
    }

    public boolean hasSuccessor() {
        return successor != null;
    }

}

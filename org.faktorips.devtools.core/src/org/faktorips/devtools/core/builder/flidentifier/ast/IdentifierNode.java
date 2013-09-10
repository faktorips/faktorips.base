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

    private final String name;
    private final Datatype datatype;
    private final IdentifierNode successor;

    public IdentifierNode(String name, Datatype datatype) {
        this(name, datatype, null);
    }

    public IdentifierNode(String name, Datatype datatype, IdentifierNode successor) {
        this.name = name;
        this.datatype = datatype;
        this.successor = successor;
    }

    public String getName() {
        return name;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public IdentifierNode getSuccessor() {
        return successor;
    }

}

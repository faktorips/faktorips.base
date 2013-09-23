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

import org.faktorips.util.message.Message;

/**
 * This node is a special kind of {@link IdentifierNode}. It do not relay represent any identifier
 * part but is created in case of exceptions or errors while parsing the identifier. The
 * {@link InvalidIdentifierNode} consists of a helpful human readable and translated error message
 * that helps the user to correct the invalid identifier part.
 * 
 * @author dirmeier
 */
public class InvalidIdentifierNode extends IdentifierNode {

    private final Message errorMessage;

    InvalidIdentifierNode(Message errorMessage) {
        super(null);
        this.errorMessage = errorMessage;
    }

    public Message getMessage() {
        return errorMessage;
    }

}

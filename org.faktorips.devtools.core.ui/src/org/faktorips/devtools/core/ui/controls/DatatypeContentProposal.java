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

package org.faktorips.devtools.core.ui.controls;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.Datatype;

/**
 * A content proposal for <tt>Datatype</tt>. The label and image of these proposals could be
 * provided by the <tt>DatatypeContentProposalProvider</tt>.
 * 
 * @see IContentProposal
 * 
 * @author hbaagil
 */

public class DatatypeContentProposal implements IContentProposal {

    private final Datatype datatype;

    public DatatypeContentProposal(Datatype dataType) {
        this.datatype = dataType;
    }

    public Datatype getDataype() {
        return datatype;
    }

    @Override
    public String getContent() {
        return datatype.getQualifiedName();
    }

    @Override
    public int getCursorPosition() {
        return getContent().length();
    }

    @Override
    public String getLabel() {
        return datatype.getName();
    }

    @Override
    public String getDescription() {
        return StringUtils.EMPTY;
    }

}

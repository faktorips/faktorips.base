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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;

public class IndexBasedAssociationNode extends AssociationNode {

    private final int index;

    public IndexBasedAssociationNode(IAssociation association, int index, IIpsProject ipsProject) throws CoreException {
        this(association, index, ipsProject, null);
    }

    public IndexBasedAssociationNode(IAssociation association, int index, IIpsProject ipsProject,
            IdentifierNode successor) throws CoreException {
        super(association, ipsProject, successor);
        this.index = index;
    }

    @Override
    protected boolean isReturningListOfTypeDatatype() {
        return getAssociation().is1ToManyIgnoringQualifier();
    }

    public int getIndex() {
        return index;
    }

}

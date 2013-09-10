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
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;

public class AssociationNode extends IdentifierNode {

    private final IAssociation association;

    private final IIpsProject ipsProject;

    public AssociationNode(String name, IAssociation association, IIpsProject ipsProject) throws CoreException {
        this(name, association, ipsProject, null);
    }

    public AssociationNode(String name, IAssociation association, IIpsProject ipsProject, IdentifierNode successor)
            throws CoreException {
        super(name, association.findTarget(ipsProject), successor);
        this.association = association;
        this.ipsProject = ipsProject;
    }

    @Override
    public Datatype getDatatype() {
        if (isReturningListOfTypeDatatype()) {
            return new ListOfTypeDatatype(super.getDatatype());
        }
        return super.getDatatype();
    }

    protected boolean isReturningListOfTypeDatatype() {
        return association.is1ToMany();
    }

    public IAssociation getAssociation() {
        return association;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

}

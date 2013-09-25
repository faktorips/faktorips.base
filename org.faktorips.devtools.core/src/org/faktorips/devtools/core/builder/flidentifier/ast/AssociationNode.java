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
import org.faktorips.devtools.core.model.type.IType;

/**
 * This identifier node represents an association. The resulting {@link Datatype} will either be of
 * {@link IType} or of {@link ListOfTypeDatatype} with an {@link IType} as basis type.
 * 
 * @author dirmeier
 */
public class AssociationNode extends IdentifierNode {

    private final IAssociation association;

    private final IIpsProject ipsProject;

    private final boolean listContext;

    AssociationNode(IAssociation association, boolean listContext, IIpsProject ipsProject) throws CoreException {
        super(association.findTarget(ipsProject), association.is1ToMany() || listContext);
        this.association = association;
        this.listContext = listContext;
        this.ipsProject = ipsProject;
    }

    public IAssociation getAssociation() {
        return association;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public boolean isListContext() {
        return listContext;
    }

}

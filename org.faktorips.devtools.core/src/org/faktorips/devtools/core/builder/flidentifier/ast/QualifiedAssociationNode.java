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
 * The qualified association node is a special form of {@link AssociationNode}. It represents an
 * identifier part that was qualified by the name of a product component. The resulting
 * {@link Datatype} will always be a subclass of {@link IType} or a {@link ListOfTypeDatatype} with
 * {@link IType} as basis type.
 * 
 * @author dirmeier
 */
public class QualifiedAssociationNode extends AssociationNode {

    private final String qualifier;
    private final String runtimeID;
    private final IType policyCmptType;

    QualifiedAssociationNode(IAssociation association, String qualifier, String runtimeID, IType poliyCcmptType,
            boolean listOfTypes, IIpsProject ipsProject) throws CoreException {
        super(association, listOfTypes, ipsProject);
        this.qualifier = qualifier;
        this.runtimeID = runtimeID;
        this.policyCmptType = poliyCcmptType;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String getRuntimeID() {
        return runtimeID;
    }

    public IType getPolicyCmptType() {
        return policyCmptType;
    }

}

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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.internal.refactor.TextRegion;
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

    AssociationNode(IAssociation association, boolean listContext, TextRegion textRegion, IIpsProject ipsProject)
            throws CoreException {
        super(association.findTarget(ipsProject), association.is1ToMany() || listContext, textRegion);
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

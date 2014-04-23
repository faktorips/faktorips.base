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
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.util.TextRegion;
import org.faktorips.util.StringUtil;

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

    @Override
    public String getText() {
        return association.getName();
    }

    @Override
    public String getDescription(MultiLanguageSupport multiLanguageSupport) {
        StringBuilder description = new StringBuilder();
        description.append(getText());
        description.append(" -> "); //$NON-NLS-1$
        if (isToManyAssociation()) {
            description.append(Messages.AssociationNode_ListDatatypeDescriptionPrefix);
        }
        description.append(getUnqualifiedTargetName());
        return description.toString();
    }

    private boolean isToManyAssociation() {
        return getAssociation().is1ToMany();
    }

    private String getUnqualifiedTargetName() {
        return StringUtil.unqualifiedName(association.getTarget());
    }

}

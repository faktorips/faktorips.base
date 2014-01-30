/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.List;

import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;

public class InheritedTypeAssociationsPageElement extends
        AbstractInheritedIpsObjectPartsPageElement<IType, IAssociation> {

    public InheritedTypeAssociationsPageElement(DocumentationContext context, IType element, List<IType> superElements) {
        super(context, element, superElements);
    }

    @Override
    protected boolean showObjectPart(IAssociation objectPart) {
        return true;
    }

    @Override
    protected String createHeadline(IType superElement) {
        return getContext().getMessage(HtmlExportMessages.InheritedTypeAssociationsPageElement_inheritedAssociations)
                + " " + superElement.getQualifiedName(); //$NON-NLS-1$
    }

    @Override
    protected List<IAssociation> getIpsObjectParts(IType ipsElement) {
        return ipsElement.getAssociations();
    }

}

/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;

public class InheritedTypeAttributesPageElement extends AbstractInheritedIpsObjectPartsPageElement<IType, IAttribute> {

    private List<String> overwritingAttributes;

    public InheritedTypeAttributesPageElement(DocumentationContext context, IType element, List<IType> superElements) {
        super(context, element, superElements);
    }

    @Override
    protected List<? extends IAttribute> getIpsObjectParts(IType element) {
        return element.getAttributes();
    }

    @Override
    public void build() {
        initializeOverwritingAttributesList();
        super.build();
    }

    private void initializeOverwritingAttributesList() {
        overwritingAttributes = new ArrayList<String>();
        List<? extends IAttribute> ipsObjectParts = getIpsObjectParts(getParentIpsElement());
        for (IAttribute attribute : ipsObjectParts) {
            if (attribute.isOverwrite()) {
                overwritingAttributes.add(attribute.getName());
            }
        }
    }

    @Override
    protected String createHeadline(IType superElement) {
        return getContext().getMessage(HtmlExportMessages.InheritedTypeAttributesPageElement_inheritedAttributes)
                + " " + superElement.getQualifiedName(); //$NON-NLS-1$
    }

    @Override
    protected boolean showObjectPart(IAttribute objectPart) {
        if (overwritingAttributes.contains(objectPart.getName())) {
            return false;
        }
        if (objectPart.isOverwrite()) {
            overwritingAttributes.add(objectPart.getName());
        }
        return true;
    }
}

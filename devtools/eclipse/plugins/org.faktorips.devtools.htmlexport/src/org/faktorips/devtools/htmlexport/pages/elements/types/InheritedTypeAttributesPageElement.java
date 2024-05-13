/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;

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
    protected void buildInternal() {
        initializeOverwritingAttributesList();
        super.buildInternal();
    }

    private void initializeOverwritingAttributesList() {
        overwritingAttributes = new ArrayList<>();
        List<? extends IAttribute> ipsObjectParts = getIpsObjectParts(getParentIpsElement());
        for (IAttribute attribute : ipsObjectParts) {
            if (attribute.isOverriding()) {
                overwritingAttributes.add(getContext().getLabel(attribute));
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
        if (overwritingAttributes.contains(getContext().getLabel(objectPart))) {
            return false;
        }
        if (objectPart.isOverriding()) {
            overwritingAttributes.add(getContext().getLabel(objectPart));
        }
        return true;
    }
}

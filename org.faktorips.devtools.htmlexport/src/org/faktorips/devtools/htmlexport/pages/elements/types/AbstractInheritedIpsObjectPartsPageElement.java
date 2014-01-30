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

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;

public abstract class AbstractInheritedIpsObjectPartsPageElement<S extends IIpsElement, T extends IIpsObjectPartContainer>
        extends AbstractCompositePageElement {

    private static final TextPageElement INHERITED_PARTS_SEPARATOR = new TextPageElement(", "); //$NON-NLS-1$
    private final DocumentationContext context;

    private final S parentIpsElement;
    private final List<S> superElements;

    public AbstractInheritedIpsObjectPartsPageElement(DocumentationContext context, S element, List<S> superElements) {
        this.context = context;
        this.parentIpsElement = element;
        this.superElements = superElements;
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutWrapperPageElement(this);
    }

    @Override
    public void build() {

        List<S> list = superElements;
        for (S superElement : list) {
            if (superElement.equals(parentIpsElement)) {
                continue;
            }

            List<? extends T> objectParts = getIpsObjectParts(superElement);

            if (objectParts.isEmpty()) {
                continue;
            }

            addInheritedObjectParts(superElement, objectParts);
        }
    }

    protected void addInheritedObjectParts(S superElement, List<? extends T> objectParts) {
        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);

        wrapper.addPageElements(new TextPageElement(createHeadline(superElement), TextType.HEADING_3));
        wrapper.addPageElements(createInheritedObjectPartsEnumeration(objectParts));

        addPageElements(wrapper);
    }

    protected WrapperPageElement createInheritedObjectPartsEnumeration(List<? extends T> objectParts) {
        WrapperPageElement inheritedObjectParts = new WrapperPageElement(WrapperType.BLOCK);
        for (T objectPart : objectParts) {
            if (!showObjectPart(objectPart)) {
                continue;
            }

            if (!inheritedObjectParts.isEmpty()) {
                inheritedObjectParts.addPageElements(INHERITED_PARTS_SEPARATOR);
            }
            inheritedObjectParts.addPageElements(createRepresentation(objectPart));
        }
        return inheritedObjectParts;
    }

    protected abstract boolean showObjectPart(T objectPart);

    protected abstract String createHeadline(S superElement);

    protected abstract List<? extends T> getIpsObjectParts(S ipsElement);

    protected IPageElement createRepresentation(T objectPart) {
        return new PageElementUtils().createLinkPageElement(context, objectPart, TargetType.CONTENT, new Style[0]);
    }

    protected S getParentIpsElement() {
        return parentIpsElement;
    }

    protected DocumentationContext getContext() {
        return context;
    }
}

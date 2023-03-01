/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.List;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * A {@link IPageElement} representing a list.
 * 
 * @author dicker
 * 
 */
public class ListPageElement extends AbstractCompositePageElement {
    private boolean ordered = false;

    public ListPageElement(DocumentationContext context) {
        super(context);
    }

    public ListPageElement(List<? extends IPageElement> listElements, DocumentationContext context) {
        super(context);
        getSubElements().addAll(listElements);
    }

    @Override
    protected void buildInternal() {
        // could be overridden
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutListPageElement(this);
    }

    /**
     * @return true, if list is ordered
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * sets list ordered if ordered is true
     * 
     */
    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    @Override
    public void visitSubElements(ILayouter layouter) {
        for (IPageElement subElement : getSubElements()) {
            // TODO HIER DIE KLASSE HOCHSCHIEBEN!!!!
            layouter.layoutWrapperPageElement(createListItem(subElement));
        }
    }

    private WrapperPageElement createListItem(IPageElement subElement) {
        if (isListItem(subElement)) {
            return (WrapperPageElement)subElement;
        }
        return new WrapperPageElement(WrapperType.LISTITEM, getContext(), subElement);
    }

    private boolean isListItem(IPageElement pageElement) {
        if (!(pageElement instanceof WrapperPageElement wrapper)) {
            return false;
        }

        return wrapper.getWrapperType() == WrapperType.LISTITEM;
    }

}

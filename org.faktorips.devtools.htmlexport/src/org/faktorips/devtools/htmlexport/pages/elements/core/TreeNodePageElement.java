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

import org.faktorips.devtools.htmlexport.context.DocumentationContext;

/**
 * A {@link TreeNodePageElement} represents a node in a tree. The children are shown within an
 * indented {@link WrapperPageElement}.
 * 
 * @author dicker
 * 
 */
public class TreeNodePageElement extends WrapperPageElement {

    private final ICompositePageElement wrapper;

    /**
     * creates a {@link TreeNodePageElement} with a IPageElement as representation
     * 
     * @param pageElement IPageElement which represents the node
     * @param context the current {@link DocumentationContext}
     */
    public TreeNodePageElement(IPageElement pageElement, DocumentationContext context) {
        super(WrapperType.BLOCK, context);
        addSubElement(pageElement);
        wrapper = new WrapperPageElement(WrapperType.BLOCK, context);
        wrapper.addStyles(Style.INDENTION);
        addSubElement(wrapper);
    }

    /**
     * adds children to the node.
     */
    @Override
    public ICompositePageElement addPageElements(IPageElement... pageElements) {
        changePageElementsToBlock(pageElements);
        wrapper.addPageElements(pageElements);
        return this;
    }

    private void changePageElementsToBlock(IPageElement... pageElements) {
        for (IPageElement pageElement : pageElements) {
            pageElement.makeBlock();
        }
    }
}

/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;


/**
 * A {@link TreeNodePageElement} represents a node in a tree. The children are shown within an
 * indented {@link WrapperPageElement}.
 * 
 * @author dicker
 * 
 */
public class TreeNodePageElement extends WrapperPageElement {

    AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);

    /**
     * creates a {@link TreeNodePageElement} with a IPageElement as representation
     * 
     * @param pageElement IPageElement which represents the node
     */
    public TreeNodePageElement(IPageElement pageElement) {
        super(WrapperType.BLOCK);
        addSubElement(pageElement);

        wrapper.addStyles(Style.INDENTION);
        addSubElement(wrapper);
    }

    /**
     * adds children to the node.
     */
    @Override
    public AbstractCompositePageElement addPageElements(IPageElement... pageElements) {
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

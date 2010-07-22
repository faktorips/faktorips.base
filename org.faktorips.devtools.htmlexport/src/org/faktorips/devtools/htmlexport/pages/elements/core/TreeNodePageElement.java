/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.faktorips.devtools.htmlexport.generators.WrapperType;

/**
 * A {@link TreeNodePageElement} represents a node in a tree. The children are shown within an
 * indented {@link WrapperPageElement}.
 * 
 * @author dicker
 * 
 */
public class TreeNodePageElement extends WrapperPageElement {

    WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);

    /**
     * creates a {@link TreeNodePageElement} with a PageElement as representation
     * 
     * @param pageElement PageElement which represents the node
     */
    public TreeNodePageElement(PageElement pageElement) {
        super(WrapperType.BLOCK);
        addSubElement(pageElement);

        wrapper.addStyles(Style.INDENTION);
        addSubElement(wrapper);
    }

    /**
     * adds children to the node.
     */
    @Override
    public AbstractCompositePageElement addPageElements(PageElement... pageElements) {
        changePageElementsToBlock(pageElements);
        wrapper.addPageElements(pageElements);
        return this;
    }

    private void changePageElementsToBlock(PageElement... pageElements) {
        for (PageElement pageElement : pageElements) {
            pageElement.makeBlock();
        }
    }
}

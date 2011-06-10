/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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

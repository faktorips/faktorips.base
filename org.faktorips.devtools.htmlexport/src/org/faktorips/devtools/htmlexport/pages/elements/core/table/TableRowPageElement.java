/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import java.util.List;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;

/**
 * {@link TableRowPageElement} represents a row of a table.
 * 
 * @author dicker
 * 
 */
public class TableRowPageElement extends WrapperPageElement {

    private TablePageElement parentTablePageElement;

    /**
     * creates a {@link TableRowPageElement} with the given {@link PageElement}s as content of the
     * cells
     * 
     */
    public TableRowPageElement(PageElement[] pageElements) {
        super(WrapperType.TABLEROW, pageElements);
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutWrapperPageElement(this);
    }

    /**
     * visitSubElements of the {@link TableRowPageElement}. The subelements are wrapped to
     * {@link TableCellPageElement}s for this.
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement
     *      #visitSubElements(org.faktorips.devtools.htmlexport.generators.ILayouter)
     * 
     * 
     */
    @Override
    public void visitSubElements(ILayouter layouter) {

        List<PageElement> subElements = getSubElements();

        int row = parentTablePageElement.getSubElements().indexOf(this);

        for (int column = 0; column < subElements.size(); column++) {
            PageElement subElement = subElements.get(column);

            TableCellPageElement columnPageElement = new TableCellPageElement(subElement);
            layoutTableCell(row, column, columnPageElement);

            layouter.layoutWrapperPageElement(columnPageElement);
        }
    }

    /**
     * layouts the given {@link TableCellPageElement} using all {@link TablePageElementLayout}s of
     * the parent {@link TablePageElement}
     * 
     */
    protected void layoutTableCell(int row, int column, TableCellPageElement columnPageElement) {
        for (TablePageElementLayout tableLayout : parentTablePageElement.getLayouts()) {
            tableLayout.layoutCell(row, column, columnPageElement);
        }
    }

    /**
     * @return the parentTablePageElement
     */
    protected TablePageElement getParentTablePageElement() {
        return parentTablePageElement;
    }

    /**
     * sets the parentTablePageElement
     * 
     */
    protected void setParentTablePageElement(TablePageElement parentTablePageElement) {
        this.parentTablePageElement = parentTablePageElement;
    }
}

/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;

/**
 * {@link TableRowPageElement} represents a row of a table.
 * 
 * @author dicker
 * 
 */
public class TableRowPageElement extends WrapperPageElement {

    private TablePageElement parentTablePageElement;

    /**
     * creates a {@link TableRowPageElement} with the given {@link IPageElement}s as content of the
     * cells
     * 
     */
    public TableRowPageElement(IPageElement[] pageElements) {
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

        List<IPageElement> subElements = getSubElements();

        int row = parentTablePageElement.getSubElements().indexOf(this);

        for (int column = 0; column < subElements.size(); column++) {
            IPageElement subElement = subElements.get(column);

            TableCellPageElement columnPageElement = getTableCellElement(subElement);
            layoutTableCell(row, column, columnPageElement);

            layouter.layoutWrapperPageElement(columnPageElement);
        }
    }

    protected TableCellPageElement getTableCellElement(IPageElement pageElement) {
        if (pageElement instanceof TableCellPageElement) {
            return (TableCellPageElement)pageElement;
        }
        return new TableCellPageElement(pageElement);
    }

    /**
     * layouts the given {@link TableCellPageElement} using all {@link ITablePageElementLayout}s of
     * the parent {@link TablePageElement}
     * 
     */
    protected void layoutTableCell(int row, int column, TableCellPageElement columnPageElement) {
        for (ITablePageElementLayout tableLayout : parentTablePageElement.getLayouts()) {
            tableLayout.layoutCell(row, column, columnPageElement);
        }
    }

    /**
     * @return the parentTablePageElement
     */
    public TablePageElement getParentTablePageElement() {
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

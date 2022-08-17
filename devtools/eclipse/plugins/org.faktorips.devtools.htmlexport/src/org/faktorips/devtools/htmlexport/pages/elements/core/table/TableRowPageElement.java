/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import java.util.List;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
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
     * @param context the current {@link DocumentationContext}
     * 
     */
    public TableRowPageElement(IPageElement[] pageElements, DocumentationContext context) {
        super(WrapperType.TABLEROW, context, pageElements);
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
     *          #visitSubElements(org.faktorips.devtools.htmlexport.generators.ILayouter)
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
        return new TableCellPageElement(getContext(), pageElement);
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

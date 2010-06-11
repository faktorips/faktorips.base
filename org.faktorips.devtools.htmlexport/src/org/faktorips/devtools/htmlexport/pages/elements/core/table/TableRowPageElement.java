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
     * @param pageElements
     */
    public TableRowPageElement(PageElement[] pageElements) {
        super(WrapperType.TABLEROW, pageElements);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement
     * #acceptLayouter(org.faktorips.devtools.htmlexport.generators.ILayouter)
     */
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
     * @param row
     * @param column
     * @param columnPageElement
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
     * @param parentTablePageElement
     */
    protected void setParentTablePageElement(TablePageElement parentTablePageElement) {
        this.parentTablePageElement = parentTablePageElement;
    }
}

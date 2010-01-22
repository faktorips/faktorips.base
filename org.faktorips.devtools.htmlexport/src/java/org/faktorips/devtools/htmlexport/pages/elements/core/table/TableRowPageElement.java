package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import java.util.List;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.PageElementWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;

public class TableRowPageElement extends WrapperPageElement {
	
	private TablePageElement tablePageElement;
	
	public TableRowPageElement(PageElement[] pageElements) {
		super(PageElementWrapperType.TABLEROW, pageElements);
	}

	@Override
	public void acceptLayouter(ILayouter layouter) {
		layouter.layoutWrapperPageElement(this);
	}

    @Override
    public void visitSubElements(ILayouter layouter) {
    	
        List<PageElement> subElements = getSubElements();
        
        int row = tablePageElement.getSubElements().indexOf(this);
        
        for (int i = 0; i < subElements.size(); i++) {
        	PageElement subElement = subElements.get(i);
            
        	TableCellPageElement columnPageElement = new TableCellPageElement(subElement);
			for (TablePageElementLayout tableLayout : tablePageElement.getLayouts()) {
				tableLayout.layoutCell(row, i, columnPageElement);
			}
            
			layouter.layoutWrapperPageElement(columnPageElement);
        }
    }

	protected TablePageElement getTablePageElement() {
		return tablePageElement;
	}

	protected void setTablePageElement(TablePageElement tablePageElement) {
		this.tablePageElement = tablePageElement;
	}
}

package org.faktorips.devtools.core.ui.editors.tablecontents;


import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.views.modeldescription.DefaultModelDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.DescriptionItem;


public class TableModelDescriptionPage extends DefaultModelDescriptionPage {
	
	private ArrayList columnsList;
	private String tableName;
		
    public TableModelDescriptionPage(ITableContents tableContents) throws CoreException {
    	super();

    	columnsList = new ArrayList();
    	this.tableName = tableContents.getName();

    	ITableStructure tableStructure = tableContents.findTableStructure();
    		
    	IColumn[] columns = tableStructure.getColumns();
 
   		for (int i = 0; i < tableContents.getNumOfColumns(); i++) {
   			IColumn column = columns[i];
   			DescriptionItem item = new DescriptionItem(column.getName(),column.getDescription());
   			columnsList.add(item);
   		}
    		    		        
        DescriptionItem[] itemList = new DescriptionItem[columnsList.size()];
        itemList = (DescriptionItem[]) columnsList.toArray(itemList);

        super.setInput(tableName, itemList);
    }
    
    public void createControl(Composite parent) {
     	super.createControl(parent);
    }
    
    public void dispose() {
    	columnsList.clear();
    	
    	super.dispose();
    }

        
}

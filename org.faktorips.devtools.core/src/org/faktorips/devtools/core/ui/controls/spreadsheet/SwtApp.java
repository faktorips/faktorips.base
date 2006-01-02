package org.faktorips.devtools.core.ui.controls.spreadsheet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;


/**
 *
 */
public class SwtApp {

    /**
     * 
     */
    public SwtApp() {
        super();
    }
    
    /**
     * Main method to launch the window 
     */
    public static void main(String[] args) {
        Display display = new Display();
	    Shell shell = new Shell(display);
	    shell.setSize(500, 500);
	    shell.setText("SWT TestApp");
	    FillLayout layout = new FillLayout();
	    shell.setLayout(layout);
	    
	    // Create a composite to hold the children
	    Composite composite = new Composite(shell, SWT.NONE);
	    GridLayout gridLayout = new GridLayout(1, false); 
	    composite.setLayout(gridLayout);
	    

        SpreadsheetControl tableControl = new SpreadsheetControl(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION,
                new SimpleTableContentProvider());
        tableControl.setLayoutData(new GridData(GridData.FILL_BOTH));
	    // Open the shell and run until a close event is detected
	    shell.open();
	    while(!shell.isDisposed()) {
	        if(!display.readAndDispatch())
	            display.sleep();
	    }
	    display.dispose();
    }
    
    private static class SimpleColumnInfo extends ColumnInfo {

        private int index;
        
        
        /**
         * @param columnName
         * @param style
         * @param initialWidth
         * @param datatype
         * @param modifiable
         */
        public SimpleColumnInfo(int index, String columnName, int style, int initialWidth,
                boolean modifiable) {
            super(columnName, style, initialWidth, modifiable);
            this.index = index;
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo#getValue(java.lang.Object)
         */
        public Object getValue(Object rowElement) {
            Object[] simpleRow = (Object[])rowElement;
            return simpleRow[index];
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo#getText(java.lang.Object)
         */
        public String getText(Object rowElement) {
            Object[] simpleRow = (Object[])rowElement;
            return simpleRow[index].toString();
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo#getImage(java.lang.Object)
         */
        public Image getImage(Object rowElement) {
            // TODO Auto-generated method stub
            return null;
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo#setValue(java.lang.Object, java.lang.Object)
         */
        public void setValue(Object rowElement, Object newValue) {
            Object[] simpleRow = (Object[])rowElement;
            simpleRow[index] = newValue;
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo#createEditControl(org.eclipse.swt.widgets.Table)
         */
        public EditField createEditField(Table table) {
            Text text = new Text(table, SWT.NONE);
            return new TextField(text);
        }
        
    }
    
    private static class SimpleTableContentProvider implements TableContentProvider {

	    Object row1 = new String[] {"Hello", "World", "b"};
	    Object row2 = new String[] {"42", "13", "a"};
	    List rows = new ArrayList();
	    
	    ColumnInfo col1 = new SimpleColumnInfo(0, "Column1", SWT.LEFT, 100, true);
	    ColumnInfo col2 = new SimpleColumnInfo(1, "Column2", SWT.RIGHT, 100, true);
	    ColumnInfo col3 = new SimpleColumnInfo(2, "Column3", SWT.LEFT, 100, true);
	    ColumnInfo[] columns = new ColumnInfo[]{col1, col2, col3};
        
        SimpleTableContentProvider() {
            rows.add(row1);
            rows.add(row2);
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.TableContentProvider#newRow()
         */
        public Object newRow() {
            Object[] newRow = new String[] {"1", "2", "3"};
            rows.add(newRow);
            return newRow;
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
            return rows.toArray();
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
            // TODO Auto-generated method stub
            
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // TODO Auto-generated method stub
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.TableContentProvider#deletePendingRow(java.lang.Object)
         */
        public boolean deletePendingRow(Object row) {
            return true;
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.TableContentProvider#deleteRow(java.lang.Object)
         */
        public boolean deleteRow(Object row) {
            return rows.remove(row);
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.TableContentProvider#getColumnInfos()
         */
        public ColumnInfo[] getColumnInfos() {
            return columns;
        }
        
    }
}

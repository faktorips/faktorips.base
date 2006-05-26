/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo;
import org.faktorips.devtools.core.ui.controls.spreadsheet.SpreadsheetControl;
import org.faktorips.devtools.core.ui.controls.spreadsheet.TableContentProvider;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class ContentPage extends IpsObjectEditorPage {
    
    final static String PAGE_ID = "Contents";  //$NON-NLS-1$

    public ContentPage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, Messages.ContentPage_title);
    }
    
    TableContentsEditor getTableEditor() {
        return (TableContentsEditor)getEditor();
    }
    
    ITableContents getTableContents() {
        return getTableEditor().getTableContents(); 
    }

    ITableStructure getTableStructure() throws CoreException {
        return getTableContents().findTableStructure(); 
    }
    
    ITableContentsGeneration getActiveGeneration() {
        return (ITableContentsGeneration)getTableEditor().getPreferredGeneration();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage#createPageContent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		GridLayout layout = new GridLayout(1, false);
		formBody.setLayout(layout);
		try {
		    ITableStructure structure = getTableStructure();
	        SpreadsheetControl tableControl = new SpreadsheetControl(formBody, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION,
	                new ContentProvider(structure));
	        tableControl.setLayoutData(new GridData(GridData.FILL_BOTH));
	        if (getActiveGeneration().getNumOfRows()==0) {
	            getActiveGeneration().newRow();
	            tableControl.refresh();
	        }
		} catch (CoreException e) {
		    throw new RuntimeException(e);
		}
    }
    
    private class TableContentsColumnInfo extends ColumnInfo {
        
        private int columnIndex;
        
        private TableContentsColumnInfo(int index, IColumn column) {
            super(column.getName(), SWT.LEFT, 100, true);
            columnIndex = index;
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo#getValue(java.lang.Object)
         */
        public Object getValue(Object rowElement) {
            IRow row = (IRow)rowElement;
            return row.getValue(columnIndex);
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo#getText(java.lang.Object)
         */
        public String getText(Object rowElement) {
            IRow row = (IRow)rowElement;
            return row.getValue(columnIndex);
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo#getImage(java.lang.Object)
         */
        public Image getImage(Object rowElement) {
            return null;
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo#setValue(java.lang.Object, java.lang.Object)
         */
        public void setValue(Object rowElement, Object newValue) {
            IRow row = (IRow)rowElement;
            row.setValue(columnIndex, (String)newValue);
            return;
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo#createEditField(org.eclipse.swt.widgets.Table)
         */
        public EditField createEditField(Table table) {
            Text text = new Text(table, SWT.NONE);
            return new TextField(text);
        }
        
    }
    
    private class ContentProvider implements TableContentProvider {
        
       private TableContentsColumnInfo[] columnInfos;
       
       private ContentProvider(ITableStructure structure) throws CoreException {
           ArgumentCheck.notNull(structure);
           IColumn[] columns = structure.getColumns();
           columnInfos = new TableContentsColumnInfo[structure.getNumOfColumns()];
           for (int i=0; i<columnInfos.length; i++) {
               columnInfos[i] = new TableContentsColumnInfo(i, columns[i]);
           }
       }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.TableContentProvider#getColumnInfos()
         */
        public ColumnInfo[] getColumnInfos() {
           return columnInfos;
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.controls.spreadsheet.TableContentProvider#newRow()
         */
        public Object newRow() {
            return getActiveGeneration().newRow();
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
        public boolean deleteRow(Object rowObject) {
            IRow row = (IRow)rowObject;
            row.delete();
            return true;
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
            return getActiveGeneration().getRows();
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        
    }

}

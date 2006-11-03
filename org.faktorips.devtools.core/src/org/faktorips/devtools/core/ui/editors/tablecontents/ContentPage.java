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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.table.TableCellEditor;

/**
 * FIXME Doku
 * 
 * @author Stefan Widmaier
 */
public class ContentPage extends IpsObjectEditorPage {

	final static String PAGE_ID = "Contents"; //$NON-NLS-1$
    
    private TableViewer tableViewer;
    

	public ContentPage(IpsObjectEditor editor) {
		super(editor, PAGE_ID, Messages.ContentPage_title);
	}


	/**
	 * {@inheritDoc}
	 */
	protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        checkDifferences(formBody, toolkit);
        
        GridLayout layout = new GridLayout(1, false);
        formBody.setLayout(layout);
          
        TableViewer tableViewer= createTableViewer(formBody);
        initTableViewerData(tableViewer, toolkit, formBody);
	}

    /**
     * 
     * @param formBody
     * @return
     */
    private TableViewer createTableViewer(Composite formBody) {
        // Table: scroll both vertically and horizontally
        Table table= new Table(formBody, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        // occupy all available space
        GridData tableGridData= new GridData(SWT.FILL, SWT.FILL, true, true);
        // FIXME: hints erzwingen Scrollbars in Table, allerdings kann dieser dan KEIN reveal() !!!
        tableGridData.widthHint= formBody.getClientArea().width;
        tableGridData.heightHint= formBody.getClientArea().height;
        table.setLayoutData(tableGridData);
        
        // Viewer
        tableViewer= new TableViewer(table);
        tableViewer.setUseHashlookup(true);
        TableContentsContentProvider contentProvider= new TableContentsContentProvider();
        tableViewer.setContentProvider(contentProvider);
        // FIXME VIRTUAL! contentprovider is at the same time setdata listener
//        tableViewer.getTable().addListener(SWT.SetData, contentProvider);
        tableViewer.setLabelProvider(new TableContentsLabelProvider());
        
        table.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e) {
                // edit first cell of the selected row
                if(e.keyCode==SWT.F2){
                    IRow selectedRow= (IRow) ((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
                    if(selectedRow!=null){
                        tableViewer.editElement(selectedRow, 0);
                    }
                }
            }
        });
        
        return tableViewer;
    }

    private void initTableViewerData(TableViewer tableViewer, UIToolkit toolkit, Composite formBody){
        try{            
            ITableStructure tableStructure= getTableStructure();
            Table table= tableViewer.getTable();
            table.removeAll();
            
            for (int i = 0; i < tableStructure.getNumOfColumns(); i++) {
                TableColumn column= new TableColumn(table, SWT.LEFT, i);
                column.setText(tableStructure.getColumn(i).getName());
                column.setWidth(125);
            }

            String[] columnProperties= new String[tableStructure.getNumOfColumns()];
            CellEditor[] editors= new CellEditor[tableStructure.getNumOfColumns()];
            tableViewer.setCellModifier(new TableContentsCellModifier(tableViewer));
            for (int i = 0; i < tableStructure.getNumOfColumns(); i++) {
                columnProperties[i]= tableStructure.getColumn(i).getName();
            }
            tableViewer.setColumnProperties(columnProperties); 
            // column properties must be set before cellEditors are created.
            for (int i = 0; i < tableStructure.getNumOfColumns(); i++) {
                ValueDatatype dataType= tableStructure.getColumn(i).findValueDatatype();
                ValueDatatypeControlFactory factory= IpsPlugin.getDefault().getValueDatatypeControlFactory(dataType);
                TableCellEditor cellEditor= factory.createCellEditor(toolkit, dataType, null, tableViewer, i);
                cellEditor.setRowCreating(true);
                editors[i]= cellEditor;
            }
            tableViewer.setCellEditors(editors);
            tableViewer.setSorter(new TableSorter());

            tableViewer.addSelectionChangedListener(new RowDeletor());
            
            // init 
            tableViewer.setInput(getTableContents());
        }catch(CoreException e){
            IpsPlugin.log(e);
        }
    }
    
    /**
     * Listener that reacts to <code>SelectionChangedEvent</code>s by deleting all empty rows
     * at the bottom of the table.
     * <p>
     * The mechanism to create new row dynamically is realized in the <code>TableCellEditor</code>.
     * @see TableCellEditor
     * @author Stefan Widmaier
     */
    private class RowDeletor implements ISelectionChangedListener{
        public void selectionChanged(SelectionChangedEvent event) {
            removeRedundantRows();
        }
        /**
         * Checks every row from the last up to the currently selected row for emptyness and
         * deleted every empty row until a non-empty row is found.
         * <p>
         * Only tries to delete rows if table has more than two rows.
         * 
         */
        private void removeRedundantRows() {
            int selectionIndex = tableViewer.getTable().getSelectionIndex();
            if (tableViewer.getTable().getItemCount() <= 2) {
                return;
            }
            for (int i = tableViewer.getTable().getItemCount() - 1; i > selectionIndex; i--) {
                IRow row = (IRow)tableViewer.getElementAt(i);
                if (isRowEmpty(row)) {
                    tableViewer.remove(row);
                    row.delete();
                } else {
                    break;
                }
            }
            /* FIXME Bug in TableViewer:
             * CellEditor position is not updated after deletion of rows.
             * The fix is to update the position artificially by scrolling 
             * the table.
             * Problem: tableViewer#scrollDown(x, y) calls the Viewer implementation
             * which does nothing.
             */ 
        }
        
        /**
         * Checks wether a row is empty or not. Returns <code>true</code> if all the given row's
         * values (columns) contain a whitespace string.
         * <p>
         * <code>null</code> is treated as content. Thus a row that contains <code>null</code>
         * values is not empty.
         */
        private boolean isRowEmpty(IRow row) {
            int columnNumber= row.getTableContents().getNumOfColumns();
            for (int i = 0; i < columnNumber; i++) {
                String value= row.getValue(i);
                if(value==null || !value.trim().equals("")){
                    return false;
                }
            }
            return true;
        }
    }

    private void checkDifferences(Composite formBody, UIToolkit toolkit) {
        try {
            ITableStructure structure = getTableStructure();
            
            if (structure == null) {
                String msg = NLS.bind(Messages.ContentPage_msgMissingStructure,
                        getTableContents().getTableStructure());
                SetStructureDialog dialog = new SetStructureDialog(
                        getTableContents(), getSite().getShell(), msg);
                int button = dialog.open();
                if (button != SetStructureDialog.OK) {
                    msg = NLS.bind(Messages.ContentPage_msgNoStructureFound,
                            getTableContents().getTableStructure());
                    toolkit.createLabel(formBody, msg);
                    return;
                } else {
                    structure = getTableStructure();
                }
            }
            
            int difference = structure.getColumns().length
            - getTableContents().getNumOfColumns();
            
            if (difference != 0) {
                IInputValidator validator = new Validator(difference);
                
                String msg = null;
                String title = null;
                if (difference > 1) {
                    title = Messages.ContentPage_titleMissingColumns;
                    msg = NLS.bind(Messages.ContentPage_msgAddMany, String
                            .valueOf(difference), String
                            .valueOf(getTableContents().getNumOfColumns()));
                    
                } else if (difference == 1) {
                    title = Messages.ContentPage_titleMissingColumn;
                    msg = NLS.bind(Messages.ContentPage_msgAddOne, String
                            .valueOf(getTableContents().getNumOfColumns()));
                } else if (difference == -1) {
                    title = Messages.ContentPage_titleTooMany;
                    msg = NLS.bind(Messages.ContentPage_msgRemoveOne, String
                            .valueOf(getTableContents().getNumOfColumns()));
                } else if (difference < -1) {
                    title = Messages.ContentPage_titleTooMany;
                    msg = NLS.bind(Messages.ContentPage_msgRemoveMany, String
                            .valueOf(Math.abs(difference)), String
                            .valueOf(getTableContents().getNumOfColumns()));
                }
                
                InputDialog dialog = new InputDialog(getSite().getShell(),
                        title, msg, "", validator); //$NON-NLS-1$
                int state = dialog.open();
                if (state == InputDialog.OK) {
                    if (difference > 0) {
                        insertColumnsAt(dialog.getValue());
                    } else {
                        removeColumns(dialog.getValue());
                    }
                } else {
                    toolkit.createLabel(formBody,
                            Messages.ContentPage_msgCantShowContent);
                    return;
                }
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }    


    private void insertColumnsAt(String insertIndices) {
		int[] indices = getIndices(insertIndices);
		for (int i = 0; i < indices.length; i++) {
			((TableContents) getTableContents())
					.newColumnAt(indices[i] + i, ""); //$NON-NLS-1$
		}
	}

	private void removeColumns(String removeIndices) {
		int[] indices = getIndices(removeIndices);
		for (int i = 0; i < indices.length; i++) {
			((TableContents) getTableContents()).deleteColumn(indices[i] - i);
		}
	}

	private int[] getIndices(String indices) {
		StringTokenizer tokenizer = getTokenizer(indices);
		int[] result = new int[tokenizer.countTokens()];
		for (int i = 0; tokenizer.hasMoreTokens(); i++) {
			result[i] = Integer.valueOf(tokenizer.nextToken()).intValue();
		}

		Arrays.sort(result);
		return result;
	}

	private StringTokenizer getTokenizer(String tokens) {
		return new StringTokenizer(tokens, ",", false); //$NON-NLS-1$
	}
    
    private TableContentsEditor getTableEditor() {
        return (TableContentsEditor) getEditor();
    }

    private ITableContents getTableContents() {
        return getTableEditor().getTableContents();
    }

    private ITableStructure getTableStructure() throws CoreException {
        return getTableContents().findTableStructure();
    }

    private ITableContentsGeneration getActiveGeneration() {
        return (ITableContentsGeneration) getTableEditor()
                .getPreferredGeneration();
    }

	private class Validator implements IInputValidator {
		private int indexCount = 0;

		public Validator(int requiredIndexCount) {
			this.indexCount = requiredIndexCount;
		}

		/**
		 * {@inheritDoc}
		 */
		public String isValid(String newText) {
			StringTokenizer tokenizer = getTokenizer(newText);
			int tokenizerItemCount = tokenizer.countTokens();

			ArrayList values = new ArrayList(tokenizerItemCount);
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				try {
					Integer value = Integer.valueOf(token);
					if (values.contains(value) && indexCount < 0) {
						return Messages.ContentPage_errorNoDuplicateIndices;
					}
					if (indexCount < 0
							&& (value.intValue() >= getTableContents()
									.getNumOfColumns() || value.intValue() < 0)) {
						return NLS.bind(
								Messages.ContentPage_errorIndexOutOfRange,
								value);
					}
					values.add(value);
				} catch (NumberFormatException e) {
					if (indexCount == 1) {
						return NLS.bind(
								Messages.ContentPage_errorInvalidValueOne,
								token);
					} else {
						return NLS.bind(
								Messages.ContentPage_errorInvalidValueMany,
								token);
					}
				}
			}

			int difference = Math.abs(indexCount) - tokenizerItemCount;
			if (difference < 0) {
				if (indexCount == 1 || indexCount == -1) {
					return Messages.ContentPage_errorTooManyOne;
				} else {
					return NLS.bind(Messages.ContentPage_errorTooManyMany,
							String.valueOf(Math.abs(indexCount)));
				}
			} else if (difference == 1) {
				return Messages.ContentPage_errorOneMore;
			} else if (difference > 1) {
				return NLS.bind(Messages.ContentPage_errorManyMore, String
						.valueOf(difference));
			}

			return null;
		}
	}
    
    
}

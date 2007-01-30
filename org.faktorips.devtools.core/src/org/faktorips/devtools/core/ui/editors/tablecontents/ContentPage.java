/*******************************************************************************
�* Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
�*
�* Alle Rechte vorbehalten.
�*
�* Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
�* Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
�* Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
�* genutzt werden, die Bestandteil der Auslieferung ist und auch unter
�* � http://www.faktorips.org/legal/cl-v01.html
�* eingesehen werden kann.
 *
�* Mitwirkende:
�* � Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
�*
�*******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.widgets.ScrolledForm;
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
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.table.TableCellEditor;
import org.faktorips.devtools.core.ui.wizards.tableexport.TableExportWizard;
import org.faktorips.util.message.MessageList;

/**
 * The content-page for the <code>TableContentsEditor</code>. Allows the editing of <code>TableContents</code>
 * using a <code>TableViewer</code>.
 * 
 * @author Stefan Widmaier
 */
public class ContentPage extends IpsObjectEditorPage {
    /*
     * SWT event type for the measure item event
     * @since 3.2
     * defined here to ensure compatibility to lower SWT versions
     */
    private static final int SWT_MeasureItem = 41;
    
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
          
        Table table= createTable(formBody);
        initTableViewer(table, toolkit, formBody);
        DeleteRowAction deleteRowAction = new DeleteRowAction(tableViewer, this);
        initTablePopupMenu(table, deleteRowAction);

        /* Create a single row if an empty tablecontents is opened. 
         * Otherwise no editing is possible.
         */
        if(getActiveGeneration().getNumOfRows()==0){
            getActiveGeneration().newRow();
        }
        
        tableViewer.setInput(getTableContents());

        ScrolledForm form= getManagedForm().getForm();
        form.getToolBarManager().add(deleteRowAction);
        form.getToolBarManager().add(new Separator());
        form.getToolBarManager().add(new OpenTableExportWizardAction(getSite().getWorkbenchWindow(), getTableContents()));
        form.updateToolBar();
	}

    /**
     * Creates a Table with the given formBody as a parent and returns it. Inits the look, layout of
     * the table and adds a KeyListener that enables the editing of the first cell in the currently
     * selected row by pressing "F2".
     * 
     * @param formBody
     * @return The newly created and initalized Table.
     */
    private Table createTable(Composite formBody) {
        // Table: scroll both vertically and horizontally
        Table table= new Table(formBody, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        // occupy all available space
        GridData tableGridData= new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGridData.widthHint= formBody.getClientArea().width;
        tableGridData.heightHint= formBody.getClientArea().height;
        table.setLayoutData(tableGridData);
        table.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e) {
                if(e.keyCode==SWT.F2){
                    IRow selectedRow= (IRow) ((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
                    if(selectedRow!=null){
                        tableViewer.editElement(selectedRow, 0);
                    }
                }
            }
        });
        
// TODO VIRTUAL table causes exceptions when creating and deleting rows dynamically, see FS#533
//        table.addListener(SWT.SetData, new VirtualTableListener(table, getTableContents()));
        
        return table;
    }
    

    /**
     * Inits the <code>TableViewer</code> for this page. Sets content- and labelprovider, column headers and
     * widths, column properties, cell editors, sorter. Inits popupmenu and hoverservice. 
     * @param table
     * @param toolkit
     * @param formBody
     */
    private void initTableViewer(Table table, UIToolkit toolkit, Composite formBody){
        try{            
            table.removeAll();
            increaseHeightOfTableRow(table, getTableContents().getNumOfColumns());
            
            tableViewer= new TableViewer(table);
            tableViewer.setUseHashlookup(true);
            tableViewer.setContentProvider(new TableContentsContentProvider());
            TableContentsLabelProvider labelProvider = new TableContentsLabelProvider();
            tableViewer.setLabelProvider(labelProvider);
            
            ITableStructure tableStructure= getTableStructure();
            String[] columnProperties= new String[getTableContents().getNumOfColumns()];
            for (int i = 0; i < getTableContents().getNumOfColumns(); i++) {
                String columnName;
                if (tableStructure==null) {
                    columnName = Messages.ContentPage_Column + (i+1);
                } else {
                    columnName = tableStructure.getColumn(i).getName();
                }
                TableColumn column= new TableColumn(table, SWT.LEFT, i);
                column.setWidth(125);
                column.setText(columnName);
                columnProperties[i]= columnName;
            }
            tableViewer.setCellModifier(new TableContentsCellModifier(tableViewer, this));
            tableViewer.setColumnProperties(columnProperties); 

            // column properties must be set before cellEditors are created.
            ValueDatatype[] datatypes = new ValueDatatype[getTableContents().getNumOfColumns()];
            if (tableStructure!=null) {
                // use the number of columns in the contents as only those can be edited.
                CellEditor[] editors= new CellEditor[getTableContents().getNumOfColumns()];
                for (int i = 0; i < getTableContents().getNumOfColumns(); i++) {
                    ValueDatatype dataType = tableStructure.getColumn(i).findValueDatatype();
                    ValueDatatypeControlFactory factory= IpsPlugin.getDefault().getValueDatatypeControlFactory(dataType);
                    TableCellEditor cellEditor= factory.createCellEditor(toolkit, dataType, null, tableViewer, i);
                    cellEditor.setRowCreating(true);
                    editors[i]= cellEditor;
                    datatypes[i] = dataType;
                }
                tableViewer.setCellEditors(editors);
            }
            labelProvider.setValueDatatypes(datatypes);
            tableViewer.setSorter(new TableSorter());
            tableViewer.addSelectionChangedListener(new RowDeletor());
            
            new TableMessageHoverService(tableViewer){
                protected MessageList getMessagesFor(Object element) throws CoreException {
                    if(element!=null){
                        return ((IRow)element).validate();
                    }
                    return null;
                }
            };
        }catch(CoreException e){
            IpsPlugin.log(e);
        }
    }
    /**
     * Adds the given deleteRowAction to the popupmenu of th given table.
     * @param table
     * @param deleteRowAction
     */
    private void initTablePopupMenu(Table table, DeleteRowAction deleteRowAction){
        // popupmenu
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(false);
        menuMgr.add(deleteRowAction);
        Menu menu = menuMgr.createContextMenu(table);
        table.setMenu(menu);
//        do not register to avoid mb additions
//        getSite().registerContextMenu(menuMgr, tableViewer);
    }


    private void increaseHeightOfTableRow(Table table, final int numOfColumns) {
        // add paint lister to increase the height of the table row,
        // because @since 3.2 in edit mode the cell becomes a border and the bottom pixel of the
        // text is hidden
        Listener paintListener = new Listener() {
            public void handleEvent(Event event) {
                switch(event.type) {        
                    case SWT_MeasureItem: {
                        if (numOfColumns == 0){
                            return;
                        }
                        TableItem item = (TableItem)event.item;
                        // column 0 will be used to determine the height,
                        // <code>event.index<code> couldn't be used because it is only available
                        // @since 3.2, that's ok because the height is always the same, even if the
                        // column contains no text, the height only depends on the font
                        String text = getText(item, 0);
                        Point size = event.gc.textExtent(text);
                        // the height will be increased by 5 pixel
                        event.height = Math.max(event.height, size.y + 5);
                        break;
                    }
                }
            }
            String getText(TableItem item, int column) {
                String text = item.getText(column);
                return text;
            }
        };
        table.addListener(SWT_MeasureItem, paintListener);
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
         * deletes every empty row until a non-empty row is found.
         * <p>
         * Only tries to delete rows if table has more than one row.
         * 
         */
        private void removeRedundantRows() {
            int selectionIndex = tableViewer.getTable().getSelectionIndex();
            if (tableViewer.getTable().getItemCount() <= 1) {
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
            /* TODO Bug in TableViewer:
             * CellEditor position is not updated after deletion of rows.
             * The fix is to update the position artificially by scrolling 
             * the table/Tableviewer.
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
                if(value==null || !value.trim().equals("")){ //$NON-NLS-1$
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
            
            int difference = structure.getColumns().length - getTableContents().getNumOfColumns();
            
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
    
    private ITableContentsGeneration getActiveGeneration(){
        return (ITableContentsGeneration) getTableEditor().getTableContents().getFirstGeneration();
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
    

    /**
     * Action that opens the wizard for exporting TableContents to M$-Excel files.
     * 
     * @author Stefan Widmaier
     */
    private class OpenTableExportWizardAction extends Action {

        /**
         * The TableContents to be exported.
         */
        ITableContents tableContents;
        IWorkbenchWindow window;
        
        public OpenTableExportWizardAction(IWorkbenchWindow window, ITableContents tableContents) {
            this.tableContents= tableContents;
            this.window= window;
            setText(Messages.ContentPage_ExportTableAction_label);
            setToolTipText(Messages.ContentPage_ExportTableAction_tooltip);
            setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("ExportTableContents.gif")); //$NON-NLS-1$
        }
        
        /**
         * Opens a TableExportWizard that is initialized with the <code>TableContents</code> this action 
         * was created with.
         * {@inheritDoc}
         */
        public void run() {
            TableExportWizard wizard= new TableExportWizard();
            wizard.init(window.getWorkbench(), new StructuredSelection(tableContents));
            WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
            dialog.open();
        }
    }
    
}

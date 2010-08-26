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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A control that contains a table on the left and buttons to add and remove rows and to move a row
 * up or down.
 */
public abstract class EditTableControl extends Composite implements IDataChangeableReadWriteAccess {

    private UIToolkit uiToolkit = new UIToolkit(null);

    private Button addButton;
    private Button removeButton;
    private Button upButton;
    private Button downButton;

    private Table table;
    private TableViewer tableViewer;
    private boolean dataChangeable;

    private Label tableLabel;

    public EditTableControl(Object modelObject, Composite parent, int style, String label) {
        super(parent, style);
        initModelObject(modelObject);
        setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);

        if (label != null) {
            tableLabel = new Label(this, SWT.NONE);
            GridData labelGd = new GridData();
            labelGd.horizontalSpan = 2;
            tableLabel.setLayoutData(labelGd);
            tableLabel.setText(label);
        }

        createTable(this);
        createButtonComposite(this);
        tableViewer.setInput(modelObject);
    }

    protected abstract void initModelObject(Object modelObject);

    public void refresh() {
        tableViewer.refresh();
    }

    protected Table getTable() {
        return table;
    }

    protected TableViewer getTableViewer() {
        return tableViewer;
    }

    private void createTable(Composite parent) {
        TableLayoutComposite layouter = new TableLayoutComposite(parent, SWT.NONE);
        addColumnLayoutData(layouter);
        table = new Table(layouter, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        createTableColumns(table);

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 40;
        layouter.setLayoutData(gd);

        tableViewer = new TableViewer(table);
        tableViewer.setUseHashlookup(true);
        tableViewer.setContentProvider(createContentProvider());
        tableViewer.setLabelProvider(createLabelProvider());

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtonsEnabledState();
            }
        });

        table.addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN && e.stateMask == SWT.NONE) {
                    editColumnOrNextPossible(0);
                    e.detail = SWT.TRAVERSE_NONE;
                }
            }
        });
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.F2 && e.stateMask == SWT.NONE) {
                    editColumnOrNextPossible(0);
                    e.doit = false;
                }
            }
        });

        tableViewer.setColumnProperties(getColumnPropertyNames());
        UnfocusableTextCellEditor[] editors = createCellEditors();
        if (editors != null && editors.length != table.getColumnCount()) {
            throw new RuntimeException("Number of editors must be equal to the number of table columns!"); //$NON-NLS-1$
        }
        tableViewer.setCellEditors(editors);
        if (getColumnPropertyNames().length != table.getColumnCount()) {
            throw new RuntimeException("Number of ColumnProperties must be equal to the number of table columns!"); //$NON-NLS-1$
        }
        tableViewer.setCellModifier(createCellModifier());

        for (int i = 0; i < editors.length; i++) {
            if (editors[i] != null) {
                addListenersToEditor(editors[i], i);
            }
        }
    }

    private void addListenersToEditor(final UnfocusableTextCellEditor editor, final int editorColumn) {
        Control control = editor.getControl();
        control.addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent e) {
                switch (e.detail) {
                    case SWT.TRAVERSE_TAB_NEXT:
                        editColumnOrNextPossible(nextColumn(editorColumn));
                        e.detail = SWT.TRAVERSE_NONE;
                        break;

                    case SWT.TRAVERSE_TAB_PREVIOUS:
                        editColumnOrPrevPossible(prevColumn(editorColumn));
                        e.detail = SWT.TRAVERSE_NONE;
                        break;

                    case SWT.TRAVERSE_ESCAPE:
                        tableViewer.cancelEditing();
                        e.detail = SWT.TRAVERSE_NONE;
                        break;

                    case SWT.TRAVERSE_RETURN:
                        editor.deactivate();
                        e.detail = SWT.TRAVERSE_NONE;
                        break;

                    default:
                        break;
                }
            }
        });
        // support switching rows while editing:
        control.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.stateMask == SWT.MOD1 || e.stateMask == SWT.MOD2) {
                    if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN) {
                        // allow starting multi-selection even if in edit mode
                        editor.deactivate();
                        e.doit = false;
                        return;
                    }
                }

                if (e.stateMask != SWT.NONE) {
                    return;
                }

                switch (e.keyCode) {
                    case SWT.ARROW_DOWN:
                        e.doit = false;
                        int nextRow = table.getSelectionIndex() + 1;
                        if (nextRow >= table.getItemCount()) {
                            break;
                        }
                        table.setSelection(nextRow);
                        editColumnOrPrevPossible(editorColumn);
                        break;

                    case SWT.ARROW_UP:
                        e.doit = false;
                        int prevRow = table.getSelectionIndex() - 1;
                        if (prevRow < 0) {
                            break;
                        }
                        table.setSelection(prevRow);
                        editColumnOrPrevPossible(editorColumn);
                        break;

                    case SWT.F2:
                        e.doit = false;
                        editor.deactivate();
                        break;
                }
            }
        });

        editor.addListener(new ICellEditorListener() {
            /*
             * bug 58540: change signature refactoring interaction: validate as you type
             * [refactoring] CellEditors validate on keystroke by updating model on
             * editorValueChanged(..)
             */
            @Override
            public void applyEditorValue() {
                // default behavior is OK
            }

            @Override
            public void cancelEditor() {
                // must reset model to original value:
                editor.fireModifyEvent(editor.getOriginalValue(), editorColumn);
            }

            @Override
            public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                editor.fireModifyEvent(editor.getValue(), editorColumn);
            }
        });
    }

    protected abstract UnfocusableTextCellEditor[] createCellEditors();

    protected abstract ICellModifier createCellModifier();

    /**
     * Returns the property names used by the cell modifier.
     */
    protected abstract String[] getColumnPropertyNames();

    protected String getProperty(int columnIndex) {
        return getColumnPropertyNames()[columnIndex];
    }

    protected abstract IStructuredContentProvider createContentProvider();

    protected ILabelProvider createLabelProvider() {
        return new DefaultLabelProvider();
    }

    protected abstract void createTableColumns(Table table);

    protected abstract void addColumnLayoutData(TableLayoutComposite layouter);

    private void editColumnOrNextPossible(int column) {
        Object[] selected = getSelectedElements();
        if (selected.length != 1) {
            return;
        }
        int nextColumn = column;
        do {
            tableViewer.editElement(selected[0], nextColumn);
            if (tableViewer.isCellEditorActive()) {
                return;
            }
            nextColumn = nextColumn(nextColumn);
        } while (nextColumn != column);
    }

    private void editColumnOrPrevPossible(int column) {
        Object[] selected = getSelectedElements();
        if (selected.length != 1) {
            return;
        }
        int prevColumn = column;
        do {
            tableViewer.editElement(selected[0], prevColumn);
            if (tableViewer.isCellEditorActive()) {
                return;
            }
            prevColumn = prevColumn(prevColumn);
        } while (prevColumn != column);
    }

    private int nextColumn(int column) {
        return (column >= table.getColumnCount() - 1) ? 0 : column + 1;
    }

    private int prevColumn(int column) {
        return (column <= 0) ? table.getColumnCount() - 1 : column - 1;
    }

    private Object[] getSelectedElements() {
        ISelection selection = tableViewer.getSelection();
        if (selection == null) {
            return new Object[0];
        }

        if (!(selection instanceof IStructuredSelection)) {
            return new Object[0];
        }

        return ((IStructuredSelection)selection).toArray();
    }

    // ---- Button bar
    // --------------------------------------------------------------------------------------

    private void createButtonComposite(Composite parent) {
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        buttonComposite.setLayout(gl);

        addButton = createAddButton(buttonComposite);
        removeButton = createRemoveButton(buttonComposite);
        addSpacer(buttonComposite);
        upButton = createMoveButton(buttonComposite, "Move up", true); //$NON-NLS-1$
        downButton = createMoveButton(buttonComposite, "Move down", false); //$NON-NLS-1$

        updateButtonsEnabledState();
    }

    private void addSpacer(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 5;
        label.setLayoutData(gd);
    }

    private void updateButtonsEnabledState() {
        if (!dataChangeable) {
            uiToolkit.setDataChangeable(addButton, false);
            uiToolkit.setDataChangeable(removeButton, false);
            uiToolkit.setDataChangeable(upButton, false);
            uiToolkit.setDataChangeable(downButton, false);
            return;
        }
        addButton.setEnabled(true);
        removeButton.setEnabled(table.getSelectionCount() != 0);
        upButton.setEnabled(table.getSelectionCount() != 0);
        downButton.setEnabled(table.getSelectionCount() != 0);
    }

    private Button createAddButton(Composite buttonComposite) {
        Button button = new Button(buttonComposite, SWT.PUSH);
        button.setText("Add"); //$NON-NLS-1$
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addElement();
                tableViewer.refresh();
                tableViewer.getControl().setFocus();
                int row = table.getItemCount() - 1;
                table.setSelection(row);
                updateButtonsEnabledState();
                editColumnOrNextPossible(0);
            }
        });
        return button;
    }

    private Button createRemoveButton(Composite buttonComposite) {
        final Button button = new Button(buttonComposite, SWT.PUSH);
        button.setText("Remove"); //$NON-NLS-1$
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] indices = table.getSelectionIndices();
                for (int i = indices.length - 1; i >= 0; i--) {
                    removeElement(indices[i]);
                }
                restoreSelection(indices[0]);
            }

            private void restoreSelection(int index) {
                tableViewer.refresh();
                tableViewer.getControl().setFocus();
                int itemCount = table.getItemCount();
                if (itemCount != 0 && index >= itemCount) {
                    index = itemCount - 1;
                    table.setSelection(index);
                }
                updateButtonsEnabledState();
            }
        });
        return button;
    }

    private Button createMoveButton(Composite buttonComposite, String text, final boolean up) {
        Button button = new Button(buttonComposite, SWT.PUSH);
        button.setText(text);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (table.getSelectionCount() == 0) {
                    return;
                }
                int[] newSelection;
                if (up) {
                    newSelection = moveUp(table.getSelectionIndices());
                } else {
                    newSelection = moveDown(table.getSelectionIndices());
                }
                tableViewer.refresh();
                table.setSelection(newSelection);
                tableViewer.getControl().setFocus();
            }
        });
        return button;
    }

    protected abstract Object addElement();

    protected abstract void removeElement(int index);

    protected int[] moveUp(int[] indices) {
        if (contains(indices, 0)) {
            return indices;
        }
        int[] newSelection = new int[indices.length];
        int j = 0;
        for (int i = 1; i < table.getItemCount(); i++) {
            if (contains(indices, i)) {
                swapElements(i - 1, i);
                newSelection[j] = i - 1;
                j++;
            }
        }
        return newSelection;
    }

    protected int[] moveDown(int[] indices) {
        if (contains(indices, table.getItemCount() - 1)) {
            return indices;
        }
        int[] newSelection = new int[indices.length];
        int j = 0;
        for (int i = table.getItemCount() - 2; i >= 0; i--) {
            if (contains(indices, i)) {
                swapElements(i, i + 1);
                newSelection[j++] = i + 1;
            }
        }
        return newSelection;

    }

    private boolean contains(int[] indices, int index) {
        for (int indice : indices) {
            if (indice == index) {
                return true;
            }
        }
        return false;
    }

    protected abstract void swapElements(int index1, int index2);

    public class UnfocusableTextCellEditor extends TextCellEditor {

        private Object fOriginalValue;
        SubjectControlContentAssistant fContentAssistant;
        private boolean fSaveNextModification;

        public UnfocusableTextCellEditor(Composite parent) {
            super(parent);
        }

        @Override
        public void activate() {
            super.activate();
            fOriginalValue = doGetValue();
        }

        public Object getOriginalValue() {
            return fOriginalValue;
        }

        public void fireModifyEvent(Object newValue, final int property) {
            tableViewer.getCellModifier().modify(((IStructuredSelection)tableViewer.getSelection()).getFirstElement(),
                    getProperty(property), newValue);
        }

        @Override
        protected void focusLost() {
            if (fContentAssistant != null && fContentAssistant.hasProposalPopupFocus()) {
                fSaveNextModification = true;
            } else {
                super.focusLost();
            }
        }

        public void setContentAssistant(SubjectControlContentAssistant assistant, final int property) {
            fContentAssistant = assistant;
            // workaround for bugs 53629, 58777:
            text.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    if (fSaveNextModification) {
                        fSaveNextModification = false;
                        final String newValue = text.getText();
                        tableViewer.getCellModifier().modify(
                                ((IStructuredSelection)tableViewer.getSelection()).getFirstElement(),
                                getProperty(property), newValue);
                        editColumnOrNextPossible(property);
                    }
                }
            });
        }
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;
        addButton.setEnabled(changeable);
        removeButton.setEnabled(changeable);
        downButton.setEnabled(changeable);
        upButton.setEnabled(changeable);
        table.setEnabled(changeable);
        if (tableLabel != null) {
            tableLabel.setEnabled(changeable);
        }
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setDataChangeable(enabled);
    }

}

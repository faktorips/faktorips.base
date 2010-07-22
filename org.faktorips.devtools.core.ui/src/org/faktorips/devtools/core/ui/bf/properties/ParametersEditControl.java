/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.properties;

/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 *******************************************************************************/

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DatatypeCompletionProcessor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * A control consisting of a table and an add and remove button by means of which parameters of a
 * business function can be defined.
 * 
 * @author Peter Erzberger
 */
public class ParametersEditControl extends Composite {

    private IBusinessFunction paramContainer;

    private static final String[] PROPERTIES = { "message", "type", "new" }; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
    private static final int MESSAGE_PROP = 0;
    private static final int TYPE_PROP = 1;
    private static final int NEWNAME_PROP = 2;

    private int tableStyle = SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION;

    private TableViewer fTableViewer;

    private Button fAddButton;
    private Button fRemoveButton;

    private IIpsProject ipsProject;
    private UIToolkit uiToolkit;

    public ParametersEditControl(Composite parent, UIToolkit uiToolkit) {
        super(parent, SWT.NONE);
        ArgumentCheck.notNull(uiToolkit, this);
        this.uiToolkit = uiToolkit;
    }

    /**
     * Creates the compoiste's controls. This method has to be called by this controls client, after
     * the control has been configured via the appropiate setter method, e.g.
     * <code>setNumOfRowsHint(int rows)</code>
     */
    public void initControl() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);
        createParameterList(this);
        createButtonComposite(this);
    }

    private MessageList validate(IParameterBFE param) throws CoreException {
        MessageList result = paramContainer.validate(ipsProject);
        return result.getMessagesFor(param);
    }

    public void setInput(IBusinessFunction paramContainer) {
        ArgumentCheck.notNull(paramContainer);
        this.paramContainer = paramContainer;
        ipsProject = paramContainer.getIpsProject();
        fTableViewer.setInput(paramContainer);
        if (paramContainer.getParameterBFEs().size() > 0) {
            fTableViewer.setSelection(new StructuredSelection(paramContainer.getParameterBFEs().get(0)));
        }
        CellEditor[] editors = fTableViewer.getCellEditors();
        installParameterTypeContentAssist(editors[TYPE_PROP].getControl());
    }

    // ---- Parameter table
    // -----------------------------------------------------------------------------------

    private void createParameterList(Composite parent) {
        Composite layouter = uiToolkit.createComposite(parent);
        GridData data = new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, true, true);
        layouter.setLayoutData(data);
        layouter.setLayout(new GridLayout(1, false));

        final Table table = uiToolkit.createTable(layouter, tableStyle);
        data = new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, true, true);
        data.minimumHeight = 200;
        table.setLayoutData(data);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc;
        tc = new TableColumn(table, SWT.NONE, MESSAGE_PROP);
        tc.setAlignment(SWT.LEFT);
        tc.setWidth(20);
        tc.setResizable(false);

        tc = new TableColumn(table, SWT.NONE, TYPE_PROP);
        tc.setResizable(true);
        tc.setWidth(200);
        tc.setText(Messages.ParametersEditControl_datatypeLabel);

        tc = new TableColumn(table, SWT.NONE, NEWNAME_PROP);
        tc.setResizable(true);
        tc.setWidth(200);
        tc.setText(Messages.ParametersEditControl_parameterNameLabel);

        fTableViewer = new TableViewer(table);
        fTableViewer.setUseHashlookup(true);
        fTableViewer.setContentProvider(new ParameterInfoContentProvider());
        fTableViewer.setLabelProvider(new ParameterInfoLabelProvider());
        new TableMessageHoverService(fTableViewer) {
            @Override
            protected MessageList getMessagesFor(Object element) throws CoreException {
                return validate((IParameterBFE)element);
            }
        };

        fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
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

        addCellEditors();
    }

    private void editColumnOrNextPossible(int column) {
        IParameterBFE[] selected = getSelectedElements();
        if (selected.length != 1) {
            return;
        }
        int nextColumn = column;
        do {
            fTableViewer.editElement(selected[0], nextColumn);
            if (fTableViewer.isCellEditorActive()) {
                return;
            }
            nextColumn = nextColumn(nextColumn);
        } while (nextColumn != column);
    }

    private void editColumnOrPrevPossible(int column) {
        IParameterBFE[] selected = getSelectedElements();
        if (selected.length != 1) {
            return;
        }
        int prevColumn = column;
        do {
            fTableViewer.editElement(selected[0], prevColumn);
            if (fTableViewer.isCellEditorActive()) {
                return;
            }
            prevColumn = prevColumn(prevColumn);
        } while (prevColumn != column);
    }

    private int nextColumn(int column) {
        return (column >= getTable().getColumnCount() - 1) ? 0 : column + 1;
    }

    private int prevColumn(int column) {
        return (column <= 0) ? getTable().getColumnCount() - 1 : column - 1;
    }

    private IParameterBFE[] getSelectedElements() {
        ISelection selection = fTableViewer.getSelection();
        if (selection == null) {
            return new IParameterBFE[0];
        }

        if (!(selection instanceof IStructuredSelection)) {
            return new IParameterBFE[0];
        }

        List<?> selected = ((IStructuredSelection)selection).toList();
        return selected.toArray(new IParameterBFE[selected.size()]);
    }

    // ---- Button bar
    // --------------------------------------------------------------------------------------

    private void createButtonComposite(Composite parent) {
        Composite buttonComposite = uiToolkit.createComposite(parent);
        buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        GridLayout gl = new GridLayout();
        gl.marginHeight = 5;
        gl.marginWidth = 5;
        buttonComposite.setLayout(gl);

        fAddButton = createAddButton(buttonComposite);
        fRemoveButton = createRemoveButton(buttonComposite);
        updateButtonsEnabledState();

        if (buttonComposite.getChildren().length == 0) {
            buttonComposite.dispose();
        }
    }

    private void updateButtonsEnabledState() {
        if (fAddButton != null) {
            fAddButton.setEnabled(true);
        }
        if (fRemoveButton != null) {
            fRemoveButton.setEnabled(getTableSelectionCount() != 0);
        }
    }

    private int getTableSelectionCount() {
        return getTable().getSelectionCount();
    }

    private int getTableItemCount() {
        return getTable().getItemCount();
    }

    public Table getTable() {
        return fTableViewer.getTable();
    }

    public TableViewer getTableViewer() {
        return fTableViewer;
    }

    private int getTableSelectionIndex() {
        return getTable().getSelectionIndex();
    }

    private Button createAddButton(Composite buttonComposite) {
        Button button = uiToolkit.createButton(buttonComposite, Messages.ParametersEditControl_AddLabel);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IParameterBFE p = paramContainer.newParameter();
                p.setName("newParam"); //$NON-NLS-1$
                fTableViewer.refresh();
                fTableViewer.getControl().setFocus();
                int row = getTableItemCount() - 1;
                getTable().setSelection(row);
                updateButtonsEnabledState();
                editColumnOrNextPossible(0);
            }
        });
        return button;
    }

    private Button createRemoveButton(Composite buttonComposite) {
        final Button button = uiToolkit.createButton(buttonComposite, Messages.ParametersEditControl_RemoveLabel);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = getTable().getSelectionIndices()[0];
                IParameterBFE[] selected = getSelectedElements();
                for (IParameterBFE element : selected) {
                    element.delete();
                }
                restoreSelection(index);
            }

            private void restoreSelection(int index) {
                fTableViewer.refresh();
                fTableViewer.getControl().setFocus();
                int itemCount = getTableItemCount();
                if (itemCount != 0 && index >= itemCount) {
                    index = itemCount - 1;
                    getTable().setSelection(index);
                }
                updateButtonsEnabledState();
            }
        });
        return button;
    }

    // ---- editing
    // -----------------------------------------------------------------------------------------------

    private void addCellEditors() {

        final CellEditor editors[] = new CellEditor[PROPERTIES.length];

        class UndoableTextCellEditor extends TextCellEditor {

            public UndoableTextCellEditor(Composite parent) {
                super(parent);
            }

            private String originalValue;

            @Override
            public void activate() {
                super.activate();
                originalValue = text.getText();
            }

            @Override
            public void performUndo() {
                text.setText(originalValue);
            }
        }
        editors[TYPE_PROP] = new UndoableTextCellEditor(getTable());
        editors[NEWNAME_PROP] = new UndoableTextCellEditor(getTable());

        for (int i = 1; i < editors.length; i++) {
            final int editorColumn = i;
            final CellEditor editor = editors[i];
            // support tabbing between columns while editing:
            editor.getControl().addTraverseListener(new TraverseListener() {
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
                        default:
                            break;
                    }
                }
            });
            // support switching rows while editing:
            editor.getControl().addKeyListener(new KeyAdapter() {
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
                            int nextRow = getTableSelectionIndex() + 1;
                            if (nextRow >= getTableItemCount()) {
                                break;
                            }
                            getTable().setSelection(nextRow);
                            editColumnOrPrevPossible(editorColumn);
                            break;

                        case SWT.ARROW_UP:
                            e.doit = false;
                            int prevRow = getTableSelectionIndex() - 1;
                            if (prevRow < 0) {
                                break;
                            }
                            getTable().setSelection(prevRow);
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
                    editor.performUndo();
                }

                @Override
                public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                    fTableViewer.getCellModifier().modify(
                            ((IStructuredSelection)fTableViewer.getSelection()).getFirstElement(),
                            PROPERTIES[editorColumn], editor.getValue());
                }
            });

        }

        fTableViewer.setCellEditors(editors);
        fTableViewer.setColumnProperties(PROPERTIES);
        fTableViewer.setCellModifier(new ParametersCellModifier());
    }

    private SubjectControlContentAssistant installParameterTypeContentAssist(Control control) {
        if (!(control instanceof Text)) {
            return null;
        }
        Text text = (Text)control;
        DatatypeCompletionProcessor processor = new DatatypeCompletionProcessor();
        processor.setIpsProject(ipsProject);
        SubjectControlContentAssistant contentAssistant = CompletionUtil.createContentAssistant(processor);
        ContentAssistHandler.createHandlerForText(text, contentAssistant);
        return contentAssistant;
    }

    private class ParameterInfoContentProvider implements IStructuredContentProvider {
        @Override
        public Object[] getElements(Object inputElement) {
            return paramContainer.getParameterBFEs().toArray();
        }

        @Override
        public void dispose() {
            // do nothing
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // do nothing
        }
    }

    private class ParameterInfoLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex != MESSAGE_PROP) {
                return null;
            }
            try {
                MessageList list = validate((IParameterBFE)element);
                return IpsUIPlugin.getImageHandling().getImage(IpsProblemOverlayIcon.getOverlay(list.getSeverity()),
                        false);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return null;
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            IParameterBFE info = (IParameterBFE)element;
            if (columnIndex == MESSAGE_PROP) {
                return ""; //$NON-NLS-1$
            }
            if (columnIndex == TYPE_PROP) {
                return info.getDatatype();
            }
            if (columnIndex == NEWNAME_PROP) {
                return info.getName();
            }
            throw new RuntimeException("Unknown column " + columnIndex); //$NON-NLS-1$
        }
    }

    private class ParametersCellModifier implements ICellModifier {
        @Override
        public boolean canModify(Object element, String property) {
            if (PROPERTIES[TYPE_PROP].equals(property) || PROPERTIES[NEWNAME_PROP].equals(property)) {
                return true;
            }
            return false;
        }

        @Override
        public Object getValue(Object element, String property) {
            IParameterBFE param = (IParameterBFE)element;
            if (property.equals(PROPERTIES[TYPE_PROP])) {
                return param.getDatatype();
            } else if (property.equals(PROPERTIES[NEWNAME_PROP])) {
                return param.getName();
            }
            return null;
        }

        @Override
        public void modify(Object element, String property, Object value) {
            if (element instanceof TableItem) {
                element = ((TableItem)element).getData();
            }
            if (!(element instanceof IParameterBFE)) {
                return;
            }
            IParameterBFE param = (IParameterBFE)element;
            if (property.equals(PROPERTIES[NEWNAME_PROP])) {
                param.setName((String)value);
                fTableViewer.update(param, new String[] { PROPERTIES[NEWNAME_PROP] });
            } else if (property.equals(PROPERTIES[TYPE_PROP])) {
                param.setDatatype((String)value);
                fTableViewer.update(param, new String[] { PROPERTIES[TYPE_PROP] });
            }

            /*
             * it is necessary to update all parameters at this point since there can be
             * dependencies between the parameters. e.g. if the parameter name of two parameters is
             * the same then an error is to display for both parameters.
             */
            fTableViewer.update(paramContainer.getParameterBFEs(), new String[] { property });
        }
    }
}

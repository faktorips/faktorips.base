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

package org.faktorips.devtools.core.ui.editors.type;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IParameterContainer;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DatatypeCompletionProcessor;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Jan Ortmann
 */
public class ParametersEditControl extends Composite implements IDataChangeableReadWriteAccess {

    private IParameterContainer paramContainer;

    private static final String[] PROPERTIES = { "message", "type", "new", "default" }; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-4$
    private static final int MESSAGE_PROP = 0;
    private static final int TYPE_PROP = 1;
    private static final int NEWNAME_PROP = 2;
    private static final int DEFAULT_PROP = 3;

    // configuration parameters
    private boolean fCanChangeParameterNames = true;
    private boolean fCanChangeTypesOfOldParameters = true;
    private boolean fCanAddParameters = true;
    private boolean fCanMoveParameters = true;
    private boolean defaultValueForNewParameters = false;
    private int tableStyle = SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION;

    private UIToolkit uiToolkit;

    private TableViewer fTableViewer;

    /** the label text above the table */
    private String label;

    // the buttons
    private Button fUpButton;
    private Button fDownButton;

    private Button fAddButton;
    private Button fRemoveButton;

    private IIpsProject ipsProject;

    private boolean dataChangeable;

    public ParametersEditControl(Composite parent, UIToolkit uiToolkit, int style, String label, IIpsProject ipsProject) {
        super(parent, style);
        this.ipsProject = ipsProject;
        this.label = label;
        this.uiToolkit = uiToolkit;
    }

    public void setCanChangeParameterNames(boolean value) {
        fCanChangeParameterNames = value;
    }

    public void setCanChangeParameterTypes(boolean value) {
        fCanChangeTypesOfOldParameters = value;
    }

    public void setCanAddParameters(boolean value) {
        fCanAddParameters = value;
    }

    public void setCanMoveParameters(boolean value) {
        fCanMoveParameters = value;
    }

    public void setDefaultValueForNewParameters(boolean value) {
        defaultValueForNewParameters = value;
    }

    public void setTableStyle(int style) {
        tableStyle = style;
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

        if (label != null) {
            Label tableLabel = new Label(this, SWT.NONE);
            GridData labelGd = new GridData();
            labelGd.horizontalSpan = 2;
            tableLabel.setLayoutData(labelGd);
            tableLabel.setText(label);
        }
        createParameterList(this);
        createButtonComposite(this);
    }

    private MessageList validate(IParameter param) throws CoreException {
        MessageList result = paramContainer.validate(ipsProject);
        return result.getMessagesFor(param);
    }

    private boolean canMove(boolean up) {
        int[] indc = getTable().getSelectionIndices();
        if (indc.length == 0) {
            return false;
        }
        for (int element : indc) {
            if (up && element == 0 || !up && element == getTable().getItems().length - 1) {
                return false;
            }
        }
        return true;
    }

    public void setInput(IParameterContainer paramContainer) {
        ArgumentCheck.notNull(paramContainer);
        this.paramContainer = paramContainer;
        fTableViewer.setInput(paramContainer);
        if (paramContainer.getParameters().length > 0) {
            fTableViewer.setSelection(new StructuredSelection(paramContainer.getParameters()[0]));
        }
    }

    // ---- Parameter table
    // -----------------------------------------------------------------------------------

    private void createParameterList(Composite parent) {
        TableLayoutComposite layouter = new TableLayoutComposite(parent, SWT.NONE);
        addColumnLayoutData(layouter);

        final Table table = new Table(layouter, tableStyle);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc;
        tc = new TableColumn(table, SWT.NONE, MESSAGE_PROP);
        tc.setAlignment(SWT.LEFT);
        tc.setResizable(false);

        tc = new TableColumn(table, SWT.NONE, TYPE_PROP);
        tc.setResizable(true);
        tc.setText(Messages.ParametersEditControl_columnLabelDatatype);

        tc = new TableColumn(table, SWT.NONE, NEWNAME_PROP);
        tc.setResizable(true);
        tc.setText(Messages.ParametersEditControl_columnLabelName);

        if (fCanAddParameters && defaultValueForNewParameters) {
            tc = new TableColumn(table, SWT.NONE, DEFAULT_PROP);
            tc.setResizable(true);
            tc.setText(Messages.ParametersEditControl_columnLabelDefaultValue);
        }

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 40;
        layouter.setLayoutData(gd);

        fTableViewer = new TableViewer(table);
        fTableViewer.setUseHashlookup(true);
        fTableViewer.setContentProvider(new ParameterInfoContentProvider());
        fTableViewer.setLabelProvider(new ParameterInfoLabelProvider());
        new TableMessageHoverService(fTableViewer) {
            @Override
            protected MessageList getMessagesFor(Object element) throws CoreException {
                return validate((IParameter)element);
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

        if (canEditTableCells()) {
            addCellEditors();
        }
    }

    private boolean canEditTableCells() {
        return (fCanChangeParameterNames || fCanChangeTypesOfOldParameters) && isDataChangeable();
    }

    private void editColumnOrNextPossible(int column) {
        IParameter[] selected = getSelectedElements();
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
        IParameter[] selected = getSelectedElements();
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

    private void addColumnLayoutData(TableLayoutComposite layouter) {
        if (fCanAddParameters && defaultValueForNewParameters) {
            layouter.addColumnData(new ColumnPixelData(20, false));
            layouter.addColumnData(new ColumnWeightData(60, true));
            layouter.addColumnData(new ColumnWeightData(20, true));
            layouter.addColumnData(new ColumnWeightData(20, true));
        } else {
            layouter.addColumnData(new ColumnPixelData(20, false));
            layouter.addColumnData(new ColumnWeightData(70, true));
            layouter.addColumnData(new ColumnWeightData(30, true));
        }
    }

    private IParameter[] getSelectedElements() {
        ISelection selection = fTableViewer.getSelection();
        if (selection == null) {
            return new IParameter[0];
        }

        if (!(selection instanceof IStructuredSelection)) {
            return new IParameter[0];
        }
        List<?> selected = ((IStructuredSelection)selection).toList();
        return selected.toArray(new IParameter[selected.size()]);
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

        if (fCanAddParameters) {
            fAddButton = createAddButton(buttonComposite);
        }

        if (fCanAddParameters) {
            fRemoveButton = createRemoveButton(buttonComposite);
        }

        if (buttonComposite.getChildren().length != 0) {
            addSpacer(buttonComposite);
        }

        if (fCanMoveParameters) {
            fUpButton = createButton(buttonComposite, Messages.ParametersEditControl_buttonLabelMoveUp, true);
            fDownButton = createButton(buttonComposite, Messages.ParametersEditControl_buttonLabelMoveDown, false);
        }
        updateButtonsEnabledState();
        if (buttonComposite.getChildren().length == 0) {
            buttonComposite.dispose();
        }
    }

    private void addSpacer(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 5;
        label.setLayoutData(gd);
    }

    private void updateButtonsEnabledState() {
        if (!isDataChangeable()) {
            uiToolkit.setDataChangeable(fUpButton, false);
            uiToolkit.setDataChangeable(fDownButton, false);
            uiToolkit.setDataChangeable(fAddButton, false);
            uiToolkit.setDataChangeable(fRemoveButton, false);
            return;
        }

        if (fUpButton != null) {
            fUpButton.setEnabled(canMove(true));
        }
        if (fDownButton != null) {
            fDownButton.setEnabled(canMove(false));
        }
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
        Button button = new Button(buttonComposite, SWT.PUSH);
        button.setText(Messages.ParametersEditControl_buttonLabelAdd);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                paramContainer.newParameter("", "newParam"); //$NON-NLS-1$ //$NON-NLS-2$
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
        final Button button = new Button(buttonComposite, SWT.PUSH);
        button.setText(Messages.ParametersEditControl_buttonLabelRemove);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = getTable().getSelectionIndices()[0];
                IParameter[] selected = getSelectedElements();
                for (IParameter element : selected) {
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

    private Button createButton(Composite buttonComposite, String text, final boolean up) {
        Button button = new Button(buttonComposite, SWT.PUSH);
        button.setText(text);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ISelection savedSelection = fTableViewer.getSelection();
                if (savedSelection == null) {
                    return;
                }
                IParameter[] selection = getSelectedElements();
                if (selection.length == 0) {
                    return;
                }
                paramContainer.moveParameters(fTableViewer.getTable().getSelectionIndices(), up);
                fTableViewer.refresh();
                fTableViewer.setSelection(savedSelection);
                fTableViewer.getControl().setFocus();
            }
        });
        return button;
    }

    // ---- editing
    // -----------------------------------------------------------------------------------------------

    private void addCellEditors() {
        class UnfocusableTextCellEditor extends TextCellEditor {
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
                fTableViewer.getCellModifier().modify(
                        ((IStructuredSelection)fTableViewer.getSelection()).getFirstElement(), PROPERTIES[property],
                        newValue);
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
                            fTableViewer.getCellModifier().modify(
                                    ((IStructuredSelection)fTableViewer.getSelection()).getFirstElement(),
                                    PROPERTIES[property], newValue);
                            editColumnOrNextPossible(property);
                        }
                    }
                });
            }
        }

        final UnfocusableTextCellEditor editors[] = new UnfocusableTextCellEditor[PROPERTIES.length];

        editors[TYPE_PROP] = new UnfocusableTextCellEditor(getTable());
        editors[NEWNAME_PROP] = new UnfocusableTextCellEditor(getTable());
        editors[DEFAULT_PROP] = new UnfocusableTextCellEditor(getTable());

        SubjectControlContentAssistant assistant = installParameterTypeContentAssist(editors[TYPE_PROP].getControl());
        editors[TYPE_PROP].setContentAssistant(assistant, TYPE_PROP);

        for (int i = 1; i < editors.length; i++) {
            final int editorColumn = i;
            final UnfocusableTextCellEditor editor = editors[i];
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

                        case SWT.TRAVERSE_ESCAPE:
                            fTableViewer.cancelEditing();
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
                    // must reset model to original value:
                    editor.fireModifyEvent(editor.getOriginalValue(), editorColumn);
                }

                @Override
                public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                    editor.fireModifyEvent(editor.getValue(), editorColumn);
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
            return paramContainer.getParameters();
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
                MessageList list = validate((IParameter)element);
                return IpsUIPlugin.getImageHandling().getImage(IpsProblemOverlayIcon.getOverlay(list.getSeverity()),
                        false);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return null;
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            IParameter info = (IParameter)element;
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
            if (!isDataChangeable()) {
                return false;
            }
            if (property.equals(PROPERTIES[TYPE_PROP])) {
                return fCanChangeTypesOfOldParameters;
            } else if (property.equals(PROPERTIES[NEWNAME_PROP])) {
                return fCanChangeParameterNames;
            }
            return false;
        }

        @Override
        public Object getValue(Object element, String property) {
            IParameter param = (IParameter)element;
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
            if (!(element instanceof IParameter)) {
                return;
            }
            IParameter param = (IParameter)element;
            if (property.equals(PROPERTIES[NEWNAME_PROP])) {
                param.setName((String)value);
            } else if (property.equals(PROPERTIES[TYPE_PROP])) {
                param.setDatatype((String)value);
            }
            /*
             * it is necessary to update all parameters at this point since there can be
             * dependencies between the parameters. e.g. if the parameter name of two parameters is
             * the same then an error is to display for both parameters.
             */
            fTableViewer.update(paramContainer.getParameters(), new String[] { property });
        }
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;
        updateButtonsEnabledState();
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }
}

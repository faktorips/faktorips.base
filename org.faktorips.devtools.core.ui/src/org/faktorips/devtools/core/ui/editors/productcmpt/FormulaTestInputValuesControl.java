/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestCase;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestInputValue;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.ProblemImageDescriptor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.UIController;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.table.BeanTableCellModifier;
import org.faktorips.devtools.core.ui.table.ColumnChangeListener;
import org.faktorips.devtools.core.ui.table.ColumnIdentifier;
import org.faktorips.devtools.core.ui.table.DelegateCellEditor;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Composite to display a table of formula test input values.
 * 
 * @author Joerg Ortmann
 */
public class FormulaTestInputValuesControl extends Composite implements ColumnChangeListener,
        IDataChangeableReadWriteAccess {
    private static final int IDX_IDENTIFIER = 1;
    private static final int IDX_VALUE_COLUMN = 2;

    private Image empytImage;

    private HashMap cachedProblemImageDescriptors = new HashMap();

    private UIToolkit uiToolkit;

    /* Table contains the input values */
    private TableViewer formulaInputTableViewer;;

    /* Label to display the result of the formula */
    private Label formulaResult;

    private IIpsProject ipsProject;

    /* Buttons */
    private Button btnNewFormulaTestCase;
    private Button btnCalculate;
    private Button btnClearInputValues;

    /* The formula test case which will be displayed and edit by this composite */
    private IFormulaTestCase formulaTestCase;

    /* Controller of the dependent ips object part */
    private UIController uiController;

    /*
     * Indicates that the formula test case can be stored as a new one. For instance if the current
     * formula test case is used to execute the formula on the first page of the formula edit dialog
     * (fast preview executing of the currently editing formula), then the store button could be
     * used to store this test case as persistent formula test case with expected result, after the
     * storing the formula test case will be displayed one the separate formula test cases page (see
     * FormulaEditDialog for details).
     */
    private boolean canStoreFormulaTestCaseAsNewFormulaTestCase = false;

    /*
     * Indicates that the formula will be executed and the result will be displayed in the
     * corresponding control. If <code>false</code> the formula will not be executed by this
     * control.
     */
    private boolean canCalculateResult = false;

    /* Indicates that the calculated result will be stored as expected result */
    private boolean storeExpectedResult = true;

    /* Contains the last calculated result */
    private Object lastCalculatedResult = null;

    /* indicates that the object is self updating */
    private boolean isUpdatingSelf;

    // Contains the cell modifier for the table
    private BeanTableCellModifier tableCellModifier;

    // The column index of the delegate cell editor
    private int delegateCellEditorColumnIndex;

    private boolean dataChangeable;

    /*
     * Label provider for the formula test input value.
     */
    private class FormulaTestInputValueTblLabelProvider extends LabelProvider implements ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            if (!(element instanceof IFormulaTestInputValue)) {
                return null;
            }
            try {
                switch (columnIndex) {
                    case 0:
                        MessageList msgList = ((IFormulaTestInputValue)element).validate(ipsProject);
                        return getImageForMsgList(empytImage, msgList);
                }
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IFormulaTestInputValue) {
                if (columnIndex == IDX_IDENTIFIER) {
                    return getTextInNullPresentationIfNull(((IFormulaTestInputValue)element).getIdentifier());
                } else if (columnIndex == IDX_VALUE_COLUMN) {
                    try {
                        ValueDatatype vd = ((IFormulaTestInputValue)element).findDatatypeOfFormulaParameter(ipsProject);
                        return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(vd,
                                ((IFormulaTestInputValue)element).getValue());
                    } catch (CoreException e) {
                        // ignore exception, return the unformated value
                    }
                    return getTextInNullPresentationIfNull(((IFormulaTestInputValue)element).getValue());
                }
            }
            return null;
        }

        private String getTextInNullPresentationIfNull(String value) {
            if (value == null) {
                value = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            }
            return value;
        }
    }

    /*
     * Inner class to store (keep) a formula test case
     */
    private class StoreFormulaRunnable implements IWorkspaceRunnable {
        private String name;

        public void run(IProgressMonitor monitor) throws CoreException {
            IFormula formula = formulaTestCase.getFormula();
            IFormulaTestCase newFormulaTestCase = formula.newFormulaTestCase();
            name = newFormulaTestCase
                    .generateUniqueNameForFormulaTestCase(Messages.FormulaTestInputValuesControl_DefaultFormulaTestCaseName);
            newFormulaTestCase.setName(name);
            newFormulaTestCase.setExpectedResult(formulaTestCase.getExpectedResult());

            IFormulaTestInputValue[] inputValues = formulaTestCase.getFormulaTestInputValues();
            for (int i = 0; i < inputValues.length; i++) {
                IFormulaTestInputValue newInputValue = newFormulaTestCase.newFormulaTestInputValue();
                newInputValue.setIdentifier(inputValues[i].getIdentifier());
                newInputValue.setValue(inputValues[i].getValue());
            }
        }

        public String getName() {
            return name;
        }
    }

    public FormulaTestInputValuesControl(Composite parent, UIToolkit uiToolkit, UIController uiController,
            IIpsProject ipsProject) {
        super(parent, SWT.NONE);
        ArgumentCheck.notNull(new Object[] { parent, uiToolkit, uiController });

        this.uiToolkit = uiToolkit;
        this.uiController = uiController;
        empytImage = new Image(getShell().getDisplay(), 16, 16);
        this.ipsProject = ipsProject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (empytImage != null) {
            empytImage.dispose();
        }
        for (Iterator iter = cachedProblemImageDescriptors.values().iterator(); iter.hasNext();) {
            ProblemImageDescriptor problemImageDescriptor = (ProblemImageDescriptor)iter.next();
            Image problemImage = IpsPlugin.getDefault().getImage(problemImageDescriptor);
            if (problemImage != null) {
                problemImage.dispose();
            }
        }
        cachedProblemImageDescriptors.clear();
        super.dispose();
    }

    /**
     * Sets if the button to store the current formula test case as a new formula test case is
     * visible or not.
     */
    public void setCanStoreFormulaTestCaseAsNewFormulaTestCase(boolean value) {
        canStoreFormulaTestCaseAsNewFormulaTestCase = value;
    }

    /**
     * Sets if the result of the formula will be calculated and displayed or not.
     */
    public void setCanCalulateResult(boolean value) {
        canCalculateResult = value;
    }

    /**
     * Sets if the calculated result will be stored as expected result.
     */
    public void setCanStoreExpectedResult(boolean storeExpectedResult) {
        this.storeExpectedResult = storeExpectedResult;
    }

    /**
     * Returns the last calculated result or <code>null</code> if the formula couldn't or wasn't
     * executed.
     */
    public Object getLastCalculatedResult() {
        return lastCalculatedResult;
    }

    /**
     * Stores the formula test case for which the parameter will be displayed and updates the ui.
     */
    public void storeFormulaTestCase(IFormulaTestCase formulaTestCase) {
        ipsProject = formulaTestCase == null ? null : formulaTestCase.getIpsProject();
        this.formulaTestCase = formulaTestCase;
        clearResult();
        setTableInput(formulaTestCase);
        repackAndResfreshParamInputTable();

        if (formulaTestCase == null) {
            return;
        }
        // update the table row cell editors
        IFormulaTestInputValue[] inputValues = formulaTestCase.getFormulaTestInputValues();
        ValueDatatype[] rowDatatypes = new ValueDatatype[inputValues.length];
        for (int i = 0; i < rowDatatypes.length; i++) {
            try {
                rowDatatypes[i] = inputValues[i].findDatatypeOfFormulaParameter(ipsProject);
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
                rowDatatypes[i] = null;
            }
        }

        tableCellModifier.initRowModifier(delegateCellEditorColumnIndex, rowDatatypes);
    }

    /**
     * Creates the compoiste's controls. This method has to be called by this controls client, after
     * the control has been configured via the appropriate setter method, e.g.
     * <code>setCanCalulateResult(int rows)</code>
     */
    public void initControl() {
        setLayout(uiToolkit.createNoMarginGridLayout(1, false));
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite formulaTestArea = uiToolkit.createComposite(this);
        formulaTestArea.setLayout(uiToolkit.createNoMarginGridLayout(2, false));
        formulaTestArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createFormulaInputTable(formulaTestArea);

        // create buttons
        Composite btns = uiToolkit.createComposite(formulaTestArea);
        btns.setLayout(uiToolkit.createNoMarginGridLayout(1, true));
        btns.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        btnCalculate = uiToolkit.createButton(btns, Messages.FormulaTestInputValuesControl_ButtonLabel_Calculate);
        btnCalculate.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
        btnCalculate.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                calculateFormulaIfValid(ipsProject);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        uiToolkit.createVerticalSpacer(btns, 5);
        uiToolkit.createHorizonzalLine(btns);
        uiToolkit.createVerticalSpacer(btns, 5);

        if (canStoreFormulaTestCaseAsNewFormulaTestCase) {
            btnNewFormulaTestCase = uiToolkit.createButton(btns,
                    Messages.FormulaTestInputValuesControl_ButtonLabel_Store);
            btnNewFormulaTestCase.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
            btnNewFormulaTestCase.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    try {
                        storeFormulaTestInputValuesAsNewFormulaTestCase();
                    } catch (CoreException ex) {
                        IpsPlugin.logAndShowErrorDialog(ex);
                    }
                }

                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
        }

        btnClearInputValues = uiToolkit.createButton(btns, Messages.FormulaTestInputValuesControl_ButtonLabel_Clear);
        btnClearInputValues.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
        btnClearInputValues.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                clearFormulaTestInputValues();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // create the label to display the formula result
        Composite resultComposite = uiToolkit.createLabelEditColumnComposite(formulaTestArea);
        Label labelResult = uiToolkit.createLabel(resultComposite, Messages.FormulaTestInputValuesControl_Label_Result);
        formulaResult = uiToolkit.createLabel(resultComposite, ""); //$NON-NLS-1$
        labelResult.setFont(JFaceResources.getBannerFont());
        formulaResult.setFont(JFaceResources.getBannerFont());
    }

    /*
     * Stores the current formula test case with all input values as new formula test case.
     */
    private void storeFormulaTestInputValuesAsNewFormulaTestCase() throws CoreException {
        StoreFormulaRunnable storeFormulaRunnable = new StoreFormulaRunnable();
        formulaTestCase.getIpsModel().runAndQueueChangeEvents(storeFormulaRunnable, null);
        if (uiController != null) {
            uiController.updateUI();
        }

        MessageDialog.openInformation(getShell(),
                Messages.FormulaTestInputValuesControl_InfoDialogSuccessfullyStored_Title, NLS.bind(
                        Messages.FormulaTestInputValuesControl_InfoDialogSuccessfullyStored_Text, storeFormulaRunnable
                                .getName()));
    }

    /*
     * Clears all values in all corresponding formula test input value object parts. Setting all
     * values to an empty string.
     */
    private void clearFormulaTestInputValues() {
        if (formulaTestCase != null) {
            IFormulaTestInputValue[] inputValues = formulaTestCase.getFormulaTestInputValues();
            for (int i = 0; i < inputValues.length; i++) {
                inputValues[i].setValue(null);
                uiController.updateUI();
                repackAndResfreshParamInputTable();
                clearResult();
            }
        }
    }

    /*
     * Creates the table
     */
    private void createFormulaInputTable(Composite parent) {
        Composite c = uiToolkit.createComposite(parent);
        c.setLayout(uiToolkit.createNoMarginGridLayout(1, false));
        c.setLayoutData(new GridData(GridData.FILL_BOTH));

        Table table = new Table(c, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.minimumHeight = 100;
        table.setLayoutData(gd);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // create the columns of the table
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(""); //$NON-NLS-1$
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.FormulaTestInputValuesControl_TableFormulaTestInputValues_Column_Parameter);
        column = new TableColumn(table, SWT.LEFT);
        // extend the value column
        StringBuffer extension = new StringBuffer();
        for (int i = 0; i < 40; i++) {
            extension.append(" "); //$NON-NLS-1$
        }
        column.setText(Messages.FormulaTestInputValuesControl_TableFormulaTestInputValues_Column_Value + extension);

        increaseHeightOfTableRow(table, 2);

        // Create the viewer and connect it to the view
        formulaInputTableViewer = new TableViewer(table);
        formulaInputTableViewer.setContentProvider(new ArrayContentProvider());
        formulaInputTableViewer.setLabelProvider(new FormulaTestInputValueTblLabelProvider());

        createTableCellModifier();

        hookTableListener();

        setTableInput(null);
        repackAndResfreshParamInputTable();
    }

    private void increaseHeightOfTableRow(Table table, final int numOfColumns) {
        // add paint lister to increase the height of the table row,
        // because @since 3.2 in edit mode the cell becomes a border and the bottom pixel of the
        // text is hidden
        Listener paintListener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                    case SWT.MeasureItem: {
                        if (numOfColumns == 0) {
                            return;
                        }
                        TableItem item = (TableItem)event.item;
                        // column 0 will be used to determine the height,
                        // <code>event.index<code> couldn't be used because it is only available
                        // @since 3.2, that's ok because the height is always the same, even if the
                        // column contains no text, the height only depends on the font
                        String text = getText(item, 0);
                        Point size = event.gc.textExtent(text);
                        // the height will be increased by 10 pixel
                        event.height = Math.max(event.height, size.y + 10);
                        break;
                    }
                }
            }

            String getText(TableItem item, int column) {
                String text = item.getText(column);
                return text;
            }
        };
        table.addListener(SWT.MeasureItem, paintListener);
    }

    private void createTableCellModifier() {
        delegateCellEditorColumnIndex = 2;
        tableCellModifier = new BeanTableCellModifier(formulaInputTableViewer, this, ipsProject);
        tableCellModifier.initModifier(uiToolkit, new String[] { "image", IIpsElement.PROPERTY_NAME, //$NON-NLS-1$
                IFormulaTestInputValue.PROPERTY_VALUE }, new ValueDatatype[] { null, null,
                DelegateCellEditor.DELEGATE_VALUE_DATATYPE });
        tableCellModifier.addListener(this);
    }

    /*
     * Adds the listener to the formula test input value table
     */
    private void hookTableListener() {
        new TableMessageHoverService(formulaInputTableViewer) {
            @Override
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element != null) {
                    return validateElement(element);
                } else {
                    return null;
                }
            }
        };
    }

    /*
     * Repacks the columns in the table
     */
    private void repackAndResfreshParamInputTable() {
        for (int i = 0, n = formulaInputTableViewer.getTable().getColumnCount(); i < n; i++) {
            formulaInputTableViewer.getTable().getColumn(i).pack();
        }
        formulaInputTableViewer.refresh();
    }

    private void setTableInput(IFormulaTestCase formulaTestCase) {
        if (formulaTestCase != null) {
            formulaInputTableViewer.setInput(formulaTestCase.getFormulaTestInputValues());
        } else {
            formulaInputTableViewer.setInput(new ArrayList());
            if (formulaResult != null) {
                clearResult();
            }
        }
    }

    /*
     * Execute the formula and displays the result if the formula is valid and all values are given.
     */
    public Object calculateFormulaIfValid(IIpsProject ipsProject) {
        if (!canCalculateResult) {
            return null;
        }

        if (!FormulaTestInputValuesControl.checkPrecondition(getShell(), formulaTestCase)) {
            return null;
        }

        try {
            if (formulaTestCase == null) {
                return null;
            }
            if (storeExpectedResult) {
                formulaTestCase.setExpectedResult(""); //$NON-NLS-1$
            }
            lastCalculatedResult = null;

            // don't execute the formula if
            // - there is an error on the corresponding config element (e.g. error in formula)
            // - the current formula test case contains at least one validation message (e.g. no
            // value given)
            MessageList ml = formulaTestCase.getFormula().validate(ipsProject);
            if (ml.getFirstMessage(Message.ERROR) != null) {
                clearResult();
                return null;
            }

            ml = formulaTestCase.validate(null);
            // don't calculate preview if there are messages, e.g. warnings because of missing
            // values
            if (ml.getNoOfMessages() > 0) {
                showFormulaResult(Messages.FormulaTestInputValuesControl_Result_ObjectIsNotValid);
                return null;
            }

            Runnable calculate = new Runnable() {
                public void run() {
                    if (isDisposed()) {
                        return;
                    }
                    try {
                        lastCalculatedResult = formulaTestCase.execute(formulaTestCase.getIpsProject());
                        lastCalculatedResult = lastCalculatedResult == null ? null : lastCalculatedResult.toString();
                    } catch (Exception e) {
                        showFormulaResult(NLS.bind(
                                Messages.FormulaTestInputValuesControl_Error_ParseExceptionWhenExecutingFormula, e
                                        .getLocalizedMessage()));
                    }
                }
            };
            BusyIndicator.showWhile(getDisplay(), calculate);

            showFormulaResult(lastCalculatedResult);
            if (storeExpectedResult) {
                formulaTestCase.setExpectedResult((String)lastCalculatedResult);
            }
            return lastCalculatedResult;
        } catch (Exception e) {
            IpsPlugin.log(e);
            showFormulaResult(Messages.FormulaTestInputValuesControl_Error_ExecutingFormula);
        }
        return null;
    }

    /**
     * Check the preconditions to executing formulas. Returns <code>true</code> if all rules are
     * valid otherwise returns <code>false</code>.<br>
     * Remark: Because the formula execution depends on the builder (table access functions uses the
     * generated source code), we have to ensure that the table was generated by the builder first.
     * The method checks if the build was executed for the current project. If not, the user will
     * asked if the project should be build first otherwise the execution of the formula will be
     * aborted.
     */
    protected static boolean checkPrecondition(Shell shell, IFormulaTestCase formulaTestCase) {
        try {
            if (formulaTestCase == null) {
                return false;
            }
            Boolean projectIsErrorFree = formulaTestCase.getIpsProject().isJavaProjectErrorFree(false);
            if (projectIsErrorFree == null) {
                // the project hasn't been build yet, ask if the project should be build now
                if (MessageDialog.openConfirm(shell,
                        Messages.FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_Title,
                        Messages.FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_ConfirmBuildProject)) {
                    ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
                    projectIsErrorFree = formulaTestCase.getIpsProject().isJavaProjectErrorFree(false);
                } else {
                    // cancel
                    return false;
                }
            }

            if (projectIsErrorFree == null) {
                MessageDialog.openError(shell,
                        Messages.FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_Title,
                        Messages.FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_ErrorDurringBuild);
            } else if (Boolean.FALSE.equals(projectIsErrorFree)) {
                // errors in project, show dialog to inform the user
                MessageDialog.openWarning(shell,
                        Messages.FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_Title,
                        Messages.FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_ErrorsInProject);
            } else {
                // the project was build and no error exists in the project,
                // thus all preconditions are ok
                return true;
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return false;
    }

    /**
     * Clears the result of the formula
     */
    public void clearResult() {
        formulaResult.setText(""); //$NON-NLS-1$
        formulaResult.pack();
    }

    private boolean updateBySelf() {
        if (isUpdatingSelf) {
            return true;
        }
        isUpdatingSelf = true;
        uiController.updateUI();
        isUpdatingSelf = false;
        return false;
    }

    /*
     * Displays the result of the formula
     */
    private void showFormulaResult(Object result) {
        if (updateBySelf()) {
            return;
        }
        String resultToDisplay = ""; //$NON-NLS-1$
        IFormula formula = formulaTestCase.getFormula();
        ValueDatatype vd;
        try {
            vd = formula.findValueDatatype(ipsProject);
            resultToDisplay = IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(vd,
                    (result == null ? null : result.toString()));
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        formulaResult.setText(resultToDisplay);
        formulaResult.pack();
    }

    /*
     * Performs and returns validation messages on the given element.
     */
    private MessageList validateElement(Object element) throws CoreException {
        MessageList messageList = new MessageList();
        // validate element
        if (element instanceof IIpsObjectPartContainer) {
            messageList.add(((IIpsObjectPartContainer)element).validate(ipsProject));
        }
        return messageList;
    }

    /*
     * Returns the image for the given message list (e.g. if there is an error return a problem
     * image)
     */
    private Image getImageForMsgList(Image defaultImage, MessageList msgList) {
        // get the cached problem descriptor for the base image
        String key = getKey(defaultImage, msgList.getSeverity());
        ProblemImageDescriptor descriptor = (ProblemImageDescriptor)cachedProblemImageDescriptors.get(key);
        if (descriptor == null && defaultImage != null) {
            descriptor = new ProblemImageDescriptor(defaultImage, msgList.getSeverity());
            cachedProblemImageDescriptors.put(key, descriptor);
        }
        return IpsPlugin.getDefault().getImage(descriptor);
    }

    /*
     * Returns an unique key for the given image and severity compination.
     */
    private String getKey(Image image, int severity) {
        if (image == null) {
            return null;
        }
        return image.hashCode() + "_" + severity; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void valueChanged(ColumnIdentifier columnIdentifier, Object value) {
        // the value in the table has changed
        clearResult();
        uiController.updateUI();
    }

    /*
     * Returns true if this control is a preview control for formula test cases, returns false if
     * this control shows stored formula test cases.
     */
    private boolean isPreviewOfFormulaTest() {
        return canStoreFormulaTestCaseAsNewFormulaTestCase;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    /**
     * {@inheritDoc}
     */
    public void setDataChangeable(boolean changeable) {
        if (isPreviewOfFormulaTest()) {
            // in preview mode the control is always changeable
            dataChangeable = true;
        } else {
            dataChangeable = changeable;
        }

        uiToolkit.setDataChangeable(btnNewFormulaTestCase, changeable && isPreviewOfFormulaTest());
        uiToolkit.setDataChangeable(btnClearInputValues, dataChangeable);
    }
}

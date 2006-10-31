/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IFormulaTestInputValue;
import org.faktorips.devtools.core.ui.ProblemImageDescriptor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.UIController;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.table.ColumnChangeListener;
import org.faktorips.devtools.core.ui.table.ColumnIdentifier;
import org.faktorips.devtools.core.ui.table.BeanTableCellModifier;
import org.faktorips.fl.parser.ParseException;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Composite to display a table of formula test input values.
 * 
 * @author Joerg Ortmann
 */
public class FormulaTestInputValuesControl extends Composite implements ColumnChangeListener {
    private static final int IDX_IDENTIFIER = 1;
    private static final int IDX_VALUE_COLUMN = 2;
    
    private Image empytImage;
    
    private HashMap cachedProblemImageDescriptors = new HashMap();
    
    private UIToolkit uiToolkit;
    
    /* Table contains the input values*/
    private TableViewer formulaInputTableViewer;;
    
    /* Label to display the result of the formula */
    private Label formulaResult;

    private Button btnNewFormulaTestCase;
    
    /* The formula test case which will be displayed and edit by this composite */ 
    private IFormulaTestCase formulaTestCase;
    
    /* Controller of the dependent ips object part */
    private UIController uiController;

    /* Indicates that the formula test case can be stored as a new one.
     * For instance if the current formula test case is used to execute the formula on the first page
     * of the formula edit dialog (fast preview executing of the currently editing formula), 
     * then the store button could be used to store this test case as persistent formula 
     * test case with expected result, after the storing the formula test case will be displayed one the 
     * separate formula test cases page (see FormulaEditDialog for details). */ 
    private boolean canStoreFormulaTestCaseAsNewFormulaTestCase = false;

    /* Indicates that the formula will be executed and the result will be displayed in the corresponding control. 
     * If <code>false</code> the formul will not be executed by this control. */
    private boolean canCalculateResult = false;
    
    /* Indicates that the calculated result will be stored as expected result */
    private boolean storeExpectedResult = true;
    
    /* Contains the last calculated result */
    private Object lastCalculatedResult = null;
    
    /* Indicates if the control is in read only state */
    private boolean viewOnly;
    
    /* indicates that the object is self updating */
    private boolean isUpdatingSelf;
    
    /*
     * Label provider for the formula test input value.
     */
    private class FormulaTestInputValueTblLabelProvider extends LabelProvider implements ITableLabelProvider{
        public Image getColumnImage(Object element, int columnIndex) {
            if (! (element instanceof IFormulaTestInputValue)){
                return null;
            }
            try {
                switch (columnIndex) {
                    case 0:
                        MessageList msgList = ((IFormulaTestInputValue) element).validate();
                        return getImageForMsgList(empytImage, msgList);
                }
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IFormulaTestInputValue){
                if (columnIndex == IDX_IDENTIFIER){
                    return ((IFormulaTestInputValue)element).getIdentifier();
                } else if (columnIndex == IDX_VALUE_COLUMN){
                    return (String)prepareObjectForSet(((IFormulaTestInputValue)element).getValue());
                }
            }
            return null;
        }
        
        /*
         * Returns the null-representation-string defined by the user (see IpsPreferences)
         * if the given object is null, the unmodified object otherwise.
         */
        private Object prepareObjectForSet(Object object) {
            if (object == null) {
                return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            }
            return object;
        }          
    }
    
    public FormulaTestInputValuesControl(Composite parent, UIToolkit uiToolkit,
            UIController uiController) {
        super(parent, SWT.NONE);
        ArgumentCheck.notNull(new Object[]{ parent, uiToolkit, uiController});

        this.uiToolkit = uiToolkit;
        this.uiController = uiController;
        this.empytImage = new Image(getShell().getDisplay(), 16, 16);
    }
    
    /**
     * {@inheritDoc}
     */
    public void dispose() {
        if (empytImage != null){
            empytImage.dispose();
        }
        for (Iterator iter = cachedProblemImageDescriptors.values().iterator(); iter.hasNext();) {
            ProblemImageDescriptor problemImageDescriptor = (ProblemImageDescriptor)iter.next();
            Image problemImage = IpsPlugin.getDefault().getImage(problemImageDescriptor);
            if (problemImage != null){
                problemImage.dispose();
            }
        }
        cachedProblemImageDescriptors.clear();        
        super.dispose();
    }

    /**
     * Sets if the button to store the current formula test case as a new formula test case is visible or not.
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
     *  Sets if the calculated result will be stored as expected result.
     */
    public void setCanStoreExpectedResult(boolean storeExpectedResult) {
        this.storeExpectedResult = storeExpectedResult;
    }

    /**
     * Sets if the control is for read only view <code>true</code> or not <code>false</code>.
     */
    public void setViewOnly(boolean viewOnly) {
        // the preview calculation is always allowed
        this.viewOnly = viewOnly;
    }    
    
    /**
     * Returns the last calculated result or <code>null</code> if the formula couldn't or wasn't executed.
     */
    public Object getLastCalculatedResult() {
        return lastCalculatedResult;
    }

    /**
     * Stors the formula test case for which the parameter will be displayed and updates the ui.
     */
    public void storeFormulaTestCase(IFormulaTestCase formulaTestCase) {
        this.formulaTestCase = formulaTestCase;
        clearResult();
        repackAndResfreshParamInputTable();
    }

    /**
     * Creates the compoiste's controls. This method has to be called by this
     * controls client, after the control has been configured via the appropiate
     * setter method, e.g. <code>setCanCalulateResult(int rows)</code>
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
        
        Button btnCalculate = uiToolkit.createButton(btns, "Calculate");
        btnCalculate.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnCalculate.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                calculateFormulaIfValid();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        uiToolkit.createVerticalSpacer(btns, 5);
        uiToolkit.createHorizonzalLine(btns);
        uiToolkit.createVerticalSpacer(btns, 5);
        
        if (canStoreFormulaTestCaseAsNewFormulaTestCase){
            btnNewFormulaTestCase = uiToolkit.createButton(btns, Messages.FormulaTestInputValuesControl_ButtonLabel_Store);
            btnNewFormulaTestCase.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
            btnNewFormulaTestCase.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    storeFormulaTestInputValuesAsNewFormulaTestCase();
                }
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });            

             btnNewFormulaTestCase.setEnabled(!viewOnly);
        }
        
        Button btnClearInputValues = uiToolkit.createButton(btns, Messages.FormulaTestInputValuesControl_ButtonLabel_Clear);
        btnClearInputValues.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnClearInputValues.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                clearFormulaTestInputValues();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        // the clear btn is enabled is not view only or if this is the control which can store the input as
        // new formula test case (e.g. preview formula on the first page of the formula edit dialog)
        btnClearInputValues.setEnabled(!viewOnly || canStoreFormulaTestCaseAsNewFormulaTestCase);
        
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
    private void storeFormulaTestInputValuesAsNewFormulaTestCase() {
        ArgumentCheck.isInstanceOf(formulaTestCase.getParent(), IConfigElement.class);
        IConfigElement configElement = (IConfigElement) formulaTestCase.getParent();
        IFormulaTestCase newFormulaTestCase = configElement.newFormulaTestCase();
        String name = newFormulaTestCase.generateUniqueNameForFormulaTestCase(Messages.FormulaTestInputValuesControl_DefaultFormulaTestCaseName);
        newFormulaTestCase.setName(name);
        newFormulaTestCase.setExpectedResult(formulaTestCase.getExpectedResult());

        IFormulaTestInputValue[] inputValues = formulaTestCase.getFormulaTestInputValues();
        for (int i = 0; i < inputValues.length; i++) {
            IFormulaTestInputValue newInputValue = newFormulaTestCase.newFormulaTestInputValue();
            newInputValue.setIdentifier(inputValues[i].getIdentifier());
            newInputValue.setValue(inputValues[i].getValue());
        }
        if (uiController != null){
            uiController.updateUI();
        }
        
        MessageDialog.openInformation(getShell(), Messages.FormulaTestInputValuesControl_InfoDialogSuccessfullyStored_Title, NLS.bind(
                Messages.FormulaTestInputValuesControl_InfoDialogSuccessfullyStored_Text, name));
    }

    /*
     * Clears all values in all corresponding formula test input value object parts.
     * Setting all values to an empty string.
     */
    private void clearFormulaTestInputValues() {
        if (formulaTestCase != null){
            IFormulaTestInputValue[] inputValues = formulaTestCase.getFormulaTestInputValues();
            for (int i = 0; i < inputValues.length; i++) {
                inputValues[i].setValue(""); //$NON-NLS-1$
                uiController.updateUI();
                repackAndResfreshParamInputTable();
                clearResult();
            }
        }
    }
    
    /*
     * Creates the table
     */
    private void createFormulaInputTable(Composite parent){
        Composite c = uiToolkit.createComposite(parent);
        c.setLayout(uiToolkit.createNoMarginGridLayout(1, false));
        c.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Table table = new Table(c, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.minimumHeight = 100;
        table.setLayoutData(gd);
        table.setHeaderVisible (true);
        table.setLinesVisible (true);
        
        // create the columns of the table
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(""); //$NON-NLS-1$
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.FormulaTestInputValuesControl_TableFormulaTestInputValues_Column_Parameter);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.FormulaTestInputValuesControl_TableFormulaTestInputValues_Column_Value);

        
        // Create the viewer and connect it to the view
        formulaInputTableViewer = new TableViewer(table);
        formulaInputTableViewer.setContentProvider (new ArrayContentProvider());
        formulaInputTableViewer.setLabelProvider (new FormulaTestInputValueTblLabelProvider());
        
        // create the cell editor
        if (!viewOnly || canStoreFormulaTestCaseAsNewFormulaTestCase){
            // the table is modifiedable if not "view only"  is set or if this is the control which can store the input as
            // new formula test case (e.g. preview formula on the first page of the formula edit dialog)

            BeanTableCellModifier tableCellModifier = new BeanTableCellModifier(formulaInputTableViewer);
            tableCellModifier.initModifier(new String[] { "image", IFormulaTestInputValue.PROPERTY_NAME,
                    IFormulaTestInputValue.PROPERTY_VALUE }, new ValueDatatype[] { null, null, ValueDatatype.STRING });
            tableCellModifier.addListener(this);
        }
        hookTableListener();     

        repackAndResfreshParamInputTable();
    }

    /*
     * Adds the listener to the formula test input value table
     */
    private void hookTableListener() {
        new TableMessageHoverService(formulaInputTableViewer) {
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element != null) {
                    return validateElement(element);
                } else
                    return null;
            }
        };
    }
    
    /*
     * Repacks the columns in the table
     */
    private void repackAndResfreshParamInputTable() {
        if (formulaTestCase != null){
            formulaInputTableViewer.setInput(formulaTestCase.getFormulaTestInputValues());
        } else {
            formulaInputTableViewer.setInput(new ArrayList());
            if (formulaResult != null){
                clearResult();
            }
        }
        
        for (int i = 0, n = formulaInputTableViewer.getTable().getColumnCount(); i < n; i++) {
            formulaInputTableViewer.getTable().getColumn(i).pack();
        }
        formulaInputTableViewer.refresh();
    }
    
    /*
     * Exceute the formula and displays the result if the formula is valid and all values are given.
     */
    public Object calculateFormulaIfValid() {
        if (!canCalculateResult){
            return null;
        }
        
        try {
            if (formulaTestCase == null){
                return null;
            }
            if (storeExpectedResult){
                formulaTestCase.setExpectedResult(""); //$NON-NLS-1$
            }
            lastCalculatedResult = null;
            
            // don't execute the formula if 
            //   - there is an error on the corresponding config element (e.g. error in formula)
            //   - the current formula test case contains at least one validation message (e.g. no value given)
            MessageList ml = ((IConfigElement) formulaTestCase.getParent()).validate();
            if (ml.getFirstMessage(Message.ERROR) != null){
                clearResult();
                return null;
            }

            ml = formulaTestCase.validate();
            // don't calculate preview if there are messages, e.g. warnings because of missing values
            if (ml.getNoOfMessages() > 0) {
                showFormulaResult("Object is not not valid.");
                return null;
            }
            Object result = formulaTestCase.execute();
            lastCalculatedResult = result;
            showFormulaResult(""+result);
            if (storeExpectedResult){
                formulaTestCase.setExpectedResult(result==null?null:result.toString());
            }
            return result;
        } catch (ParseException e){
            showFormulaResult(NLS.bind(Messages.FormulaTestInputValuesControl_Error_ParseExceptionWhenExecutingFormula, e.getLocalizedMessage()));
        } catch (Exception e) {
            showFormulaResult(Messages.FormulaTestInputValuesControl_Error_ExecutingFormula);
        }
        return null;
    }
    
    /**
     * Clears the result of the formula
     */
    public void clearResult(){
        formulaResult.setText(""); //$NON-NLS-1$
        formulaResult.pack();
    }
    
    private boolean updateBySelf(){
        if (isUpdatingSelf){
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
    private void showFormulaResult(String result){
        if (updateBySelf()){
            return;
        }
        formulaResult.setText(""+result); //$NON-NLS-1$
        formulaResult.pack();
    }
    
    /*
     * Performs and returns validation messages on the given element.
     */
    private MessageList validateElement(Object element) throws CoreException{
        MessageList messageList = new MessageList();
        // validate element
        if (element instanceof IIpsObjectPartContainer){
            messageList.add(((IIpsObjectPartContainer)element).validate());
        }
        return messageList;
    }
    
    /*
     * Returns the image for the given message list (e.g. if there is an error return a problem image)
     */
    private Image getImageForMsgList(Image defaultImage, MessageList msgList) {
        // get the cached problem descriptor for the base image
        String key = getKey(defaultImage, msgList.getSeverity());
        ProblemImageDescriptor descriptor = (ProblemImageDescriptor) cachedProblemImageDescriptors.get(key);
        if (descriptor == null && defaultImage != null){
            descriptor = new ProblemImageDescriptor(defaultImage, msgList.getSeverity());
            cachedProblemImageDescriptors.put(key, descriptor);
        }
        return IpsPlugin.getDefault().getImage(descriptor);
    }
    
    /*
     * Returns an unique key for the given image and severity compination.
     */
    private String getKey(Image image, int severity) {
        if (image == null){
            return null;
        }
        return image.hashCode() + "_" + severity; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void valueChanged(ColumnIdentifier columnIdentifier, Object value) {
        // the value in the table has changed
        repackAndResfreshParamInputTable();
        clearResult();
        uiController.updateUI();  
    }
}

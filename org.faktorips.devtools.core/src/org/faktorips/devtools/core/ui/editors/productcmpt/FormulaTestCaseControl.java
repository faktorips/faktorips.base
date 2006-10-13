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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IFormulaTestInputValue;
import org.faktorips.devtools.core.ui.ProblemImageDescriptor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Composite to display a table of formula test cases and their details.
 * 
 * @author Joerg Ortmann
 */
public class FormulaTestCaseControl extends Composite {
    private static final int IDX_COLUMN_IMAGE = 0;
    private static final int IDX_COLUMN_NAME = 1;
    private static final int IDX_COLUMN_EXPECTED_RESULT = 2;
    private static final int IDX_COLUMN_ACTUAL_RESULT = 3;
    
    private static final String PROPERTY_SELECTED = "selected"; //$NON-NLS-1$
    private static final String PROPERTY_ACTUAL_RESULT = "actualResult"; //$NON-NLS-1$
    
    private static final int TEST_ERROR = 1;
    private static final int TEST_FAILURE = 2;
    private static final int TEST_OK = 3;
    private static final int TEST_UNKNOWN = 4;
    
    private UIToolkit uiToolkit;
    
    private Image empytImage;
    
    private HashMap cachedProblemImageDescriptors = new HashMap();
    
    /* Controller of the dependent ips object part */
    private IpsPartUIController uiController;
    
    /*
     * Composite controler contains the dependent object part ui controller and the dummy contoler
     * to update the ui for this composite e.g. the actual value will be set afer executing the
     * formula
     */
    private CompositeUIController compositeUiController;
    
    /* The formula test cases which are displayed in the table */
    private List formulaTestCases = new ArrayList();

    /* Contains the table viewer to display and edit the formula test cases */
    private TableViewer formulaTestCaseTableViewer;
    
    /*
     * Contains the table to display the details of the currently selected formula test case which
     * is selected in the formula test case table
     */
    private FormulaTestInputValuesControl formulaTestInputValuesControl;
    
    /* The status bar which contains the corresponding color of the last test run */
    private Control testStatusBar;
    
    /* Buttons */
    private Button btnDeleteFormulaTestCase;
    private Button btnUpdateFormulaTestCase;
    private Button btnMoveFormulaTestCaseUp;
    private Button btnMoveFormulaTestCaseDown;
    
    /* Contains the cache for the extended data for the formula test cases */
    private HashMap extDataForFormulaTestCases = new HashMap();

    /* Contains the colors for the test status */
    private Color failureColor;
    private Color okColor;
    
    /*
     * Key Adapater class to move up or down to the next or prev column if an arrow key or return is pressed
     */
    private class KeyColumnAdapter extends KeyAdapter {
        private int idxColumn = IDX_COLUMN_EXPECTED_RESULT;
        public KeyColumnAdapter(int idxColumn){
            this.idxColumn = idxColumn;
        }
        public void keyPressed(KeyEvent e) {
            int selIdx = formulaTestCaseTableViewer.getTable().getSelectionIndex();
            int keyCode = e.keyCode;
            keyCode = (keyCode == SWT.KEYPAD_CR || keyCode == SWT.CR) ? SWT.ARROW_DOWN : keyCode;
            if (keyCode == SWT.ARROW_DOWN || keyCode == SWT.ARROW_UP) {
                selIdx = selIdx + (keyCode == SWT.ARROW_DOWN ? 1 : -1);
                Object nextObject = formulaTestCaseTableViewer.getElementAt(selIdx);
                postEditFormulaTestInputValue((IFormulaTestCase)nextObject, idxColumn);
            }
        }
    }
    
    /*
     * Extended data which is displayed beside the model data in the table
     */
    private class ExtDataForFormulaTestCase {
        Object actualResult = null;
        String message = ""; //$NON-NLS-1$
        public Object getActualResult() {
            return actualResult;
        }
        public void setActualResult(Object actualResult) {
            this.actualResult = actualResult;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    /*
     * Cell Modifier for the formula test case
     */
    private class FormulaTestCaseCellModifier implements ICellModifier {
        /**
         * {@inheritDoc}
         */
        public boolean canModify(Object element, String property) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public Object getValue(Object element, String property) {
            if (property.equals(IFormulaTestCase.PROPERTY_NAME)) {
                return getFormulaTestCaseFromObject(element).getName();
            } else if (property.equals(IFormulaTestCase.PROPERTY_EXPECTED_RESULT)) {
                return getFormulaTestCaseFromObject(element).getExpectedResult();
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public void modify(Object element, String property, Object value) {
            TableItem tableItem = (TableItem) element;
            IFormulaTestCase fct = getFormulaTestCaseFromObject(tableItem.getData());
            if (property.equals(IFormulaTestCase.PROPERTY_NAME)) {
                if (value != null && !value.equals(fct.getName())) {
                    fct.setName((String)value);
                    tableItem.setText(IDX_COLUMN_NAME, (String)value);
                    refreshUi();
                }
            } else if (property.equals(IFormulaTestCase.PROPERTY_EXPECTED_RESULT)) {
                if (value != null && !value.equals(fct.getExpectedResult())) {
                    fct.setExpectedResult((String)value);
                    tableItem.setText(IDX_COLUMN_EXPECTED_RESULT, (String)value);
                    refreshUi();
                }
            }
        }

        private IFormulaTestCase getFormulaTestCaseFromObject(Object obj){
            ArgumentCheck.isInstanceOf(obj, IFormulaTestCase.class);
            return (IFormulaTestCase) obj;
        }
        
        private void refreshUi() {
            // resets the color of the last test run
            testStatusBar.setBackground(getBackground());
            repackAndResfreshForumlaTestCaseTable();
            if (compositeUiController != null) {
                compositeUiController.updateUI();
            }
        }        
    }
    
    /*
     * Returns the status error, failure, or ok of the given formula test case
     */
    private int getFormulaTestCaseTestStatus(IFormulaTestCase formulaTestCase){
        Object actualResult = getActualResult(formulaTestCase);
        String expectedResult = formulaTestCase.getExpectedResult();
        String actualResultStr = actualResult == null?"":actualResult.toString(); //$NON-NLS-1$
        
        if (StringUtils.isEmpty(expectedResult)){
            if (StringUtils.isNotEmpty(actualResultStr)){
                return TEST_ERROR;
            }
        } else {
            if (StringUtils.isNotEmpty(actualResultStr)){
                if (actualResult.toString().equals(expectedResult.toString())){
                    return TEST_OK;
                } else {
                    return TEST_FAILURE;
                }
            }
        }
        if (actualResult != null){
            return TEST_ERROR;
        }
        return TEST_UNKNOWN;
    }
    
    /*
     * Label provider for the formula test input value.
     */
    private class FormulaTestCaseTblLabelProvider extends LabelProvider implements ITableLabelProvider{
        public Image getColumnImage(Object element, int columnIndex) {
            if (! (element instanceof IFormulaTestCase)){
                return null;
            }
            try {
                switch (columnIndex) {
                    case IDX_COLUMN_IMAGE:
                        IFormulaTestCase formulaTestCase = (IFormulaTestCase) element;
                        Image defaultImage = empytImage;
                        int result = getFormulaTestCaseTestStatus(formulaTestCase);
                        if (result == TEST_ERROR){
                            defaultImage = IpsPlugin.getDefault().getImage("obj16/testerr.gif"); //$NON-NLS-1$
                        } else if (result == TEST_OK){
                            defaultImage = IpsPlugin.getDefault().getImage("obj16/testok.gif"); //$NON-NLS-1$
                        } else if (result == TEST_FAILURE){
                            defaultImage = IpsPlugin.getDefault().getImage("obj16/testfail.gif"); //$NON-NLS-1$
                        }
                        MessageList msgList = formulaTestCase.validate();
                        // displays the validation image in the name column
                        return getImageForMsgList(defaultImage, msgList);
                }
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IFormulaTestCase){
                if (columnIndex == IDX_COLUMN_NAME){
                    return ((IFormulaTestCase)element).getName();
                } else if (columnIndex == IDX_COLUMN_EXPECTED_RESULT){
                    return ((IFormulaTestCase)element).getExpectedResult();
                } else if (columnIndex == IDX_COLUMN_ACTUAL_RESULT){
                    Object actualResult = getActualResult((IFormulaTestCase)element);
                    if (actualResult == null){
                        return ""; //$NON-NLS-1$
                    }
                    return actualResult.toString();
                }
            }
            return null;
        }
    }
    
    public FormulaTestCaseControl(Composite parent, UIToolkit uiToolkit,
            IpsPartUIController uiController, IConfigElement configElement) {
        super(parent, SWT.NONE);
        ArgumentCheck.notNull(new Object[]{ parent, uiToolkit, uiController, configElement});
        
        this.uiToolkit = uiToolkit;
        this.uiController = uiController;
        
        this.empytImage = new Image(getShell().getDisplay(), 16, 16);

        // create images for ok and failure indicators
        //   colors are taken from the JUnit test runner to show a corporate identify for test support
        failureColor = new Color(getDisplay(), 159, 63, 63);
        okColor = new Color(getDisplay(), 95, 191, 95);
        
        initControl();
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        empytImage.dispose();
        
        for (Iterator iter = cachedProblemImageDescriptors.values().iterator(); iter.hasNext();) {
            ProblemImageDescriptor problemImageDescriptor = (ProblemImageDescriptor)iter.next();
            Image problemImage = IpsPlugin.getDefault().getImage(problemImageDescriptor);
            if (problemImage != null){
                problemImage.dispose();
            }
        }
        cachedProblemImageDescriptors.clear();   
        
        extDataForFormulaTestCases.clear();
        
        failureColor.dispose();
        okColor.dispose();
        
        super.dispose();
    }
    
    /**
     * Sets and updates the to be displaying formula test cases.
     */    
    public void storeFormulaTestCases(List newFormulaTestCases) {
        boolean changed = true;
        if (newFormulaTestCases.size() == formulaTestCases.size()){
            changed = false;
            for (int i = 0; i < newFormulaTestCases.size(); i++) {
                if (! newFormulaTestCases.get(i).equals(formulaTestCases.get(i))){
                    changed = true;
                    break;
                }
            }
        }
        if (changed){
            extDataForFormulaTestCases.clear();
            for (Iterator iter = newFormulaTestCases.iterator(); iter.hasNext();) {
                extDataForFormulaTestCases.put(iter.next(), new ExtDataForFormulaTestCase());
            }
            this.formulaTestCases = newFormulaTestCases;
            repackAndResfreshForumlaTestCaseTable();
        } else {
            formulaTestCaseTableViewer.refresh();
            updateStatusOfUpdateButton(getSelectedFormulaTestCase());
        }
    }

    /*
     * Creates the compoiste's controls.
     */
    public void initControl() {
        setLayout(uiToolkit.createNoMarginGridLayout(1, false));
        setLayoutData(new GridData(GridData.FILL_BOTH));

        testStatusBar = uiToolkit.createVerticalSpacer(this, 5);
        
        Group formulaTestCaseGroup = uiToolkit.createGroup(this, Messages.FormulaTestCaseControl_GroupLabel_TestCases);

        Composite formulaTestCaseArea = uiToolkit.createComposite(formulaTestCaseGroup);
        formulaTestCaseArea.setLayout(uiToolkit.createNoMarginGridLayout(2, false));
        formulaTestCaseArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        createFormulaTestCaseTable(formulaTestCaseArea);

        // create buttons
        Composite btns = uiToolkit.createComposite(formulaTestCaseArea);
        btns.setLayout(uiToolkit.createNoMarginGridLayout(1, true));
        btns.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        
        Button btnExecFormulaTestCase = uiToolkit.createButton(btns, Messages.FormulaTestCaseControl_Button_ExecuteAll);
        btnExecFormulaTestCase.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnExecFormulaTestCase.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                executeClicked();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });  
        
        uiToolkit.createVerticalSpacer(btns, 5);
        uiToolkit.createHorizonzalLine(btns);
        uiToolkit.createVerticalSpacer(btns, 5);
        
        btnDeleteFormulaTestCase = uiToolkit.createButton(btns, Messages.FormulaTestCaseControl_Button_Delete);
        btnDeleteFormulaTestCase.setEnabled(false);
        btnDeleteFormulaTestCase.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnDeleteFormulaTestCase.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                deleteClicked();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        btnMoveFormulaTestCaseUp = uiToolkit.createButton(btns, Messages.FormulaTestCaseControl_Button_Up);
        btnMoveFormulaTestCaseUp.setEnabled(false);
        btnMoveFormulaTestCaseUp.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnMoveFormulaTestCaseUp.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                moveFormulaTestInputValues(getSelectedFormulaTestCase(), true);
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        btnMoveFormulaTestCaseDown = uiToolkit.createButton(btns, Messages.FormulaTestCaseControl_Button_Down);
        btnMoveFormulaTestCaseDown.setEnabled(false);
        btnMoveFormulaTestCaseDown.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnMoveFormulaTestCaseDown.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                moveFormulaTestInputValues(getSelectedFormulaTestCase(), false);
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        uiToolkit.createVerticalSpacer(btns, 5);
        
        btnUpdateFormulaTestCase = uiToolkit.createButton(btns, Messages.FormulaTestCaseControl_Button_Update);
        btnUpdateFormulaTestCase.setEnabled(false);
        btnUpdateFormulaTestCase.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnUpdateFormulaTestCase.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                updateClicked();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        // create the formula test detail table, to display and editing the formula test input values
        compositeUiController = new CompositeUIController();
        compositeUiController.add(uiController);
        compositeUiController.add(createDummyUIController(null));        
        
        Group formulaTestInputGroup = uiToolkit.createGroup(this, Messages.FormulaTestCaseControl_GroupLabel_TestInput);
        formulaTestInputValuesControl = new FormulaTestInputValuesControl(formulaTestInputGroup, uiToolkit, compositeUiController);
        formulaTestInputValuesControl.setCanCalulateResult(true);
        formulaTestInputValuesControl.setCanStoreExpectedResult(false);
        formulaTestInputValuesControl.setCanStoreFormulaTestCaseAsNewFormulaTestCase(false);
        formulaTestInputValuesControl.initControl();
    }
    
    /*
     * Moves the given formula test case up or down
     */
    private void moveFormulaTestInputValues(IFormulaTestCase formulaTestCase, boolean up){
        int[] selectedIndexes = null;
        IConfigElement configElement = (IConfigElement) formulaTestCase.getParent();
        IFormulaTestCase[] ftcs = configElement.getFormulaTestCases();
        for (int i = 0; i < ftcs.length; i++) {
            if (ftcs[i].equals(formulaTestCase)){
                selectedIndexes = new int[]{i};
                break;
            }
        }
        if (selectedIndexes != null){
            configElement.moveFormulaTestCases(selectedIndexes, up);
            uiController.updateUI();
        }
    }
    
    /*
     * Update the currently selected formula test case store new formula test input values and
     * delete unnecessary parameters
     */
    protected void updateClicked() {
        IFormulaTestCase selElement = getSelectedFormulaTestCase();
        try {
            IConfigElement configElem = (IConfigElement) selElement.getParent();
            String messageForChangeInfoDialog = buildMessageForUpdateInformation(selElement);
            if (selElement.addOrDeleteFormulaTestInputValues(configElem.getIdentifierUsedInFormula())) {
                // there were changes, thus trigger that the input value table will be refreshed
                selectionFormulaTestCaseChanged(selElement);
                MessageDialog.openInformation(getShell(), Messages.FormulaTestCaseControl_InformationDialogUpdateInputValues_Title,
                        messageForChangeInfoDialog);
            }
            MessageList mlConfigElement = configElem.validate();
            if ( configElem.isValid()){
                MessageList ml = selElement.validate();
                if (ml.getNoOfMessages()==0){
                    // exceute the formula test if no validation message exists
                    Object result = selElement.execute();
                    storeActualResult(selElement, result);
                }
            } else {
                // don't execute because config element is not valid
                // show message in row as tooltip
                setMessage(selElement, mlConfigElement.getFirstMessage(Message.ERROR).getText());
            }
            // refresh ui
            repackAndResfreshForumlaTestCaseTable();  
            uiController.updateUI();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /* 
     * Generates and returns the message which informs about the new and deleted parameters
     */
    private String buildMessageForUpdateInformation(IFormulaTestCase selElement) throws CoreException {
        IConfigElement configElement = (IConfigElement) selElement.getParent();
        String[] identifiersInFormula = configElement.getIdentifierUsedInFormula();
        List idsInFormula = new ArrayList(identifiersInFormula.length);
        idsInFormula.addAll(Arrays.asList(identifiersInFormula));
        List idsInTestCase = new ArrayList();
        IFormulaTestInputValue[] inputValues = selElement.getFormulaTestInputValues();
        for (int i = 0; i < inputValues.length; i++) {
            boolean found = false;
            for (int j = idsInFormula.size()-1; j >= 0; j--) {
                if (idsInFormula.get(j).equals(inputValues[i].getIdentifier())){
                    idsInFormula.remove(j);
                    found = true;
                    break;
                }
            }
            if (!found){
                idsInTestCase.add(inputValues[i].getIdentifier());
            }
        }
        String newParams = ""; //$NON-NLS-1$
        for (Iterator iter = idsInFormula.iterator(); iter.hasNext();) {
            newParams += newParams.length() > 0 ? ", " : ""; //$NON-NLS-1$ //$NON-NLS-2$
            newParams += iter.next();
        }
        String delParams = ""; //$NON-NLS-1$
        for (Iterator iter = idsInTestCase.iterator(); iter.hasNext();) {
            delParams += delParams.length() > 0 ? ", " : ""; //$NON-NLS-1$ //$NON-NLS-2$
            delParams += iter.next();
        }
        String messageNewParameter = NLS.bind(Messages.FormulaTestCaseControl_InformationDialogUpdateInputValues_NewValueParams, newParams);
        String messageDelParameter = NLS.bind(Messages.FormulaTestCaseControl_InformationDialogUpdateInputValues_DeletedValueParams, delParams);
        String messageForChangeInfoDialog = Messages.FormulaTestCaseControl_InformationDialogUpdateInputValues_TextTop + (idsInFormula.size() > 0 ? messageNewParameter : "") //$NON-NLS-2$
                + (idsInFormula.size() > 0 && idsInTestCase.size() > 0 ? "\n" : "") //$NON-NLS-1$ //$NON-NLS-2$
                + (idsInTestCase.size() > 0 ? messageDelParameter : ""); //$NON-NLS-1$
        return messageForChangeInfoDialog;
    }

    /*
     * Execute the formula for all formula test cases
     */
    protected void executeClicked() {
        boolean isErrorOrFailure = false;
        for (Iterator iter = formulaTestCases.iterator(); iter.hasNext();) {
            IFormulaTestCase element = (IFormulaTestCase)iter.next();
            Object result = ""; //$NON-NLS-1$
            try {
                IConfigElement configElement = (IConfigElement) element.getParent();
                MessageList mlConfigElement = configElement.validate();
                if ( configElement.isValid()){
                    MessageList ml = element.validate();
                    if (ml.getNoOfMessages() == 0) {
                        result = element.execute();
                    }
                } else {
                    setMessage(element, mlConfigElement.getFirstMessage(Message.ERROR).getText());
                }
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
            storeActualResult(element, result);
            int testResultStatus = getFormulaTestCaseTestStatus(element);
            if (testResultStatus != TEST_OK){
                isErrorOrFailure = true;
            }
        }
        repackAndResfreshForumlaTestCaseTable();
        if (isErrorOrFailure){
            testStatusBar.setBackground(failureColor);
        } else {
            testStatusBar.setBackground(okColor);
        }
    }

    /*
     * The delete button was clicked
     */
    private void deleteClicked() {
        IFormulaTestCase selElement = getSelectedFormulaTestCase();
        if (selElement == null){
            return;
        }
        // get the object which will be selected after the delete
        int idxBeforeLast = formulaTestCases.size()>1?formulaTestCases.size()-2:0;
        IFormulaTestCase nextElement = (IFormulaTestCase) formulaTestCases.get(idxBeforeLast);
        for (int i = formulaTestCases.size()-1; i >= 0 ; i--) {
            if (selElement.equals(formulaTestCases.get(i))){
                break;
            }
            nextElement = (IFormulaTestCase) formulaTestCases.get(i);
        }
        
        formulaTestCases.remove(selElement);
        extDataForFormulaTestCases.remove(selElement);
        selElement.delete();
        repackAndResfreshForumlaTestCaseTable();
        
        // select the next object which was evaluated before
        if (nextElement!=null){
            formulaTestCaseTableViewer.setSelection(new StructuredSelection(nextElement));
        } 
        if (formulaTestCases.size() == 0){
            formulaTestInputValuesControl.storeFormulaTestCase(null);
        }
        uiController.updateUI();
    }

    /*
     * Returns the first selected formula test case or <code>null</code> if nothing is selected.
     */
    private IFormulaTestCase getSelectedFormulaTestCase(){
        ISelection selection = formulaTestCaseTableViewer.getSelection();
        if (selection instanceof IStructuredSelection){
            IFormulaTestCase selectedFromulaTestCase = (IFormulaTestCase) ((IStructuredSelection)selection).getFirstElement();
            return selectedFromulaTestCase;
        }
        return null;
    }
    
    /*
     * Creates the dummy controller to control ui changes for the actual value
     */
    private IpsPartUIController createDummyUIController(IIpsObjectPart part) {
        IpsPartUIController controller = new IpsPartUIController(part) {
            public void updateUI() {
                // sets the actual result of currenly selected row in the formula test case table
                IFormulaTestCase selectedFromulaTestCase = getSelectedFormulaTestCase();
                if (selectedFromulaTestCase != null) {
                    storeActualResult(selectedFromulaTestCase, formulaTestInputValuesControl
                            .getLastCalculatedResult());
                    repackAndResfreshForumlaTestCaseTable();
                }
            }
        };
        return controller;
    }    
    
    /*
     * Creates the table to dipsplay and editing the formula test case.
     */
    private void createFormulaTestCaseTable(Composite c) {
        Table table = new Table(c, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible (true);
        table.setLinesVisible (true);
        
        // create the columns of the table
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(""); //$NON-NLS-1$
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.FormulaTestCaseControl_TableTestCases_Column_Name);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.FormulaTestCaseControl_TableTestCases_ColumnExpectedResult);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.FormulaTestCaseControl_TableTestCases_Column_ActualResult);
        
        // Create the viewer and connect it to the view
        formulaTestCaseTableViewer = new TableViewer(table);
        formulaTestCaseTableViewer.setContentProvider (new ArrayContentProvider());
        formulaTestCaseTableViewer.setLabelProvider (new FormulaTestCaseTblLabelProvider());
        
        // create the cell editor
        TextCellEditor textCellEditorExpResult = new TextCellEditor(table);
        textCellEditorExpResult.getControl().addKeyListener(new KeyColumnAdapter(IDX_COLUMN_EXPECTED_RESULT));
        
        // create the cell editor
        TextCellEditor textCellEditorName = new TextCellEditor(table);
        textCellEditorName.getControl().addKeyListener(new KeyColumnAdapter(IDX_COLUMN_NAME));
        
        // create cell editors
        formulaTestCaseTableViewer.setColumnProperties(new String[] { 
                PROPERTY_SELECTED, 
                IFormulaTestCase.PROPERTY_NAME, 
                IFormulaTestCase.PROPERTY_EXPECTED_RESULT, 
                PROPERTY_ACTUAL_RESULT });
        formulaTestCaseTableViewer.setCellEditors(new CellEditor[] { 
                null, 
                textCellEditorName, 
                textCellEditorExpResult, 
                null });
        formulaTestCaseTableViewer.setCellModifier(new FormulaTestCaseCellModifier());
        
        hookFormulaTestCaseTableListener();     
        
        // pack the table
        repackAndResfreshForumlaTestCaseTable();
    }

    /*
     * Set the focus to the edit field for editing the value.
     */
    private void postEditFormulaTestInputValue(final IFormulaTestCase formulaTestCase, final int idx){
        if (formulaTestCase == null){
            return;
        }
        if (!getDisplay().isDisposed()){
            getDisplay().asyncExec(new Runnable(){
                public void run() {
                    formulaTestCaseTableViewer.editElement(formulaTestCase, idx);
                }
            });
        }
    }
    
    /*
     * Adds the listener to the formula test case table
     */
    private void hookFormulaTestCaseTableListener() {
        // add listener to the table view
        formulaTestCaseTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            Object prevSelection = null;
            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection() instanceof IStructuredSelection){
                    Object selObject = ((IStructuredSelection)event.getSelection()).getFirstElement();
                    if (selObject != prevSelection){
                        prevSelection = selObject;
                        selectionFormulaTestCaseChanged((IFormulaTestCase)selObject);
                        return;
                    }
                }
            }
        });
        
        new TableMessageHoverService(formulaTestCaseTableViewer) {
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element != null) {
                    MessageList ml = validateElement(element);
                    IFormulaTestCase ftc = (IFormulaTestCase)element;
                    if (getFormulaTestCaseTestStatus(ftc) == TEST_FAILURE){
                        Object actualResult = getActualResult(ftc);
                        String actualResultStr = actualResult==null? "":actualResult.toString(); //$NON-NLS-1$
                        String text = NLS.bind(Messages.FormulaTestCaseControl_TestFailureMessage_ExpectedButWas, ftc.getExpectedResult(), actualResultStr);
                        Message msg = new Message("NONE", text, Message.INFO, this, PROPERTY_ACTUAL_RESULT); //$NON-NLS-1$
                        ml.add(msg);
                    } else if (getFormulaTestCaseTestStatus(ftc) == TEST_ERROR){
                        String message = getMessage(ftc);
                        if (StringUtils.isNotEmpty(message) && StringUtils.isNotEmpty(ftc.getExpectedResult())){
                            // the expected result is not empty, but there is an error (message), display the error
                            Message msg = new Message("NONE", message, Message.INFO, this, PROPERTY_ACTUAL_RESULT); //$NON-NLS-1$
                            ml.add(msg);
                        } else if (StringUtils.isEmpty(ftc.getExpectedResult())){
                            String text = Messages.FormulaTestCaseControl_TestError_NoExpectedResultGiven;
                            Message msg = new Message("NONE", text, Message.INFO, this, PROPERTY_ACTUAL_RESULT); //$NON-NLS-1$
                            ml.add(msg);
                        }
                    }
                    return ml;
                } else
                    return null;
            }
        };
    }

    /*
     * Method to indicate that the selection in the formula test case table has changed
     */
    protected void selectionFormulaTestCaseChanged(IFormulaTestCase selectedFormulaTestCase) {
        if (selectedFormulaTestCase == null){
            btnDeleteFormulaTestCase.setEnabled(false);
            btnMoveFormulaTestCaseUp.setEnabled(false);
            btnMoveFormulaTestCaseDown.setEnabled(false);
            btnUpdateFormulaTestCase.setEnabled(false);            
            return;
        }
        btnDeleteFormulaTestCase.setEnabled(true);
        btnMoveFormulaTestCaseUp.setEnabled(true);
        btnMoveFormulaTestCaseDown.setEnabled(true);
        updateStatusOfUpdateButton(selectedFormulaTestCase);
        
        try{
            getShell().setRedraw(false);
            formulaTestInputValuesControl.storeFormulaTestCase(selectedFormulaTestCase);
        } finally {
            getShell().setRedraw(true);
        }
        formulaTestInputValuesControl.calculateFormulaIfValid();
    }

    /*
     * Updates the status of the update button to enabled or disabled. Enable the button if
     * there is an mismatch between the formulas and the formula test case parameters
     */
    private void updateStatusOfUpdateButton(IFormulaTestCase selectedFormulaTestCase) {
        if (btnUpdateFormulaTestCase == null){
            return;
        }
        btnUpdateFormulaTestCase.setEnabled(false);
        if (selectedFormulaTestCase == null){
            return;
        }
        try {
            btnUpdateFormulaTestCase.setEnabled(false);
            MessageList ml;
            ml = selectedFormulaTestCase.validate();
            if (ml.getMessageByCode(IFormulaTestCase.MSGCODE_IDENTIFIER_MISMATCH) != null){
                btnUpdateFormulaTestCase.setEnabled(true);
            }             
        } catch (CoreException e) {
            // exception ignored, the validation exception will not be diplayed here
        }
    }
    
    /*
     * Repacks the columns in the table
     */
    private void repackAndResfreshForumlaTestCaseTable() {
        if (formulaTestCases != null){
            formulaTestCaseTableViewer.setInput(formulaTestCases);
        }
        for (int i = 0, n = formulaTestCaseTableViewer.getTable().getColumnCount(); i < n; i++) {
            formulaTestCaseTableViewer.getTable().getColumn(i).pack();
        }
        // resets the color of the last test run
        testStatusBar.setBackground(getBackground());
        formulaTestCaseTableViewer.refresh();
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
 
    /*
     * Sets the actual result of the given formula test case
     */
    private void storeActualResult(IFormulaTestCase formulaTestCase, Object actualResult){
        ExtDataForFormulaTestCase extData = (ExtDataForFormulaTestCase) extDataForFormulaTestCases.get(formulaTestCase);
        if (extData != null){
            extData.setActualResult(actualResult);
        }
    }

    /*
     * Returns the actual result of the given formula test case
     */
    private Object getActualResult(IFormulaTestCase formulaTestCase){
        ExtDataForFormulaTestCase extData = (ExtDataForFormulaTestCase) extDataForFormulaTestCases.get(formulaTestCase);
        if (extData != null){
            return extData.getActualResult();
        }
        return null;
    }
    
    /*
     * Sets the actual result of the given formula test case
     */
    private void setMessage(IFormulaTestCase formulaTestCase, String message){
        ExtDataForFormulaTestCase extData = (ExtDataForFormulaTestCase) extDataForFormulaTestCases.get(formulaTestCase);
        if (extData != null){
            extData.setMessage(message);
        }
    }

    /*
     * Returns the actual result of the given formula test case
     */
    private String getMessage(IFormulaTestCase formulaTestCase){
        ExtDataForFormulaTestCase extData = (ExtDataForFormulaTestCase) extDataForFormulaTestCases.get(formulaTestCase);
        if (extData != null){
            return extData.getMessage();
        }
        return ""; //$NON-NLS-1$
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
}

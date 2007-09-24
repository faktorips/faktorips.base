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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IFormula;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IFormulaTestInputValue;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.ChangeParametersControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.devtools.core.ui.editors.pctype.ParameterInfo;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;


/**
 *
 */
public class FormulaEditDialog extends IpsPartEditDialog {
    private static final String UI_FORMULA_TEST_CASE_NAME = "UIFormulaTest"; //$NON-NLS-1$
    
    // the formula configuration element being edited
    private IFormula formula;
    
    // the formulas method signature
    private IMethod signature;
    
    private IIpsProject ipsProject;
    
    // control to display & edit the formula parameters
    private ChangeParametersControl parametersControl;
    
    // edit fields
    private TextField formulaField;
    
    // the formula test cases composite displayed on the formula test case page
    private FormulaTestCaseControl formulaTestCaseControl;

    // the table to display the formula test case, to preview the result of the editing formula
    private FormulaTestInputValuesControl formulaDummyTestInputValuesControl;
    
    /**
     * Creates a new dialog which allows to edit a formula.
     * 
     * @param configElement The config element the formula is for.
     * @param parentShell The shell as parent for the dialog.
     * 
     * @throws CoreException if the config element is invalid (e.g. no datatype can be found for it).
     */
    public FormulaEditDialog(IFormula formula, Shell parentShell) throws CoreException {
        super(formula, parentShell, Messages.FormulaEditDialog_editFormula, true);
        ArgumentCheck.notNull(formula);
        this.formula = formula;
        this.ipsProject = formula.getIpsProject();
        signature = formula.findFormulaSignature(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    protected IpsObjectUIController createUIController(IIpsObjectPart part) {
        IpsObjectUIController controller = new IpsObjectUIController(part) {

			protected MessageList validatePartContainerAndUpdateUI() {
			    MessageList messages = super.validatePartContainerAndUpdateUI();
				return validateAndUpdateDialogUI(messages);
			}
        };
        return controller;
    }

    /*
     * Validates and updates the dialog
     */
    private MessageList validateAndUpdateDialogUI(MessageList messages) {       
        MessageList relevantMessages = new MessageList();
		if (messages.getNoOfMessages() > 0) {
			// get only message wich are not for the dummy formula test case
			// (getTransientFormulaTestCases)
			for (Iterator iter = messages.iterator(); iter.hasNext();) {
				Message msg = (Message) iter.next();
				if (isNotMessageForDummyFormulaTestCase(msg)) {
					relevantMessages.add(msg);
				}
			}
			if (!relevantMessages.isEmpty()) {
				Message firstMessage = relevantMessages.getMessage(0);
				setMessage(firstMessage.getText(),
						getJFaceMessageType(firstMessage.getSeverity()));
			} else {
				setMessage(""); //$NON-NLS-1$
			}
		} else {
			setMessage(""); //$NON-NLS-1$
		}
		updateUiFormulaTestCaseTab();
		updateUiPreviewFormulaResult();
		return relevantMessages;
    }
    
    /**
	 * Returns <code>true</code> if the message is no message which is direcly
	 * related to the dummy formula test case.
	 */
    private boolean isNotMessageForDummyFormulaTestCase(Message msg) {
        ObjectProperty[] props = msg.getInvalidObjectProperties();
        for (int i = 0; i < props.length; i++) {
            if (props[i].getObject() instanceof IFormulaTestInputValue) {
                IFormulaTestInputValue inputValue = (IFormulaTestInputValue)props[i].getObject();
                if (inputValue.getParent().equals(getTransientFormulaTestCases())) {
                    return false;
                }
            } else if (props[i].getObject() instanceof IFormulaTestCase) {
                if (props[i].getObject().equals(getTransientFormulaTestCases())) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * Updates the formula test case tab.
     */
    private void updateUiFormulaTestCaseTab() {
        formulaTestCaseControl.storeFormulaTestCases(getPersistentFormulaTestCases());
    }

    /*
     * Returns all relevant formula test cases which will be displayed in the formula test case tab.
     * Note the formula test case displayed on the first page (used as preview the formula result), is a 
     * none persistent formula test case, because it will be deleted if closing this dialog and will be recreated
     * if opening the dialog, therefore it will not be returned by this method (see UI_FORMULA_TEST_CASE_NAME this
     * is the name of this "dummy" formula test case.
     */
    private List getPersistentFormulaTestCases(){
        List persitentFormulaTestCases = new ArrayList();
        IFormulaTestCase[] ftc = formula.getFormulaTestCases();
        for (int i = 0; i < ftc.length; i++) {
            if (! ftc[i].getName().equals(UI_FORMULA_TEST_CASE_NAME)){
                persitentFormulaTestCases.add(ftc[i]);
            }
        }
        return persitentFormulaTestCases;
    }
    
    /*
     * Returns the transient formula test case which is used to preview the formula result on the first page.
     */
    private IFormulaTestCase getTransientFormulaTestCases(){
        return formula.getFormulaTestCase(UI_FORMULA_TEST_CASE_NAME);
    }
    
    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.FormulaEditDialog_Formula);
        firstPage.setControl(createFirstPage(folder));
        
        createFormulaTestCasesTab(folder);
        
        createDescriptionTabItem(folder);
        
        return folder;
    }
    
    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        GridLayout layout = (GridLayout)c.getLayout();
        layout.verticalSpacing = 20;


        parametersControl = new ChangeParametersControl(c, uiToolkit, SWT.NONE, Messages.FormulaEditDialog_availableParameters, ipsProject) {

            public MessageList validate(int paramIndex) throws CoreException {
                return new MessageList();
            }
            
        };
        parametersControl.setCanAddParameters(false);
        parametersControl.setCanChangeParameterTypes(false);
        parametersControl.setCanChangeParameterNames(false);
        parametersControl.setCanMoveParameters(false);
        parametersControl.setTableStyle(SWT.BORDER);
        parametersControl.setCanChangeParameterNames(isDataChangeable());
        parametersControl.initControl();
        parametersControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Text formulaText = uiToolkit.createMultilineText(c);
        try {
            FormulaCompletionProcessor completionProcessor = new FormulaCompletionProcessor(formula);
            ContentAssistHandler.createHandlerForText(formulaText, CompletionUtil.createContentAssistant(completionProcessor));
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        
        // create fields
        formulaField = new TextField(formulaText);

        // create the formula input composite
        Group formulaTestGroup = uiToolkit.createGroup(c, Messages.FormulaEditDialog_GroupLabel_FormulaTestInput);
        formulaDummyTestInputValuesControl = new FormulaTestInputValuesControl(formulaTestGroup, uiToolkit, uiController);
        formulaDummyTestInputValuesControl.setCanCalulateResult(true);
        formulaDummyTestInputValuesControl.setCanStoreFormulaTestCaseAsNewFormulaTestCase(true);
        formulaDummyTestInputValuesControl.setCanStoreExpectedResult(true);
        formulaDummyTestInputValuesControl.initControl();
        
        return c;
    }

    /*
     * If necessary updates the ui formula test case to calculate a preview result and execute it if the formula is valid.
     */
    private void updateUiPreviewFormulaResult(){
        try {
            String[] parameterIdentifiers = formula.getParameterIdentifiersUsedInFormula(formula.getIpsProject());
            IFormulaTestCase formulaTestCase = getTransientFormulaTestCases();
            if (formulaTestCase == null){
                formulaTestCase = formula.newFormulaTestCase();
                formulaTestCase.setName(UI_FORMULA_TEST_CASE_NAME);
            }
            if (formula.isValid()){
                if (formulaTestCase.addOrDeleteFormulaTestInputValues(parameterIdentifiers, ipsProject) 
                        || formulaTestCase.getFormulaTestInputValues().length == 0){
                    // only if the parameter have changes repack the table of input values
                    formulaDummyTestInputValuesControl.storeFormulaTestCase(formulaTestCase);
                }
            }
            formulaDummyTestInputValuesControl.clearResult();
        } catch (Exception ex) {
            IpsPlugin.logAndShowErrorDialog(ex);
        }
    }
    
    private int getJFaceMessageType(int severity){
    	
    	if(Message.ERROR == severity){
    		return IMessageProvider.ERROR;
    	}
    	if(Message.INFO ==  severity){
    		return IMessageProvider.INFORMATION;
    	}
    	if(Message.WARNING == severity){
    		return IMessageProvider.WARNING;
    	}
    	
    	return IMessageProvider.NONE;
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(formulaField, IFormula.PROPERTY_EXPRESSION);
        List infos = ParameterInfo.createInfosAsList(signature.getParameters());
        parametersControl.setInput(infos);
    }
    
    /**
     * Overrides the parent method and returns a combination of attribute name and datatype name of
     * the attribute this formula relates to.
     */
    protected String buildTitle() {
    	return formula.getFormulaSignature() + " - " + signature.getDatatype(); //$NON-NLS-1$
    }

    protected void okPressed() {
        // delete dummy formula test case for testing the formel on the first editor page
        IFormulaTestCase formulaTestCase = getTransientFormulaTestCases();
        if (formulaTestCase != null){
            formulaTestCase.delete();
        }
        super.okPressed();
    }
    
    /*
     * Create the tab to displaying and editing all formula test cases for the config items formula.
     */
    private void createFormulaTestCasesTab(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        formulaTestCaseControl = new FormulaTestCaseControl(c, uiToolkit, uiController, formula);
        formulaTestCaseControl.initControl();
        formulaTestCaseControl.storeFormulaTestCases(getPersistentFormulaTestCases());
        
        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(Messages.FormulaEditDialog_TabText_FormulaTestCases);
        item.setControl(c);
        
    }
}

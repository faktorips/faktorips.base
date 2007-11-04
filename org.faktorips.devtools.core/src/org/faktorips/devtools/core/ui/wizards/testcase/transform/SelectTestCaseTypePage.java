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

package org.faktorips.devtools.core.ui.wizards.testcase.transform;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.TestCaseTypeRefControl;
import org.faktorips.util.ArgumentCheck;

/**
 * Wizard page to select the test case type and optional the name extension 
 * of the new imported test cases.
 * 
 * @author Joerg Ortmann
 */
public class SelectTestCaseTypePage extends WizardPage {
	private static final String PAGE_ID = "SelectTestCaseType"; //$NON-NLS-1$
	
	private TransformRuntimeTestCaseWizard wizard;
	
    private TextButtonField supertypeField;
    
    private TextField extensionField;
    
    private Composite testCaseTypeComposite;
    
    private Composite prevTestCaseTypeComposite;
    
    private Composite parentOfSuperPcTypeComposite;
    
	public SelectTestCaseTypePage(TransformRuntimeTestCaseWizard wizard) {
		super(PAGE_ID, Messages.TransformWizard_SelectTestCaseType_title, null);
		setDescription(Messages.TransformWizard_SelectTestCaseType_description);
		this.wizard = wizard;
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
		UIToolkit uiToolkit = wizard.getUiToolkit();
        
		Composite content = uiToolkit.createComposite(parent);
		content.setLayout(new GridLayout(1, false));
		content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		testCaseTypeComposite = uiToolkit.createComposite(content);
		testCaseTypeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		testCaseTypeComposite.setLayout(layout);
			
		
        Composite editFields = uiToolkit.createLabelEditColumnComposite(content);
        uiToolkit.createFormLabel(editFields, Messages.TransformWizard_SelectTestCaseType_TestCaseTypeLabel);
        
        parentOfSuperPcTypeComposite = uiToolkit.createComposite(editFields);
        parentOfSuperPcTypeComposite.setLayout(uiToolkit.createNoMarginGridLayout(1, false));
        parentOfSuperPcTypeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        createTestCaseTypeCtrl(parentOfSuperPcTypeComposite);

        uiToolkit.createLabel(editFields, Messages.TransformWizard_SelectTestCaseType_ExtensionLabel);
        extensionField = new TextField(uiToolkit.createText(editFields));
        extensionField.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        extensionField.addChangeListener(new ValueChangeListener(){
		    public void valueChanged(FieldValueChangedEvent e) {
		    	wizard.setNewTestCaseNameExtension((String) e.field.getValue());
		    }
		});
    	
        setControl(content);
	}

	/**
	 * Creates the control to select the test case type. The to be selected test case type depends on
	 * the target package fragment from the previous page.
	 */
	void createTestCaseTypeControl(){
	    ArgumentCheck.notNull(parentOfSuperPcTypeComposite);
        
		if (prevTestCaseTypeComposite != null){
			prevTestCaseTypeComposite.dispose();
        }
		
        prevTestCaseTypeComposite = createTestCaseTypeCtrl(parentOfSuperPcTypeComposite);

        parentOfSuperPcTypeComposite.getParent().pack();
        parentOfSuperPcTypeComposite.getParent().getParent().layout();             
	}
	
    private Composite createTestCaseTypeCtrl(Composite parent){
        UIToolkit uiToolkit = wizard.getUiToolkit();
        IIpsPackageFragment targetIpsPackageFragment = wizard.getTargetIpsPackageFragment();
        if (wizard.getTargetIpsPackageFragment()!= null){
            TestCaseTypeRefControl superTypeControl = 
                uiToolkit.createTestCaseTypeRefControl(targetIpsPackageFragment.getIpsProject(), parent);

            supertypeField = new TextButtonField(superTypeControl);
            supertypeField.addChangeListener(new ValueChangeListener(){
                public void valueChanged(FieldValueChangedEvent e) {
                    wizard.setTestCaseTypeName((String) e.field.getValue());
                }
            });
            superTypeControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            superTypeControl.getTextControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            return superTypeControl;
        }
        return null;
    }
    
	/**
	 * Sets an error message or remove current error message.
	 */
	void setErrorMsg(String errorMsg){
		if (errorMsg.length()>0)
			super.setErrorMessage(errorMsg);
		else
			super.setErrorMessage(null);
	}
}

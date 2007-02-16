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

package org.faktorips.devtools.core.ui.editors.pctype.relationwizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Abstract class for all relation wizard property pages.
 */
abstract class AbstractPcTypeRelationWizardPage extends WizardPage {

	// the wizard which the page belongs to
	protected NewPcTypeRelationWizard wizard;
	
	public AbstractPcTypeRelationWizardPage(String pageName, String title, String description,
			NewPcTypeRelationWizard newPcTypeRelationWizard) {
		super(pageName, title, null);

		super.setDescription(description);

		this.wizard = newPcTypeRelationWizard;
		
		setPageComplete(true);
	}
	
	/**
	 * {@inheritDoc}
	 */
    public boolean canFlipToNextPage() {
        boolean canFlipToNextPage=false;

        // check if the page has changed
		wizard.checkAndStorePageChangeEvent();
		
        if (wizard.isError()){
        	return false;
        }
        
		// set the controls status (disabled or hidden) depending on the model
		if (!updateControlStatus()){
			return false;
		}
		
		// only if an existing target is chosen then next page will be displayed
		if (wizard.getTargetPolicyCmptType()==null)
			return false;
		
		// check if the current page is not a hidden page
		if (getNextPage() != null){
			canFlipToNextPage = ((AbstractPcTypeRelationWizardPage)getNextPage()).isPageVisible();
		}else{
			canFlipToNextPage = false;
		}

    	return canFlipToNextPage;
    }

    /**
     * 
     * {@inheritDoc}
     */
	public void createControl(Composite parent) {
		UIToolkit uiToolkit = wizard.getUiToolkit();
		
		Composite c = uiToolkit.createGridComposite(parent, 1, false, true);
	    ((GridLayout)c.getLayout()).marginHeight = 12;

	    createControls(c);
	    connectToModel();
	    
	    setControl(c);
	}
	
    /**
     * This method returns false to disable the finish button.
     * {@inheritDoc}
     */
    public boolean isPageComplete() {
        if (wizard.isValidationError()){
            return false;
        } else {
            return super.isPageComplete();
        }
    }
    
	//
	// Abstract methods.
	//
    
    /**
     * Create all controls.
     */
	protected abstract void createControls(Composite parent);
	
	/**
	 * Connect the model controlls to the model.
	 *
	 */
	protected abstract void connectToModel();
	
	/** 
	 * Returns if the page is visible or not. 
	 */
	protected abstract boolean isPageVisible();
	
	/**
	 * Update the control status concering the current model.
	 * This function should disable or hide the controls on the page.
	 * This function is called when the page will be shown in the wizard.
	 */
	protected abstract boolean updateControlStatus();
}

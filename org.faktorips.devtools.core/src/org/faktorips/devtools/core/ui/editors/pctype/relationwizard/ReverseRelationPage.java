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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Relation wizard page to specify is a new one, an existing, or none reverse relation should be created / used.
 */
public class ReverseRelationPage extends AbstractPcTypeRelationWizardPage {
	private static final String PAGE_ID = "NewPcTypeRelationWizard.ReverseRelation"; //$NON-NLS-1$

	/** edit fields */
	private Button newReverseRelation;
	private Button useExistingRelation;
	private Button noReverseRelation;
	private Button prevSelection;

    // indicate if the page was shown before
    private boolean visibleBefore = false;
		
	public ReverseRelationPage(NewPcTypeRelationWizard newPcTypeRelationWizard) {
		super(PAGE_ID, Messages.NewPcTypeRelationWizard_reverseRelation_title,
				Messages.NewPcTypeRelationWizard_reverseRelation_description,
				newPcTypeRelationWizard);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createControls(Composite parent) {
		UIToolkit uiToolkit = wizard.getUiToolkit();

		// create controls
		
		// create radio buttons
		ReverseRelationSelectionListener listener = new ReverseRelationSelectionListener();

		newReverseRelation = uiToolkit.createRadioButton(parent, Messages.NewPcTypeRelationWizard_reverseRelation_labelNewReverseRelation);
		newReverseRelation.addSelectionListener(listener);
		
		uiToolkit.createVerticalSpacer(parent, 1);

		useExistingRelation = uiToolkit.createRadioButton(parent, Messages.NewPcTypeRelationWizard_reverseRelation_labelUseExistingRelation);
		useExistingRelation.addSelectionListener(listener);
		
		uiToolkit.createVerticalSpacer(parent, 1);
		
		noReverseRelation = uiToolkit.createRadioButton(parent, Messages.NewPcTypeRelationWizard_reverseRelation_labelNoReverseRelation);
		noReverseRelation.addSelectionListener(listener);
		
		// set the default selection
		newReverseRelation.setSelection(true);
        
		prevSelection = newReverseRelation;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void connectToModel() {
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean updateControlStatus() {
		wizard.setFocusIfPageChanged(newReverseRelation);
        
		return true;
	}

	/**
	 * Listener for the radio buttons.
	 */
	private class ReverseRelationSelectionListener implements SelectionListener {
		/**
		 * {@inheritDoc}
		 */
		public void widgetSelected(SelectionEvent e) {
			// if no reverse relation is selected then disable next wizard page
			// other wise enable next wizard page
			if (prevSelection != e.getSource()){
				prevSelection = (Button) e.getSource();
				wizard.storeReverseRelation(null);
				if (e.getSource() == useExistingRelation) {
					wizard.restoreMementoTargetBeforeChange();
					wizard.setExistingReverseRelation();
					wizard.updateDescriptionReverseRelationPropertiesPage(Messages.NewPcTypeRelationWizard_reverseRelationProp_description_existing);
				}else if(e.getSource() == newReverseRelation){
                    wizard.restoreMementoTargetBeforeChange();
					wizard.setNewReverseRelation();
				}else if(e.getSource() == noReverseRelation){
					wizard.restoreMementoTargetBeforeChange();
					wizard.setNoneReverseRelation();
				}
				if (getWizard().getContainer() != null){
					getWizard().getContainer().updateButtons();
				}
				
				// informs the property page of the reverse relation about the change
				wizard.resetReverseRelationPropertiesPage();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected boolean isPageVisible(){
		return true;
    }
    
    /**
     * Returns <code>true</code> if the next button could be enabled.
     * The next button is only enabled if the relation is valid or
     * the next page after this page was displayed.
     * {@inheritDoc}
     */
    public boolean canFlipToNextPage() {
        if (!visibleBefore ){
            visibleBefore = true;
            // indicate that a new reverse relation will be created (default)
            wizard.setNewReverseRelation();
        }
        boolean canFlipToNextPage = super.canFlipToNextPage();
        
        if (useExistingRelation.getSelection() && ! wizard.relationsExists()){
            String message = Messages.ReverseRelationPage_NewPcTypeRelationWizard_message_no_reverse_relation_found;
            setMessage(message);
            wizard.getContainer().updateMessage();
            return false;
        } else {
            setMessage(Messages.NewPcTypeRelationWizard_reverseRelation_description);
            wizard.getContainer().updateMessage();
        }
        
        return canFlipToNextPage;
    }
}

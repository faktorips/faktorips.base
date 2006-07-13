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
 * Relation wizard page to specify if none, a new one, or an existing reverse relation should be used.
 */
public class ReverseRelationPage extends AbstractPcTypeRelationWizardPage {
	private static final String PAGE_ID = "NewPcTypeRelationWizard.ReverseRelation"; //$NON-NLS-1$

	/** edit fields */
	private Button newReverseRelation;
	private Button useExistingRelation;
	private Button noReverseRelation;

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
		noReverseRelation = uiToolkit.createRadioButton(parent, Messages.NewPcTypeRelationWizard_reverseRelation_labelNoReverseRelation);
		noReverseRelation.addSelectionListener(listener);
		
		uiToolkit.createVerticalSpacer(parent, 1);
		
		newReverseRelation = uiToolkit.createRadioButton(parent, Messages.NewPcTypeRelationWizard_reverseRelation_labelNewReverseRelation);
		newReverseRelation.addSelectionListener(listener);
		
		uiToolkit.createVerticalSpacer(parent, 1);
		
		useExistingRelation = uiToolkit.createRadioButton(parent, Messages.NewPcTypeRelationWizard_reverseRelation_labelUseExistingRelation);
		useExistingRelation.addSelectionListener(listener);
		
		// set the default selection
		noReverseRelation.setSelection(true);
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
			wizard.setNoneReverseRelation();
			if (e.getSource() == useExistingRelation) {
				wizard.storeReverseRelation(null);
				wizard.restoreMementoTargetBeforeChange();
				wizard.setExistingReverseRelation();
				wizard.updateDescriptionReverseRelationPropertiesPage(Messages.NewPcTypeRelationWizard_reverseRelationProp_description_existing);
			}else if(e.getSource() == newReverseRelation){
				wizard.setNewReverseRelation();
			}
			getContainer().updateButtons();
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
}

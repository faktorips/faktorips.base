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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;

/**
 * Relation wizard page to define the target, type with flag for read-only container, 
 * and the description of the relation.
 */
public class RelationTargetPage extends AbstractPcTypeRelationWizardPage  {
	private static final String PAGE_ID = "RelationTarget"; //$NON-NLS-1$

	/** edit fields */
	private TextButtonField targetField;
	private EnumValueField typeField;
	private CheckboxField abstractContainerField;
	private TextField descriptionField;

	public RelationTargetPage(NewPcTypeRelationWizard newPcTypeRelationWizard) {
		super(PAGE_ID, Messages.NewPcTypeRelationWizard_target_title,
				Messages.NewPcTypeRelationWizard_target_description,
				newPcTypeRelationWizard);
	}

	/** State variable to store if the ui is initialized */
	private boolean isUiInitialized=false;
	
	/**
	 * {@inheritDoc}
	 */
	public void createControls(Composite parent) {
		UIToolkit uiToolkit = wizard.getUiToolkit();
	
		Composite mainComposite;
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 12;
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setBackground(parent.getBackground());
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite workArea = uiToolkit.createLabelEditColumnComposite(mainComposite);
		workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

		uiToolkit.createFormLabel(workArea,
				Messages.NewPcTypeRelationWizard_target_labelTarget);
		final PcTypeRefControl targetControl = uiToolkit.createPcTypeRefControl(
				wizard.getRelation().getIpsProject(), workArea);
		targetControl.getTextControl().addListener (SWT.Modify, new Listener () {
			public void handleEvent (Event ev) {
				//	first store the current modified value, because the controller updates the model later
				wizard.getRelation().setTarget(targetControl.getText());
				if (wizard.getTargetPolicyCmptType() == null
						|| !wizard.getRelation().getTarget().equals(
								wizard.getTargetPolicyCmptType()
										.getQualifiedName())) {
					// target changed
					storeTarget();
					// update the buttons to enable next if an existing target was chosen
					getContainer().updateButtons();
				}
                setDefaults();
			}
		});
		
		uiToolkit.createFormLabel(workArea,
				Messages.NewPcTypeRelationWizard_target_labelType);
		final Combo typeCombo = uiToolkit.createCombo(workArea, RelationType.getEnumType());
		typeCombo.addListener (SWT.Modify, new Listener () {
			public void handleEvent (Event ev) {
				// first store the current modified value, because the controller updates the model later
				wizard.getRelation().setRelationType(RelationType.getRelationType(typeCombo.getSelectionIndex()));
				setDefaults();
			}
		});
		
        uiToolkit.createFormLabel(workArea, Messages.NewPcTypeRelationWizard_target_labelReadOnlyContainer);
        final Checkbox abstractContainerCheckbox = uiToolkit.createCheckbox(workArea);        
        abstractContainerCheckbox.getButton().addSelectionListener( new SelectionAdapter () {
			 public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				// first store the current modified value, because the controller updates the model later
				 wizard.getRelation().setReadOnlyContainer(abstractContainerCheckbox.getButton().getSelection());
				 setDefaults();
			}
		});
		
        uiToolkit.createVerticalSpacer(mainComposite, 12);
		uiToolkit.createFormLabel(mainComposite,
				Messages.NewPcTypeRelationWizard_target_labelDescription);
		Text text = uiToolkit.createMultilineText(parent);

		// create fields
		targetField = new TextButtonField(targetControl);
		typeField = new EnumValueField(typeCombo, RelationType.getEnumType());
		abstractContainerField = new CheckboxField(abstractContainerCheckbox);
		descriptionField = new TextField(text);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void connectToModel() {
		wizard.addToUiControllerRelation(targetField, IRelation.PROPERTY_TARGET);
		wizard.addToUiControllerRelation(typeField, IRelation.PROPERTY_RELATIONTYPE);
		wizard.addToUiControllerRelation(abstractContainerField,
				IRelation.PROPERTY_READONLY_CONTAINER);
		wizard.addToUiControllerRelation(descriptionField, IRelation.PROPERTY_DESCRIPTION);
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean updateControlStatus() {
		// first initialization of ui
		if (!isUiInitialized){
			setDefaults();
			
			// set composition type as default
			typeField.getCombo().setText(RelationType.COMPOSITION_MASTER_TO_DETAIL.getName());
			
			isUiInitialized = true;
		}
		
		wizard.setFocusIfPageChanged(targetField.getControl());
		
		return true;
	}
	
	/**
	 * Set default values depending on the type of relation.
	 */
	private void setDefaults() {
		wizard.setDefaultsByRelationTypeAndTarget(wizard.getRelation());
		wizard.getUiControllerRelation().updateUI();
		
		if (wizard.isNewReverseRelation() && wizard.getReverseRelation() != null){
			wizard.getReverseRelation().setRelationType(NewPcTypeRelationWizard.getCorrespondingRelationType(wizard.getRelation().getRelationType()));
            wizard.setDefaultsByRelationTypeAndTarget(wizard.getReverseRelation());
            wizard.getUiControllerReverseRelation().updateUI();
		}
	}
	
	/**
	 * Stores the policy component type of the target.
	 */
	private void storeTarget() {
		try {
			wizard.storeTargetPolicyCmptType(null);
			IPolicyCmptType targetPcType = wizard.getRelation().findTarget();
			wizard.storeTargetPolicyCmptType(targetPcType);
		} catch (CoreException e) {
			IpsPlugin.log(e);
			wizard.showErrorPage(e);
		}
	}	
	
	/**
	 * {@inheritDoc}
	 */
	protected boolean isPageVisible(){
		return true;
	}		
}

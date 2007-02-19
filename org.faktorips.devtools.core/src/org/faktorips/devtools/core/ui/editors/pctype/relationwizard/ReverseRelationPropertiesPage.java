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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.LabelField;

/**
 * Relation wizard page to define the reverse relation fields.
 */
public class ReverseRelationPropertiesPage extends AbstractPropertiesPage {
	private static final String PAGE_ID = "NewPcTypeRelationWizard.ReverseProperties"; //$NON-NLS-1$

	/* Edit fields */
	private ComboField existingRelationsField;
	private LabelField existingRelationsLabel;
	private EnumValueField typeField;

	/* State variables */
	private String prevSelExistingRelation = ""; //$NON-NLS-1$
	private String prevTarget = ""; //$NON-NLS-1$
	private RelationType prevRelType = RelationType.COMPOSITION_MASTER_TO_DETAIL;
	private boolean prevIsExisting;
	private boolean prevIsNew;
	
    // cached list of existing target relations
    private List existingRelations = new ArrayList();
    
    /**
     * Returns relations from the target if:<br>
     * <ul>
     * <li>the target of the target relation points to the source
     * <li>the target relation type is the corresponding relation type of the
     * source (Assoziation=Assoziation, Composition=>ReverseComp,
     * ReverseComp=>Compostion)
     * </ul>
     * If no relation is found on the target then an empty (not null) ArrayList
     * is returned.
     * 
     * @throws CoreException
     */
     public static List getCorrespondingTargetRelations(IRelation sourceRelation,
            IPolicyCmptType target) throws CoreException {
        ArrayList relationsOfTarget = new ArrayList();
        IPolicyCmptType currTargetPolicyCmptType = target;
        while (currTargetPolicyCmptType != null){
            IRelation[] relations = currTargetPolicyCmptType.getRelations();
            for (int i = 0; i < relations.length; i++) {
                // add the relation of the target if it points to the source policy cmpt
                // and the type is matching to the source relation
                if (relations[i].getTarget().equals(
                        sourceRelation.getPolicyCmptType().getQualifiedName())
                        && relations[i].getRelationType() == NewPcTypeRelationWizard.getCorrespondingRelationType(sourceRelation
                                .getRelationType())) {
                    relationsOfTarget.add(relations[i]);
                }
            }
            currTargetPolicyCmptType = currTargetPolicyCmptType.findSupertype();
        }
        return relationsOfTarget;
    }
    
	public ReverseRelationPropertiesPage(
			NewPcTypeRelationWizard newPcTypeRelationWizard) {
		super(PAGE_ID,
				Messages.NewPcTypeRelationWizard_reverseRelationProp_title, "", //$NON-NLS-1$
				newPcTypeRelationWizard);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createControls(Composite parent) {
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

		uiToolkit.createFormLabel(workArea, Messages.NewPcTypeRelationWizard_reverseRelationProp_labelTarget);
        Text targetControl = uiToolkit.createText(workArea);
        targetControl.setEnabled(false);
        targetControl.setText(wizard.getPolicyCmptTypeQualifiedName());

        uiToolkit.createFormLabel(workArea, Messages.NewPcTypeRelationWizard_reverseRelationProp_labelType);
        Combo typeCombo = uiToolkit.createCombo(workArea, RelationType.getEnumType());
        typeCombo.setEnabled(false);

        Label existingRelLabel = uiToolkit.createFormLabel(workArea,
                Messages.NewPcTypeRelationWizard_reverseRelationProp_labelExistingRelation);
        final Combo existingRelCombo = uiToolkit.createCombo(workArea);
        existingRelCombo.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event ev) {
                existingRelationSelectionChanged();
            }
        });

		uiToolkit.createVerticalSpacer(mainComposite, 12);
		
		existingRelationsField = new ComboField(existingRelCombo);
		existingRelationsLabel = new LabelField(existingRelLabel);
		typeField = new EnumValueField(typeCombo, RelationType.getEnumType());

		super.createControls(mainComposite);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void connectToModel() {
		wizard.addToUiControllerReverseRelation(typeField,
				IRelation.PROPERTY_RELATIONTYPE);

		wizard.addToUiControllerReverseRelation(minCardinalityField,
				IRelation.PROPERTY_MIN_CARDINALITY);
		wizard.addToUiControllerReverseRelation(maxCardinalityField,
				IRelation.PROPERTY_MAX_CARDINALITY);
		wizard.addToUiControllerReverseRelation(targetRoleSingularField,
				IRelation.PROPERTY_TARGET_ROLE_SINGULAR);
		wizard.addToUiControllerReverseRelation(targetRolePluralField,
				IRelation.PROPERTY_TARGET_ROLE_PLURAL);

		wizard.addToUiControllerReverseRelation(productRelevantField,
				IRelation.PROPERTY_PRODUCT_RELEVANT);

		wizard.addToUiControllerReverseRelation(minCardinalityProdRelevantField,
				IRelation.PROPERTY_MIN_CARDINALITY_PRODUCTSIDE);
		wizard.addToUiControllerReverseRelation(maxCardinalityProdRelevantField,
				IRelation.PROPERTY_MAX_CARDINALITY_PRODUCTSIDE);
		wizard.addToUiControllerReverseRelation(targetRoleSingularProdRelevantField,
				IRelation.PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE);
		wizard.addToUiControllerReverseRelation(targetRolePluralProdRelevantField,
				IRelation.PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE);
        
        // Connect the extension controls to the ui controller
        if (wizard.getUiControllerReverseRelation() != null && wizard.getExtensionFactoryReverseRelation() != null)
            wizard.getExtensionFactoryReverseRelation().connectToModel(wizard.getUiControllerReverseRelation());
	}

    private void createPropertyFields(){
        mainComposite.setRedraw(false);
        wizard.createExtensionFactoryReverseRelation();
        createPropertyGroup(wizard.getUiToolkit());
        super.createPropertiesFields(wizard.getUiToolkit(), propertiesGroup);
        propertiesGroup.pack();
        propertiesGroup.getParent().pack();
        mainComposite.pack();
        
        mainComposite.getParent().layout(true);
        mainComposite.getParent().getParent().layout(true);
        mainComposite.setRedraw(true);
    }
    
    /*
     * Resets the previous state of this page
     */
    void reset(){
		prevIsExisting = false;
		prevIsNew = false;
    }
    
    /**
	 * {@inheritDoc}
	 */
	protected boolean updateControlStatus() {
	    // show the existing relation drop down only if the existing relation
	    // radio button was chosen on the previous page
		if (wizard.isExistingReverseRelation()) {
			// if selection of target or type changes reinitialize existing relation control
			if (!(prevTarget.equals(wizard.getRelation().getTarget()) &&
				  prevRelType.equals(wizard.getRelation().getRelationType()) &&
				  prevIsExisting)){

                prevIsExisting = true;
				prevIsNew = false;
				prevTarget = wizard.getRelation().getTarget();
				prevRelType = wizard.getRelation().getRelationType();
				
                setVisibleExistingRelationDropDown(true);

				wizard.restoreMementoTargetBeforeChange();
				wizard.storeMementoTargetBeforeChange();
				try {
                    // get all existing relations which matches as reverse for the new relation
                    existingRelations = getCorrespondingTargetRelations(
							wizard.getRelation(), wizard.getTargetPolicyCmptType());
					if (existingRelations.size() > 0) {
						String[] names = new String[existingRelations.size()];
						for (int i = 0; i < existingRelations.size(); i++) {
							names[i] = (((IRelation) existingRelations.get(i)).getName());
						}
						existingRelationsField.getCombo().setItems(names);
					} else {
						existingRelationsField.getCombo().setItems(
								new String[0]);
					}
                    // by default select the first relation
                    if (existingRelations.size() > 0){
                        existingRelationsField.getCombo().select(0);
                    }
				} catch (CoreException e) {
					IpsPlugin.log(e);
					wizard.showErrorPage(e);
					return false;
				}
			}
		} else if (wizard.isNewReverseRelation()){
			if (! prevIsNew ){
                prevIsExisting = false;
    			prevIsNew = true;
                prevRelType = wizard.getRelation().getRelationType();
                
    			setVisibleExistingRelationDropDown(false);
    			
    			// create a new reverse relation
    			wizard.restoreMementoTargetBeforeChange();
    			wizard.storeMementoTargetBeforeChange();
    			try {
					createNewReverseRelation();
				} catch (CoreException e) {
					IpsPlugin.log(e);
					wizard.showErrorPage(e);
					return false;
				}
    
                createPropertyFields();
                setStatusPropertyFields();
                
                connectRelationToUi();
            } else if (prevRelType != null && ! prevRelType.equals(wizard.getRelation().getRelationType())){
                prevRelType = wizard.getRelation().getRelationType();
                setStatusPropertyFields();
            }
        } else {
			prevIsExisting = false;
			prevIsNew = false;
		}
		return true;
	}

	/**
	 * Set the status of the property fields.
	 */
	private void setStatusPropertyFields() {
		if (wizard.getReverseRelation() == null) {
			removePcTypeControlsFromModel();
			removeProdRelevantControlsFromModel();
		} else {
			super.updateControlStatus();
			// disable all controls in case of existing relation
			if (wizard.isExistingReverseRelation())
				setEnabledAllPropertyControls(false);			
		}
	}

	/**
	 * Sets the visibility for the existing relation control.
	 */
	private void setVisibleExistingRelationDropDown(boolean visible) {
		existingRelationsField.getControl().setVisible(visible);
		existingRelationsLabel.getControl().setVisible(visible);
	}

	/**
	 * Event function to indicate a change of the existing relation.
	 */
	private void existingRelationSelectionChanged() {
		int selIdx = existingRelationsField.getCombo().getSelectionIndex();
		if (selIdx>=0){
			String selExistingRelation = existingRelationsField.getCombo().getItem(selIdx);
			if (!selExistingRelation.equals(prevSelExistingRelation)){
				prevSelExistingRelation = selExistingRelation;

				wizard.storeReverseRelation((IRelation) existingRelations.get(selIdx));
                
                createPropertyFields();
                setStatusPropertyFields();
                
                // connect to ui
				// add a new ui controler for the existing relation object
				wizard.createUIControllerReverseRelation(wizard.getReverseRelation());
				wizard.getUiControllerReverseRelation().updateUI();
			}
		}
	}

	/*
	 * Create a new reverse relation, i.e. create a new relation on the target policy component type object.
	 */
	private void createNewReverseRelation() throws CoreException {
		if (wizard.getTargetPolicyCmptType()==null){
			return;
        }
        
		IRelation newReverseRelation = wizard.getTargetPolicyCmptType().newRelation();
		newReverseRelation.setTarget(wizard.getPolicyCmptTypeQualifiedName());
		newReverseRelation.setTargetRoleSingular(wizard.getRelation().getPolicyCmptType().getName());
		newReverseRelation.setRelationType(NewPcTypeRelationWizard.getCorrespondingRelationType(wizard.getRelation().getRelationType()));
		IRelation containerRelation = wizard.getRelation().findContainerRelation();
		if (containerRelation != null){
			newReverseRelation.setContainerRelation(containerRelation.getReverseRelation());
		}
		if (wizard.getRelation().isReadOnlyContainer()){
			newReverseRelation.setReadOnlyContainer(true);
        }
        
		wizard.setDefaultsByRelationType(newReverseRelation);
		wizard.storeReverseRelation(newReverseRelation);
	}
	
    /*
     * Connects the relation object with the ui controller.
     */
    private void connectRelationToUi(){
        // add a new ui controler for the new relation object
        wizard.createUIControllerReverseRelation(wizard.getReverseRelation());
        wizard.getUiControllerReverseRelation().updateUI();

        wizard.updateDescriptionReverseRelationPropertiesPage(Messages.NewPcTypeRelationWizard_reverseRelationProp_description_new);
    }
    
	/**
	 * Remove the properties controls from the model.
	 */
	protected void removePcTypeControlsFromModel() {
		if (minCardinalityField  == null)
            // return because fields currently not created
            return;
        
		minCardinalityField.setText(""); //$NON-NLS-1$
		maxCardinalityField.setText(""); //$NON-NLS-1$
		targetRoleSingularField.setText(""); //$NON-NLS-1$
		targetRolePluralField.setText(""); //$NON-NLS-1$
		
		minCardinalityField.setMessages(null);
		maxCardinalityField.setMessages(null);
		targetRoleSingularField.setMessages(null);
		targetRolePluralField.setMessages(null);	
		
		wizard.removeFromUiControllerReverseRelation(minCardinalityField);
		wizard.removeFromUiControllerReverseRelation(maxCardinalityField);
		wizard.removeFromUiControllerReverseRelation(targetRoleSingularField);
		wizard.removeFromUiControllerReverseRelation(targetRolePluralField);
	}
	
	/**
	 * Remove the product relevant property controls from the model.
	 * And reset the state of the controls.
	 */
	protected void removeProdRelevantControlsFromModel() {
		productRelevantField.setValue(Boolean.FALSE);
		minCardinalityProdRelevantField.setText(""); //$NON-NLS-1$
		maxCardinalityProdRelevantField.setText(""); //$NON-NLS-1$
		targetRoleSingularProdRelevantField.setText(""); //$NON-NLS-1$
		targetRolePluralProdRelevantField.setText(""); //$NON-NLS-1$
		
		productRelevantField.setMessages(null);
		minCardinalityProdRelevantField.setMessages(null);
		maxCardinalityProdRelevantField.setMessages(null);
		targetRoleSingularProdRelevantField.setMessages(null);
		targetRolePluralProdRelevantField.setMessages(null);
		
		wizard.removeFromUiControllerReverseRelation(productRelevantField);
		wizard.removeFromUiControllerReverseRelation(minCardinalityProdRelevantField);
		wizard.removeFromUiControllerReverseRelation(maxCardinalityProdRelevantField);
		wizard.removeFromUiControllerReverseRelation(targetRoleSingularProdRelevantField);
		wizard.removeFromUiControllerReverseRelation(targetRolePluralProdRelevantField);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected boolean isPageVisible() {
		return (wizard.isExistingReverseRelation() || wizard.isNewReverseRelation());
	}

    /**
	 * {@inheritDoc}
	 */
	protected IRelation getCurrentRelation(){
		return wizard.getReverseRelation();
	}

    /**
	 * {@inheritDoc}
	 */
	protected IRelation getReverseOfCurrentRelation(){
	    return wizard.getRelation();
	}

    /**
     * {@inheritDoc}
     */
    protected IPolicyCmptType getCurrentTarget(){
        return wizard.getRelation().getPolicyCmptType();
    }
    
    protected void createPropertiesFields(UIToolkit uiToolkit, Composite c) {
        // nothing to to, the property fields will be created if a reverse relation exists
    }
    
    /**
     * {@inheritDoc}
     */
    protected void createExtensionFields(Composite parent, UIToolkit uiToolkit, String position) {
        wizard.getExtensionFactoryReverseRelation().createControls(parent, uiToolkit,
                (IIpsObjectPartContainer)getCurrentRelation(), position);
    }
    
    /**
	 * {@inheritDoc}
	 */
	protected void addFocusListenerUpdateButtons(EditField field) {
		// no extra focus listener needed
	}	
}

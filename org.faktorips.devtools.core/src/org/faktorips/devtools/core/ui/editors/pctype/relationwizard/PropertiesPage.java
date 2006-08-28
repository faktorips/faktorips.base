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
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;

/**
 * Relation wizard page to define the relation properties and optional the product relevant properties.
 */
public class PropertiesPage extends AbstractPropertiesPage {
	private static final String PAGE_ID = "Properties"; //$NON-NLS-1$
	
	public PropertiesPage(NewPcTypeRelationWizard newPcTypeRelationWizard) {
		super(PAGE_ID, Messages.NewPcTypeRelationWizard_properties_title,
				Messages.NewPcTypeRelationWizard_properties_description,
				newPcTypeRelationWizard);
	}

    /**
     * {@inheritDoc}
     */
    protected void createControls(Composite parent) {
        super.createControls(parent);
        // create spacer on bottom, to ensure the correct size of the reverse property page
        // the reverse property page contains the same controlls like this page plus
        // three edit fields on the top of the page, therefore the spacer must fit the additionall size
        // this is necessary because the reverse properties fiels will be created later if a reverse
        // relation is selected or created but the wizard calculates the max size when the dialog is created
        UIToolkit uiToolkit = wizard.getUiToolkit();
        uiToolkit.createVerticalSpacer(parent, 90);
    }
    
	/**
	 * {@inheritDoc}
	 */
	protected void connectToModel(){
		wizard.addToUiControllerRelation(minCardinalityField, IRelation.PROPERTY_MIN_CARDINALITY);     
		wizard.addToUiControllerRelation(maxCardinalityField, IRelation.PROPERTY_MAX_CARDINALITY);     
		wizard.addToUiControllerRelation(targetRoleSingularField, IRelation.PROPERTY_TARGET_ROLE_SINGULAR);
		wizard.addToUiControllerRelation(targetRolePluralField, IRelation.PROPERTY_TARGET_ROLE_PLURAL);
        
		wizard.addToUiControllerRelation(productRelevantField, IRelation.PROPERTY_PRODUCT_RELEVANT);
        
		wizard.addToUiControllerRelation(minCardinalityProdRelevantField, IRelation.PROPERTY_MIN_CARDINALITY_PRODUCTSIDE);
		wizard.addToUiControllerRelation(maxCardinalityProdRelevantField, IRelation.PROPERTY_MAX_CARDINALITY_PRODUCTSIDE); 	
		wizard.addToUiControllerRelation(targetRoleSingularProdRelevantField, IRelation.PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE);
		wizard.addToUiControllerRelation(targetRolePluralProdRelevantField, IRelation.PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE);
		
		// add listener on model, 
		//   if the model changed check if the next button could be enabled if the relation is valid
		wizard.getRelation().getIpsModel().addChangeListener(new ContentsChangeListener(){
			public void contentsChanged(ContentChangeEvent event) {
				if (!wizard.isReverseRelationPageDisplayed()){
					// check only until the next page wasn't displayed
					if (event.getIpsSrcFile().equals(wizard.getRelation().getIpsObject().getIpsSrcFile())){
						if (getContainer() != null && getContainer().getCurrentPage() != null)
							getContainer().updateButtons();
					}
				}
			}	
		});
        
        // Connect the extension controls to the ui controller
        if (wizard.getUiControllerRelation() != null)
            wizard.getExtensionFactory().connectToModel(wizard.getUiControllerRelation());        
	}

	/**
	 * Returns <code>true</code> if the next button could be enabled.
	 * The next button is only enabled if the relation is valid or
	 * the next page after this page was displayed.
	 * {@inheritDoc}
	 */
	public boolean canFlipToNextPage() {
		boolean canFlipToNextPage = super.canFlipToNextPage();
		if (canFlipToNextPage){
			try {
				canFlipToNextPage = (wizard.isReverseRelationPageDisplayed() || wizard.getRelation().isValid());
			} catch (CoreException e) {
				IpsPlugin.log(e);
				wizard.showErrorPage(e);
			}
		}
		if (canFlipToNextPage) 
			wizard.setReverseRelationPageDisplayed(true);
		return canFlipToNextPage;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected IRelation getCurrentRelation(){
		return wizard.getRelation();
	}

	/**
	 * {@inheritDoc}
	 */
	protected IRelation getReverseOfCurrentRelation(){
	    return wizard.getReverseRelation();
	}

    /**
     * {@inheritDoc}
     */
    protected IPolicyCmptType getCurrentTarget(){
        return wizard.getTargetPolicyCmptType();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void createExtensionFields(Composite parent, UIToolkit uiToolkit, String position) {
        wizard.getExtensionFactory().createControls(parent, uiToolkit, (IpsObjectPartContainer)getCurrentRelation(),
                position);
    }


	/**
	 * {@inheritDoc}
	 */
	protected void addFocusListenerUpdateButtons(EditField field) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected boolean isPageVisible(){
		return true;
	}	
}

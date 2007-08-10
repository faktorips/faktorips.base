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
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * Relation wizard class.
 */
public class NewPcTypeRelationWizard extends Wizard implements ContentsChangeListener, IPageChangedListener{
	private final static int NEW_REVERSE_RELATION = 0;
	private final static int USE_EXISTING_REVERSE_RELATION = 1;
	private final static int NONE_REVERSE_RELATION = 2;
	
	/* UI controllers */
	private IpsObjectUIController uiControllerRelation;
	private IpsObjectUIController uiControllerReverseRelation;

	private UIToolkit uiToolkit = new UIToolkit(null);

	/* Model objects */
	private IRelation relation;
	private IRelation reverseRelation;
	private IPolicyCmptType targetPolicyCmptType;
    
	/* State variables */
	private Memento mementoTargetBeforeNewRelation;
	
	// stores if a new an existing or none reverse relation will be created/used on the target
	private int reverseRelationManipulation = NONE_REVERSE_RELATION;
	
	// stores if the chosen target was dirty before manipulated by this wizard
	private boolean targetIsDirty;
	
	// True if the next pages after the property page was displayed.
	// The first time next can only be clicked if the relation is valid,
	// but aftwerwards the next button can be used every time, because
	// maybe the validation error is on a page after the property page
	private boolean reverseRelationPageDisplayed = false;
	
	private boolean isError = false;
	
	/* Wizard pages */
	private ReverseRelationPropertiesPage reverseRelationPropertiesPage;
	private ErrorPage errorPage;
	private List pages = new ArrayList();
	
	/* Factory to created the extension edit fields */
	private ExtensionPropertyControlFactory extensionFactory;
    private ExtensionPropertyControlFactory extensionFactoryReverseRelation;
    
    /* Variables to check the page changed event */
    private boolean pageChanged = false;
	private IWizardPage prevPage;
	
    /**
     * Returns the corresponding relation type.<br>
     *  in: ASSOZIATION => out: ASSOZIATION<br>
     *  in: COMPOSITION => out: REVERSE_COMPOSITION<br>
     *  in: REVERSE_COMPOSITION => out: COMPOSITION<br>
     */
    public static RelationType getCorrespondingRelationType(RelationType sourceRelType){
        return sourceRelType == null                ? null :
               sourceRelType.isAssoziation()        ? RelationType.ASSOCIATION :
               sourceRelType.isCompositionDetailToMaster() ? RelationType.COMPOSITION_MASTER_TO_DETAIL :
               sourceRelType.isCompositionMasterToDetail()        ? RelationType.COMPOSITION_DETAIL_TO_MASTER : null;
    }
    
    public NewPcTypeRelationWizard(IRelation relation) {
    	super();

    	ArgumentCheck.notNull(relation);
		super.setWindowTitle(Messages.NewPcTypeRelationWizard_title);
		this.relation = relation;

		uiControllerRelation = createUIController(this.relation);
        extensionFactory = new ExtensionPropertyControlFactory(relation.getClass());
        
    	// add listener on model,
		// if the model changed check if the new relation has to be updated
		relation.getIpsModel().addChangeListener(this);  
	}
    
    /*
     * Update the new reverse relation and resets the container if necessary.
     */
    private void updateNewReverseRelationAndRelationContainer(ContentChangeEvent event){
		if (event.getIpsSrcFile().equals(
				relation.getIpsObject().getIpsSrcFile())) {

			if (relation.isReadOnlyContainer()){
				relation.setContainerRelation(""); //$NON-NLS-1$
			}
			
			if (isNewReverseRelation() && reverseRelation != null){
				reverseRelation.setTarget(relation.getPolicyCmptType().getQualifiedName());
				reverseRelation.setTargetRoleSingular(relation.getPolicyCmptType().getName());
				reverseRelation.setRelationType(getCorrespondingRelationType(relation.getRelationType()));
				if (reverseRelation.isAssoziation()) {
                    reverseRelation.setReadOnlyContainer(relation.isReadOnlyContainer());
                } else {
                    reverseRelation.setReadOnlyContainer(false);
                }
				
				IRelation containerRelation;
				try {
					containerRelation = relation.findContainerRelation();
				} catch (CoreException e) {
					IpsPlugin.log(e);
					showErrorPage(e);
					return;
				}
				if (containerRelation != null){
					reverseRelation.setContainerRelation(containerRelation.getInverseRelation());
				}else{
					reverseRelation.setContainerRelation(""); //$NON-NLS-1$
				}
			}
		}
    }
    
    public void createExtensionFactoryReverseRelation(){
        extensionFactoryReverseRelation = new ExtensionPropertyControlFactory(relation.getClass());
    }
	/**
	 * {@inheritDoc}
	 */
	public final void addPages() {
		try {
			WizardPage relationTargetPage = new RelationTargetPage(this);
			WizardPage containerRelationPagePage = new ContainerRelationPage(
					this);
			WizardPage propertiesPage = new PropertiesPage(this);
			WizardPage reverseRelationPage = new ReverseRelationPage(this);
			reverseRelationPropertiesPage = new ReverseRelationPropertiesPage(
					this);
			errorPage = new ErrorPage(this);
			
			pages.add(relationTargetPage);
			addPage(relationTargetPage);

			pages.add(containerRelationPagePage);
			addPage(containerRelationPagePage);

			pages.add(propertiesPage);
			addPage(propertiesPage);

			pages.add(reverseRelationPage);
			addPage(reverseRelationPage);

			pages.add(reverseRelationPropertiesPage);
			addPage(reverseRelationPropertiesPage);
			
			pages.add(errorPage);
			addPage(errorPage);	
            
            // add listener for page changes
            if (getContainer() instanceof WizardDialog){
                ((WizardDialog)getContainer()).addPageChangedListener(this);
            }
		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		AbstractPcTypeRelationWizardPage nextPage = null;

		if (isError) {
			// in case of an error no next page will be displayed
			return null;
		}

		int index = pages.indexOf(page);
		if (index == pages.size() - 1 || index == -1) {
			// last page or page not found
			return null;
		}
		nextPage = (AbstractPcTypeRelationWizardPage) pages.get(index + 1);
		while (!nextPage.isPageVisible()) {
			index++;
			if (index == pages.size() - 1)
				// last page
				return null;
			nextPage = (AbstractPcTypeRelationWizardPage) pages.get(index + 1);
		}
		
		return (IWizardPage) nextPage;
	}
    
	/**
	 * Checks if a page has changed and set the boolean which indicates this event to <code>true</code>.
	 */
	public void checkAndStorePageChangeEvent(){
		if (getContainer() != null){
			if(prevPage != null && prevPage != getContainer().getCurrentPage()){
				pageChanged = true;
			}
			prevPage = getContainer().getCurrentPage();
		}
	}
	
	/**
	 * Sets the focus to the given control if the page has changed.
	 */
    public void setFocusIfPageChanged(final Control control){
    	if (pageChanged){
    		Runnable r = new Runnable() {
    	        public void run() {
    	        	if (getContainer().getShell().getDisplay().isDisposed())
    	        		return;
    	    		control.setFocus();
    	    		pageChanged = false;
    	        }};
    	        
    		getContainer().getShell().getDisplay().syncExec(r);
    	}
    }
    
	/**
	 * Save changes and check if the source file of the target is dirty,
	 * if the source file of the target is dirty then a dialog is shown to ask for automatically saving.
	 * 
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		try {
            relation.getIpsModel().removeChangeListener(this);
			boolean saveTargetAutomatically = false;
			if (targetPolicyCmptType != null && 
					! targetPolicyCmptType.getIpsSrcFile().equals(relation.getIpsObject().getIpsSrcFile())){
				// there is a target selected and the target is not the object this new relation belongs to
				if (targetIsDirty){
					// target policy component type was dirty before editing by the wizard,
					//   ask to save automatically
					String msg = Messages.NewPcTypeRelationWizard_target_askForAutomaticallySaving;
					saveTargetAutomatically = MessageDialog.openQuestion(getShell(),
							Messages.NewPcTypeRelationWizard_target_askForAutomaticallySavingTitle, msg);
				}else{
					// target policy component type is not dirty, therefore save the changed on the target
					saveTargetAutomatically = true;
				}
			}
			
			// Note: if the target policy component type source file is open in an other editor,
			// the first changes will be refreshed correctly in the open editor 
			// after automatically saving; remark: the refresh works only if the refresh functionality is enabled for alle editor
			// views (not only the active one) this feature is currently not enabled.
			// But after this saving, the content (the currently displayed object) within this target editor 
			// will be detached from the model, because the editor control holds a copy of the object
			// and after saving the object, the cache for this object will be destroyed 
			// and therefore the copy will not be updated any more.
			// The consequence is that all further background changes on this object will not be displayed in the target editor.
			// Only if the target editor will be reopened then the current object is displayed correctly.
			if (saveTargetAutomatically) {
				targetPolicyCmptType.getIpsSrcFile().save(true, null);
			}
		} catch (CoreException e) {
			IpsPlugin.log(e);
			showErrorPage(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
    public boolean performCancel() {
    	relation.getIpsModel().removeChangeListener(this);
    	restoreMementoTargetBeforeChange();    	
    	return true;
    }
	
	/**
	 * Set the default values depending on the relation type and read-only container flag.
	 */
	void setDefaultsByRelationTypeAndTarget(IRelation newRelation){
		RelationType type = newRelation.getRelationType();
		if (type != null) {
            boolean targetIsProductRelevantOrNull = getTargetPolicyCmptType()==null?true:getTargetPolicyCmptType().isConfigurableByProductCmptType();
			boolean defaultProductRelevant = newRelation.getPolicyCmptType().isConfigurableByProductCmptType() && targetIsProductRelevantOrNull;
            if (type.isCompositionMasterToDetail()) {
				newRelation.setMaxCardinality(Integer.MAX_VALUE);
                newRelation.setProductRelevant(defaultProductRelevant);
			} else if (type.isCompositionDetailToMaster()) {
				newRelation.setMinCardinality(1);
				newRelation.setMaxCardinality(1);
				newRelation.setTargetRolePluralProductSide(""); //$NON-NLS-1$
				newRelation.setTargetRoleSingularProductSide(""); //$NON-NLS-1$
				newRelation.setProductRelevant(false);
			} else if (type.isAssoziation()) {
				newRelation.setContainerRelation(""); //$NON-NLS-1$
				newRelation.setReadOnlyContainer(false);
				if (newRelation.isReadOnlyContainer()){
					newRelation.setMaxCardinality(Integer.MAX_VALUE);
				}
				newRelation.setProductRelevant(defaultProductRelevant);
			}
		}
	}
	
	/**
	 * Return the ui toolkit.
	 */
	UIToolkit getUiToolkit() {
		return uiToolkit;
	}
	
	/**
	 * Add an edit field to the relation ui controller linked by the given property.
	 * If the ui controller not exists yet nothing will be added.
	 */
	void addToUiControllerRelation(EditField edit, String propertyName) {
		if (uiControllerRelation != null)
			uiControllerRelation.add(edit, propertyName);
	}

	/**
	 * Add an edit field to the reverse relation ui controller linked by the given property.
	 * If the ui controller not exists yet nothing will be added
	 */
	void addToUiControllerReverseRelation(EditField edit, String propertyName) {
		if (uiControllerReverseRelation != null)
			uiControllerReverseRelation.add(edit, propertyName);
	}
	
	/**
	 * Removes an edit field from the reverse relation ui controller linked by the given property.
	 * If the ui controller not exists yet nothing will be added
	 */
	void removeFromUiControllerReverseRelation(EditField edit) {
		if (uiControllerReverseRelation != null)
			uiControllerReverseRelation.remove(edit);
	}

	/**
	 * Stores a new memento  for the target relation.
	 */
	void storeMementoTargetBeforeChange(){
		if (targetPolicyCmptType==null)
			return;
		
		mementoTargetBeforeNewRelation = targetPolicyCmptType.newMemento();
	}
	
	/**
	 * Restores the target relation memento.
	 */	
	void restoreMementoTargetBeforeChange(){
		if (targetPolicyCmptType==null || mementoTargetBeforeNewRelation == null)
			return;
		targetPolicyCmptType.setState(mementoTargetBeforeNewRelation);
        if (!targetIsDirty)
            // if the target wasn't dirty before the discard the changes
            targetPolicyCmptType.getIpsSrcFile().discardChanges();
		mementoTargetBeforeNewRelation = null;
	}
	
	/**
	 * Create a new ui controller for a reverse relation.
	 */
	void createUIControllerReverseRelation(IRelation newReverseRelation){
		uiControllerReverseRelation = createUIController(newReverseRelation);
		reverseRelationPropertiesPage.connectToModel();
	}
		
	/**
	 * Udates the description on the reverse relation property page.
	 */
	void updateDescriptionReverseRelationPropertiesPage(String description){
		reverseRelationPropertiesPage.setDescription(description);
	}
	
	/**
	 * Returns the relation object ui controller.
	 * Or null if the controller not exists.
	 */
    IpsObjectUIController getUiControllerRelation() {
		return uiControllerRelation;
	}
	
	/**
	 * Returns the relation object ui controller.
	 * Or null if the controller not exists.
	 */
    IpsObjectUIController getUiControllerReverseRelation() {
		return uiControllerReverseRelation;
	}
	
	/**
	 * Returns the relation.
	 */
	IRelation getRelation() {
		return relation;
	}

	/**
	 * Returns the reverse relation.
	 */
	IRelation getInverseRelation() {
		return reverseRelation;
	}
	
	/**
	 * Stores a reverse relation.
	 * Additional the correct reverse relation names will be set in both relation.
	 */
	void storeInverseRelation(IRelation reverseRelation){
	    this.reverseRelation = reverseRelation;
		if (reverseRelation != null && reverseRelation.isAssoziation()){
			relation.setInverseRelation(reverseRelation.getTargetRoleSingular());
            reverseRelation.setInverseRelation(relation.getTargetRoleSingular());
		}else{
			relation.setInverseRelation(""); //$NON-NLS-1$
		}
	}
	
    /**
	 * Returns the qualified name of the currently editing policy component
	 * type.
	 */
    String getPolicyCmptTypeQualifiedName() {
		return relation.getPolicyCmptType().getQualifiedName();
	}

	/**
	 * Returns the policy component type of the target.
	 */
	IPolicyCmptType getTargetPolicyCmptType() {
		return targetPolicyCmptType;
	}

	/**
	 * Stores the policy component type object of the target.
	 * And check if the target source file is dirty to ask the
	 * user about the automatically saving when finishing the wizard.
	 */
	void storeTargetPolicyCmptType(IPolicyCmptType targetPolicyCmptType) {
		this.targetPolicyCmptType = targetPolicyCmptType;
		if (targetPolicyCmptType != null)
			targetIsDirty = targetPolicyCmptType.getIpsSrcFile().isDirty();
	}
	
	/**
	 * Returns true if the reverse relation is an existing relation on the target.
	 */
	boolean isExistingReverseRelation() {
		return reverseRelationManipulation == USE_EXISTING_REVERSE_RELATION;
	}	
	
	/**
	 * Returns true if the reverse relation is new relation on the target.
	 */
	boolean isNewReverseRelation() {
		return reverseRelationManipulation == NEW_REVERSE_RELATION;
	}

    /**
     * Returns true if none reverse relation should be defined.
     */
    boolean isNoneReverseRelation() {
        return reverseRelationManipulation == NONE_REVERSE_RELATION;
    }
    
	/**
	 * Sets that the revese relation is an existing relation on the target.
	 */
	void setExistingReverseRelation() {
		reverseRelationManipulation = USE_EXISTING_REVERSE_RELATION;
	}	
	
	/**
	 * Sets that the revese relation is a new relation on the target.
	 */
	void setNewReverseRelation() {
		reverseRelationManipulation = NEW_REVERSE_RELATION;
	}	
	
	/**
	 * Sets that the revese relation will not defined by using this wizard.
	 */
	void setNoneReverseRelation() {
		reverseRelationManipulation = NONE_REVERSE_RELATION;
	}	
	
	/** 
	 * Creates a new ui controller for the given object.
	 */
	private IpsObjectUIController createUIController(IIpsObjectPart part) {
        IpsObjectUIController controller = new IpsObjectUIController(part) {
			public void valueChanged(FieldValueChangedEvent e) {
				try {
					super.valueChanged(e);
				} catch (Exception ex) {
					IpsPlugin.logAndShowErrorDialog(ex);
				}
			}
		};
		return controller;
	}

	/**
	 * True if the reverse property page was displayed.
	 */
	public boolean isReverseRelationPageDisplayed() {
		return reverseRelationPageDisplayed;
	}

	/**
	 * Sets if the reverse property page was displayed.
	 */
	public void setReverseRelationPageDisplayed(boolean isNextPageDisplayed) {
		this.reverseRelationPageDisplayed = isNextPageDisplayed;
	}

	/**
	 * Shows the error page.
	 */
	public void showErrorPage(Exception e) {
		isError = true;
		errorPage.storeErrorDetails(e.getLocalizedMessage());
		getContainer().showPage(errorPage);
		getContainer().updateButtons();
	}

	/**
     * Returns <code>true</code> if the relation or reverse relation contains a validation error.
     * Otherwise <code>false</code>.
     */
    boolean isValidationError() {
        try {
            return ((relation == null ? false
                    : relation.validate().containsErrorMsg()) || (reverseRelation == null ? false : reverseRelation
                            .validate().containsErrorMsg()));
        } catch (CoreException e) {
            showErrorPage(e);
            return false;
        }
    }

	/**
	 * Returns <code>true</code> if there was an error (e.g. Exception an occured). Note: validation errors
     * should be checked by using @see @link #isValidationError()}
	 */
	public boolean isError() {
		return isError;
	}

    /**
     * Returns the extension property control factory.
     */
    public ExtensionPropertyControlFactory getExtensionFactory() {
        return extensionFactory;
    }
    
    /**
     * Returns the extension property control factory for the reverse relation.
     */
    public ExtensionPropertyControlFactory getExtensionFactoryReverseRelation() {
        return extensionFactoryReverseRelation;
    }
    
    /**
     * Return <code>true</code> if the selected target pc type has relations which can be selected as reverse relation.
     * Return <code>false</code> if no such relations exists.
     */
     boolean relationsExists() {
        try {
            return (ReverseRelationPropertiesPage.getCorrespondingTargetRelations(getRelation(),
                    getTargetPolicyCmptType()).size() > 0);
        } catch (Exception e) {
        }
        return false;
    }
    
	/**
	 * Resets the reverse relation property page, thus if the page is displayed
	 * again the objects will be new initialized
	 */
    void resetReverseRelationPropertiesPage(){
    	reverseRelationPropertiesPage.reset();
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        if (event.isAffected(relation) || event.isAffected(reverseRelation)) {
            updateNewReverseRelationAndRelationContainer(event);
            // update the button enable state (e.g. finish and next)
            if (getContainer() != null){
                getContainer().updateButtons();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void pageChanged(PageChangedEvent event) {
        // update the button enable state (e.g. finish and next)
        if (getContainer() != null){
            getContainer().updateButtons();
        }
    }
}

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

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.editors.pctype.relationwizard.Messages;
import org.faktorips.devtools.core.ui.editors.pctype.relationwizard.NewPcTypeRelationWizard;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class NewPcTypeAssociationWizard extends Wizard implements ContentsChangeListener {
    private final static int NEW_REVERSE_RELATION = 0;
    private final static int USE_EXISTING_REVERSE_RELATION = 1;
    private final static int NONE_REVERSE_RELATION = 2;
    
    private boolean isError;
    
    protected BindingContext bindingContext = new BindingContext();;
    private UIToolkit toolkit = new UIToolkit(null);
    private ExtensionPropertyControlFactory extFactoryAssociation;
    private ExtensionPropertyControlFactory extFactoryInverseAssociation;
    
    private IIpsProject ipsProject;
    private IPolicyCmptTypeAssociation association;
    private IPolicyCmptTypeAssociation inverseAssociation;
    private IAssociation productCmptTypeAssociation;
    private IPolicyCmptType targetPolicyCmptType;

    // states of changes ips objects
    private Memento mementoTargetBeforeNewRelation;
    private Memento mementoProductCmptTypeBeforeRelation;
    private boolean targetIsDirty;
    private boolean productCmptTypeIsDirty;

    private List pages = new ArrayList();
    private DerivedUnionPage derivedUnionPage;
    private PropertyPage propertyPage;
    private InverseRelationPropertyPage inverseRelationPropertyPage;
    private ErrorPage errorPage;

    private int reverseRelationManipulation = NEW_REVERSE_RELATION;
    private int previousreverseRelationManipulation = NONE_REVERSE_RELATION;
    private String previousTarget;
    private AssociationType previousTargetRelationType;
    
    public NewPcTypeAssociationWizard(IPolicyCmptTypeAssociation association) {
        super.setWindowTitle("New association");
        
        this.association = association;
        this.ipsProject = association.getIpsProject();
        
        this.extFactoryAssociation = new ExtensionPropertyControlFactory(association.getClass());
        this.extFactoryInverseAssociation = new ExtensionPropertyControlFactory(association.getClass());
        
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public void addPages() {
        derivedUnionPage = new DerivedUnionPage(this, association, toolkit, bindingContext);
        propertyPage = new PropertyPage(this, association, toolkit, bindingContext);
        inverseRelationPropertyPage = new InverseRelationPropertyPage(this, toolkit, bindingContext);
        errorPage = new ErrorPage(toolkit);

        addPage(new AssociationTargetPage(this, association, toolkit, bindingContext));
        addPage(derivedUnionPage);
        addPage(propertyPage);
        addPage(new InverseRelationPage(this, toolkit));
        addPage(inverseRelationPropertyPage);
        addPage(errorPage);
    }

    /**
     * {@inheritDoc}
     */
    public void addPage(IWizardPage page) {
        pages.add(page);
        super.addPage(page);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
        bindingContext.dispose();
        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        IWizardPage currentPage = getContainer().getCurrentPage();
        IProductCmptType productCmptType = findProductCmptType();
        if  (! (event.isAffected(association) || 
                event.isAffected(inverseAssociation) ||
                productCmptType == null ? true : event.isAffected(productCmptType)) || 
                currentPage == null){
            return;
        }
        contentsChanged(currentPage);
    }

    public void contentsChanged(IWizardPage currentPage){
        if (!association.getTarget().equals(previousTarget)){
            previousTarget = association.getTarget();
            try {
                storeTargetPolicyCmptType((IPolicyCmptType)association.findTarget(ipsProject));
                if (targetPolicyCmptType != null){
                    derivedUnionPage.fillComboWithDerivedUnionCandidates(association.findDerivedUnionCandidates(ipsProject));
                }
            } catch (CoreException e) {
                showAndLogError(e);
                return;
            }
        }
        if (currentPage instanceof IBlockedValidationWizardPage) {
            isValidPage((IBlockedValidationWizardPage)currentPage, true);
        }
        getContainer().updateButtons();
    }

    /*
     * Evaluates the valifation state of the given page
     */
    public boolean isValidPage(IBlockedValidationWizardPage page, boolean updatePageState) {
        boolean valid = true;
        IPolicyCmptTypeAssociation association = null;
        if (page instanceof InverseRelationPropertyPage){
            association = inverseAssociation;
        } else {
            association = this.association;
        }
        if (association == null){
            return true;
        }
        
        MessageList list;
        try {
            list = association.validate();
            if (productCmptTypeAssociation != null){
                MessageList list2 = productCmptTypeAssociation.validate();
                list.add(list2);
            }
        } catch (CoreException e) {
            showAndLogError(e);
            return false;
        }
        for (Iterator iter = page.getProperties().iterator(); iter.hasNext();) {
            String prop = (String)iter.next();
            MessageList messagesFor = list.getMessagesFor(association, prop);
            if (messagesFor.containsErrorMsg()) {
                page.setErrorMessage(messagesFor.getFirstMessage(Message.ERROR).getText());
                valid = false;
                break;
            }
        }
        if (valid && productCmptTypeAssociation != null){
            for (Iterator iter = page.getProperties().iterator(); iter.hasNext();) {
                String prop = (String)iter.next();
                MessageList messagesFor = list.getMessagesFor(productCmptTypeAssociation, prop);
                if (messagesFor.containsErrorMsg()) {
                    page.setErrorMessage(messagesFor.getFirstMessage(Message.ERROR).getText());
                    valid = false;
                    break;
                }
            }
        }
        if (updatePageState){
            page.setPageComplete(valid);
        }
        return valid;
    }

    /**
     * {@inheritDoc}
     */
    public IWizardPage getNextPage(IWizardPage page) {
        if (isError) {
            // in case of an error no next page will be displayed
            return null;
        }

        if (page == inverseRelationPropertyPage){
            updateInverseAssociationSelectionState();
        }
        
        int index = pages.indexOf(page);
        if (index == pages.size() - 1 || index == -1) {
            // last page or page not found
            return null;
        }

        IWizardPage nextPage = (IWizardPage)pages.get(index + 1);
        while (!isPageVisible(nextPage)) {
            index++;
            if (index == pages.size() - 1) {
                // last page
                return null;
            }
            nextPage = (IWizardPage) pages.get(index + 1);
        }
        return (IWizardPage) nextPage;
    }

    /**
     * Returns the visibility of the given page.
     * 
     * @param page the page to check the visibility
     * @return <code>true</code> if the given page is visible, <code>false</code> if the page should be hidden
     */
    private boolean isPageVisible(IWizardPage page) {
        boolean visible = false;
        if (page instanceof DerivedUnionPage) {
            ((DerivedUnionPage)page).isPageVisible();
        } else if (page instanceof ErrorPage) {
            visible = false;
        } else if (page instanceof InverseRelationPropertyPage) {
            ((WizardPage)getContainer().getCurrentPage()).setMessage(null);
            ((InverseRelationPropertyPage)page).isPageVisible();
            if (isExistingReverseRelation()){
                ((WizardPage)getContainer().getCurrentPage()).setMessage("No relation wich could be used as inverse releation found on target policy component.");
            }
        } else {
            visible = true;
        }
        // if page is not visible set page complete to enable finishing of the wizard
        if (!visible && page instanceof IBlockedValidationWizardPage) {
            ((IBlockedValidationWizardPage)page).setPageComplete(true);
        }
        return visible;
    }

    /**
     * @return Returns the targetPolicyCmptType.
     */
    public IPolicyCmptType getTargetPolicyCmptType() {
        return targetPolicyCmptType;
    }
    
    /*
     * 
     */
    private void updateInverseAssociationSelectionState() {
        // show the existing relation drop down only if the existing relation
        // radio button was chosen on the previous page
        boolean selectionChanged = false;

        // if selection changed restore last state
        if (previousreverseRelationManipulation != reverseRelationManipulation){
            previousreverseRelationManipulation = reverseRelationManipulation;
            restoreMementoTargetBeforeChange();
            storeMementoTargetBeforeChange();
            storeInverseRelation(null);
            selectionChanged = true;
        }

        if (isExistingReverseRelation()
                && (selectionChanged || !(association.getTarget() == previousTarget && previousTargetRelationType == association
                        .getAssociationType()))) {
            previousTarget = association.getTarget();
            previousTargetRelationType = association.getAssociationType();

            inverseRelationPropertyPage.setDescription("Select an existing association");
            
            inverseRelationPropertyPage.setShowExistingRelationDropDown(true);
            try {
                // get all existing relations that matches as reverse for the new relation
                List existingRelations = getCorrespondingTargetRelations(association, targetPolicyCmptType);
                if (existingRelations.size() > 0) {
                    String[] names = new String[existingRelations.size()];
                    for (int i = 0; i < existingRelations.size(); i++) {
                        names[i] = (((IPolicyCmptTypeAssociation)existingRelations.get(i)).getName());
                    }
                    inverseRelationPropertyPage.setExistingAssociations(names);
                } else {
                    inverseRelationPropertyPage.setExistingAssociations(new String[0]);
                }
                inverseRelationPropertyPage.refreshControls();
            } catch (CoreException e) {
                showAndLogError(e);
            }
        } else if (isNewReverseRelation() && selectionChanged) {
            inverseRelationPropertyPage.setDescription("Define new inverse relation");
            
            inverseRelationPropertyPage.setShowExistingRelationDropDown(false);
            // create a new reverse relation
            storeInverseRelation(null);
            try {
                createNewInverseRelation();
            } catch (CoreException e) {
                IpsPlugin.log(e);
                showAndLogError(e);
            }
            inverseRelationPropertyPage.refreshControls();
        }
        previousreverseRelationManipulation = reverseRelationManipulation;
    }

    /*
     * Create a new reverse relation, i.e. create a new relation on the target policy component type
     * object.
     */
    private void createNewInverseRelation() throws CoreException {
        if (targetPolicyCmptType==null){
            return;
        }
        
        IPolicyCmptTypeAssociation newReverseRelation = targetPolicyCmptType.newPolicyCmptTypeAssociation();
        newReverseRelation.setTarget(association.getPolicyCmptType().getQualifiedName());
        newReverseRelation.setTargetRoleSingular(association.getPolicyCmptType().getName());
        newReverseRelation.setAssociationType(NewPcTypeAssociationWizard.getCorrespondingRelationType(association.getAssociationType()));
        IPolicyCmptTypeAssociation containerRelation = (IPolicyCmptTypeAssociation)association.findSubsettedDerivedUnion(ipsProject);
        if (newReverseRelation.isAssoziation() && containerRelation != null){
            newReverseRelation.setSubsettedDerivedUnion(containerRelation.getInverseRelation());
        }
        if (association.isAssoziation() && association.isDerivedUnion()){
            newReverseRelation.setDerivedUnion(true);
        }
        
        setDefaultsByRelationTypeAndTarget(newReverseRelation);
        storeInverseRelation(newReverseRelation);
    }

    /**
     * Set the default values depending on the relation type and read-only container flag.
     */
    void setDefaultsByRelationTypeAndTarget(IPolicyCmptTypeAssociation newRelation){
        AssociationType type = newRelation.getAssociationType();
        if (type != null) {
            boolean targetIsProductRelevantOrNull = targetPolicyCmptType==null?true:targetPolicyCmptType.isConfigurableByProductCmptType();
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
                newRelation.setDerivedUnion(false);
            } else if (type.isAssoziation()) {
                newRelation.setSubsettedDerivedUnion(""); //$NON-NLS-1$
                newRelation.setDerivedUnion(false);
                if (newRelation.isDerivedUnion()){
                    newRelation.setMaxCardinality(Integer.MAX_VALUE);
                }
                newRelation.setProductRelevant(defaultProductRelevant);
            }
        }
    }

    /**
     * Returns the corresponding relation type.<br>
     *  in: ASSOZIATION => out: ASSOZIATION<br>
     *  in: COMPOSITION => out: REVERSE_COMPOSITION<br>
     *  in: REVERSE_COMPOSITION => out: COMPOSITION<br>
     */
    public static AssociationType getCorrespondingRelationType(AssociationType sourceRelType){
        return sourceRelType == null                ? null :
               sourceRelType.isAssoziation()        ? AssociationType.ASSOCIATION :
               sourceRelType.isCompositionDetailToMaster() ? AssociationType.COMPOSITION_MASTER_TO_DETAIL :
               sourceRelType.isCompositionMasterToDetail()        ? AssociationType.COMPOSITION_DETAIL_TO_MASTER : null;
    }
    
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
     public static List getCorrespondingTargetRelations(IPolicyCmptTypeAssociation sourceRelation,
            IPolicyCmptType target) throws CoreException {
        ArrayList relationsOfTarget = new ArrayList();
        IPolicyCmptType currTargetPolicyCmptType = target;
        while (currTargetPolicyCmptType != null){
            IPolicyCmptTypeAssociation[] relations = currTargetPolicyCmptType.getPolicyCmptTypeAssociations();
            for (int i = 0; i < relations.length; i++) {
                // add the relation of the target if it points to the source policy cmpt
                // and the type is matching to the source relation
                if (relations[i].getTarget().equals(
                        sourceRelation.getPolicyCmptType().getQualifiedName())
                        && relations[i].getAssociationType() == NewPcTypeRelationWizard.getCorrespondingRelationType(sourceRelation
                                .getAssociationType())) {
                    relationsOfTarget.add(relations[i]);
                }
            }
            currTargetPolicyCmptType = currTargetPolicyCmptType.findSupertype();
        }
        return relationsOfTarget;
    }

    /**
     * @return Returns the extFactoryAssociation.
     */
    public ExtensionPropertyControlFactory getExtFactoryAssociation() {
        return extFactoryAssociation;
    }

    /**
     * @return Returns the extFactoryInverseAssociation.
     */
    public ExtensionPropertyControlFactory getExtFactoryInverseAssociation() {
        return extFactoryInverseAssociation;
    }

    /**
     * {@inheritDoc}
     */
    public boolean performFinish() {
        try {
            updateInverseAssociationSelectionState();
            
            boolean saveTargetAutomatically = false;
            if (targetPolicyCmptType != null && 
                    ! targetPolicyCmptType.getIpsSrcFile().equals(association.getIpsObject().getIpsSrcFile())){
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
            
            if (saveTargetAutomatically) {
                targetPolicyCmptType.getIpsSrcFile().save(true, null);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
            showAndLogError(e);
            return false;
        }
        return true;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public boolean performCancel() {
        restoreMementoTargetBeforeChange();
        restoreMementoProductCmptTypeBeforeChange();
        return true;
    }

    /**
     * Stores the policy component type object of the target.
     * And check if the target source file is dirty to ask the
     * user about the automatically saving when finishing the wizard.
     */
    void storeTargetPolicyCmptType(IPolicyCmptType targetPolicyCmptType) {
        this.targetPolicyCmptType = targetPolicyCmptType;
        if (targetPolicyCmptType != null) {
            targetIsDirty = targetPolicyCmptType.getIpsSrcFile().isDirty();
        }
    }
    
    /**
     * Stores memento of target policy cmpt before change.
     */
    void storeMementoTargetBeforeChange(){
        if (targetPolicyCmptType==null){
            return;
        }
        
        mementoTargetBeforeNewRelation = targetPolicyCmptType.newMemento();
    }
    
    public void storeInverseRelation(IPolicyCmptTypeAssociation inverseAssociation) {
        this.inverseAssociation = inverseAssociation;
        if (inverseAssociation != null && inverseAssociation.isAssoziation()){
            association.setInverseRelation(inverseAssociation.getTargetRoleSingular());
            inverseAssociation.setInverseRelation(association.getTargetRoleSingular());
        }else{
            association.setInverseRelation(""); //$NON-NLS-1$
        }        
        inverseRelationPropertyPage.setAssociation(inverseAssociation);
    }

    public void storeExistingInverseRelation(String inverseAssociation) {
        IPolicyCmptTypeAssociation[] policyCmptTypeAssociations = targetPolicyCmptType.getPolicyCmptTypeAssociations();
        for (int i = 0; i < policyCmptTypeAssociations.length; i++) {
            if (policyCmptTypeAssociations[i].getName().equals(inverseAssociation)) {
                storeInverseRelation(policyCmptTypeAssociations[i]);
                return;
            }
        }
        // error not found
        IpsStatus errorStatus = new IpsStatus("Error assosiation " + inverseAssociation
                + " not found in policy cmpt type " + targetPolicyCmptType);
        IpsPlugin.log(errorStatus);
        showAndLogError(new CoreException(errorStatus));
    }
    
    public void restoreMementoTargetBeforeChange() {
        if (targetPolicyCmptType==null || mementoTargetBeforeNewRelation == null){
            return;
        }
        targetPolicyCmptType.setState(mementoTargetBeforeNewRelation);
        if (!targetIsDirty) {
            // if the target wasn't dirty before the discard the changes
            targetPolicyCmptType.getIpsSrcFile().discardChanges();
        }
        mementoTargetBeforeNewRelation = null;
    }

    /**
     * Stores memento of product cmpt type before change.
     */
    void storeMementoProductCmptTypeBeforeChange(){
        if (targetPolicyCmptType==null){
            return;
        }
        
        IProductCmptType productCmptType = findProductCmptType();
        if (productCmptType == null){
            return;
        }
        mementoProductCmptTypeBeforeRelation = productCmptType.newMemento();
    }

    public IProductCmptType findProductCmptType() {
        IProductCmptType productCmptType = null;
        try {
            productCmptType = association.getPolicyCmptType().findProductCmptType(ipsProject);
        } catch (CoreException e) {
            showAndLogError(e);
        }
        return productCmptType;
    }
    
    public void restoreMementoProductCmptTypeBeforeChange() {
        productCmptTypeAssociation = null;
        if (targetPolicyCmptType==null || mementoProductCmptTypeBeforeRelation == null){
            return;
        }
        IProductCmptType productCmptType = findProductCmptType();
        if (productCmptType == null){
            return;
        }
        productCmptType.setState(mementoProductCmptTypeBeforeRelation);
        if (!productCmptTypeIsDirty) {
            // if the target wasn't dirty before the discard the changes
            productCmptType.getIpsSrcFile().discardChanges();
        }
        mementoProductCmptTypeBeforeRelation = null;
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

    public void inverseRelationOperationHasChanged() {
        contentsChanged(getContainer().getCurrentPage());
    }

    /**
     * Creates a new association in the product cmpt type.
     */
    public void newProductCmptTypeAssociation() {
        IProductCmptType  productCmptType = findProductCmptType();
        if (productCmptType == null){
            return;
        }
        productCmptTypeAssociation = productCmptType.newAssociation();
        IProductCmptType productCmptTypeTarget;
        try {
            productCmptTypeTarget = targetPolicyCmptType.findProductCmptType(ipsProject);
            productCmptTypeAssociation.setTarget(productCmptTypeTarget.getQualifiedName());
            propertyPage.setProductCmptTypeAssociation(productCmptTypeAssociation);
        } catch (Exception e) {
            showAndLogError(e);
        }
    }

    /**
     * Swith to the error page and log the given exception.
     */
    public void showAndLogError(Exception e) {
        IpsPlugin.log(e);
        isError = true;
        errorPage.storeErrorDetails(e.getLocalizedMessage());
        getContainer().showPage(errorPage);
        getContainer().updateButtons();
    }
    
}

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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class NewPcTypeAssociationWizard extends Wizard implements ContentsChangeListener {
    final static int NEW_REVERSE_RELATION = 0;
    final static int USE_EXISTING_REVERSE_RELATION = 1;
    final static int NONE_REVERSE_RELATION = 2;
    
    private UIToolkit toolkit = new UIToolkit(null);

    // indicates that there was an error
    private boolean isError;
    
    protected BindingContext bindingContext = new BindingContext();;
    private ExtensionPropertyControlFactory extFactoryAssociation;
    private ExtensionPropertyControlFactory extFactoryInverseAssociation;
    private ExtensionPropertyControlFactory extFactoryProductCmptTypeAssociation;
    
    // model objects
    private IIpsProject ipsProject;
    
    private IPolicyCmptTypeAssociation association;
    private IPolicyCmptTypeAssociation inverseAssociation;

    private IPolicyCmptType targetPolicyCmptType;
    
    private IAssociation productCmptTypeAssociation;


    // model objects states
    private Memento mementoTargetBeforeNewRelation;
    private Memento mementoProductCmptTypeBeforeRelation;
    private boolean targetIsDirty;
    private boolean productCmptTypeIsDirty;

    // wizard pages
    private List pages = new ArrayList();
    private PropertyPage propertyPage;
    private InverseRelationPage inverseRelationPage;
    private InverseRelationPropertyPage inverseRelationPropertyPage;
    private ConfigureProductCmptTypePage configureProductCmptTypePage;
    private ConfProdCmptTypePropertyPage confProdCmptTypePropertyPage;
    private ErrorPage errorPage;

    // stores page selections
    private int inverseAssociationManipulation = NEW_REVERSE_RELATION;
    private int previousreverseRelationManipulation = NONE_REVERSE_RELATION;
    private boolean configureProductCmptType = true;
    private boolean prevConfigureProductCmptType = false;
    private String previousTarget;
    private AssociationType previousTargetAssociationType;

    // pages which will be hidden if the type of the association is detail to master
    private List detailToMasterHiddenPages = new ArrayList(10);
    
    public NewPcTypeAssociationWizard(IPolicyCmptTypeAssociation association) {
        super.setWindowTitle("New association");
        
        this.association = association;
        this.ipsProject = association.getIpsProject();
        this.extFactoryAssociation = new ExtensionPropertyControlFactory(association.getClass());
        this.extFactoryInverseAssociation = new ExtensionPropertyControlFactory(association.getClass());
        this.extFactoryProductCmptTypeAssociation = new ExtensionPropertyControlFactory(association.getClass());

        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public void addPages() {
        propertyPage = new PropertyPage(this, association, toolkit, bindingContext);
        inverseRelationPage = new InverseRelationPage(this, toolkit);
        inverseRelationPropertyPage = new InverseRelationPropertyPage(this, toolkit, bindingContext);
        configureProductCmptTypePage = new ConfigureProductCmptTypePage(this, toolkit);
        confProdCmptTypePropertyPage = new ConfProdCmptTypePropertyPage(this, toolkit, bindingContext);
        errorPage = new ErrorPage(toolkit);

        addPage(new AssociationTargetPage(this, association, toolkit, bindingContext));
        addPage(propertyPage);
        addPage(inverseRelationPage);
        addPage(inverseRelationPropertyPage);
        addPage(configureProductCmptTypePage);
        addPage(confProdCmptTypePropertyPage);
        addPage(errorPage);
        
        detailToMasterHiddenPages.add(inverseRelationPage);
        detailToMasterHiddenPages.add(inverseRelationPropertyPage);
        detailToMasterHiddenPages.add(configureProductCmptTypePage);
        detailToMasterHiddenPages.add(confProdCmptTypePropertyPage);
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

    /**
     * Indicates that there was a change on the given page
     */
    public void contentsChanged(IWizardPage currentPage){
        if (!association.getTarget().equals(previousTarget)){
            // target has changed
            // find new derived union candidates
            previousTarget = association.getTarget();
            try {
                storeTargetPolicyCmptType((IPolicyCmptType)association.findTarget(ipsProject));
            } catch (CoreException e) {
                showAndLogError(e);
                return;
            }
        }
        
        // handle validate state of current page
        if (currentPage instanceof IBlockedValidationWizardPage) {
            handleValidationOfPage((IBlockedValidationWizardPage)currentPage);
        }
        
        // handle validate state of next page
        IWizardPage nextPage = getNextWizardPage(currentPage);
        if (nextPage instanceof IBlockedValidationWizardPage) {
            handleValidationOfPage((IBlockedValidationWizardPage)nextPage);
        }
        
        getContainer().updateButtons();
    }

    /*
     * Validates the given page and updates the message and page complete state. If the given page
     * contains a validation error then show an error message and sets the page completion to
     * <code>false</code> .
     */
    private void handleValidationOfPage(IBlockedValidationWizardPage page) {
        page.setPageComplete(validatePageAndDisplayError(page));
    }

    /**
     * Returns <code>true</code> if the page can flip to the next page. Only if the given page is
     * valid then the user can flip to the next page. If there is no next page then this method
     * returns always <code>null</code>. If there is a validation error then the error will be
     * displayed in the message area of the given page.
     */
    public boolean canPageFlipToNextPage(IBlockedValidationWizardPage page){
        page.setErrorMessage(null);

        boolean valid = validatePageAndDisplayError(page);

        if (page.getNextPage() == null){
            return false;
        }
        
        
        return valid;
    }
    
    /*
     * Validates the given page
     */
    private boolean validatePageAndDisplayError(IBlockedValidationWizardPage page) {
        boolean valid = true;
        Validatable association = getAssociationFor(page);
        if (association == null){
            return true;
        }
        
        if (!isValidationDisabledFor(page)){
            MessageList list;
            try {
                list = association.validate();
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
        }
        
        return valid;
    }

    /*
     * Check if the validation for the given page is disabled.
     */
    private boolean isValidationDisabledFor(IBlockedValidationWizardPage page) {
        if (page instanceof ConfProdCmptTypePropertyPage && !configureProductCmptType){
            return true;
        } else if (page instanceof InverseRelationPropertyPage && inverseAssociationManipulation == NONE_REVERSE_RELATION){
            return true;
        }
        return false;
    }

    /*
     * Returns the association which could be edit by the give page
     */
    private Validatable getAssociationFor(IBlockedValidationWizardPage page) {
        if (page instanceof InverseRelationPropertyPage){
            return inverseAssociation;
        } else if (page instanceof PropertyPage || page instanceof AssociationTargetPage){
            return association;
        } else if (page instanceof ConfProdCmptTypePropertyPage){
            return productCmptTypeAssociation;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IWizardPage getNextPage(IWizardPage page) {
        // in case of an error no next page will be displayed
        if (isError) {
            return null;
        }
        
        // resets the special case message "no existing inverse relation"
        if (page instanceof InverseRelationPage){
            ((WizardPage)getContainer().getCurrentPage()).setMessage(null);
        }
        
        // if there is an error on the current page then to no next page could be switched
        if (page instanceof IBlockedValidationWizardPage){
            if (!validatePageAndDisplayError((IBlockedValidationWizardPage)page)){
                return null;
            }
        }

        // check if this is the last page
        int index = pages.indexOf(page);
        if (index == -1) {
            return null;
        }

        // get the next page
        IWizardPage nextPage = (IWizardPage)pages.get(index + 1);
        
        // if all next next pages are invisible then no next page is available
        while (!isPageVisible(nextPage)) {
            // if page is not visible set page complete to enable finishing of the wizard
            if (nextPage instanceof IBlockedValidationWizardPage) {
                ((IBlockedValidationWizardPage)nextPage).setPageComplete(true);
            }
            
            index++;
            if (index == pages.size() - 1) {
                // last page
                return null;
            }
            
            // display information in special case "no existing inverse relation"
            //   if an existing inverse relation should be used but no corresponding association exists
            if (nextPage instanceof InverseRelationPropertyPage) {
                ((WizardPage)getContainer().getCurrentPage()).setMessage(null);
                if (isExistingReverseRelation()) {
                    ((WizardPage)getContainer().getCurrentPage())
                            .setMessage(
                                    "No association wich could be used as inverse releation found. The inverse association property page will be skipped.",
                                    IMessageProvider.WARNING);
                }
            }
            
            nextPage = (IWizardPage) pages.get(index + 1);
        }
        
        return (IWizardPage) nextPage;
    }
    
    private IWizardPage getNextWizardPage(IWizardPage page){
        int index = pages.indexOf(page);
        if (index == pages.size() - 1 || index == -1) {
            // last page or page not found
            return null;
        }
        return (IWizardPage)pages.get(index + 1);
    }

    /*
     * Returns the visibility of the given page.
     * 
     * @param page the page to check the visibility state
     * 
     * @return <code>true</code> if the given page is visible, <code>false</code> if the page should be hidden
     */
    private boolean isPageVisible(IWizardPage page) {
        // if a detail to master association should be created then hide all pages which are not necessary
        if (isDetailToMasterAssociation() && detailToMasterHiddenPages.contains(page)) {
            return false;
        } 
        
        // if the given page supports the visible check then check the visible state of the page
        if (page instanceof IHiddenWizardPage) {
            return ((IHiddenWizardPage)page).isPageVisible();
        } 
        
        return true;
    }
    
    /**
     * Returns all existing associations on the target which could be used as inverse associaton,
     * of the association created by this wizard.
     */
    public List getExistingInverseAssociationCandidates() {
        try {
            return NewPcTypeAssociationWizard.getCorrespondingTargetAssociations(getAssociation(), targetPolicyCmptType);
        } catch (CoreException e) {
            showAndLogError(e);
        };
        return null;
    }
    
    /**
     * Check if the selection of the configure product component type (yes/no) has changes and
     * reset the state and optional create the product component type association. 
     */
    void handleConfProdCmptTypeSelectionState() {
        if (prevConfigureProductCmptType != configureProductCmptType){
            prevConfigureProductCmptType = configureProductCmptType;
            restoreMementoProductCmptTypeBeforeChange();
            if (configureProductCmptType){
                storeMementoProductCmptTypeBeforeChange();
                createNewProductCmptTypeAssociation();
            }
        }
    }
    
    /**
     * Check if the selection of the inverse association (none/use existing/create new) has changes and
     * reset the state and optional create a new inverse association. 
     */
    void handleInverseAssociationSelectionState() {
        // show the existing relation drop down only if the existing relation
        // radio button was chosen on the previous page

        // if selection changed restore last state
        if (previousreverseRelationManipulation != inverseAssociationManipulation){
            previousreverseRelationManipulation = inverseAssociationManipulation;
            restoreMementoTargetBeforeChange();
            storeMementoTargetBeforeChange();
            storeInverseRelation(null);
        } else {
            return;
        }

        if (isExistingReverseRelation()
                && (!(association.getTarget() == previousTarget && previousTargetAssociationType == association
                        .getAssociationType()))) {
            previousTarget = association.getTarget();
            previousTargetAssociationType = association.getAssociationType();

            inverseRelationPropertyPage.setDescription("Select an existing association");
            
            inverseRelationPropertyPage.setShowExistingRelationDropDown(true);
            try {
                // get all existing relations that matches as reverse for the new relation
                List existingRelations = getCorrespondingTargetAssociations(association, targetPolicyCmptType);
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
        } else if (isNewReverseRelation()) {
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
        } 
        
        inverseRelationPropertyPage.refreshControls();
    }

    /*
     * Create a new reverse relation, i.e. create a new relation on the target policy component type
     * object.
     */
    private void createNewInverseRelation() throws CoreException {
        if (targetPolicyCmptType==null){
            return;
        }
        
        IPolicyCmptTypeAssociation newInverseAssociation = association.newInverseAssociation();
        storeInverseRelation(newInverseAssociation);
        setDefaultsByRelationTypeAndTarget(newInverseAssociation);
    }

    /*
     * Creates a new association on the product component type.
     */
    private void createNewProductCmptTypeAssociation() {
        IProductCmptType  productCmptType = findProductCmptType();
        if (productCmptType == null){
            return;
        }
        productCmptTypeIsDirty = productCmptType.getIpsSrcFile().isDirty();
        productCmptTypeAssociation = productCmptType.newAssociation();
        IProductCmptType productCmptTypeTarget;
        try {
            productCmptTypeTarget = targetPolicyCmptType.findProductCmptType(ipsProject);
            productCmptTypeAssociation.setTarget(productCmptTypeTarget.getQualifiedName());
            confProdCmptTypePropertyPage.setProductCmptTypeAssociationAndUpdatePage((IProductCmptTypeAssociation)productCmptTypeAssociation);
        } catch (Exception e) {
            showAndLogError(e);
        }
    }
    
    /**
     * Set the default values depending on the relation type and read-only container flag.
     * FIXME Joerg: in model aufnehmen?
     */
    void setDefaultsByRelationTypeAndTarget(IPolicyCmptTypeAssociation newRelation){
        AssociationType type = newRelation.getAssociationType();
        if (type != null) {
            if (type.isCompositionMasterToDetail()) {
                newRelation.setMaxCardinality(Integer.MAX_VALUE);
            } else if (type.isCompositionDetailToMaster()) {
                newRelation.setMinCardinality(1);
                newRelation.setMaxCardinality(1);
                newRelation.setDerivedUnion(false);
            } else if (type.isAssoziation()) {
                newRelation.setSubsettedDerivedUnion(""); //$NON-NLS-1$
                newRelation.setDerivedUnion(false);
                if (newRelation.isDerivedUnion()){
                    newRelation.setMaxCardinality(Integer.MAX_VALUE);
                }
            }
        }
    }

    /**
     * Returns association from the target if:<br>
     * <ul>
     * <li>the target of the target relation points to the source (policy component type of the given sourceAssociation)
     * <li>the target association type is the corresponding relation type of the source (Assoziation=Assoziation, Composition=>ReverseComp, ReverseComp=>Compostion)
     * </ul>
     * If no relation is found on the target then an empty (not null) ArrayList
     * is returned.
     * 
     * @throws CoreException
     */
     public static List getCorrespondingTargetAssociations(IPolicyCmptTypeAssociation sourceAssociation,
            IPolicyCmptType target) throws CoreException {
        String source = sourceAssociation.getPolicyCmptType().getQualifiedName();
        AssociationType correspondingAssociationType = sourceAssociation.getCorrespondingAssociationType();
        IAssociation[] associations = target.findAssociationsForTargetAndAssociationType(source,
                correspondingAssociationType, sourceAssociation.getIpsProject());
        return Arrays.asList(associations);
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
     * @return Returns the extFactoryInverseAssociation.
     */
    public ExtensionPropertyControlFactory getExtFactoryProductCmptTypeAssociation() {
        return extFactoryProductCmptTypeAssociation;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean performFinish() {
        try {
            // check and perform the last state change of the radio selection pages
            handleInverseAssociationSelectionState();
            handleConfProdCmptTypeSelectionState();
            
            boolean saveTargetAutomatically = false;
            if (targetPolicyCmptType != null && 
                    ! targetPolicyCmptType.getIpsSrcFile().equals(association.getIpsObject().getIpsSrcFile())){
                // there is a target selected and the target is not the object this new relation belongs to
                if (targetIsDirty){
                    // target policy component type was dirty before editing by the wizard,
                    //   ask to save automatically
                    String msg = "The chosen target is currently not saved, should the target automatically saved?";
                    saveTargetAutomatically = MessageDialog.openQuestion(getShell(),
                            "Save target source file", msg);
                }else{
                    // target policy component type is not dirty, therefore save the changed on the target
                    saveTargetAutomatically = true;
                }
            }
            
            if (saveTargetAutomatically) {
                targetPolicyCmptType.getIpsSrcFile().save(true, null);
            }
            
            boolean saveProductCmptTypeAutomatically = true;
            if (productCmptTypeAssociation != null) {
                if (productCmptTypeIsDirty) {
                    // target policy component type was dirty before editing by the wizard,
                    // ask to save automatically
                    String msg = "The corresponding product component type is currently not saved, should the product component type automatically saved?";
                    saveProductCmptTypeAutomatically = MessageDialog.openQuestion(getShell(),
                            "Save product component type source file", msg);
                }
                if (saveProductCmptTypeAutomatically) {
                    productCmptTypeAssociation.getIpsSrcFile().save(true, null);
                }
            }
        } catch (CoreException e) {
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
     * Stores memento of target policy component before change.
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
            association.setInverseAssociation(inverseAssociation.getTargetRoleSingular());
            inverseAssociation.setInverseAssociation(association.getTargetRoleSingular());
        }else{
            association.setInverseAssociation(""); //$NON-NLS-1$
        }        
        inverseRelationPropertyPage.setAssociationAndUpdatePage(inverseAssociation);
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
                + " not found in policy component type " + targetPolicyCmptType);
        showAndLogError(new CoreException(errorStatus));
    }
    
    /**
     * Stores memento of product component type before change.
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
            // if the product component type wasn't dirty before mark as clean
            productCmptType.getIpsSrcFile().markAsClean();
        }
        mementoProductCmptTypeBeforeRelation = null;
    }
    
    public void restoreMementoTargetBeforeChange() {
        inverseAssociation = null;
        if (targetPolicyCmptType==null || mementoTargetBeforeNewRelation == null){
            return;
        }
        targetPolicyCmptType.setState(mementoTargetBeforeNewRelation);
        if (!targetIsDirty) {
            // if the target wasn't dirty before mark as clean
            targetPolicyCmptType.getIpsSrcFile().markAsClean();
        }
        mementoTargetBeforeNewRelation = null;
    }
    
    /**
     * Returns true if the reverse relation is an existing relation on the target.
     */
    boolean isExistingReverseRelation() {
        return inverseAssociationManipulation == USE_EXISTING_REVERSE_RELATION;
    }   
    
    /**
     * Returns true if the reverse relation is new relation on the target.
     */
    boolean isNewReverseRelation() {
        return inverseAssociationManipulation == NEW_REVERSE_RELATION;
    }

    /**
     * Returns true if none reverse relation should be defined.
     */
    boolean isNoneReverseRelation() {
        return inverseAssociationManipulation == NONE_REVERSE_RELATION;
    }
    
    /**
     * Sets that the revese relation is an existing relation on the target.
     */
    void setInverseAssociationManipulation(int type) {
        inverseAssociationManipulation = type;
    }

    public void pageHasChanged() {
        contentsChanged(getContainer().getCurrentPage());
    }

    public boolean isProductCmptTypeAvailable(){
        if (targetPolicyCmptType == null){
            return false;
        }
        boolean confByProdCmptTypeEnabled = findProductCmptType() != null;
        if (confByProdCmptTypeEnabled){
            IProductCmptType productCmptTypeTarget;
            try {
                productCmptTypeTarget = targetPolicyCmptType.findProductCmptType(ipsProject);
            } catch (CoreException e) {
                showAndLogError(e);
                return false;
            }
            if (productCmptTypeTarget != null){
                return true;
            }
        }
        return false;
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

    public void setConfigureProductCmptType(boolean configureProductCmptType) {
        this.configureProductCmptType = configureProductCmptType; 
    }

    /**
     * @return Returns the configureProductCmptType.
     */
    public boolean isConfigureProductCmptType() {
        return configureProductCmptType;
    }
    
    public boolean isDetailToMasterAssociation(){
        return association.getAssociationType().equals(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
    }

    /**
     * @return Returns the association.
     */
    public IPolicyCmptTypeAssociation getAssociation() {
        return association;
    }
    
    public IIpsProject getIpsProject() {
        return association.getIpsProject();
    }
    
    /**
     * Creates a descripton control.
     */
    public Text createDescriptionText(Composite parent, int span){
        toolkit.createVerticalSpacer(parent, 20);
        
        Composite composite = toolkit.createComposite(parent);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan =  span;
        composite.setLayoutData(gridData);
        composite.setLayout(new GridLayout());
        
        toolkit.createFormLabel(composite, "Description");
        Text description = toolkit.createMultilineText(composite);
        
        bindingContext.bindContent(description, association, IProductCmptTypeAssociation.PROPERTY_DESCRIPTION);
        return description;
    }
    
    /**
     * Creates a composite for the wizard pages with default margins.
     */
    public Composite createPageComposite(Composite parent) {
        Composite composite = toolkit.createGridComposite(parent, 1, false, true);
        ((GridLayout)composite.getLayout()).marginHeight = 12;
        ((GridLayout)composite.getLayout()).marginWidth = 5;
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return composite;
    }
}

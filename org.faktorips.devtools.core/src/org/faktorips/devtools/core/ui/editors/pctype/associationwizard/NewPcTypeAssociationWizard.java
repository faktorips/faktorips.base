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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
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
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.util.QNameUtil;
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
    
    // model objects
    private IIpsProject ipsProject;
    private IPolicyCmptTypeAssociation association;
    private PmoAssociation pmoAssociation;
    private IPolicyCmptTypeAssociation inverseAssociation;
    private IAssociation productCmptTypeAssociation;
    private IPolicyCmptType targetPolicyCmptType;

    // model objects states
    private Memento mementoTargetBeforeNewRelation;
    private Memento mementoProductCmptTypeBeforeRelation;
    private boolean targetIsDirty;
    private boolean productCmptTypeIsDirty;

    // wizard pages
    private List pages = new ArrayList();
    private DerivedUnionPage derivedUnionPage;
    private PropertyPage propertyPage;
    private InverseRelationPage inverseRelationPage;
    private InverseRelationPropertyPage inverseRelationPropertyPage;
    private ConfigureProductCmptTypePage configureProductCmptTypePage;
    private ConfProdCmptTypePropertyPage confProdCmptTypePropertyPage;
    private ErrorPage errorPage;

    // stores page selections
    private int inverseAssociationManipulation = NONE_REVERSE_RELATION;
    private int previousreverseRelationManipulation = NONE_REVERSE_RELATION;
    private boolean configureProductCmptType;
    private boolean prevConfigureProductCmptType = false;
    private String previousTarget;
    private AssociationType previousTargetRelationType;

    // pages which will be hidden if the type of the association is detail to master
    private List detailToMasterHiddenPages = new ArrayList(10);
    
    public NewPcTypeAssociationWizard(IPolicyCmptTypeAssociation association) {
        super.setWindowTitle("New association");
        
        this.association = association;
        this.ipsProject = association.getIpsProject();
        
        this.extFactoryAssociation = new ExtensionPropertyControlFactory(association.getClass());
        this.extFactoryInverseAssociation = new ExtensionPropertyControlFactory(association.getClass());

        pmoAssociation = new PmoAssociation(association);
        
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public void addPages() {
        derivedUnionPage = new DerivedUnionPage(this, association, toolkit, bindingContext);
        propertyPage = new PropertyPage(this, association, toolkit, bindingContext);
        inverseRelationPage = new InverseRelationPage(this, toolkit);
        inverseRelationPropertyPage = new InverseRelationPropertyPage(this, toolkit, bindingContext);
        configureProductCmptTypePage = new ConfigureProductCmptTypePage(this, toolkit);
        confProdCmptTypePropertyPage = new ConfProdCmptTypePropertyPage(this, toolkit, bindingContext);
        errorPage = new ErrorPage(toolkit);

        addPage(new AssociationTargetPage(this, association, toolkit, bindingContext));
        addPage(derivedUnionPage);
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
        IWizardPage nextPage = getNextWizardPage(currentPage);
        if (nextPage instanceof IBlockedValidationWizardPage) {
            isValidPage((IBlockedValidationWizardPage)nextPage, true);
        }
        
        getContainer().updateButtons();
    }

    /*
     * Evaluates the valifation state of the given page
     */
    public boolean isValidPage(IBlockedValidationWizardPage page, boolean updatePageState) {
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
        
        if (updatePageState){
            page.setPageComplete(valid);
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
        if (isError) {
            // in case of an error no next page will be displayed
            return null;
        }

        if (page instanceof IBlockedValidationWizardPage){
            if (!isValidPage((IBlockedValidationWizardPage)page, false)){
                return null;
            }
        }
        
        int index = pages.indexOf(page);
        if (index == pages.size() - 1 || index == -1) {
            // last page or page not found
            return null;
        }

        IWizardPage nextPage = (IWizardPage)pages.get(index + 1);
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
     * @param page the page to check the visibility
     * 
     * @return <code>true</code> if the given page is visible, <code>false</code> if the page should be hidden
     */
    private boolean isPageVisible(IWizardPage page) {
        boolean visible = false;

        if (isDetailToMasterAssociation() && detailToMasterHiddenPages.contains(page)) {
            // hide page if detail to master association should be created and page is only for master detail associations
            visible = false;
        } else {
            // hide hidden pages
            if (page instanceof IHiddenWizardPage) {
                visible = ((IHiddenWizardPage)page).isPageVisible();
            } else {
                visible = true;
            }
        }
        
        // display information in special case
        //   if an existing inverse relation should be used but no association exists
        if (page instanceof InverseRelationPropertyPage) {
            ((WizardPage)getContainer().getCurrentPage()).setMessage(null);
            if (!visible && isExistingReverseRelation()) {
                ((WizardPage)getContainer().getCurrentPage())
                        .setMessage("No relation wich could be used as inverse releation found on target policy component.");
            }
        }
        
        return visible;
    }

    /**
     * @return Returns the targetPolicyCmptType.
     */
    public IPolicyCmptType getTargetPolicyCmptType() {
        return targetPolicyCmptType;
    }
    
    /**
     * Check if the selection of the configure product cmpt type (yes/no) has changes and
     * reset the state and optional create the product cmpt type association. 
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
        boolean selectionChanged = false;

        // if selection changed restore last state
        if (previousreverseRelationManipulation != inverseAssociationManipulation){
            previousreverseRelationManipulation = inverseAssociationManipulation;
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
        previousreverseRelationManipulation = inverseAssociationManipulation;
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

    /*
     * Creates a new association on the product cmpt type.
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
            confProdCmptTypePropertyPage.setProductCmptTypeAssociation((IProductCmptTypeAssociation)productCmptTypeAssociation);
        } catch (Exception e) {
            showAndLogError(e);
        }
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
                        && relations[i].getAssociationType() == NewPcTypeAssociationWizard.getCorrespondingRelationType(sourceRelation
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
        showAndLogError(new CoreException(errorStatus));
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
            // if the product cmpt type wasn't dirty before mark as clean
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
        IPolicyCmptType targetPolicyCmptType = getTargetPolicyCmptType();
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
    
    public Text createDescriptionText(Composite parent){
        toolkit.createVerticalSpacer(parent, 20);
        Composite composite = toolkit.createGridComposite(parent, 1, true, false);
        GridData gd = (GridData)composite.getLayoutData();
        gd.horizontalSpan =  2;
        composite.setLayoutData(gd);
        toolkit.createFormLabel(composite, "Description");
        Composite editComposite = toolkit.createGridComposite(composite, 1, true, false);
        Text description = toolkit.createMultilineText(editComposite);
        Composite spacer = new Composite(editComposite, SWT.NONE);
        spacer.setLayout(new GridLayout());
        GridData data = new GridData(GridData.FILL_BOTH);
        spacer.setLayoutData(data);
        bindingContext.bindContent(description, association, IProductCmptTypeAssociation.PROPERTY_DESCRIPTION);
        return description;
    }

    public IIpsProject getIpsProject() {
        return association.getIpsProject();
    }
    
    public class PmoAssociation extends IpsObjectPartPmo {

        public final static String PROPERTY_SUBSET = "subset";
        public final static String PROPERTY_QUALIFICATION_LABEL = "qualificationLabel";
        public final static String PROPERTY_QUALIFICATION_NOTE = "qualificationNote";
        public final static String PROPERTY_QUALIFICATION_POSSIBLE = "qualificationPossible";
        public final static String PROPERTY_CONSTRAINED_NOTE = "constrainedNote";

        private boolean subset;
        
        public PmoAssociation(IPolicyCmptTypeAssociation association) {
            super(association);
            subset = association.isSubsetOfADerivedUnion();
        }
        
        public boolean isSubset() {
            return subset;
        }
        
        public void setSubset(boolean newValue) {
            subset = newValue;
            if (!subset) {
                association.setSubsettedDerivedUnion("");
            }
            notifyListeners();
        }
        
        public String getQualificationLabel() {
            String label = "This association is qualified";
            try {
                String productCmptType = QNameUtil.getUnqualifiedName(association.findQualifierCandidate(ipsProject));
                if (StringUtils.isNotEmpty(productCmptType)) {
                    label = label + " by type '" + productCmptType + "'";
                }
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return StringUtils.rightPad(label, 80);            
        }

        public String getQualificationNote() {
            String note = "Note: ";
            if (!association.isCompositionMasterToDetail()) {
                note = note + "Qualification is only applicable for compositions (master to detail).";
            } else {
                try {
                    if (!association.isQualificationPossible(ipsProject)) {
                        note = note + "Qualification is only applicable, if the target type is configurable by a product.";
                    } else {
                        note = note + "For qualified associations multiplicty is defined per qualified instance.";
                    }
                }
                catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            return StringUtils.rightPad(note, 90);
        }
        
        public boolean isQualificationPossible() {
            try {
                return association.isQualificationPossible(ipsProject);
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
        }

        public String getConstrainedNote() {
            try {
                if (association.isCompositionDetailToMaster()) {
                    return StringUtils.rightPad("", 120) + StringUtils.rightPad("\n", 120) + StringUtils.right("\n", 120);
                }
                IProductCmptTypeAssociation matchingAss = association.findMatchingProductCmptTypeAssociation(ipsProject);
                if (matchingAss!=null) {
                    String type = matchingAss.getProductCmptType().getName();
                    return "Note: This association is constrained by product structure. " 
                    +" The matching \nassociation in type '" + type + "' is '" + matchingAss.getTargetRoleSingular() + "' (rolename)."
                    + StringUtils.rightPad("\n", 120); 
                } else {
                    String note = "Note: This association is not constrained by product structure."; 
                    IProductCmptType sourceProductType = association.getPolicyCmptType().findProductCmptType(ipsProject);
                    IPolicyCmptType targetType = association.findTargetPolicyCmptType(ipsProject);
                    if (sourceProductType!=null && targetType!=null) {
                        IProductCmptType targetProductType = targetType.findProductCmptType(ipsProject);
                        if (targetProductType!=null) {
                            return note + "\nTo constrain the association by product structure, create an association between the "
                                + "\nproduct component types '" + sourceProductType.getName() + "' and '" + targetProductType.getName() + "'.";
                        }
                    }
                    return note + StringUtils.rightPad("\n", 120) + StringUtils.rightPad("\n", 120) ;
                }
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
                return "";
            }
            
        }

        /**
         * {@inheritDoc}
         */
        protected void partHasChanged() {
            if (association.isCompositionDetailToMaster()) {
                subset = false;
            }
        }
    }

    public PmoAssociation getPmoAssociation() {
        return pmoAssociation;
    }    
}

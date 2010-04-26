/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
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
    private static final ArrayList<IAssociation> EMPTY_ASSOCIATION_ARRAY_LIST = new ArrayList<IAssociation>(0);
    final static int NEW_INVERSE_ASSOCIATION = 0;
    final static int USE_EXISTING_INVERSE_ASSOCIATION = 1;
    final static int NONE_INVERSE_ASSOCIATION = 2;

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
    private Memento mementoTargetBeforeNewAssociation;
    private Memento mementoProductCmptTypeBeforeAssociation;
    private boolean targetIsDirty;
    private boolean productCmptTypeIsDirty;

    // wizard pages
    private List<IWizardPage> pages = new ArrayList<IWizardPage>();
    private AssociationTargetPage associationTargetPage;
    private PropertyPage propertyPage;
    private InverseAssociationPage inverseAssociationPage;
    private InverseAssociationPropertyPage inverseAssociationPropertyPage;
    private ConfigureProductCmptTypePage configureProductCmptTypePage;
    private ConfProdCmptTypePropertyPage confProdCmptTypePropertyPage;
    private ErrorPage errorPage;

    // stores page selections
    private int inverseAssociationManipulation = NEW_INVERSE_ASSOCIATION;
    private int previousInverseAssociationManipulation = NONE_INVERSE_ASSOCIATION;
    private boolean configureProductCmptType = true;
    private boolean prevConfigureProductCmptType = false;
    private String previousTarget;
    private AssociationType previousTargetAssociationType;

    // pages which will be hidden if the type of the association is detail to master
    private List<WizardPage> detailToMasterHiddenPages = new ArrayList<WizardPage>(10);

    // helper fields to suppress error messages
    private Set<IWizardPage> displayErrorMessageForPages = new HashSet<IWizardPage>();
    private Set<IWizardPage> visiblePages = new HashSet<IWizardPage>();
    HashMap<IWizardPage, Integer> suppressedEventForPages = new HashMap<IWizardPage, Integer>();

    // stores the last inverse association, to indicate changes of this property
    private String prevInverseAssociationRoleName = "";
    private String prevAssociationRoleName = "";
    private IPolicyCmptTypeAssociation prevInverseAssociation;
    private String prevInverseAssociationName;

    public NewPcTypeAssociationWizard(IPolicyCmptTypeAssociation association) {
        super.setWindowTitle(Messages.NewPcTypeAssociationWizard_wizardTitle);

        this.association = association;
        ipsProject = association.getIpsProject();
        extFactoryAssociation = new ExtensionPropertyControlFactory(association.getClass());
        extFactoryInverseAssociation = new ExtensionPropertyControlFactory(association.getClass());
        extFactoryProductCmptTypeAssociation = new ExtensionPropertyControlFactory(IProductCmptTypeAssociation.class);

        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPages() {
        associationTargetPage = new AssociationTargetPage(this, association, toolkit, bindingContext);
        propertyPage = new PropertyPage(this, association, toolkit, bindingContext);
        inverseAssociationPage = new InverseAssociationPage(this, toolkit);
        inverseAssociationPropertyPage = new InverseAssociationPropertyPage(this, toolkit, bindingContext);
        configureProductCmptTypePage = new ConfigureProductCmptTypePage(this, toolkit);
        confProdCmptTypePropertyPage = new ConfProdCmptTypePropertyPage(this, toolkit, bindingContext);
        errorPage = new ErrorPage(toolkit);

        addPage(associationTargetPage);
        addPage(propertyPage);
        addPage(inverseAssociationPage);
        addPage(inverseAssociationPropertyPage);
        addPage(configureProductCmptTypePage);
        addPage(confProdCmptTypePropertyPage);
        addPage(errorPage);

        detailToMasterHiddenPages.add(configureProductCmptTypePage);
        detailToMasterHiddenPages.add(confProdCmptTypePropertyPage);

        visiblePages.add(associationTargetPage);

        initSuppressedEventsForPages();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPage(IWizardPage page) {
        pages.add(page);
        super.addPage(page);
    }

    private void initSuppressedEventsForPages() {
        suppressedEventForPages.put(associationTargetPage, new Integer(1));
        suppressedEventForPages.put(propertyPage, new Integer(1));
        suppressedEventForPages.put(inverseAssociationPropertyPage, new Integer(2));
        suppressedEventForPages.put(confProdCmptTypePropertyPage, new Integer(2));
    }

    private boolean isSuppressedEventFor(IWizardPage page, boolean decreaseEvents) {
        Integer current = suppressedEventForPages.get(page);
        if (current == null) {
            return false;
        }
        if (current.intValue() == 0) {
            return false;
        }

        if (decreaseEvents) {
            int i = current.intValue() - 1;
            suppressedEventForPages.put(page, new Integer(i));
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        IWizardPage currentPage = getContainer().getCurrentPage();
        IProductCmptType productCmptType = findProductCmptType();
        if (!(event.isAffected(association) || event.isAffected(inverseAssociation) || productCmptType == null ? true
                : event.isAffected(productCmptType))
                || currentPage == null) {
            return;
        }

        handleInverseAssociationRoleNameChange(event);

        contentsChanged(currentPage);
    }

    private void handleInverseAssociationRoleNameChange(ContentChangeEvent event) {
        // if the target role singular name has changed
        // then we have to update the inverse association definition in the corresponding inverse
        // association
        if (event.isAffected(inverseAssociation)
                && !prevInverseAssociationRoleName.equals(inverseAssociation.getTargetRoleSingular())) {
            storeInverseAssociation(inverseAssociation);
        } else if (event.isAffected(association)
                && !prevAssociationRoleName.equals(association.getTargetRoleSingular())) {
            storeInverseAssociation(inverseAssociation);
        }
    }

    /**
     * Indicates that there was a change on the given page
     */
    public void contentsChanged(IWizardPage currentPage) {
        if (!association.getTarget().equals(previousTarget)) {
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

        inverseAssociationPage.setVisibleStateForDetailToMasterAssociation(association.isCompositionDetailToMaster());

        // handle validate state of current page
        if (currentPage instanceof IBlockedValidationWizardPage) {
            handleValidationOfPage((IBlockedValidationWizardPage)currentPage);
        }

        // handle validate state of next page
        IWizardPage nextPage = getNextWizardPage(currentPage);
        if (nextPage instanceof IBlockedValidationWizardPage) {
            handleValidationOfPage((IBlockedValidationWizardPage)nextPage);
        }

        // check if the given page is now enabled to display the error messages
        // used to suppress the error message if the wizard page is displayed the first time
        boolean suppressed = isSuppressedEventFor(currentPage, true);
        if (!suppressed && visiblePages.contains(currentPage)) {
            displayErrorMessageForPages.add(currentPage);
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
    public boolean canPageFlipToNextPage(IBlockedValidationWizardPage page) {
        page.setErrorMessage(null);

        boolean valid = validatePageAndDisplayError(page);

        if (page.getNextPage() == null) {
            return false;
        }

        return valid;
    }

    /*
     * Validates the given page
     */
    private boolean validatePageAndDisplayError(IBlockedValidationWizardPage page) {
        IAssociation association = getAssociationFor(page);
        if (association == null) {
            return true;
        }

        if (isValidationDisabledFor(page)) {
            return true;
        }

        MessageList list;
        try {
            if (association instanceof IPolicyCmptTypeAssociation) {
                // in case of policy component type associations we must
                // validate using the parent, because the check for duplicate
                // attributes/associations is implemented in type#validateThis() method
                list = ((IPolicyCmptTypeAssociation)association).getPolicyCmptType().validate(ipsProject);
            } else {
                list = association.validate(ipsProject);
            }
        } catch (CoreException e) {
            showAndLogError(e);
            return false;
        }

        for (String prop : page.getProperties()) {
            MessageList messagesFor = list.getMessagesFor(association, prop);
            if (messagesFor.containsErrorMsg()) {
                if (displayErrorMessageForPages.contains(page)) {
                    page.setErrorMessage(messagesFor.getFirstMessage(Message.ERROR).getText());
                }
                return false;
            }
        }
        return true;
    }

    /*
     * Check if the validation for the given page is disabled.
     */
    private boolean isValidationDisabledFor(IBlockedValidationWizardPage page) {
        if (page instanceof ConfProdCmptTypePropertyPage && !configureProductCmptType) {
            return true;
        } else if (page instanceof InverseAssociationPropertyPage
                && inverseAssociationManipulation == NONE_INVERSE_ASSOCIATION) {
            return true;
        }
        return false;
    }

    /*
     * Returns the association which could be edit by the give page
     */
    private IAssociation getAssociationFor(IBlockedValidationWizardPage page) {
        if (page instanceof InverseAssociationPropertyPage) {
            return inverseAssociation;
        } else if (page instanceof PropertyPage || page instanceof AssociationTargetPage) {
            return association;
        } else if (page instanceof ConfProdCmptTypePropertyPage) {
            return productCmptTypeAssociation;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWizardPage getNextPage(final IWizardPage page) {
        // in case of an error no next page will be displayed
        if (isError) {
            return null;
        }

        // set default focus
        if (page instanceof IDefaultFocusPage) {
            if (isSuppressedEventFor(page, false)) {
                getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        ((IDefaultFocusPage)page).setDefaultFocus();
                    }
                });
            }
        }

        // indicates that the page was displayed before
        visiblePages.add(page);

        // resets the special case message "no existing inverse association"
        ((WizardPage)getContainer().getCurrentPage()).setMessage(null);

        // if there is an error on the current page then to no next page could be switched
        if (page instanceof IBlockedValidationWizardPage) {
            if (!validatePageAndDisplayError((IBlockedValidationWizardPage)page)) {
                return null;
            }
        }

        // check if this is the last page
        int index = pages.indexOf(page);
        if (index == -1) {
            return null;
        }

        // get the next page
        IWizardPage nextPage = pages.get(index + 1);

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

            // display information in special case "no existing inverse association"
            // if an existing inverse association should be used but no corresponding association
            // exists
            if (nextPage instanceof InverseAssociationPropertyPage) {
                ((WizardPage)getContainer().getCurrentPage()).setMessage(null);
                if (isExistingInverseAssociation()) {
                    if (!areExistingAssociationsAvailable()) {
                        ((WizardPage)getContainer().getCurrentPage()).setMessage(
                                Messages.NewPcTypeAssociationWizard_warningNoExistingAssociationFound,
                                IMessageProvider.WARNING);
                    }
                }
            }

            nextPage = pages.get(index + 1);
        }

        return nextPage;
    }

    private IWizardPage getNextWizardPage(IWizardPage page) {
        int index = pages.indexOf(page);
        if (index == pages.size() - 1 || index == -1) {
            // last page or page not found
            return null;
        }
        return pages.get(index + 1);
    }

    /*
     * Returns the visibility of the given page.
     * 
     * @param page the page to check the visibility state
     * 
     * @return <code>true</code> if the given page is visible, <code>false</code> if the page should
     * be hidden
     */
    private boolean isPageVisible(IWizardPage page) {
        // if a detail to master association should be created then hide all pages which are not
        // necessary
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
     * Returns all existing associations on the target which could be used as inverse associaton, of
     * the association created by this wizard.
     */
    public List<IAssociation> getExistingInverseAssociationCandidates() {
        try {
            return NewPcTypeAssociationWizard
                    .getCorrespondingTargetAssociations(getAssociation(), targetPolicyCmptType);
        } catch (CoreException e) {
            showAndLogError(e);
        };
        return null;
    }

    /**
     * Check if the selection of the configure product component type (yes/no) has changes and reset
     * the state and optional create the product component type association.
     */
    void handleConfProdCmptTypeSelectionState() {
        if (prevConfigureProductCmptType != configureProductCmptType) {
            prevConfigureProductCmptType = configureProductCmptType;
            restoreMementoProductCmptTypeBeforeChange();
            if (configureProductCmptType) {
                storeMementoProductCmptTypeBeforeChange();
                createNewProductCmptTypeAssociation();
            }
        }
    }

    /**
     * Check if the selection of the inverse association (none/use existing/create new) has changes
     * and reset the state and optional create a new inverse association.
     */
    void handleInverseAssociationSelectionState() {
        if (previousInverseAssociationManipulation == inverseAssociationManipulation
                && previousTarget.equals(targetPolicyCmptType.getQualifiedName())
                && previousTargetAssociationType == association.getAssociationType()) {
            return;
        }

        // if selection changed restore last state
        previousInverseAssociationManipulation = inverseAssociationManipulation;
        previousTarget = association.getTarget();
        previousTargetAssociationType = association.getAssociationType();

        restoreMementoTargetBeforeChange();
        storeMementoTargetBeforeChange();
        storeInverseAssociation(null);

        if (isExistingInverseAssociation()) {
            inverseAssociationPropertyPage
                    .setDescription(Messages.NewPcTypeAssociationWizard_descriptionSelectExistingInverseAssociation);
            inverseAssociationPropertyPage.setShowExistingAssociationDropDown(true);
            try {
                // get all existing association that matches as inverse for the new association
                List<IAssociation> existingAssociations = getCorrespondingTargetAssociations(association,
                        targetPolicyCmptType);
                if (existingAssociations.size() > 0) {
                    String[] names = new String[existingAssociations.size()];
                    for (int i = 0; i < existingAssociations.size(); i++) {
                        names[i] = (((IPolicyCmptTypeAssociation)existingAssociations.get(i)).getName());
                    }
                    inverseAssociationPropertyPage.setExistingAssociations(names);
                } else {
                    inverseAssociationPropertyPage.setExistingAssociations(new String[0]);
                }
            } catch (CoreException e) {
                showAndLogError(e);
            }
        } else if (isNewInverseAssociation()) {
            inverseAssociationPropertyPage
                    .setDescription(Messages.NewPcTypeAssociationWizard_descriptionDefineNewInverseAssociation);
            inverseAssociationPropertyPage.setShowExistingAssociationDropDown(false);
            // create a new inverse association
            storeInverseAssociation(null);
            try {
                createNewInverseAssociation();
            } catch (CoreException e) {
                IpsPlugin.log(e);
                showAndLogError(e);
            }
        }

        inverseAssociationPropertyPage.refreshControls();
    }

    /*
     * Create a new inverse association, i.e. create a new association on the target policy
     * component type object.
     */
    private void createNewInverseAssociation() throws CoreException {
        if (targetPolicyCmptType == null) {
            return;
        }

        IPolicyCmptTypeAssociation newInverseAssociation = association.newInverseAssociation();
        storeInverseAssociation(newInverseAssociation);
    }

    /*
     * Creates a new association on the product component type.
     */
    private void createNewProductCmptTypeAssociation() {
        IProductCmptType productCmptType = findProductCmptType();
        if (productCmptType == null) {
            return;
        }
        productCmptTypeIsDirty = productCmptType.getIpsSrcFile().isDirty();
        productCmptTypeAssociation = productCmptType.newAssociation();
        IProductCmptType productCmptTypeTarget;
        try {
            productCmptTypeTarget = targetPolicyCmptType.findProductCmptType(ipsProject);
            productCmptTypeAssociation.setTarget(productCmptTypeTarget.getQualifiedName());
            confProdCmptTypePropertyPage
                    .setProductCmptTypeAssociationAndUpdatePage((IProductCmptTypeAssociation)productCmptTypeAssociation);
        } catch (Exception e) {
            showAndLogError(e);
        }
    }

    /**
     * Returns association from the target if:<br>
     * <ul>
     * <li>the target of the target association points to the source (policy component type of the
     * given sourceAssociation)
     * <li>the target association type is the corresponding association type of the source
     * </ul>
     * If no association is found on the target then an empty (not null) ArrayList is returned.
     * 
     * @throws CoreException
     */
    // TODO pk 30-09-2008 shouldn't this method be a method of IPolicyCmptTypeAssociation?
    public static List<IAssociation> getCorrespondingTargetAssociations(IPolicyCmptTypeAssociation sourceAssociation,
            IPolicyCmptType target) throws CoreException {
        if (target == null) {
            return EMPTY_ASSOCIATION_ARRAY_LIST;
        }
        String source = sourceAssociation.getPolicyCmptType().getQualifiedName();
        AssociationType correspondingAssociationType = sourceAssociation.getAssociationType()
                .getCorrespondingAssociationType();
        IAssociation[] associations = target.findAssociationsForTargetAndAssociationType(source,
                correspondingAssociationType, sourceAssociation.getIpsProject(), false);
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
    @Override
    public boolean performFinish() {
        try {
            // check and perform the last state change of the radio selection pages
            if (inverseAssociationManipulation == NONE_INVERSE_ASSOCIATION) {
                handleInverseAssociationSelectionState();
            }
            if (!configureProductCmptType) {
                handleConfProdCmptTypeSelectionState();
            }
            if (association.getPolicyCmptType().isPersistentEnabled()) {
                initPersistentAssociationInfo();
            }
            boolean saveTargetAutomatically = false;
            if (targetPolicyCmptType != null
                    && !targetPolicyCmptType.getIpsSrcFile().equals(association.getIpsObject().getIpsSrcFile())) {
                // there is a target selected and the target is not the object this new association
                // belongs to
                if (targetIsDirty) {
                    // target policy component type was dirty before editing by the wizard,
                    // ask to save automatically
                    String msg = Messages.NewPcTypeAssociationWizard_dialogMessageTargetIsDirty;
                    saveTargetAutomatically = MessageDialog.openQuestion(getShell(),
                            Messages.NewPcTypeAssociationWizard_dialogTitleTargetIsDirty, msg);
                } else {
                    // target policy component type is not dirty, therefore save the changed on the
                    // target
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
                    String msg = Messages.NewPcTypeAssociationWizard_dialogMessageProductComponentTypeIsDirty;
                    saveProductCmptTypeAutomatically = MessageDialog.openQuestion(getShell(),
                            Messages.NewPcTypeAssociationWizard_dialogTitleProductComponentTypeIsDirty, msg);
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

    private void initPersistentAssociationInfo() throws CoreException {
        association.getPersistenceAssociatonInfo().initDefaults();
        IPolicyCmptTypeAssociation inverseAssociation = association.findInverseAssociation(ipsProject);
        if (inverseAssociation == null) {
            return;
        }
        inverseAssociation.getPersistenceAssociatonInfo().initDefaults();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean performCancel() {
        restoreMementoTargetBeforeChange();
        restoreMementoProductCmptTypeBeforeChange();
        return true;
    }

    /**
     * Stores the policy component type object of the target. And check if the target source file is
     * dirty to ask the user about the automatically saving when finishing the wizard.
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
    void storeMementoTargetBeforeChange() {
        if (targetPolicyCmptType == null) {
            return;
        }

        mementoTargetBeforeNewAssociation = targetPolicyCmptType.newMemento();
    }

    private void storeInverseAssociation(IPolicyCmptTypeAssociation inverseAssociation) {
        if (inverseAssociation != null) {
            if (prevInverseAssociation != null && prevInverseAssociation != inverseAssociation) {
                prevInverseAssociation.setInverseAssociation(prevInverseAssociationName);
            }
            prevInverseAssociationName = inverseAssociation.getInverseAssociation();
            association.setInverseAssociation(inverseAssociation.getTargetRoleSingular());
            inverseAssociation.setInverseAssociation(association.getTargetRoleSingular());
        } else {
            association.setInverseAssociation(""); //$NON-NLS-1$
        }
        this.inverseAssociation = inverseAssociation;
        inverseAssociationPropertyPage.setAssociationAndUpdatePage(inverseAssociation);

        // store role names to handle the next role name change
        // the role names are used as definition for the corresponding inverse association

        prevAssociationRoleName = association.getTargetRoleSingular();
        prevInverseAssociationRoleName = inverseAssociation == null ? "" : inverseAssociation.getTargetRoleSingular();
        prevInverseAssociation = inverseAssociation;
    }

    public void storeExistingInverseAssociation(String inverseAssociation) {
        IPolicyCmptTypeAssociation[] policyCmptTypeAssociations = targetPolicyCmptType.getPolicyCmptTypeAssociations();
        for (IPolicyCmptTypeAssociation policyCmptTypeAssociation : policyCmptTypeAssociations) {
            if (policyCmptTypeAssociation.getName().equals(inverseAssociation)) {
                storeInverseAssociation(policyCmptTypeAssociation);
                return;
            }
        }
        // error not found
        IpsStatus errorStatus = new IpsStatus("Error assosiation " + inverseAssociation //$NON-NLS-1$
                + " not found in policy component type " + targetPolicyCmptType); //$NON-NLS-1$
        showAndLogError(new CoreException(errorStatus));
    }

    /**
     * Stores memento of product component type before change.
     */
    void storeMementoProductCmptTypeBeforeChange() {
        if (targetPolicyCmptType == null) {
            return;
        }

        IProductCmptType productCmptType = findProductCmptType();
        if (productCmptType == null) {
            return;
        }
        mementoProductCmptTypeBeforeAssociation = productCmptType.newMemento();
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
        if (targetPolicyCmptType == null || mementoProductCmptTypeBeforeAssociation == null) {
            return;
        }
        IProductCmptType productCmptType = findProductCmptType();
        if (productCmptType == null) {
            return;
        }
        productCmptType.setState(mementoProductCmptTypeBeforeAssociation);
        if (!productCmptTypeIsDirty) {
            // if the product component type wasn't dirty before mark as clean
            productCmptType.getIpsSrcFile().markAsClean();
        }
        mementoProductCmptTypeBeforeAssociation = null;
    }

    public void restoreMementoTargetBeforeChange() {
        inverseAssociation = null;
        if (targetPolicyCmptType == null || mementoTargetBeforeNewAssociation == null) {
            return;
        }
        targetPolicyCmptType.setState(mementoTargetBeforeNewAssociation);
        if (!targetIsDirty) {
            // if the target wasn't dirty before mark as clean
            targetPolicyCmptType.getIpsSrcFile().markAsClean();
        }
        mementoTargetBeforeNewAssociation = null;
    }

    /**
     * Returns true if the inverse association is an existing association on the target.
     */
    boolean isExistingInverseAssociation() {
        return inverseAssociationManipulation == USE_EXISTING_INVERSE_ASSOCIATION;
    }

    /**
     * Returns true if the inverse association is new association on the target.
     */
    boolean isNewInverseAssociation() {
        return inverseAssociationManipulation == NEW_INVERSE_ASSOCIATION;
    }

    /**
     * Returns true if none inverse association should be defined.
     */
    boolean isNoneInverseAssociation() {
        return inverseAssociationManipulation == NONE_INVERSE_ASSOCIATION;
    }

    /**
     * Sets that the inverse association is an existing association on the target.
     */
    void setInverseAssociationManipulation(int type) {
        inverseAssociationManipulation = type;
    }

    public void pageHasChanged() {
        contentsChanged(getContainer().getCurrentPage());
    }

    /**
     * @see this
     *      {@link #isProductCmptTypeAvailable(IIpsProject, IPolicyCmptType, IPolicyCmptTypeAssociation)}
     */
    public boolean isProductCmptTypeAvailable() {
        try {
            return isProductCmptTypeAvailable(ipsProject, association.getPolicyCmptType(), targetPolicyCmptType);
        } catch (CoreException e) {
            showAndLogError(e);
            return false;
        }
    }

    /**
     * Returns <code>true</code> if an association could be created for the product component type.
     * Returns <code>false</code> if no product component type was foud for the source or for the
     * target of the policy component type association or the source or the target are not
     * configurable by a product cmp type.
     */
    public static boolean isProductCmptTypeAvailable(IIpsProject ipsProject,
            IPolicyCmptType sourcePolicyCmptType,
            IPolicyCmptType targetPolicyCmptType) throws CoreException {
        if (targetPolicyCmptType == null) {
            // target not set
            return false;
        }
        if (!sourcePolicyCmptType.isConfigurableByProductCmptType()
                || !targetPolicyCmptType.isConfigurableByProductCmptType()) {
            return false;
        }
        if (sourcePolicyCmptType.findProductCmptType(ipsProject) == null) {
            // product cmpt type not found
            return false;
        }
        if (targetPolicyCmptType.findProductCmptType(ipsProject) == null) {
            // targets product cmpt type not found
            return false;
        }
        return true;
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

    public boolean isDetailToMasterAssociation() {
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
    public Text createDescriptionText(Composite parent, int span) {
        toolkit.createVerticalSpacer(parent, 20);

        Composite composite = toolkit.createComposite(parent);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = span;
        composite.setLayoutData(gridData);
        composite.setLayout(new GridLayout());

        toolkit.createFormLabel(composite, Messages.NewPcTypeAssociationWizard_labelDescription);
        Text description = toolkit.createMultilineText(composite);

        bindingContext.bindContent(description, association, IIpsObjectPart.PROPERTY_DESCRIPTION);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);

        bindingContext.dispose();

        super.dispose();
    }

    public boolean areExistingAssociationsAvailable() {
        if (targetPolicyCmptType == null) {
            return false;
        }
        List<IAssociation> correspondingAssociations = getExistingInverseAssociationCandidates();
        if (correspondingAssociations.size() == 0) {
            return false;
        }

        // if one association found and the last operation was create new the this is the created
        // association
        if (correspondingAssociations.size() == 1 && previousInverseAssociationManipulation == NEW_INVERSE_ASSOCIATION) {
            return false;
        }
        return true;
    }
}

/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.binding.ButtonTextBinding;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.binding.EnableBinding;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.util.QNameUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * A dialog to edit an association.
 */
public class AssociationEditDialog extends IpsPartEditDialog2 {

    /**
     * Keep track of the content of the name fields to be able to determine whether they have
     * changed.
     */
    private final String initialName;
    private final String initialPluralName;

    private final IIpsProject ipsProject;
    private final IPolicyCmptTypeAssociation association;

    private final ExtensionPropertyControlFactory extFactory;

    // the association that matches this when the dialog is opened. We need to update this
    // association in case of changes to the matching association.
    private IProductCmptTypeAssociation oldMatchingAssociation;

    private final PmoPolicyCmptTypeAssociation pmoAssociation;

    private AssociationPersistenceTab persistenceTabItem;

    public AssociationEditDialog(IPolicyCmptTypeAssociation relation2, Shell parentShell) {
        super(relation2, parentShell, Messages.AssociationEditDialog_title, true);
        association = relation2;
        initialName = association.getName();
        initialPluralName = association.getTargetRolePlural();
        ipsProject = association.getIpsProject();
        extFactory = new ExtensionPropertyControlFactory(association);
        pmoAssociation = new PmoPolicyCmptTypeAssociation(association, ipsProject);

        oldMatchingAssociation = association.findMatchingProductCmptTypeAssociation(association.getIpsProject());

        /*
         * In case direct refactoring is activated the inverse relation will be updated by the
         * refactoring
         */
        if (IpsPlugin.getDefault().getIpsPreferences().isRefactoringModeDirect()) {
            getBindingContext().addIgnoredMessageCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH);
        }
        getBindingContext().addIgnoredMessageCode(IPolicyCmptTypeAssociation.MSGCODE_MATCHING_ASSOCIATION_INVALID);
    }

    /**
     * Some error messages are ignored by this dialog. To show an information if there are ignored
     * error messages we add an information message. We always add the information if there are any
     * error messages because only the message with the highest priority will be displayed.
     */
    @Override
    protected void addAdditionalDialogMessages(MessageList messageList) {
        super.addAdditionalDialogMessages(messageList);
        if (messageList.containsErrorMsg()) {
            messageList.add(new Message(StringUtils.EMPTY, Messages.AssociationEditDialog_info_dialogAutoFixErrors,
                    Message.INFO));
        }
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        new PropertiesTabItem(folder, SWT.NONE);
        new MatchingAssociationTabItem(folder, SWT.NONE);

        createPersistenceTabItemIfNecessary(folder);

        return folder;
    }

    @Override
    protected void okPressed() {
        if (IpsPlugin.getDefault().getIpsPreferences().isRefactoringModeDirect()) {
            String newName = association.getName();
            String newPluralName = association.getTargetRolePlural();
            if (!(newName.equals(initialName) && newPluralName.equals(initialPluralName))) {
                applyRenameRefactoring(newName, newPluralName);
            }
        }
        updateMatchingProductCmptTypeAssociation();
        super.okPressed();
    }

    private void applyRenameRefactoring(String newName, String newPluralName) {
        // First, reset the initial names as otherwise errors 'names must not equal' will occur
        association.setTargetRoleSingular(initialName);
        association.setTargetRolePlural(initialPluralName);

        IIpsRefactoring ipsRenameRefactoring = IpsPlugin.getIpsRefactoringFactory().createRenameRefactoring(association,
                newName, newPluralName, false);
        IpsRefactoringOperation refactoringOperation = new IpsRefactoringOperation(ipsRenameRefactoring, getShell());
        refactoringOperation.runDirectExecution();
    }

    void updateMatchingProductCmptTypeAssociation() {
        IProductCmptTypeAssociation newConstrainedAssociation = null;
        newConstrainedAssociation = association.findMatchingProductCmptTypeAssociation(ipsProject);
        if (oldMatchingAssociation != null) {
            needToSaveOldMatchingAssociation(newConstrainedAssociation);
        }

        if (pmoAssociation.matchingExplicitly && newConstrainedAssociation != null) {
            boolean needToSave = !newConstrainedAssociation.getProductCmptType().getIpsSrcFile().isDirty();
            newConstrainedAssociation.setMatchingAssociationName(association.getName());
            newConstrainedAssociation.setMatchingAssociationSource(association.getPolicyCmptType().getQualifiedName());
            if (needToSave) {
                try {
                    newConstrainedAssociation.getProductCmptType().getIpsSrcFile().save(false,
                            new NullProgressMonitor());
                } catch (IpsException e) {
                    IpsPlugin.log(e);
                }
            }
        }
    }

    private void needToSaveOldMatchingAssociation(IProductCmptTypeAssociation newConstrainedAssociation) {
        if ((!pmoAssociation.matchingExplicitly || !oldMatchingAssociation.equals(newConstrainedAssociation))
                && association.getPolicyCmptType().getQualifiedName()
                        .equals(oldMatchingAssociation.getMatchingAssociationSource())
                && association.getName().equals(oldMatchingAssociation.getMatchingAssociationName())) {

            boolean needToSave = !oldMatchingAssociation.getProductCmptType().getIpsSrcFile().isDirty();
            oldMatchingAssociation.setMatchingAssociationName(StringUtils.EMPTY);
            oldMatchingAssociation.setMatchingAssociationSource(StringUtils.EMPTY);
            if (needToSave) {
                try {
                    oldMatchingAssociation.getProductCmptType().getIpsSrcFile().save(false, new NullProgressMonitor());
                } catch (IpsException e) {
                    IpsPlugin.log(e);
                }
            }
        }
    }

    private void createPersistenceTabItemIfNecessary(TabFolder tabFolder) {
        if (ipsProject.getReadOnlyProperties().isPersistenceSupportEnabled()) {
            persistenceTabItem = new AssociationPersistenceTab(tabFolder, association, getBindingContext(),
                    getToolkit());
        }
    }

    @Override
    protected void setDataChangeableThis(boolean changeable) {
        super.setDataChangeableThis(changeable);
        if (persistenceTabItem != null) {
            persistenceTabItem.setDataChangeable(changeable);
        }
    }

    @Override
    public boolean close() {
        pmoAssociation.dispose();
        return super.close();
    }

    private class PropertiesTabItem {

        private final TabItem tabItem;

        public PropertiesTabItem(TabFolder parent, int style) {
            tabItem = new TabItem(parent, style);
            tabItem.setText(Messages.AssociationEditDialog_textFirstPage);
            Composite c = createTabItemComposite(parent, 1, false);
            tabItem.setControl(createPage(c));
        }

        /**
         * Creates the first tab page. With the following goups: general, policy side, and product
         * side.
         */
        private Control createPage(Composite c) {
            Group groupGeneral = getToolkit().createGroup(c, Messages.AssociationEditDialog_generalGroup);
            createGeneralControls(groupGeneral);

            getToolkit().createVerticalSpacer(c, 8);

            createQualificationGroup(getToolkit().createGroup(c, Messages.AssociationEditDialog_qualificationGroup));

            getToolkit().createVerticalSpacer(c, 8);
            new AssociationDerivedUnionGroup(getToolkit(), getBindingContext(), c, association);

            return c;
        }

        private void createGeneralControls(Composite c) {
            Composite workArea = getToolkit().createLabelEditColumnComposite(c);
            workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

            // top extensions
            extFactory.createControls(workArea, getToolkit(), association, IExtensionPropertyDefinition.POSITION_TOP);

            // target
            getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_targetLabel);
            PcTypeRefControl targetControl = getToolkit().createPcTypeRefControl(association.getIpsProject(), workArea);
            getBindingContext().bindContent(targetControl, association, IAssociation.PROPERTY_TARGET);

            // type
            getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_associationTypeLabel);
            final Combo typeCombo = getToolkit().createCombo(workArea);
            getBindingContext().bindContent(typeCombo, association, IAssociation.PROPERTY_ASSOCIATION_TYPE,
                    IPolicyCmptTypeAssociation.APPLICABLE_ASSOCIATION_TYPES);
            typeCombo.setFocus();

            // role singular
            getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_targetRoleSingularLabel);
            final Text targetRoleSingularText = getToolkit().createText(workArea);
            getBindingContext().bindContent(targetRoleSingularText, association,
                    IAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
            targetRoleSingularText.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
                        association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
                    }
                }
            });

            // role plural
            getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_targetRolePluralLabel);
            final Text targetRolePluralText = getToolkit().createText(workArea);
            getBindingContext().bindContent(targetRolePluralText, association,
                    IAssociation.PROPERTY_TARGET_ROLE_PLURAL);
            targetRolePluralText.addFocusListener(new FocusAdapter() {

                @Override
                public void focusGained(FocusEvent e) {
                    if (StringUtils.isEmpty(targetRolePluralText.getText())
                            && association.isTargetRolePluralRequired()) {
                        association.setTargetRolePlural(association.getDefaultTargetRolePlural());
                    }
                }
            });

            // min cardinality
            getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_minimumCardinality);
            Text minCardinalityText = getToolkit().createText(workArea);
            CardinalityField cardinalityField = new CardinalityField(minCardinalityText);
            getBindingContext().bindContent(cardinalityField, association, IAssociation.PROPERTY_MIN_CARDINALITY);

            // max cardinality
            getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_maximumCardinality);
            Text maxCardinalityText = getToolkit().createText(workArea);
            cardinalityField = new CardinalityField(maxCardinalityText);
            getBindingContext().bindContent(cardinalityField, association, IAssociation.PROPERTY_MAX_CARDINALITY);

            // inverse association
            getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_inverseAssociationLabel);
            Composite inverseAssoComposite = getToolkit().createComposite(workArea);
            inverseAssoComposite.setLayout(getToolkit().createNoMarginGridLayout(2, false));
            inverseAssoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            Text inverseRelationText = getToolkit().createText(inverseAssoComposite);
            getBindingContext().bindContent(inverseRelationText, association,
                    IPolicyCmptTypeAssociation.PROPERTY_INVERSE_ASSOCIATION);
            InverseAssociationCompletionProcessor inverseAssociationCompletionProcessor = new InverseAssociationCompletionProcessor(
                    association);
            inverseAssociationCompletionProcessor.setComputeProposalForEmptyPrefix(true);
            CompletionUtil.createHandlerForText(inverseRelationText, inverseAssociationCompletionProcessor);

            // shared associations
            if (ipsProject.getReadOnlyProperties().isSharedDetailToMasterAssociations()) {
                Button sharedAssociationCheck = getToolkit().createButton(inverseAssoComposite,
                        Messages.AssociationEditDialog_sharedAssociations, SWT.CHECK);
                sharedAssociationCheck.setToolTipText(Messages.AssociationEditDialog_sharedAssociationsTooltip);
                getBindingContext().bindContent(sharedAssociationCheck, association,
                        IPolicyCmptTypeAssociation.PROPERTY_SHARED_ASSOCIATION);
                getBindingContext().bindEnabled(inverseRelationText, association,
                        IPolicyCmptTypeAssociation.PROPERTY_SHARED_ASSOCIATION, false);
                getBindingContext().add(new EnableBinding(sharedAssociationCheck, association,
                        IPolicyCmptTypeAssociation.PROPERTY_ASSOCIATION_TYPE,
                        AssociationType.COMPOSITION_DETAIL_TO_MASTER));
            } else {
                ((GridData)inverseRelationText.getLayoutData()).verticalSpan = 2;
            }

            // bottom extensions
            extFactory.createControls(workArea, getToolkit(), association,
                    IExtensionPropertyDefinition.POSITION_BOTTOM);
            extFactory.bind(getBindingContext());
        }

        private void createQualificationGroup(Composite c) {
            Composite workArea = getToolkit().createGridComposite(c, 1, true, true);
            workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

            Checkbox qualifiedCheckbox = getToolkit().createCheckbox(workArea);
            getBindingContext().bindContent(qualifiedCheckbox, association, IAssociation.PROPERTY_QUALIFIED);
            getBindingContext().bindEnabled(qualifiedCheckbox, pmoAssociation,
                    PmoPolicyCmptTypeAssociation.PROPERTY_QUALIFICATION_POSSIBLE);
            Label note = getToolkit().createFormLabel(workArea, StringUtils.rightPad("", 120)); //$NON-NLS-1$
            getBindingContext().bindContent(note, pmoAssociation,
                    PmoPolicyCmptTypeAssociation.PROPERTY_QUALIFICATION_NOTE);
            getBindingContext().add(new ButtonTextBinding(qualifiedCheckbox, pmoAssociation,
                    PmoPolicyCmptTypeAssociation.PROPERTY_QUALIFICATION_LABEL));
        }
    }

    private class MatchingAssociationTabItem {

        private final TabItem tabItem;

        public MatchingAssociationTabItem(TabFolder parent, int style) {
            tabItem = new TabItem(parent, style);
            tabItem.setText(Messages.AssociationEditDialog_tab_productAssociation);
            Composite c = createTabItemComposite(parent, 1, false);
            tabItem.setControl(createPage(c));
        }

        private Control createPage(Composite c) {
            getToolkit().createVerticalSpacer(c, 2);

            Composite labelEditColumnComposite = getToolkit().createLabelEditColumnComposite(c);
            getToolkit().createLabel(labelEditColumnComposite, Messages.AssociationEditDialog_label_foundByFaktorIps);
            Label matchingAssociationInfoLabel = getToolkit().createLabel(labelEditColumnComposite, StringUtils.EMPTY);
            getBindingContext().bindContent(matchingAssociationInfoLabel, pmoAssociation,
                    PmoPolicyCmptTypeAssociation.PROPERTY_INFO_LABEL);
            getToolkit().createVerticalSpacer(labelEditColumnComposite, 3);

            Group groupMatching = getToolkit().createGroup(c, Messages.AssociationEditDialog_group_selectExplicitly);
            createMatchingAssociationGroup(groupMatching);
            Group groupConfig = getToolkit().createGroup(c, Messages.AssociationEditDialog_group_configuration);
            createConfigurationGroup(groupConfig);
            return c;
        }

        private void createMatchingAssociationGroup(Composite c) {
            Composite workArea = getToolkit().createLabelEditColumnComposite(c);
            workArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Checkbox constrainedCheckbox = getToolkit().createCheckbox(workArea,
                    Messages.AssociationEditDialog_check_selectMatchingAssociationExpliclitly);
            ((GridData)constrainedCheckbox.getLayoutData()).horizontalSpan = 2;
            getBindingContext().bindContent(constrainedCheckbox, pmoAssociation,
                    PmoPolicyCmptTypeAssociation.PROPERTY_MATCHING_EXPLICITLY);

            getToolkit().createLabel(workArea, Messages.AssociationEditDialog_label_productCmptType);
            ProductCmptType2RefControl refControl = new ProductCmptType2RefControl(ipsProject, workArea, getToolkit(),
                    false);
            EditField<String> refControlEditField = getBindingContext().bindContent(refControl, pmoAssociation,
                    IPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE);
            getBindingContext().bindProblemMarker(refControlEditField, association,
                    IPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE);

            getToolkit().createLabel(workArea, Messages.AssociationEditDialog_label_matchingAssociation);
            Combo matchingAssociation = getToolkit().createCombo(workArea);
            final ComboViewerField<String> configuringAssociationField = new ComboViewerField<>(
                    matchingAssociation, String.class);
            configuringAssociationField.setAllowEmptySelection(true);

            getBindingContext().bindEnabled(matchingAssociation, pmoAssociation,
                    PmoPolicyCmptTypeAssociation.PROPERTY_MATCHING_EXPLICITLY);
            getBindingContext().bindContent(configuringAssociationField, pmoAssociation,
                    IPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_NAME);
            getBindingContext().bindProblemMarker(configuringAssociationField, association,
                    IPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_NAME);

            getBindingContext().bindEnabled(refControl, pmoAssociation,
                    PmoPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_REF_CONTROL_ENABLED);
            getBindingContext().add(new ControlPropertyBinding(refControl, association,
                    IPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE, String.class) {

                @Override
                public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                    pmoAssociation.updateConstrainingAssociationCombo(configuringAssociationField);
                }

            });

            pmoAssociation.updateConstrainingAssociationCombo(configuringAssociationField);
        }

        private void createConfigurationGroup(Group groupMatching) {
            Checkbox checkbox = getToolkit().createCheckbox(groupMatching,
                    Messages.AssociationEditDialog_check_configuration);
            getBindingContext().bindContent(checkbox, pmoAssociation,
                    PmoPolicyCmptTypeAssociation.PROPERTY_CONFIGURABLE);
            getBindingContext().bindEnabled(checkbox, pmoAssociation,
                    PmoPolicyCmptTypeAssociation.PROPERTY_CONFIGURABLE_CHECKBOX_ENABLED);
        }
    }

    public static class PmoPolicyCmptTypeAssociation extends IpsObjectPartPmo {

        public static final String PROPERTY_MATCHING_ASSOCIATION_REF_CONTROL_ENABLED = "matchingAssociationRefControlEnabled"; //$NON-NLS-1$
        public static final String PROPERTY_QUALIFICATION_LABEL = "qualificationLabel"; //$NON-NLS-1$
        public static final String PROPERTY_QUALIFICATION_NOTE = "qualificationNote"; //$NON-NLS-1$
        public static final String PROPERTY_QUALIFICATION_POSSIBLE = "qualificationPossible"; //$NON-NLS-1$
        public static final String PROPERTY_MATCHING_EXPLICITLY = "matchingExplicitly"; //$NON-NLS-1$
        public static final String PROPERTY_INFO_LABEL = "infoLabel"; //$NON-NLS-1$
        public static final String PROPERTY_CONFIGURABLE = "configurable"; //$NON-NLS-1$
        public static final String PROPERTY_CONFIGURABLE_CHECKBOX_ENABLED = "configurableCheckboxEnabled"; //$NON-NLS-1$
        private boolean matchingExplicitly;

        private String actualConfiguredAssociationSourceName;
        private final IIpsProject ipsProject;

        public PmoPolicyCmptTypeAssociation(IPolicyCmptTypeAssociation association, IIpsProject ipsProject) {
            super(association);
            this.ipsProject = ipsProject;
            matchingExplicitly = !StringUtils.isEmpty(association.getMatchingAssociationSource())
                    && !StringUtils.isEmpty(association.getMatchingAssociationName());
        }

        private IPolicyCmptTypeAssociation getAssociation() {
            return (IPolicyCmptTypeAssociation)super.getIpsObjectPartContainer();
        }

        public boolean isConfigurableCheckboxEnabled() {
            return getAssociation().isConstrainedByProductStructure(ipsProject);
        }

        public String getQualificationLabel() {
            String label = Messages.AssociationEditDialog_associationIsQualified;
            try {
                String productCmptType = QNameUtil
                        .getUnqualifiedName(getAssociation().findQualifierCandidate(ipsProject));
                if (StringUtils.isNotEmpty(productCmptType)) {
                    label = label + NLS.bind(Messages.AssociationEditDialog_qualifiedByType, productCmptType);
                }
            } catch (IpsException e) {
                IpsPlugin.log(e);
            }
            return StringUtils.rightPad(label, 80);
        }

        public String getQualificationNote() {
            String note = Messages.AssociationEditDialog_note;
            if (!getAssociation().isCompositionMasterToDetail()) {
                note = note + Messages.AssociationEditDialog_qualificationOnlyForMasterDetail;
            } else {
                try {
                    if (!getAssociation().isQualificationPossible(ipsProject)) {
                        note = note + Messages.AssociationEditDialog_qualificationOnlyIfTheTargetTypeIsConfigurable;
                    } else {
                        note = note + Messages.AssociationEditDialog_multiplicityIsDefineddPerQualifier;
                    }
                } catch (IpsException e) {
                    IpsPlugin.log(e);
                }
            }
            return StringUtils.rightPad(note, 90);
        }

        public boolean isQualificationPossible() {
            try {
                return getAssociation().isQualificationPossible(ipsProject);
            } catch (IpsException e) {
                IpsPlugin.log(e);
                return false;
            }
        }

        /**
         * @return Returns the refControlEnabled.
         */
        public boolean isMatchingAssociationRefControlEnabled() {
            return matchingExplicitly && !getAssociation().getPolicyCmptType().isConfigurableByProductCmptType();
        }

        /**
         * This method is binded by property {@link #PROPERTY_MATCHING_EXPLICITLY}
         * 
         * @param matchingExplicitly The matchingExplicitly to set.
         */
        public void setMatchingExplicitly(boolean matchingExplicitly) {
            this.matchingExplicitly = matchingExplicitly;
            if (matchingExplicitly) {
                IPolicyCmptType policyCmptType = getAssociation().getPolicyCmptType();
                if (policyCmptType.isConfigurableByProductCmptType()) {
                    setMatchingAssociationSource(policyCmptType.getProductCmptType());
                }
                IProductCmptTypeAssociation matchingProductCmptTypeAssociation = getDefaultMatchingAssociation();
                getAssociation().setMatchingAssociationName(matchingProductCmptTypeAssociation != null
                        ? matchingProductCmptTypeAssociation.getName()
                        : null);
            } else {
                getAssociation().setMatchingAssociationSource(StringUtils.EMPTY);
                getAssociation().setMatchingAssociationName(null);
            }
            notifyListeners();
        }

        /**
         * This method is binded by property {@link #PROPERTY_MATCHING_EXPLICITLY}
         * 
         * @return Returns the matchingExplicitly.
         */
        public boolean isMatchingExplicitly() {
            return matchingExplicitly;
        }

        /**
         * This method is binded by property
         * {@link IPolicyCmptTypeAssociation#PROPERTY_MATCHING_ASSOCIATION_NAME}
         * 
         * @param matchingAssociationName The constrainingAssociationName to set.
         */
        public void setMatchingAssociationName(String matchingAssociationName) {
            if (matchingExplicitly) {
                getAssociation().setMatchingAssociationName(matchingAssociationName);
            }
        }

        /**
         * This method is binded by property
         * {@link IPolicyCmptTypeAssociation#PROPERTY_MATCHING_ASSOCIATION_NAME}
         * 
         * @return Returns the constrainingAssociationName.
         */
        public String getMatchingAssociationName() {
            if (matchingExplicitly) {
                return getAssociation().getMatchingAssociationName();
            } else {
                return null;
            }
        }

        /**
         * @param matchingAssociationSource The matchingAssociationSource to set.
         */
        public void setMatchingAssociationSource(String matchingAssociationSource) {
            if (matchingExplicitly) {
                getAssociation().setMatchingAssociationSource(matchingAssociationSource);
            }
        }

        /**
         * @return Returns the matchingAssociationSource.
         */
        public String getMatchingAssociationSource() {
            if (matchingExplicitly) {
                return getAssociation().getMatchingAssociationSource();
            } else {
                return StringUtils.EMPTY;
            }
        }

        private IProductCmptTypeAssociation getDefaultMatchingAssociation() {
            try {
                IProductCmptTypeAssociation matchingProductCmptTypeAssociation = getAssociation()
                        .findDefaultMatchingProductCmptTypeAssociation(ipsProject);
                return matchingProductCmptTypeAssociation;
            } catch (IpsException e) {
                IpsPlugin.log(e);
                return null;
            }
        }

        public String getInfoLabel() {
            IProductCmptTypeAssociation defaultMatchingAssociation = getDefaultMatchingAssociation();
            if (defaultMatchingAssociation != null) {
                return defaultMatchingAssociation.getProductCmptType().getName() + "." //$NON-NLS-1$
                        + defaultMatchingAssociation.getName();
            } else {
                return Messages.AssociationEditDialog_label_none;
            }
        }

        /**
         * @param configurable The configured to set.
         */
        public void setConfigurable(boolean configurable) {
            getAssociation().setConfigurable(configurable);
        }

        /**
         * @return Returns the configured.
         */
        public boolean isConfigurable() {
            if (!getAssociation().isConstrainedByProductStructure(ipsProject)) {
                return false;
            }
            return getAssociation().isConfigurable();
        }

        /**
         * Updating the list of possibly matching associations in the given {@link ComboViewerField}
         * if the matching association source has changed
         * 
         * @param configuringAssociationField The {@link ComboViewerField} that should get the list
         *            of possibly matchning associations
         */
        public void updateConstrainingAssociationCombo(final ComboViewerField<String> configuringAssociationField) {
            String configuredAssociationSourceName = getAssociation().getMatchingAssociationSource();
            if (actualConfiguredAssociationSourceName != null
                    && actualConfiguredAssociationSourceName.equals(configuredAssociationSourceName)) {
                return;
            }
            actualConfiguredAssociationSourceName = configuredAssociationSourceName;
            IProductCmptType configuredAssociationSource = null;
            configuredAssociationSource = ipsProject.findProductCmptType(configuredAssociationSourceName);
            if (configuredAssociationSource == null) {
                configuringAssociationField.setInput(new String[0]);
                return;
            }
            List<IAssociation> associations = configuredAssociationSource.getAssociations();
            ArrayList<String> associationsNames = new ArrayList<>();
            for (IAssociation aAsso : associations) {
                if (!aAsso.getAssociationType().isCompositionDetailToMaster()) {
                    associationsNames.add(aAsso.getName());
                }
            }
            String[] input = associationsNames.toArray(new String[associationsNames.size()]);
            configuringAssociationField.setInput(input);
        }
    }
}

/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.ButtonField;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.type.DerivedUnionCompletionProcessor;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
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

    private final IProductCmptTypeAssociation association;
    private final PmoAssociation pmoAssociation;
    private final ExtensionPropertyControlFactory extFactory;

    // the association that matches this when the dialog is opened. We need to update this
    // association in case of changes to the matching association.
    private IPolicyCmptTypeAssociation oldMatchingAssociation;

    public AssociationEditDialog(IProductCmptTypeAssociation association, Shell parentShell) {
        super(association, parentShell, Messages.AssociationEditDialog_title, true);
        this.association = association;
        initialName = association.getName();
        initialPluralName = association.getTargetRolePlural();
        this.pmoAssociation = new PmoAssociation(association);
        extFactory = new ExtensionPropertyControlFactory(association);
        oldMatchingAssociation = association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject());
        getBindingContext().addIgnoredMessageCode(IProductCmptTypeAssociation.MSGCODE_MATCHING_ASSOCIATION_INVALID);
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

        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.AssociationEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));
        new MatchingAssociationTabItem(folder, SWT.NONE);

        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite panel = createTabItemComposite(folder, 1, false);

        createExtensionArea(panel, IExtensionPropertyDefinition.POSITION_TOP);
        createGeneralGroup(getToolkit().createGroup(panel, Messages.AssociationEditDialog_generalGroup));

        Group displayGroup = getToolkit().createGroup(panel, Messages.EditDialog_displayGroup);
        createDisplayGroupContent(displayGroup);

        createDerivedUnionGroup(getToolkit().createGroup(panel, Messages.AssociationEditDialog_derivedUnionGroup));
        createExtensionArea(panel, IExtensionPropertyDefinition.POSITION_BOTTOM);

        extFactory.bind(getBindingContext());

        return panel;
    }

    private void createExtensionArea(Composite parent, String position) {
        if (!extFactory.needsToCreateControlsFor(position)) {
            return;
        }
        Composite workArea = getToolkit().createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        extFactory.createControls(workArea, getToolkit(), association, position);
    }

    private void createGeneralGroup(Composite parent) {
        Composite workArea = getToolkit().createLabelEditColumnComposite(parent);

        // target
        getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_targetLabel);
        ProductCmptType2RefControl targetControl = new ProductCmptType2RefControl(association.getIpsProject(), workArea,
                getToolkit(), false);
        getBindingContext().bindContent(targetControl, association, IProductCmptTypeAssociation.PROPERTY_TARGET);
        targetControl.setFocus();

        // aggregation kind
        getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_typeLabel);
        Combo typeCombo = getToolkit().createCombo(workArea);
        getBindingContext().bindContent(typeCombo, association, IAssociation.PROPERTY_ASSOCIATION_TYPE,
                IProductCmptTypeAssociation.APPLICABLE_ASSOCIATION_TYPES);

        // Changing over time checkbox
        getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_changeOverTimeLabel);
        Button changeOverTimeCheckbox = getToolkit().createButton(workArea,
                NLS.bind(Messages.AssociationEditDialog_changeOverTimeCheckbox, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural()),
                SWT.CHECK);
        getBindingContext().bindContent(changeOverTimeCheckbox, association,
                IProductCmptTypeAssociation.PROPERTY_CHANGING_OVER_TIME);

        // role singular
        getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_roleSingularLabel);
        final Text targetRoleSingularText = getToolkit().createText(workArea);
        getBindingContext().bindContent(targetRoleSingularText, association,
                IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
                    association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
                }
            }
        });

        // role plural
        getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_rolePluralLabel);
        final Text targetRolePluralText = getToolkit().createText(workArea);
        getBindingContext().bindContent(targetRolePluralText, association,
                IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralText.getText()) && association.isTargetRolePluralRequired()) {
                    association.setTargetRolePlural(association.getDefaultTargetRolePlural());
                }
            }
        });

        // min cardinality
        getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_minCardLabel);
        Text minCardinalityText = getToolkit().createText(workArea);
        CardinalityField cardinalityField = new CardinalityField(minCardinalityText);
        getBindingContext().bindContent(cardinalityField, association,
                IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);

        // max cardinality
        getToolkit().createFormLabel(workArea, Messages.AssociationEditDialog_maxCardLabel);
        Text maxCardinalityText = getToolkit().createText(workArea);
        cardinalityField = new CardinalityField(maxCardinalityText);
        getBindingContext().bindContent(cardinalityField, association,
                IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
    }

    private void createDisplayGroupContent(Composite c) {
        Composite workArea = getToolkit().createLabelEditColumnComposite(c);
        final Button checkbox = getToolkit().createButton(workArea, Messages.AttributeEditDialog_visibilityNote,
                SWT.CHECK);
        ButtonField buttonField = new ButtonField(checkbox, false);
        getBindingContext().bindContent(buttonField, association, IProductCmptTypeAssociation.PROPERTY_RELEVANT);
    }

    private void createDerivedUnionGroup(Composite workArea) {

        Button constrainCheckbox = getToolkit().createButton(workArea, Messages.AssociationEditDialog_constrain,
                SWT.CHECK);
        constrainCheckbox.setToolTipText(Messages.AssociationEditDialog_toolTipOverride);
        getBindingContext().bindContent(constrainCheckbox, association, IAssociation.PROPERTY_CONSTRAIN);
        getBindingContext().bindEnabled(constrainCheckbox, pmoAssociation, PmoAssociation.PROPERTY_CONSTRAIN_ENABLED);

        Button derivedUnionCheckbox = getToolkit().createButton(workArea,
                Messages.AssociationEditDialog_derivedUnionCheckbox, SWT.CHECK);
        derivedUnionCheckbox.setToolTipText(Messages.AssociationEditDialog_toolTipDerivedUnion);
        getBindingContext().bindContent(derivedUnionCheckbox, association,
                IProductCmptTypeAssociation.PROPERTY_DERIVED_UNION);
        getBindingContext().bindEnabled(derivedUnionCheckbox, pmoAssociation,
                PmoAssociation.PROPERTY_DERIVEDUNION_OR_SUBSET_ENABLED);

        Button subsetCheckbox = getToolkit().createButton(workArea, Messages.AssociationEditDialog_subsetCheckbox,
                SWT.CHECK);
        subsetCheckbox.setToolTipText(Messages.AssociationEditDialog_toolTipDerivedUnion);
        getBindingContext().bindContent(subsetCheckbox, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
        getBindingContext().bindEnabled(subsetCheckbox, pmoAssociation,
                PmoAssociation.PROPERTY_DERIVEDUNION_OR_SUBSET_ENABLED);

        Composite temp = getToolkit().createLabelEditColumnComposite(workArea);
        temp.setLayoutData(new GridData(GridData.FILL_BOTH));

        getToolkit().createFormLabel(temp, Messages.AssociationEditDialog_derivedUnionLabel);
        Text unionText = getToolkit().createText(temp);
        getBindingContext().bindContent(unionText, association,
                IProductCmptTypeAssociation.PROPERTY_SUBSETTED_DERIVED_UNION);
        getBindingContext().bindEnabled(unionText, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
        DerivedUnionCompletionProcessor completionProcessor = new DerivedUnionCompletionProcessor(association);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        CompletionUtil.createHandlerForText(unionText, completionProcessor);
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
        updateMatchingPolicyCmptTypeAssociation();
        super.okPressed();
    }

    void updateMatchingPolicyCmptTypeAssociation() {
        IPolicyCmptTypeAssociation newConstrainedAssociation = pmoAssociation.matchingAssociation;

        if (oldMatchingAssociation != null) {
            needToSaveOldMatchingAssociation(newConstrainedAssociation);
        }
        if (pmoAssociation.matchingExplicitly && newConstrainedAssociation != null) {
            boolean needToSave = !newConstrainedAssociation.getPolicyCmptType().getIpsSrcFile().isDirty();
            newConstrainedAssociation.setMatchingAssociationName(association.getName());
            newConstrainedAssociation.setMatchingAssociationSource(association.getProductCmptType().getQualifiedName());
            if (needToSave) {
                try {
                    newConstrainedAssociation.getPolicyCmptType().getIpsSrcFile().save(false,
                            new NullProgressMonitor());
                } catch (CoreRuntimeException e) {
                    IpsPlugin.log(e);
                }
            }
        }
    }

    private void needToSaveOldMatchingAssociation(IPolicyCmptTypeAssociation newConstrainedAssociation) {
        if ((!pmoAssociation.matchingExplicitly || !oldMatchingAssociation.equals(newConstrainedAssociation))
                && association.getProductCmptType().getQualifiedName()
                        .equals(oldMatchingAssociation.getMatchingAssociationSource())
                && association.getName().equals(oldMatchingAssociation.getMatchingAssociationName())) {

            boolean needToSave = !oldMatchingAssociation.getPolicyCmptType().getIpsSrcFile().isDirty();
            oldMatchingAssociation.setMatchingAssociationName(StringUtils.EMPTY);
            oldMatchingAssociation.setMatchingAssociationSource(StringUtils.EMPTY);
            if (needToSave) {
                try {
                    oldMatchingAssociation.getPolicyCmptType().getIpsSrcFile().save(false, new NullProgressMonitor());
                } catch (CoreRuntimeException e) {
                    IpsPlugin.log(e);
                }
            }
        }
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

    @Override
    public boolean close() {
        pmoAssociation.dispose();
        return super.close();
    }

    private class MatchingAssociationTabItem {

        private final TabItem tabItem;

        public MatchingAssociationTabItem(TabFolder parent, int style) {
            tabItem = new TabItem(parent, style);
            tabItem.setText(Messages.AssociationEditDialog_tab_policyAssociation);
            Composite c = createTabItemComposite(parent, 1, false);
            tabItem.setControl(createPage(c));
        }

        private Control createPage(Composite c) {

            getToolkit().createVerticalSpacer(c, 2);

            Composite labelEditColumnComposite = getToolkit().createLabelEditColumnComposite(c);
            getToolkit().createLabel(labelEditColumnComposite, Messages.AssociationEditDialog_label_foundAssociation);
            Label matchingAssociationInfoLabel = getToolkit().createLabel(labelEditColumnComposite, StringUtils.EMPTY);
            getBindingContext().bindContent(matchingAssociationInfoLabel, pmoAssociation,
                    PmoAssociation.PROPERTY_INFO_LABEL);
            getToolkit().createVerticalSpacer(labelEditColumnComposite, 3);

            Group groupMatching = getToolkit().createGroup(c, Messages.AssociationEditDialog_group_selectExplicitly);
            createMatchingAssociationGroup(groupMatching);
            return c;
        }

        private void createMatchingAssociationGroup(Composite parent) {
            Composite workArea = getToolkit().createLabelEditColumnComposite(parent);
            Button constrainedCheckbox = getToolkit().createButton(workArea,
                    Messages.AssociationEditDialog_check_selectExplicitly, SWT.CHECK);
            getBindingContext().bindContent(constrainedCheckbox, pmoAssociation,
                    PmoAssociation.PROPERTY_MATCHING_EXPLICITLY);
            GridData constrainedGD = new GridData(SWT.CENTER | GridData.FILL_HORIZONTAL);
            constrainedGD.horizontalSpan = 2;
            constrainedCheckbox.setLayoutData(constrainedGD);

            getToolkit().createLabel(workArea, Messages.AssociationEditDialog_label_matchingAssociation);
            Combo constrainedCombo = getToolkit().createCombo(workArea);
            getBindingContext().bindEnabled(constrainedCombo, pmoAssociation,
                    PmoAssociation.PROPERTY_MATCHING_EXPLICITLY);
            ComboViewerField<IPolicyCmptTypeAssociation> comboViewerField = new ComboViewerField<>(
                    constrainedCombo, IPolicyCmptTypeAssociation.class);
            comboViewerField.setAllowEmptySelection(true);
            comboViewerField.setLabelProvider(new MatchingAssociationLabelProvider());
            try {
                IPolicyCmptTypeAssociation[] possiblyMatchingAssociations = association
                        .findPossiblyMatchingPolicyCmptTypeAssociations(association.getIpsProject())
                        .toArray(new IPolicyCmptTypeAssociation[0]);
                comboViewerField.setInput(possiblyMatchingAssociations);
            } catch (CoreRuntimeException e) {
                IpsPlugin.log(e);
            }
            getBindingContext().bindContent(comboViewerField, pmoAssociation,
                    PmoAssociation.PROPERTY_MATCHING_ASSOCIATION);
        }

    }

    public class PmoAssociation extends IpsObjectPartPmo {

        public static final String PROPERTY_CONSTRAIN_ENABLED = "constrainEnabled"; //$NON-NLS-1$

        public static final String PROPERTY_INFO_LABEL = "infoLabel"; //$NON-NLS-1$

        public static final String PROPERTY_SUBSET = "subset"; //$NON-NLS-1$

        public static final String PROPERTY_MATCHING_EXPLICITLY = "matchingExplicitly"; //$NON-NLS-1$

        public static final String PROPERTY_MATCHING_ASSOCIATION = "matchingAssociation"; //$NON-NLS-1$

        public static final String PROPERTY_DERIVEDUNION_OR_SUBSET_ENABLED = "derivedUnionOrSubSetEnabled"; //$NON-NLS-1$

        private boolean subset;

        private boolean matchingExplicitly;

        private IPolicyCmptTypeAssociation matchingAssociation;

        public PmoAssociation(IProductCmptTypeAssociation association) {
            super(association);
            subset = association.isSubsetOfADerivedUnion();
            matchingAssociation = association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject());
            matchingExplicitly = !StringUtils.isEmpty(association.getMatchingAssociationSource())
                    && !StringUtils.isEmpty(association.getMatchingAssociationName());
        }

        private IProductCmptTypeAssociation getAssociation() {
            return (IProductCmptTypeAssociation)super.getIpsObjectPartContainer();
        }

        public boolean isSubset() {
            return subset;
        }

        public void setSubset(boolean newValue) {
            subset = newValue;
            if (!subset) {
                getAssociation().setSubsettedDerivedUnion(""); //$NON-NLS-1$
            } else {
                IAssociation[] derivedUnionCandidates;
                try {
                    derivedUnionCandidates = getAssociation().findDerivedUnionCandidates(association.getIpsProject());
                    if (derivedUnionCandidates != null && derivedUnionCandidates.length > 0) {
                        getAssociation().setSubsettedDerivedUnion(derivedUnionCandidates[0].getName());
                    }
                } catch (CoreRuntimeException e) {
                    IpsPlugin.log(e);
                }
            }
            notifyListeners();
        }

        /**
         * @param matchingExplicitly The matchingExplicitly to set.
         */
        public void setMatchingExplicitly(boolean matchingExplicitly) {
            this.matchingExplicitly = matchingExplicitly;
            if (matchingExplicitly) {
                if (matchingAssociation != null) {
                    getAssociation().setMatchingAssociationName(matchingAssociation.getName());
                    getAssociation()
                            .setMatchingAssociationSource(matchingAssociation.getPolicyCmptType().getQualifiedName());
                }
            } else {
                getAssociation().setMatchingAssociationName(StringUtils.EMPTY);
                getAssociation().setMatchingAssociationSource(StringUtils.EMPTY);
                matchingAssociation = getAssociation()
                        .findMatchingPolicyCmptTypeAssociation(getAssociation().getIpsProject());
            }
            notifyListeners();
        }

        /**
         * @return Returns the matchingExplicitly.
         */
        public boolean isMatchingExplicitly() {
            return matchingExplicitly;
        }

        /**
         * @param matchingAssociation The matchingAssociation to set.
         */
        public void setMatchingAssociation(IPolicyCmptTypeAssociation matchingAssociation) {
            this.matchingAssociation = matchingAssociation;
            getAssociation().setMatchingAssociationSource(matchingAssociation.getPolicyCmptType().getQualifiedName());
            getAssociation().setMatchingAssociationName(matchingAssociation.getName());
            notifyListeners();
        }

        /**
         * @return Returns the matchingAssociation.
         */
        public IPolicyCmptTypeAssociation getMatchingAssociation() {
            return matchingAssociation;
        }

        public String getInfoLabel() {
            IPolicyCmptTypeAssociation defaultPolicyCmptTypeAssociation;
            defaultPolicyCmptTypeAssociation = getAssociation()
                    .findDefaultPolicyCmptTypeAssociation(getAssociation().getIpsProject());
            if (defaultPolicyCmptTypeAssociation != null) {
                return defaultPolicyCmptTypeAssociation.getPolicyCmptType().getName() + "." //$NON-NLS-1$
                        + defaultPolicyCmptTypeAssociation.getName();
            }
            return Messages.AssociationEditDialog_label_none;
        }

        public boolean isConstrainEnabled() {
            if (StringUtils.isEmpty(getAssociation().getProductCmptType().getSupertype())) {
                // only disable the checkbox when it's not checked
                // because there is no way to remove the mark.
                if (!getAssociation().isConstrain()) {
                    return false;
                }
            }
            if (getAssociation().isDerivedUnion() || isSubset()) {
                return false;
            }
            return true;
        }

        public boolean isDerivedUnionOrSubSetEnabled() {
            return !getAssociation().isConstrain();
        }
    }

    private final class MatchingAssociationLabelProvider extends DefaultLabelProvider {
        @Override
        public String getText(Object element) {
            StringBuilder result = new StringBuilder();
            if (element instanceof IPolicyCmptTypeAssociation) {
                IPolicyCmptTypeAssociation policyCmptTypeAssociation = (IPolicyCmptTypeAssociation)element;
                String associationName = policyCmptTypeAssociation.getName();
                String pcType = policyCmptTypeAssociation.getPolicyCmptType().getName();
                result.append(NLS.bind(Messages.AssociationEditDialog_associationLabel_AssociationNameInPolicyCmptType,
                        associationName, pcType));
                IProductCmptTypeAssociation matchingProductCmptTypeAssociation = policyCmptTypeAssociation
                        .findMatchingProductCmptTypeAssociation(association.getIpsProject());
                if (matchingProductCmptTypeAssociation != null && matchingProductCmptTypeAssociation != association) {
                    result.append(NLS.bind(" (" + Messages.AssociationEditDialog_hint_AlreadyConfiguredIn + ")", //$NON-NLS-1$//$NON-NLS-2$
                            matchingProductCmptTypeAssociation.getName()));
                }
            }
            return result.toString();
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.type.DerivedUnionCompletionProcessor;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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

    private IProductCmptTypeAssociation association;
    private PmoAssociation pmoAssociation;
    private ExtensionPropertyControlFactory extFactory;

    // the association that matches this when the dialog is opened. We need to update this
    // association in case of changes to the matching association.
    private IPolicyCmptTypeAssociation oldMatchingAssociation;

    public AssociationEditDialog(IProductCmptTypeAssociation association, Shell parentShell) {
        super(association, parentShell, Messages.AssociationEditDialog_title, true);
        this.association = association;
        initialName = association.getName();
        initialPluralName = association.getTargetRolePlural();
        this.pmoAssociation = new PmoAssociation();
        extFactory = new ExtensionPropertyControlFactory(association.getClass());
        try {
            oldMatchingAssociation = association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject());
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        bindingContext.addIgnoredMessageCode(IProductCmptTypeAssociation.MSGCODE_MATCHING_ASSOCIATION_INVALID);
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
        createGenerellGroup(uiToolkit.createGroup(panel, Messages.AssociationEditDialog_generalGroup));

        createDerivedUnionGroup(uiToolkit.createGroup(panel, Messages.AssociationEditDialog_derivedUnionGroup));
        createExtensionArea(panel, IExtensionPropertyDefinition.POSITION_BOTTOM);

        extFactory.bind(bindingContext);

        return panel;
    }

    private void createExtensionArea(Composite parent, String position) {
        if (!extFactory.needsToCreateControlsFor(association, position)) {
            return;
        }
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        extFactory.createControls(workArea, uiToolkit, association, position);
    }

    private void createGenerellGroup(Composite parent) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);

        // target
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_targetLabel);
        ProductCmptType2RefControl targetControl = new ProductCmptType2RefControl(association.getIpsProject(),
                workArea, uiToolkit, false);
        bindingContext.bindContent(targetControl, association, IProductCmptTypeAssociation.PROPERTY_TARGET);
        targetControl.setFocus();

        // aggregation kind
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_typeLabel);
        Combo typeCombo = uiToolkit.createCombo(workArea);
        bindingContext.bindContent(typeCombo, association, IAssociation.PROPERTY_ASSOCIATION_TYPE,
                IProductCmptTypeAssociation.APPLICABLE_ASSOCIATION_TYPES);

        // role singular
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_roleSingularLabel);
        final Text targetRoleSingularText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRoleSingularText, association,
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
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_rolePluralLabel);
        final Text targetRolePluralText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRolePluralText, association,
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
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_minCardLabel);
        Text minCardinalityText = uiToolkit.createText(workArea);
        CardinalityField cardinalityField = new CardinalityField(minCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);

        // max cardinality
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_maxCardLabel);
        Text maxCardinalityText = uiToolkit.createText(workArea);
        cardinalityField = new CardinalityField(maxCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
    }

    private void createDerivedUnionGroup(Composite workArea) {
        Checkbox derivedUnion = uiToolkit.createCheckbox(workArea, Messages.AssociationEditDialog_derivedUnionCheckbox);
        bindingContext.bindContent(derivedUnion, association, IProductCmptTypeAssociation.PROPERTY_DERIVED_UNION);

        Checkbox subsetCheckbox = uiToolkit.createCheckbox(workArea, Messages.AssociationEditDialog_subsetCheckbox);
        bindingContext.bindContent(subsetCheckbox, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);

        Composite temp = uiToolkit.createLabelEditColumnComposite(workArea);
        temp.setLayoutData(new GridData(GridData.FILL_BOTH));

        uiToolkit.createFormLabel(temp, Messages.AssociationEditDialog_derivedUnionLabel);
        Text unionText = uiToolkit.createText(temp);
        bindingContext
                .bindContent(unionText, association, IProductCmptTypeAssociation.PROPERTY_SUBSETTED_DERIVED_UNION);
        bindingContext.bindEnabled(unionText, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
        DerivedUnionCompletionProcessor completionProcessor = new DerivedUnionCompletionProcessor(association);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler
                .createHandlerForText(unionText, CompletionUtil.createContentAssistant(completionProcessor));
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
        if (oldMatchingAssociation != null
                && (!pmoAssociation.matchingExplicitly || !oldMatchingAssociation.equals(newConstrainedAssociation))
                && association.getProductCmptType().getQualifiedName()
                        .equals(oldMatchingAssociation.getMatchingAssociationSource())
                && association.getName().equals(oldMatchingAssociation.getMatchingAssociationName())) {

            boolean needToSave = !oldMatchingAssociation.getPolicyCmptType().getIpsSrcFile().isDirty();
            oldMatchingAssociation.setMatchingAssociationName(StringUtils.EMPTY);
            oldMatchingAssociation.setMatchingAssociationSource(StringUtils.EMPTY);
            if (needToSave) {
                try {
                    oldMatchingAssociation.getPolicyCmptType().getIpsSrcFile().save(false, new NullProgressMonitor());
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
        }
        if (pmoAssociation.matchingExplicitly && newConstrainedAssociation != null) {
            boolean needToSave = !newConstrainedAssociation.getPolicyCmptType().getIpsSrcFile().isDirty();
            newConstrainedAssociation.setMatchingAssociationName(association.getName());
            newConstrainedAssociation.setMatchingAssociationSource(association.getProductCmptType().getQualifiedName());
            if (needToSave) {
                try {
                    newConstrainedAssociation.getPolicyCmptType().getIpsSrcFile()
                            .save(false, new NullProgressMonitor());
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
        }
    }

    private void applyRenameRefactoring(String newName, String newPluralName) {
        // First, reset the initial names as otherwise errors 'names must not equal' will occur
        association.setTargetRoleSingular(initialName);
        association.setTargetRolePlural(initialPluralName);

        IIpsRefactoring ipsRenameRefactoring = IpsPlugin.getIpsRefactoringFactory().createRenameRefactoring(
                association, newName, newPluralName, false);
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

            uiToolkit.createVerticalSpacer(c, 2);

            Composite labelEditColumnComposite = uiToolkit.createLabelEditColumnComposite(c);
            uiToolkit.createLabel(labelEditColumnComposite, Messages.AssociationEditDialog_label_foundAssociation);
            Label matchingAssociationInfoLabel = uiToolkit.createLabel(labelEditColumnComposite, StringUtils.EMPTY);
            bindingContext
                    .bindContent(matchingAssociationInfoLabel, pmoAssociation, PmoAssociation.PROPERTY_INFO_LABEL);
            uiToolkit.createVerticalSpacer(labelEditColumnComposite, 3);

            Group groupMatching = uiToolkit.createGroup(c, Messages.AssociationEditDialog_group_selectExplicitly);
            createMatchingAssociationGroup(groupMatching);
            return c;
        }

        private void createMatchingAssociationGroup(Composite parent) {
            Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
            Checkbox constrainedCheckbox = uiToolkit.createCheckbox(workArea,
                    Messages.AssociationEditDialog_check_selectExplicitly);
            ((GridData)constrainedCheckbox.getLayoutData()).horizontalSpan = 2;
            bindingContext
                    .bindContent(constrainedCheckbox, pmoAssociation, PmoAssociation.PROPERTY_MATCHING_EXPLICITLY);

            uiToolkit.createLabel(workArea, Messages.AssociationEditDialog_label_matchingAssociation);
            Combo constrainedCombo = uiToolkit.createCombo(workArea);
            bindingContext.bindEnabled(constrainedCombo, pmoAssociation, PmoAssociation.PROPERTY_MATCHING_EXPLICITLY);
            ComboViewerField<IPolicyCmptTypeAssociation> comboViewerField = new ComboViewerField<IPolicyCmptTypeAssociation>(
                    constrainedCombo, IPolicyCmptTypeAssociation.class);
            comboViewerField.setAllowEmptySelection(true);
            comboViewerField.setLabelProvider(new MatchingAssociationLabelProvider());
            try {
                IPolicyCmptTypeAssociation[] possiblyMatchingAssociations = association
                        .findPossiblyMatchingPolicyCmptTypeAssociations(association.getIpsProject()).toArray(
                                new IPolicyCmptTypeAssociation[0]);
                comboViewerField.setInput(possiblyMatchingAssociations);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            bindingContext.bindContent(comboViewerField, pmoAssociation, PmoAssociation.PROPERTY_MATCHING_ASSOCIATION);
        }

    }

    public class PmoAssociation extends IpsObjectPartPmo {

        public static final String PROPERTY_INFO_LABEL = "infoLabel"; //$NON-NLS-1$

        public final static String PROPERTY_SUBSET = "subset"; //$NON-NLS-1$

        public final static String PROPERTY_MATCHING_EXPLICITLY = "matchingExplicitly"; //$NON-NLS-1$

        public final static String PROPERTY_MATCHING_ASSOCIATION = "matchingAssociation"; //$NON-NLS-1$

        private boolean subset;

        private boolean matchingExplicitly;

        private IPolicyCmptTypeAssociation matchingAssociation;

        public PmoAssociation() {
            super(association);
            subset = association.isSubsetOfADerivedUnion();
            try {
                matchingAssociation = association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject());
                matchingExplicitly = !StringUtils.isEmpty(association.getMatchingAssociationSource())
                        && !StringUtils.isEmpty(association.getMatchingAssociationName());
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }

        public boolean isSubset() {
            return subset;
        }

        public void setSubset(boolean newValue) {
            subset = newValue;
            if (!subset) {
                association.setSubsettedDerivedUnion(""); //$NON-NLS-1$
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
                    association.setMatchingAssociationName(matchingAssociation.getName());
                    association
                            .setMatchingAssociationSource(matchingAssociation.getPolicyCmptType().getQualifiedName());
                }
            } else {
                association.setMatchingAssociationName(StringUtils.EMPTY);
                association.setMatchingAssociationSource(StringUtils.EMPTY);
                try {
                    matchingAssociation = association
                            .findMatchingPolicyCmptTypeAssociation(association.getIpsProject());
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
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
            association.setMatchingAssociationSource(matchingAssociation.getPolicyCmptType().getQualifiedName());
            association.setMatchingAssociationName(matchingAssociation.getName());
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
            try {
                defaultPolicyCmptTypeAssociation = association.findDefaultPolicyCmptTypeAssociation(association
                        .getIpsProject());
                if (defaultPolicyCmptTypeAssociation != null) {
                    return defaultPolicyCmptTypeAssociation.getPolicyCmptType().getName() + "." //$NON-NLS-1$
                            + defaultPolicyCmptTypeAssociation.getName();
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return Messages.AssociationEditDialog_label_none;
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
                try {
                    IProductCmptTypeAssociation matchingProductCmptTypeAssociation = policyCmptTypeAssociation
                            .findMatchingProductCmptTypeAssociation(association.getIpsProject());
                    if (matchingProductCmptTypeAssociation != null && matchingProductCmptTypeAssociation != association) {
                        result.append(NLS.bind(" (" + Messages.AssociationEditDialog_hint_AlreadyConfiguredIn + ")", //$NON-NLS-1$//$NON-NLS-2$
                                matchingProductCmptTypeAssociation.getName()));
                    }
                } catch (CoreException e) {
                    // do nothing - it is only a hint
                }
            }
            return result.toString();
        }
    }
}

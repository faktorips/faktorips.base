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

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
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
import org.faktorips.devtools.core.internal.model.pctype.PersistentAssociationInfo;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo.FetchType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.binding.ButtonTextBinding;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.binding.EnableBinding;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.StringUtil;
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

    private IIpsProject ipsProject;
    private IPolicyCmptTypeAssociation association;
    private PmoPolicyCmptTypeAssociation pmoAssociation;

    private ExtensionPropertyControlFactory extFactory;
    private IPolicyCmptTypeAssociation inverseAssociation;
    private Composite joinColumnComposite;
    private Composite cascadeTypesComposite;

    // the association that matches this when the dialog is opened. We need to update this
    // association in case of changes to the matching association.
    private IProductCmptTypeAssociation oldMatchingAssociation;

    public AssociationEditDialog(IPolicyCmptTypeAssociation relation2, Shell parentShell) {
        super(relation2, parentShell, Messages.AssociationEditDialog_title, true);
        association = relation2;
        initialName = association.getName();
        initialPluralName = association.getTargetRolePlural();
        ipsProject = association.getIpsProject();
        pmoAssociation = new PmoPolicyCmptTypeAssociation(association);
        extFactory = new ExtensionPropertyControlFactory(association.getClass());
        searchInverseAssociation();

        try {
            oldMatchingAssociation = association.findMatchingProductCmptTypeAssociation(association.getIpsProject());
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        /*
         * In case direct refactoring is activated the inverse relation will be updated by the
         * refactoring
         */
        if (IpsPlugin.getDefault().getIpsPreferences().isRefactoringModeDirect()) {
            bindingContext.addIgnoredMessageCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH);
        }
        bindingContext.addIgnoredMessageCode(IPolicyCmptTypeAssociation.MSGCODE_MATCHING_ASSOCIATION_INVALID);
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

    private void searchInverseAssociation() {
        try {
            inverseAssociation = association.findInverseAssociation(ipsProject);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.AssociationEditDialog_textFirstPage);
        firstPage.setControl(createFirstPage(folder));

        createPersistenceTabItemIfNecessary(folder);

        return folder;
    }

    /**
     * Creates the first tab page. With the following goups: general, policy side, and product side.
     */
    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Group groupGeneral = uiToolkit.createGroup(c, Messages.AssociationEditDialog_generalGroup);
        createGeneralControls(groupGeneral);

        uiToolkit.createVerticalSpacer(c, 8);
        Group groupConfiguration = uiToolkit.createGroup(c,
                Messages.AssociationEditDialog_group_matchingProdCmptTypeAssociation);
        createMatchingAssociationGroup(groupConfiguration);

        createQualificationGroup(uiToolkit.createGroup(c, Messages.AssociationEditDialog_qualificationGroup));

        uiToolkit.createVerticalSpacer(c, 8);
        new AssociationDerivedUnionGroup(uiToolkit, bindingContext, c, association);

        return c;
    }

    private void createGeneralControls(Composite c) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        // top extensions
        extFactory.createControls(workArea, uiToolkit, association, IExtensionPropertyDefinition.POSITION_TOP);

        // target
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_targetLabel);
        PcTypeRefControl targetControl = uiToolkit.createPcTypeRefControl(association.getIpsProject(), workArea);
        bindingContext.bindContent(targetControl, association, IAssociation.PROPERTY_TARGET);

        // type
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_associationTypeLabel);
        final Combo typeCombo = uiToolkit.createCombo(workArea);
        bindingContext.bindContent(typeCombo, association, IAssociation.PROPERTY_ASSOCIATION_TYPE,
                IPolicyCmptTypeAssociation.APPLICABLE_ASSOCIATION_TYPES);
        typeCombo.setFocus();

        // role singular
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_targetRoleSingularLabel);
        final Text targetRoleSingularText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRoleSingularText, association, IAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
                    association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
                }
            }
        });

        // role plural
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_targetRolePluralLabel);
        final Text targetRolePluralText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRolePluralText, association, IAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralText.getText()) && association.isTargetRolePluralRequired()) {
                    association.setTargetRolePlural(association.getDefaultTargetRolePlural());
                }
            }
        });

        // min cardinality
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_minimumCardinality);
        Text minCardinalityText = uiToolkit.createText(workArea);
        CardinalityField cardinalityField = new CardinalityField(minCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IAssociation.PROPERTY_MIN_CARDINALITY);

        // max cardinality
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_maximumCardinality);
        Text maxCardinalityText = uiToolkit.createText(workArea);
        cardinalityField = new CardinalityField(maxCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IAssociation.PROPERTY_MAX_CARDINALITY);

        // inverse association
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_inverseAssociationLabel);
        Composite inverseAssoComposite = uiToolkit.createComposite(workArea);
        inverseAssoComposite.setLayout(uiToolkit.createNoMarginGridLayout(2, false));
        inverseAssoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        Text inverseRelationText = uiToolkit.createText(inverseAssoComposite);
        bindingContext.bindContent(inverseRelationText, association,
                IPolicyCmptTypeAssociation.PROPERTY_INVERSE_ASSOCIATION);
        InverseAssociationCompletionProcessor inverseAssociationCompletionProcessor = new InverseAssociationCompletionProcessor(
                association);
        inverseAssociationCompletionProcessor.setComputeProposalForEmptyPrefix(true);
        CompletionUtil.createHandlerForText(inverseRelationText, inverseAssociationCompletionProcessor);

        // shared associations
        if (ipsProject.getProperties().isSharedDetailToMasterAssociations()) {
            Button sharedAssociationCheck = uiToolkit.createButton(inverseAssoComposite,
                    Messages.AssociationEditDialog_sharedAssociations, SWT.CHECK);
            sharedAssociationCheck.setToolTipText(Messages.AssociationEditDialog_sharedAssociationsTooltip);
            bindingContext.bindContent(sharedAssociationCheck, association,
                    IPolicyCmptTypeAssociation.PROPERTY_SHARED_ASSOCIATION);
            bindingContext.bindEnabled(inverseRelationText, association,
                    IPolicyCmptTypeAssociation.PROPERTY_SHARED_ASSOCIATION, false);
            bindingContext
                    .add(new EnableBinding(sharedAssociationCheck, association,
                            IPolicyCmptTypeAssociation.PROPERTY_ASSOCIATION_TYPE,
                            AssociationType.COMPOSITION_DETAIL_TO_MASTER));
        } else {
            ((GridData)inverseRelationText.getLayoutData()).verticalSpan = 2;
        }

        // bottom extensions
        extFactory.createControls(workArea, uiToolkit, association, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(bindingContext);
    }

    private void createMatchingAssociationGroup(Composite c) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        Checkbox constrainedCheckbox = uiToolkit.createCheckbox(workArea,
                Messages.AssociationEditDialog_check_selectMatchingAssociationExpliclitly);
        ((GridData)constrainedCheckbox.getLayoutData()).horizontalSpan = 2;
        bindingContext.bindContent(constrainedCheckbox, pmoAssociation,
                PmoPolicyCmptTypeAssociation.PROPERTY_MATCHING_EXPLICITLY);

        uiToolkit.createVerticalSpacer(workArea, 4);

        Label matchingAssociationInfoLabel = uiToolkit.createLabel(workArea, StringUtils.EMPTY);
        ((GridData)matchingAssociationInfoLabel.getLayoutData()).horizontalSpan = 2;
        bindingContext.bindContent(matchingAssociationInfoLabel, pmoAssociation,
                PmoPolicyCmptTypeAssociation.PROPERTY_INFO_LABEL);

        uiToolkit.createLabel(workArea, Messages.AssociationEditDialog_label_productCmptType);
        ProductCmptType2RefControl refControl = new ProductCmptType2RefControl(ipsProject, workArea, uiToolkit, false);
        EditField<String> refControlEditField = bindingContext.bindContent(refControl, pmoAssociation,
                IPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE);
        bindingContext.bindProblemMarker(refControlEditField, association,
                IPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE);

        uiToolkit.createLabel(workArea, Messages.AssociationEditDialog_label_matchingAssociation);
        Combo matchingAssociation = uiToolkit.createCombo(workArea);
        final ComboViewerField<String> configuringAssociationField = new ComboViewerField<String>(matchingAssociation,
                String.class);
        configuringAssociationField.setAllowEmptySelection(true);

        bindingContext.bindEnabled(matchingAssociation, pmoAssociation,
                PmoPolicyCmptTypeAssociation.PROPERTY_MATCHING_EXPLICITLY);
        bindingContext.bindContent(configuringAssociationField, pmoAssociation,
                IPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_NAME);
        bindingContext.bindProblemMarker(configuringAssociationField, association,
                IPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_NAME);

        bindingContext.bindEnabled(refControl, pmoAssociation,
                PmoPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_REF_CONTROL_ENABLED);
        bindingContext.add(new ControlPropertyBinding(refControl, association,
                IPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE, String.class) {

            @Override
            public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                pmoAssociation.updateConstrainingAssociationCombo(configuringAssociationField);
            }

        });

        pmoAssociation.updateConstrainingAssociationCombo(configuringAssociationField);
    }

    private void createQualificationGroup(Composite c) {
        Composite workArea = uiToolkit.createGridComposite(c, 1, true, true);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        Checkbox qualifiedCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(qualifiedCheckbox, association, IAssociation.PROPERTY_QUALIFIED);
        bindingContext.bindEnabled(qualifiedCheckbox, pmoAssociation,
                PmoPolicyCmptTypeAssociation.PROPERTY_QUALIFICATION_POSSIBLE);
        Label note = uiToolkit.createFormLabel(workArea, StringUtils.rightPad("", 120)); //$NON-NLS-1$
        bindingContext.bindContent(note, pmoAssociation, PmoPolicyCmptTypeAssociation.PROPERTY_QUALIFICATION_NOTE);
        bindingContext.add(new ButtonTextBinding(qualifiedCheckbox, pmoAssociation,
                PmoPolicyCmptTypeAssociation.PROPERTY_QUALIFICATION_LABEL));
    }

    private void createPersistenceTabItemIfNecessary(TabFolder tabFolder) {
        if (!ipsProject.getProperties().isPersistenceSupportEnabled()) {
            return;
        }
        TabItem persistencePage = new TabItem(tabFolder, SWT.NONE);
        persistencePage.setText(Messages.AssociationEditDialog_labelPersistence);

        Composite c = createTabItemComposite(tabFolder, 1, false);
        persistencePage.setControl(c);

        final Checkbox checkTransient = uiToolkit.createCheckbox(c,
                Messages.AssociationEditDialog_labelAssociationIsTransient);
        checkTransient.setEnabled(false);
        bindingContext.bindContent(checkTransient, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_TRANSIENT);

        final Composite allPersistentProps = uiToolkit.createGridComposite(c, 1, true, true);

        uiToolkit.createVerticalSpacer(allPersistentProps, 12);
        final Composite joinTableComposite = createGroupJoinTable(allPersistentProps);

        uiToolkit.createVerticalSpacer(allPersistentProps, 12);
        final Group groupForeignKey = createGroupForeignKey(allPersistentProps);

        uiToolkit.createVerticalSpacer(allPersistentProps, 12);
        createGroupOtherPersistentProps(allPersistentProps);

        uiToolkit.createVerticalSpacer(allPersistentProps, 12);
        createGroupCascadeType(allPersistentProps);

        // persistence is enabled initialize enable / disable bindings
        // disable all persistent controls if attribute is marked as transient
        bindingContext.add(new NotificationPropertyBinding(allPersistentProps, association
                .getPersistenceAssociatonInfo(), IPersistentAssociationInfo.PROPERTY_TRANSIENT, Boolean.TYPE) {
            @Override
            public void propertyChanged() {
                IPersistentAssociationInfo associationInfo = (IPersistentAssociationInfo)getObject();
                boolean persistEnabled = isPersistEnabled(associationInfo);
                if (!persistEnabled) {
                    uiToolkit.setDataChangeable(checkTransient, false);
                    uiToolkit.setDataChangeable(getControl(), false);
                } else {
                    uiToolkit.setDataChangeable(getControl(), !associationInfo.isTransient());
                }
            }
        });
        // disable join table
        bindingContext.add(new NotificationPropertyBinding(joinTableComposite, association
                .getPersistenceAssociatonInfo(), IPersistentAssociationInfo.PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION,
                Boolean.TYPE) {
            @Override
            public void propertyChanged() {
                IPersistentAssociationInfo associationInfo = (IPersistentAssociationInfo)getObject();
                boolean persistEnabled = isPersistEnabled(associationInfo);
                if (!persistEnabled) {
                    uiToolkit.setDataChangeable(getControl(), false);
                    return;
                }
                if (associationInfo.isTransient()) {
                    return;
                }
                uiToolkit.setDataChangeable(getControl(), associationInfo.isOwnerOfManyToManyAssociation());
                try {
                    uiToolkit.setDataChangeable(groupForeignKey, !associationInfo.isJoinTableRequired());
                    enableOrDisableForeignKeyColumn();
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        });
        // disable cascade type checkboxes
        bindingContext.add(new NotificationPropertyBinding(cascadeTypesComposite, association
                .getPersistenceAssociatonInfo(), IPersistentAssociationInfo.PROPERTY_CASCADE_TYPE_OVERWRITE_DEFAULT,
                Boolean.TYPE) {
            @Override
            public void propertyChanged() {
                IPersistentAssociationInfo associationInfo = (IPersistentAssociationInfo)getObject();
                boolean persistEnabled = isPersistEnabled(associationInfo);
                if (!persistEnabled) {
                    uiToolkit.setDataChangeable(getControl(), false);
                    return;
                }
                uiToolkit.setDataChangeable(getControl(), associationInfo.isCascadeTypeOverwriteDefault());
            }
        });
    }

    private Group createGroupCascadeType(Composite parentComposite) {

        Group group = uiToolkit.createGroup(parentComposite, Messages.AssociationEditDialog_labelCascadeType);
        GridData layoutData = (GridData)group.getLayoutData();
        layoutData.grabExcessVerticalSpace = false;

        bindingContext.bindContent(
                uiToolkit.createCheckbox(group, Messages.AssociationEditDialog_labelOverwriteDefaultCascadeTypes),
                association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_CASCADE_TYPE_OVERWRITE_DEFAULT);

        cascadeTypesComposite = uiToolkit.createLabelEditColumnComposite(group);
        cascadeTypesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createCheckbox(cascadeTypesComposite, Messages.AssociationEditDialog_labelCascadeTypePersist,
                IPersistentAssociationInfo.PROPERTY_CASCADE_TYPE_PERSIST);
        createCheckbox(cascadeTypesComposite, Messages.AssociationEditDialog_labelCascadeTypeMerge,
                IPersistentAssociationInfo.PROPERTY_CASCADE_TYPE_MERGE);
        createCheckbox(cascadeTypesComposite, Messages.AssociationEditDialog_labelCascadeTypeRemove,
                IPersistentAssociationInfo.PROPERTY_CASCADE_TYPE_REMOVE);
        createCheckbox(cascadeTypesComposite, Messages.AssociationEditDialog_labelCascadeTypeRefresh,
                IPersistentAssociationInfo.PROPERTY_CASCADE_TYPE_REFRESH);

        return group;
    }

    private void createCheckbox(Composite composite, String label, String property) {
        uiToolkit.createFormLabel(composite, label);
        bindingContext.bindContent(uiToolkit.createCheckbox(composite), association.getPersistenceAssociatonInfo(),
                property);
    }

    private boolean isPersistEnabled(IPersistentAssociationInfo associationInfo) {
        if (!isDataChangeable()) {
            return false;
        }
        return associationInfo.getPolicyComponentTypeAssociation().getPolicyCmptType().isPersistentEnabled();
    }

    private Group createGroupOtherPersistentProps(Composite allPersistentProps) {
        Group groupOtherProps = uiToolkit.createGroup(allPersistentProps,
                Messages.AssociationEditDialog_labelProperties);
        GridData layoutData = (GridData)groupOtherProps.getLayoutData();
        layoutData.grabExcessVerticalSpace = false;

        Composite otherPropsComposite = uiToolkit.createLabelEditColumnComposite(groupOtherProps);
        otherPropsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        uiToolkit.createFormLabel(otherPropsComposite, Messages.AssociationEditDialog_labelFetchType);
        Combo fetchTypeCombo = uiToolkit.createCombo(otherPropsComposite);
        ComboField<FetchType> fetchTypeField = new EnumField<FetchType>(fetchTypeCombo, FetchType.class);
        bindingContext.bindContent(fetchTypeField, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_FETCH_TYPE);

        String labelText = Messages.AssociationEditDialog_labelOrphanRemoval;
        if (ipsProject.getIpsArtefactBuilderSet().isPersistentProviderSupportOrphanRemoval()) {
            createCheckbox(otherPropsComposite, labelText, IPersistentAssociationInfo.PROPERTY_ORPHAN_REMOVAL);
        } else {
            Label label = uiToolkit.createFormLabel(otherPropsComposite, labelText);
            Label text = uiToolkit.createLabel(otherPropsComposite,
                    Messages.AssociationEditDialog_textNotSupportedByPersistenceProvider);
            label.setEnabled(false);
            text.setEnabled(false);
        }

        return groupOtherProps;
    }

    private Group createGroupForeignKey(Composite allPersistentProps) {
        Group groupJoinColumn = uiToolkit.createGroup(allPersistentProps,
                Messages.AssociationEditDialog_labelForeignKeyJoinColumn);
        GridData layoutData = (GridData)groupJoinColumn.getLayoutData();
        layoutData.grabExcessVerticalSpace = false;

        joinColumnComposite = uiToolkit.createLabelEditColumnComposite(groupJoinColumn);
        joinColumnComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        uiToolkit.createFormLabel(joinColumnComposite, Messages.AssociationEditDialog_labelForeignKeyColumnName);
        Text joinColumnNameText = uiToolkit.createText(joinColumnComposite);
        bindingContext.bindContent(joinColumnNameText, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NAME);

        String labelText = Messages.AssociationEditDialog_labelJoinColumnNameNullable;
        createCheckbox(joinColumnComposite, labelText, IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NULLABLE);

        PersistentAssociationInfo persistentAssociationInfo = (PersistentAssociationInfo)association
                .getPersistenceAssociatonInfo();
        if (!persistentAssociationInfo.isOwnerOfManyToManyAssociation()) {
            // special information about foreign key column
            if (persistentAssociationInfo.isForeignKeyColumnDefinedOnTargetSide(inverseAssociation)) {
                uiToolkit.createLabel(groupJoinColumn,
                        Messages.AssociationEditDialog_noteForeignKeyColumnDefinedInInverseAssociation);
            } else if (persistentAssociationInfo.isForeignKeyColumnCreatedOnTargetSide(inverseAssociation)) {
                uiToolkit.createLabel(
                        groupJoinColumn,
                        NLS.bind(Messages.AssociationEditDialog_noteForeignKeyIsColumnOfTheTargetEntity,
                                StringUtil.unqualifiedName(association.getTarget())));
            }
        }
        return groupJoinColumn;
    }

    private void enableOrDisableForeignKeyColumn() {
        PersistentAssociationInfo persistentAssociationInfo = (PersistentAssociationInfo)association
                .getPersistenceAssociatonInfo();
        boolean foreignKeyDefinedOnTargetSide = persistentAssociationInfo
                .isForeignKeyColumnDefinedOnTargetSide(inverseAssociation);
        if (persistentAssociationInfo.isOwnerOfManyToManyAssociation()) {
            foreignKeyDefinedOnTargetSide = true;
        }
        try {
            if (persistentAssociationInfo.isJoinTableRequired()) {
                return;
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        uiToolkit.setDataChangeable(joinColumnComposite, !foreignKeyDefinedOnTargetSide);
    }

    private Composite createGroupJoinTable(Composite allPersistentProps) {
        Group groupJoinTable = uiToolkit.createGroup(allPersistentProps, Messages.AssociationEditDialog_labelJoinTable);
        GridData layoutData = (GridData)groupJoinTable.getLayoutData();
        layoutData.grabExcessVerticalSpace = false;

        Checkbox checkOwner = uiToolkit.createCheckbox(groupJoinTable,
                Messages.AssociationEditDialog_labelAssociationIsOwningSideOfManyToMany);
        bindingContext.bindContent(checkOwner, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION);

        Composite joinTableComposte = uiToolkit.createLabelEditColumnComposite(groupJoinTable);
        joinTableComposte.setLayoutData(new GridData(GridData.FILL_BOTH));

        uiToolkit.createFormLabel(joinTableComposte, Messages.AssociationEditDialog_labelJoinTableName);
        Text joinTableNameText = uiToolkit.createText(joinTableComposte);
        bindingContext.bindContent(joinTableNameText, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_JOIN_TABLE_NAME);

        uiToolkit.createFormLabel(joinTableComposte, Messages.AssociationEditDialog_labelSourceColumnName);
        Text sourceColumnNameText = uiToolkit.createText(joinTableComposte);
        bindingContext.bindContent(sourceColumnNameText, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_SOURCE_COLUMN_NAME);

        uiToolkit.createFormLabel(joinTableComposte, Messages.AssociationEditDialog_labelTargetColumnName);
        Text targetColumnNameText = uiToolkit.createText(joinTableComposte);
        bindingContext.bindContent(targetColumnNameText, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_TARGET_COLUMN_NAME);

        return joinTableComposte;
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

    void updateMatchingProductCmptTypeAssociation() {
        IProductCmptTypeAssociation newConstrainedAssociation = null;
        try {
            newConstrainedAssociation = association.findMatchingProductCmptTypeAssociation(ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        if (oldMatchingAssociation != null
                && (!pmoAssociation.matchingExplicitly || !oldMatchingAssociation.equals(newConstrainedAssociation))
                && association.getPolicyCmptType().getQualifiedName()
                        .equals(oldMatchingAssociation.getMatchingAssociationSource())
                && association.getName().equals(oldMatchingAssociation.getMatchingAssociationName())) {

            boolean needToSave = !oldMatchingAssociation.getProductCmptType().getIpsSrcFile().isDirty();
            oldMatchingAssociation.setMatchingAssociationName(StringUtils.EMPTY);
            oldMatchingAssociation.setMatchingAssociationSource(StringUtils.EMPTY);
            if (needToSave) {
                try {
                    oldMatchingAssociation.getProductCmptType().getIpsSrcFile().save(false, new NullProgressMonitor());
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
        }
        if (pmoAssociation.matchingExplicitly && newConstrainedAssociation != null) {
            boolean needToSave = !newConstrainedAssociation.getProductCmptType().getIpsSrcFile().isDirty();
            newConstrainedAssociation.setMatchingAssociationName(association.getName());
            newConstrainedAssociation.setMatchingAssociationSource(association.getPolicyCmptType().getQualifiedName());
            if (needToSave) {
                try {
                    newConstrainedAssociation.getProductCmptType().getIpsSrcFile()
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

    public class PmoPolicyCmptTypeAssociation extends IpsObjectPartPmo {

        public static final String PROPERTY_MATCHING_ASSOCIATION_REF_CONTROL_ENABLED = "matchingAssociationRefControlEnabled"; //$NON-NLS-1$
        public final static String PROPERTY_QUALIFICATION_LABEL = "qualificationLabel"; //$NON-NLS-1$
        public final static String PROPERTY_QUALIFICATION_NOTE = "qualificationNote"; //$NON-NLS-1$
        public final static String PROPERTY_QUALIFICATION_POSSIBLE = "qualificationPossible"; //$NON-NLS-1$
        public static final String PROPERTY_MATCHING_EXPLICITLY = "matchingExplicitly"; //$NON-NLS-1$
        public static final String PROPERTY_INFO_LABEL = "infoLabel"; //$NON-NLS-1$

        private boolean matchingExplicitly;

        private String actualConfiguredAssociationSourceName;

        public PmoPolicyCmptTypeAssociation(IPolicyCmptTypeAssociation association) {
            super(association);
            matchingExplicitly = !StringUtils.isEmpty(association.getMatchingAssociationSource())
                    && !StringUtils.isEmpty(association.getMatchingAssociationName());
        }

        public String getQualificationLabel() {
            String label = Messages.AssociationEditDialog_associationIsQualified;
            try {
                String productCmptType = QNameUtil.getUnqualifiedName(association.findQualifierCandidate(ipsProject));
                if (StringUtils.isNotEmpty(productCmptType)) {
                    label = label + NLS.bind(Messages.AssociationEditDialog_qualifiedByType, productCmptType);
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return StringUtils.rightPad(label, 80);
        }

        public String getQualificationNote() {
            String note = Messages.AssociationEditDialog_note;
            if (!association.isCompositionMasterToDetail()) {
                note = note + Messages.AssociationEditDialog_qualificationOnlyForMasterDetail;
            } else {
                try {
                    if (!association.isQualificationPossible(ipsProject)) {
                        note = note + Messages.AssociationEditDialog_qualificationOnlyIfTheTargetTypeIsConfigurable;
                    } else {
                        note = note + Messages.AssociationEditDialog_multiplicityIsDefineddPerQualifier;
                    }
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            return StringUtils.rightPad(note, 90);
        }

        public boolean isQualificationPossible() {
            try {
                return association.isQualificationPossible(ipsProject);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
        }

        /**
         * @return Returns the refControlEnabled.
         */
        public boolean isMatchingAssociationRefControlEnabled() {
            return matchingExplicitly && !association.getPolicyCmptType().isConfigurableByProductCmptType();
        }

        /**
         * This method is binded by property {@link #PROPERTY_MATCHING_EXPLICITLY}
         * 
         * @param matchingExplicitly The matchingExplicitly to set.
         */
        public void setMatchingExplicitly(boolean matchingExplicitly) {
            this.matchingExplicitly = matchingExplicitly;
            if (matchingExplicitly) {
                IPolicyCmptType policyCmptType = association.getPolicyCmptType();
                if (policyCmptType.isConfigurableByProductCmptType()) {
                    association.setMatchingAssociationSource(policyCmptType.getProductCmptType());
                }
                IProductCmptTypeAssociation matchingProductCmptTypeAssociation = getDefaultMatchingAssociation();
                association
                        .setMatchingAssociationName(matchingProductCmptTypeAssociation != null ? matchingProductCmptTypeAssociation
                                .getName() : StringUtils.EMPTY);
            } else {
                association.setMatchingAssociationSource(StringUtils.EMPTY);
                association.setMatchingAssociationName(StringUtils.EMPTY);
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
                association.setMatchingAssociationName(matchingAssociationName);
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
                return association.getMatchingAssociationName();
            } else {
                IProductCmptTypeAssociation matchingProductCmptTypeAssociation = getDefaultMatchingAssociation();
                return matchingProductCmptTypeAssociation != null ? matchingProductCmptTypeAssociation.getName()
                        : StringUtils.EMPTY;
            }
        }

        /**
         * @param matchingAssociationSource The matchingAssociationSource to set.
         */
        public void setMatchingAssociationSource(String matchingAssociationSource) {
            if (matchingExplicitly) {
                association.setMatchingAssociationSource(matchingAssociationSource);
            }
        }

        /**
         * @return Returns the matchingAssociationSource.
         */
        public String getMatchingAssociationSource() {
            if (matchingExplicitly) {
                return association.getMatchingAssociationSource();
            } else {
                IProductCmptTypeAssociation matchingProductCmptTypeAssociation = getDefaultMatchingAssociation();
                return matchingProductCmptTypeAssociation != null ? matchingProductCmptTypeAssociation
                        .getProductCmptType().getName() : StringUtils.EMPTY;
            }
        }

        private IProductCmptTypeAssociation getDefaultMatchingAssociation() {
            try {
                IProductCmptTypeAssociation matchingProductCmptTypeAssociation = association
                        .findMatchingProductCmptTypeAssociation(ipsProject);
                return matchingProductCmptTypeAssociation;
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return null;
            }
        }

        public String getInfoLabel() {
            if (matchingExplicitly) {
                return Messages.AssociationEditDialog_info_chooseMatchingAssociation;
            } else {
                if (getDefaultMatchingAssociation() != null) {
                    return Messages.AssociationEditDialog_info_showMatchingAssociation;
                } else {
                    return Messages.AssociationEditDialog_info_noMatchingAssociation;
                }
            }
        }

        /**
         * Updating the list of possibly matching associations in the given {@link ComboViewerField}
         * if the matching association source has changed
         * 
         * @param configuringAssociationField The {@link ComboViewerField} that should get the list
         *            of possibly matchning associations
         */
        public void updateConstrainingAssociationCombo(final ComboViewerField<String> configuringAssociationField) {
            String configuredAssociationSourceName = association.getMatchingAssociationSource();
            if (actualConfiguredAssociationSourceName != null
                    && actualConfiguredAssociationSourceName.equals(configuredAssociationSourceName)) {
                return;
            }
            actualConfiguredAssociationSourceName = configuredAssociationSourceName;
            IProductCmptType configuredAssociationSource = null;
            try {
                configuredAssociationSource = ipsProject.findProductCmptType(configuredAssociationSourceName);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            if (configuredAssociationSource == null) {
                configuringAssociationField.setInput(new String[0]);
                return;
            }
            List<IAssociation> associations = configuredAssociationSource.getAssociations();
            ArrayList<String> associationsNames = new ArrayList<String>();
            for (IAssociation aAsso : associations) {
                if (!aAsso.getAssociationType().isCompositionDetailToMaster()) {
                    associationsNames.add(aAsso.getName());
                }
            }
            String[] input = associationsNames.toArray(new String[associationsNames.size()]);
            configuringAssociationField.setInput(input);
        }

    }

    private abstract class NotificationPropertyBinding extends ControlPropertyBinding {
        public NotificationPropertyBinding(Control control, Object object, String propertyName, Class<?> exptectedType) {
            super(control, object, propertyName, exptectedType);
        }

        private Object oldValue;

        @Override
        public void updateUiIfNotDisposed(String nameOfChangedProperty) {
            try {
                Object newValue = getProperty().getReadMethod().invoke(getObject(), new Object[0]);
                if (oldValue == null || !oldValue.equals(newValue)) {
                    oldValue = newValue;
                    propertyChanged();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected abstract void propertyChanged();

    }

}

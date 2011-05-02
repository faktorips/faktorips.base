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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
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
import org.faktorips.devtools.core.internal.model.type.AssociationType;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo.FetchType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.binding.ButtonTextBinding;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.binding.EnableBinding;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.type.DerivedUnionCompletionProcessor;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.StringUtil;

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
    private PmoAssociation pmoAssociation;

    private ExtensionPropertyControlFactory extFactory;
    private IPolicyCmptTypeAssociation inverseAssociation;
    private Composite joinColumnComposite;
    private Composite cascadeTypesComposite;

    public AssociationEditDialog(IPolicyCmptTypeAssociation relation2, Shell parentShell) {
        super(relation2, parentShell, Messages.AssociationEditDialog_title, true);
        association = relation2;
        initialName = association.getName();
        initialPluralName = association.getTargetRolePlural();
        ipsProject = association.getIpsProject();
        pmoAssociation = new PmoAssociation(association);
        extFactory = new ExtensionPropertyControlFactory(association.getClass());
        searchInverseAssociation();

        /*
         * In case direct refactoring is activated the inverse relation will be updated by the
         * refactoring
         */
        if (IpsPlugin.getDefault().getIpsPreferences().isRefactoringModeDirect()) {
            bindingContext.addIgnoredMessageCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH);
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

        uiToolkit.createVerticalSpacer(c, 12);
        createQualificationGroup(uiToolkit.createGroup(c, Messages.AssociationEditDialog_qualificationGroup));

        uiToolkit.createVerticalSpacer(c, 12);
        createDerivedUnionGroup(uiToolkit.createGroup(c, Messages.AssociationEditDialog_derivedUnionGroup));

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
        final Combo typeCombo = uiToolkit
                .createCombo(workArea, IPolicyCmptTypeAssociation.APPLICABLE_ASSOCIATION_TYPES);
        bindingContext.bindContent(typeCombo, association, IAssociation.PROPERTY_ASSOCIATION_TYPE,
                AssociationType.class);
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

        Composite info = uiToolkit.createGridComposite(c, 1, true, false);
        Label note = uiToolkit.createLabel(info, pmoAssociation.getConstrainedNote());
        bindingContext.bindContent(note, pmoAssociation, PmoAssociation.PROPERTY_CONSTRAINED_NOTE);

        // bottom extensions
        extFactory.createControls(workArea, uiToolkit, association, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(bindingContext);
    }

    private void createDerivedUnionGroup(Composite c) {
        // derived union checkbox
        Checkbox containerCheckbox = uiToolkit.createCheckbox(c,
                Messages.AssociationEditDialog_associationIsADerivedUnion);
        bindingContext.bindContent(containerCheckbox, association, IAssociation.PROPERTY_DERIVED_UNION);
        bindingContext.bindEnabled(containerCheckbox, association,
                IPolicyCmptTypeAssociation.PROPERTY_SUBSETTING_DERIVED_UNION_APPLICABLE);

        // is subset checkbox
        Checkbox subsetCheckbox = uiToolkit.createCheckbox(c, Messages.AssociationEditDialog_associationDefinesSubset);
        bindingContext.bindContent(subsetCheckbox, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
        bindingContext.bindEnabled(subsetCheckbox, association,
                IPolicyCmptTypeAssociation.PROPERTY_SUBSETTING_DERIVED_UNION_APPLICABLE);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_derivedUnionLabel);
        Text derivedUnion = uiToolkit.createText(workArea);
        bindingContext.bindContent(derivedUnion, association, IAssociation.PROPERTY_SUBSETTED_DERIVED_UNION);
        bindingContext.bindEnabled(derivedUnion, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
        DerivedUnionCompletionProcessor completionProcessor = new DerivedUnionCompletionProcessor(association);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        CompletionUtil.createHandlerForText(derivedUnion, completionProcessor);
    }

    private void createQualificationGroup(Composite c) {
        Composite workArea = uiToolkit.createGridComposite(c, 1, true, true);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        Checkbox qualifiedCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(qualifiedCheckbox, association, IAssociation.PROPERTY_QUALIFIED);
        bindingContext.bindEnabled(qualifiedCheckbox, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_POSSIBLE);
        Label note = uiToolkit.createFormLabel(workArea, StringUtils.rightPad("", 120)); //$NON-NLS-1$
        bindingContext.bindContent(note, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_NOTE);
        bindingContext.add(new ButtonTextBinding(qualifiedCheckbox, pmoAssociation,
                PmoAssociation.PROPERTY_QUALIFICATION_LABEL));
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

    public class PmoAssociation extends IpsObjectPartPmo {

        public final static String PROPERTY_SUBSET = "subset"; //$NON-NLS-1$
        public final static String PROPERTY_QUALIFICATION_LABEL = "qualificationLabel"; //$NON-NLS-1$
        public final static String PROPERTY_QUALIFICATION_NOTE = "qualificationNote"; //$NON-NLS-1$
        public final static String PROPERTY_QUALIFICATION_POSSIBLE = "qualificationPossible"; //$NON-NLS-1$
        public final static String PROPERTY_CONSTRAINED_NOTE = "constrainedNote"; //$NON-NLS-1$

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
                association.setSubsettedDerivedUnion(""); //$NON-NLS-1$
            }
            notifyListeners();
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

        public String getConstrainedNote() {
            try {
                if (association.isCompositionDetailToMaster()) {
                    return StringUtils.rightPad("", 120) + StringUtils.rightPad("\n", 120) //$NON-NLS-1$ //$NON-NLS-2$
                            + StringUtils.right("\n", 120); //$NON-NLS-1$
                }
                IProductCmptTypeAssociation matchingAss = association
                        .findMatchingProductCmptTypeAssociation(ipsProject);
                if (matchingAss != null) {
                    String type = matchingAss.getProductCmptType().getName();
                    return NLS.bind(Messages.AssociationEditDialog_noteAssociationIsConstrainedByProductStructure,
                            type, matchingAss.getTargetRoleSingular()) + StringUtils.rightPad("\n", 120); //$NON-NLS-1$
                } else {
                    String note = Messages.AssociationEditDialog_noteAssociationNotConstrainedByProductStructure;
                    IProductCmptType sourceProductType = association.getPolicyCmptType()
                            .findProductCmptType(ipsProject);
                    IPolicyCmptType targetType = association.findTargetPolicyCmptType(ipsProject);
                    if (sourceProductType != null && targetType != null) {
                        IProductCmptType targetProductType = targetType.findProductCmptType(ipsProject);
                        if (targetProductType != null) {
                            return note
                                    + NLS.bind(Messages.AssociationEditDialog_toConstraintTheAssociation,
                                            sourceProductType.getName(), targetProductType.getName());
                        }
                    }
                    return note + StringUtils.rightPad("\n", 120) + StringUtils.rightPad("\n", 120); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return ""; //$NON-NLS-1$
            }

        }

        @Override
        protected void partHasChanged() {
            if (association.isCompositionDetailToMaster()) {
                subset = false;
            }
        }

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
        super.okPressed();
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

    private abstract class NotificationPropertyBinding extends ControlPropertyBinding {
        public NotificationPropertyBinding(Control control, Object object, String propertyName, Class<?> exptectedType) {
            super(control, object, propertyName, exptectedType);
        }

        private Object oldValue;

        @Override
        public void updateUiIfNotDisposed() {
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

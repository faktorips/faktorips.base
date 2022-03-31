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

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PropertyChangeBinding;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.model.internal.pctype.persistence.PersistentAssociationInfo;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAssociationInfo;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAssociationInfo.FetchType;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.faktorips.util.StringUtil;

/**
 * This UI class builds the content persistence tab for the {@link AssociationEditDialog}.
 * 
 */
class AssociationPersistenceTab {

    private final TabItem tabItem;

    private final IPolicyCmptTypeAssociation association;

    private final UIToolkit toolkit;

    private final BindingContext bindingContext;

    private IPolicyCmptTypeAssociation inverseAssociation;

    private Composite joinColumnComposite;

    private Composite cascadeTypesComposite;

    private boolean changeable;

    public AssociationPersistenceTab(TabFolder parent, IPolicyCmptTypeAssociation association,
            BindingContext bindingContext, UIToolkit toolkit) {
        tabItem = new TabItem(parent, SWT.NONE);
        this.association = association;
        searchInverseAssociation();
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;
        tabItem.setText(Messages.AssociationEditDialog_labelPersistence);
        Composite c = toolkit.createGridComposite(parent, 1, false, true);
        ((GridLayout)c.getLayout()).marginHeight = 12;
        tabItem.setControl(c);
        createPersistenceTabItem(c);
    }

    public UIToolkit getToolkit() {
        return toolkit;
    }

    public BindingContext getBindingContext() {
        return bindingContext;
    }

    public void setDataChangeable(boolean changeable) {
        this.changeable = changeable;
    }

    private void searchInverseAssociation() {
        try {
            inverseAssociation = association.findInverseAssociation(getIpsProject());
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private IIpsProject getIpsProject() {
        return association.getIpsProject();
    }

    private void createPersistenceTabItem(Composite c) {
        if (!getIpsProject().getReadOnlyProperties().isPersistenceSupportEnabled()) {
            return;
        }

        final Checkbox checkTransient = getToolkit().createCheckbox(c,
                Messages.AssociationEditDialog_labelAssociationIsTransient);
        checkTransient.setEnabled(false);
        getBindingContext().bindContent(checkTransient, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_TRANSIENT);

        final Composite allPersistentProps = getToolkit().createGridComposite(c, 1, true, true);

        getToolkit().createVerticalSpacer(allPersistentProps, 12);
        final Composite joinTableComposite = createGroupJoinTable(allPersistentProps);

        getToolkit().createVerticalSpacer(allPersistentProps, 12);
        final Group groupForeignKey = createGroupForeignKey(allPersistentProps);

        getToolkit().createVerticalSpacer(allPersistentProps, 12);
        createGroupOtherPersistentProps(allPersistentProps);

        getToolkit().createVerticalSpacer(allPersistentProps, 12);
        createGroupCascadeType(allPersistentProps);

        bindContext(checkTransient, allPersistentProps, joinTableComposite, groupForeignKey);
    }

    private void bindContext(final Checkbox checkTransient,
            final Composite allPersistentProps,
            final Composite joinTableComposite,
            final Group groupForeignKey) {
        // persistence is enabled initialize enable / disable bindings
        // disable all persistent controls if attribute is marked as transient
        getBindingContext().add(
                new PropertyChangeBinding<>(allPersistentProps, association.getPersistenceAssociatonInfo(),
                        IPersistentAssociationInfo.PROPERTY_TRANSIENT, Boolean.TYPE) {
                    @Override
                    public void propertyChanged(Boolean old, Boolean newValue) {
                        IPersistentAssociationInfo associationInfo = (IPersistentAssociationInfo)getObject();
                        boolean persistEnabled = isPersistEnabled(associationInfo);
                        if (!persistEnabled) {
                            getToolkit().setDataChangeable(checkTransient, false);
                            getToolkit().setDataChangeable(getControl(), false);
                        } else {
                            getToolkit().setDataChangeable(getControl(), !associationInfo.isTransient());
                        }
                    }
                });
        // disable join table
        getBindingContext().add(
                new PropertyChangeBinding<>(joinTableComposite, association.getPersistenceAssociatonInfo(),
                        IPersistentAssociationInfo.PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION, Boolean.TYPE) {
                    @Override
                    public void propertyChanged(Boolean old, Boolean newValue) {
                        IPersistentAssociationInfo associationInfo = (IPersistentAssociationInfo)getObject();
                        boolean persistEnabled = isPersistEnabled(associationInfo);
                        if (associationInfo.isTransient()) {
                            return;
                        }
                        boolean ownerOfManyToManyAssociation = associationInfo.isOwnerOfManyToManyAssociation();
                        try {
                            boolean joinTableRequired = associationInfo.isJoinTableRequired();
                            getToolkit().setDataChangeable(groupForeignKey,
                                    persistEnabled && ownerOfManyToManyAssociation && !joinTableRequired);
                            enableOrDisableForeignKeyColumn();
                        } catch (IpsException e) {
                            IpsPlugin.logAndShowErrorDialog(e);
                        }
                    }
                });
        // disable cascade type checkboxes
        getBindingContext().add(
                new PropertyChangeBinding<>(cascadeTypesComposite, association.getPersistenceAssociatonInfo(),
                        IPersistentAssociationInfo.PROPERTY_CASCADE_TYPE_OVERWRITE_DEFAULT, Boolean.TYPE) {
                    @Override
                    public void propertyChanged(Boolean old, Boolean newValue) {
                        IPersistentAssociationInfo associationInfo = (IPersistentAssociationInfo)getObject();
                        boolean persistEnabled = isPersistEnabled(associationInfo);
                        if (!persistEnabled) {
                            getToolkit().setDataChangeable(getControl(), false);
                            return;
                        }
                        getToolkit().setDataChangeable(getControl(), associationInfo.isCascadeTypeOverwriteDefault());
                    }
                });
    }

    private boolean isPersistEnabled(IPersistentAssociationInfo associationInfo) {
        if (changeable) {
            return associationInfo.getPolicyComponentTypeAssociation().getPolicyCmptType().isPersistentEnabled();
        } else {
            return false;
        }
    }

    private Composite createGroupJoinTable(Composite allPersistentProps) {
        Group groupJoinTable = getToolkit().createGroup(allPersistentProps,
                Messages.AssociationEditDialog_labelJoinTable);
        GridData layoutData = (GridData)groupJoinTable.getLayoutData();
        layoutData.grabExcessVerticalSpace = false;

        Checkbox checkOwner = getToolkit().createCheckbox(groupJoinTable,
                Messages.AssociationEditDialog_labelAssociationIsOwningSideOfManyToMany);
        getBindingContext().bindContent(checkOwner, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION);

        Composite joinTableComposte = getToolkit().createLabelEditColumnComposite(groupJoinTable);
        joinTableComposte.setLayoutData(new GridData(GridData.FILL_BOTH));

        getToolkit().createFormLabel(joinTableComposte, Messages.AssociationEditDialog_labelJoinTableName);
        Text joinTableNameText = getToolkit().createText(joinTableComposte);
        getBindingContext().bindContent(joinTableNameText, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_JOIN_TABLE_NAME);

        getToolkit().createFormLabel(joinTableComposte, Messages.AssociationEditDialog_labelSourceColumnName);
        Text sourceColumnNameText = getToolkit().createText(joinTableComposte);
        getBindingContext().bindContent(sourceColumnNameText, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_SOURCE_COLUMN_NAME);

        getToolkit().createFormLabel(joinTableComposte, Messages.AssociationEditDialog_labelTargetColumnName);
        Text targetColumnNameText = getToolkit().createText(joinTableComposte);
        getBindingContext().bindContent(targetColumnNameText, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_TARGET_COLUMN_NAME);

        return joinTableComposte;
    }

    private Group createGroupOtherPersistentProps(Composite allPersistentProps) {
        Group groupOtherProps = getToolkit().createGroup(allPersistentProps,
                Messages.AssociationEditDialog_labelProperties);
        GridData layoutData = (GridData)groupOtherProps.getLayoutData();
        layoutData.grabExcessVerticalSpace = false;

        Composite otherPropsComposite = getToolkit().createLabelEditColumnComposite(groupOtherProps);
        otherPropsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        getToolkit().createFormLabel(otherPropsComposite, Messages.AssociationEditDialog_labelFetchType);
        Combo fetchTypeCombo = getToolkit().createCombo(otherPropsComposite);
        ComboField<FetchType> fetchTypeField = new EnumField<>(fetchTypeCombo, FetchType.class);
        getBindingContext().bindContent(fetchTypeField, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_FETCH_TYPE);

        String labelText = Messages.AssociationEditDialog_labelOrphanRemoval;
        if (getIpsProject().getIpsArtefactBuilderSet().getPersistenceProvider() != null
                && getIpsProject().getIpsArtefactBuilderSet().getPersistenceProvider().isSupportingOrphanRemoval()) {
            createCheckbox(otherPropsComposite, labelText, IPersistentAssociationInfo.PROPERTY_ORPHAN_REMOVAL);
        } else {
            Label label = getToolkit().createFormLabel(otherPropsComposite, labelText);
            Label text = getToolkit().createLabel(otherPropsComposite,
                    Messages.AssociationEditDialog_textNotSupportedByPersistenceProvider);
            label.setEnabled(false);
            text.setEnabled(false);
        }

        getToolkit().createFormLabel(otherPropsComposite, Messages.AttributeEditDialog_labelIndexName);
        if (getIpsProject().getIpsArtefactBuilderSet().getPersistenceProvider() == null
                || getIpsProject().getIpsArtefactBuilderSet().getPersistenceProvider().isSupportingIndex()) {
            Text indexName = getToolkit().createText(otherPropsComposite);
            getBindingContext().bindContent(indexName, association.getPersistenceAssociatonInfo(),
                    IPersistentAttributeInfo.PROPERTY_INDEX_NAME);
        } else {
            Text converterQualifiedName = getToolkit().createText(otherPropsComposite);
            converterQualifiedName.setEnabled(false);
            converterQualifiedName.setText(Messages.AssociationEditDialog_textNotSupportedByPersistenceProvider);
        }

        return groupOtherProps;
    }

    private void createCheckbox(Composite composite, String label, String property) {
        getToolkit().createFormLabel(composite, label);
        getBindingContext().bindContent(getToolkit().createCheckbox(composite),
                association.getPersistenceAssociatonInfo(), property);
    }

    private Group createGroupForeignKey(Composite allPersistentProps) {
        Group groupJoinColumn = getToolkit().createGroup(allPersistentProps,
                Messages.AssociationEditDialog_labelForeignKeyJoinColumn);
        GridData layoutData = (GridData)groupJoinColumn.getLayoutData();
        layoutData.grabExcessVerticalSpace = false;

        joinColumnComposite = getToolkit().createLabelEditColumnComposite(groupJoinColumn);
        joinColumnComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        getToolkit().createFormLabel(joinColumnComposite, Messages.AssociationEditDialog_labelForeignKeyColumnName);
        Text joinColumnNameText = getToolkit().createText(joinColumnComposite);
        getBindingContext().bindContent(joinColumnNameText, association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NAME);

        String labelText = Messages.AssociationEditDialog_labelJoinColumnNameNullable;
        createCheckbox(joinColumnComposite, labelText, IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NULLABLE);

        PersistentAssociationInfo persistentAssociationInfo = (PersistentAssociationInfo)association
                .getPersistenceAssociatonInfo();
        if (!persistentAssociationInfo.isOwnerOfManyToManyAssociation()) {
            // special information about foreign key column
            if (persistentAssociationInfo.isForeignKeyColumnDefinedOnTargetSide(inverseAssociation)) {
                getToolkit().createLabel(groupJoinColumn,
                        Messages.AssociationEditDialog_noteForeignKeyColumnDefinedInInverseAssociation);
            } else if (persistentAssociationInfo.isForeignKeyColumnCreatedOnTargetSide(inverseAssociation)) {
                getToolkit().createLabel(
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
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        getToolkit().setDataChangeable(joinColumnComposite, !foreignKeyDefinedOnTargetSide);
    }

    private Group createGroupCascadeType(Composite parentComposite) {
        Group group = getToolkit().createGroup(parentComposite, Messages.AssociationEditDialog_labelCascadeType);
        GridData layoutData = (GridData)group.getLayoutData();
        layoutData.grabExcessVerticalSpace = false;

        getBindingContext().bindContent(
                getToolkit().createCheckbox(group, Messages.AssociationEditDialog_labelOverwriteDefaultCascadeTypes),
                association.getPersistenceAssociatonInfo(),
                IPersistentAssociationInfo.PROPERTY_CASCADE_TYPE_OVERWRITE_DEFAULT);

        cascadeTypesComposite = getToolkit().createLabelEditColumnComposite(group);
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
}
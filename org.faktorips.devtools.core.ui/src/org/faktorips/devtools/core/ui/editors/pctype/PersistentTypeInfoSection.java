/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.DiscriminatorDatatype;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.PersistentType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Section to display and edit the persistence properties specific to an {@link IPolicyCmptType}.
 * <p>
 * The editable properties are Table Name, Inheritance Strategy amongst others.
 * 
 * @author Roman Grutza
 */
public class PersistentTypeInfoSection extends IpsSection {

    private static final String ID = "org.faktorips.devtools.core.ui.editors.pctype.PersistentTypeInfoSection"; //$NON-NLS-1$

    private final IPolicyCmptType ipsObject;

    private UIToolkit uiToolkit;

    private List<Control> persistentComposites = new ArrayList<Control>();

    public PersistentTypeInfoSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(ID, parent, GridData.FILL_HORIZONTAL, toolkit);
        this.ipsObject = ipsObject;

        initControls();
        setText(Messages.PersistentTypeInfoSection_sectionTitleJpaEntityInformation);
        setExpanded(false);
    }

    private class EnabledControlsBindingByProperty extends ControlPropertyBinding {

        private UIToolkit toolkit;

        private boolean checkEnable = true;

        private Boolean oldValue;

        public EnabledControlsBindingByProperty(Control control, UIToolkit toolkit, String property, boolean checkEnable) {
            super(control, ipsObject.getPersistenceTypeInfo(), property, Boolean.TYPE);
            this.toolkit = toolkit;
            this.checkEnable = checkEnable;
        }

        @Override
        public void updateUiIfNotDisposed(String nameOfChangedProperty) {
            try {
                boolean enabled = (Boolean)getProperty().getReadMethod().invoke(getObject(), new Object[0]);
                if (oldValue != null && enabled == oldValue) {
                    return;
                }
                oldValue = enabled;
                updateUiIfNotDisposedAndPropertyChanged(enabled);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void updateUiIfNotDisposedAndPropertyChanged(boolean enabled) {
            if (!(ipsObject.getPersistenceTypeInfo().getPersistentType() == PersistentType.ENTITY)) {
                toolkit.setDataChangeable(getControl(), false);
            } else {
                if (!checkEnable) {
                    enabled = !enabled;
                }
                toolkit.setDataChangeable(getControl(), enabled);
            }
        }
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        uiToolkit = toolkit;
        client.setLayout(new GridLayout(1, false));

        Composite detailComposite = toolkit.createLabelEditColumnComposite(client);
        toolkit.createLabel(detailComposite, Messages.PersistentTypeInfoSection_labelPersistentType);
        Combo persistentTypeCombo = toolkit.createCombo(detailComposite);
        EnumField<PersistentType> persistentTypeField = new EnumField<PersistentType>(persistentTypeCombo,
                PersistentType.class);

        Label inheritanceStrateyLabel = toolkit.createLabel(detailComposite,
                Messages.PersistentTypeInfoSection_labelInheritanceStrategy);
        Combo inheritanceStrategyCombo = toolkit.createCombo(detailComposite);
        EnumField<InheritanceStrategy> inheritanceStrategyField = new EnumField<InheritanceStrategy>(
                inheritanceStrategyCombo, InheritanceStrategy.class);
        persistentComposites.add(inheritanceStrateyLabel);
        persistentComposites.add(inheritanceStrategyCombo);

        Composite tableAndDiscrComposite = uiToolkit.createGridComposite(client, 2, true, false);

        // create table group
        Group tableGroup = toolkit.createGroup(tableAndDiscrComposite, Messages.PersistentTypeInfoSection_labelTable);
        persistentComposites.add(tableGroup);
        Checkbox checkboxTableDefinedInSuperclass = toolkit.createCheckbox(tableGroup);
        checkboxTableDefinedInSuperclass.setText(Messages.PersistentTypeInfoSection_labelUseTableDefinedInSupertype);

        Composite tableNameComposite = toolkit.createLabelEditColumnComposite(tableGroup);
        toolkit.createLabel(tableNameComposite, Messages.PersistentTypeInfoSection_labelTableName);
        final Text tableNameText = toolkit.createText(tableNameComposite);

        // create discriminator group
        Group discriminatorGroup = toolkit.createGroup(tableAndDiscrComposite,
                Messages.PersistentTypeInfoSection_labelDescriminator);
        persistentComposites.add(discriminatorGroup);

        Checkbox defineDiscriminatorColumn = toolkit.createCheckbox(discriminatorGroup);
        defineDiscriminatorColumn
        .setText(Messages.PersistentTypeInfoSection_labelThisTypeDefinesTheDiscriminatorColumn);

        Composite discriminatorDefComposite = toolkit.createLabelEditColumnComposite(discriminatorGroup);

        toolkit.createLabel(discriminatorDefComposite, Messages.PersistentTypeInfoSection_labelColumnName);
        Text descriminatorColumnNameText = toolkit.createText(discriminatorDefComposite);

        toolkit.createLabel(discriminatorDefComposite, Messages.PersistentTypeInfoSection_labelDatatype);
        Combo descriminatorDatatypeCombo = toolkit.createCombo(discriminatorDefComposite);
        EnumField<DiscriminatorDatatype> descriminatorDatatypeField = new EnumField<DiscriminatorDatatype>(
                descriminatorDatatypeCombo, DiscriminatorDatatype.class);

        toolkit.createLabel(discriminatorDefComposite, Messages.PersistentTypeInfoSection_labelColumnValue);
        Text descriminatorColumnValueText = toolkit.createText(discriminatorDefComposite);

        if (ipsObject.getPersistenceTypeInfo() != null) {
            getBindingContext().bindContent(persistentTypeField, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_PERSISTENT_TYPE);
            getBindingContext().add(
                    new ControlPropertyBinding(persistentTypeField.getControl(), ipsObject.getPersistenceTypeInfo(),
                            "enabled", Boolean.TYPE) { //$NON-NLS-1$
                        @Override
                        public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                            IPersistentTypeInfo persTypeInfo = (IPersistentTypeInfo)getObject();
                            boolean enabled = persTypeInfo.getPersistentType() == PersistentType.ENTITY;
                            for (Control ctrl : persistentComposites) {
                                uiToolkit.setDataChangeable(ctrl, enabled);
                            }
                        }
                    });

            getBindingContext().bindContent(inheritanceStrategyField, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY);
            getBindingContext().bindContent(checkboxTableDefinedInSuperclass, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_USE_TABLE_DEFINED_IN_SUPERTYPE);
            getBindingContext().add(
                    new EnabledControlsBindingByProperty(checkboxTableDefinedInSuperclass, toolkit,
                            IPersistentTypeInfo.PROPERTY_USE_TABLE_DEFINED_IN_SUPERTYPE, false) {
                        @Override
                        public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                            IPersistentTypeInfo persistenceTypeInfo = ipsObject.getPersistenceTypeInfo();
                            if (!(ipsObject.getPersistenceTypeInfo().getPersistentType() == PersistentType.ENTITY)) {
                                uiToolkit.setEnabled(tableNameText, false);
                                return;
                            }

                            if (persistenceTypeInfo.isUseTableDefinedInSupertype()) {
                                uiToolkit.setEnabled(tableNameText, false);
                            } else {
                                uiToolkit.setEnabled(tableNameText, true);
                            }
                        }
                    });

            getBindingContext().bindContent(tableNameText,
                    new PersistenceTableNamePmo(ipsObject.getPersistenceTypeInfo()),
                    IPersistentTypeInfo.PROPERTY_TABLE_NAME);

            getBindingContext().bindContent(defineDiscriminatorColumn, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN);
            getBindingContext().add(
                    new EnabledControlsBindingByProperty(descriminatorColumnNameText, toolkit,
                            IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN, true));
            getBindingContext().add(
                    new EnabledControlsBindingByProperty(descriminatorDatatypeCombo, toolkit,
                            IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN, true));

            getBindingContext().bindContent(descriminatorColumnNameText, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_COLUMN_NAME);
            getBindingContext().bindContent(descriminatorDatatypeField, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_DATATYPE);

            getBindingContext().bindContent(descriminatorColumnValueText, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE);
        }
    }

    public static class PersistenceTableNamePmo extends IpsObjectPartPmo {

        public PersistenceTableNamePmo(IPersistentTypeInfo persistentTypeInfo) {
            super(persistentTypeInfo);
        }

        @Override
        public IPersistentTypeInfo getIpsObjectPartContainer() {
            return (IPersistentTypeInfo)super.getIpsObjectPartContainer();
        }

        public IPolicyCmptType getPolicyCmptType() {
            return (IPolicyCmptType)getIpsObjectPartContainer().getIpsObject();
        }

        public String getTableName() {
            if (getIpsObjectPartContainer().isUseTableDefinedInSupertype()) {
                try {
                    IPolicyCmptType rootEntity = getIpsObjectPartContainer().findRootEntity();
                    IType superType = getPolicyCmptType().findSupertype(getIpsProject());
                    if (superType == null) {
                        return Messages.PersistentTypeInfoSection_textSupertypeNotFound;
                    } else if (rootEntity == null) {
                        return Messages.PersistentTypeInfoSection_textRootEntityNotFound;
                    } else {
                        return rootEntity.getPersistenceTypeInfo().getTableName();
                    }
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            } else {
                return getIpsObjectPartContainer().getTableName();
            }
        }

        public void setTableName(String tableName) {
            getIpsObjectPartContainer().setTableName(tableName);
        }

    }

}

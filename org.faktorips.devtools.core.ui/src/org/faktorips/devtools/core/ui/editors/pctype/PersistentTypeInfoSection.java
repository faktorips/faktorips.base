/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.DiscriminatorDatatype;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.PersistentType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
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
        public void updateUiIfNotDisposed() {
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
        ComboField persistentTypeField = new EnumField<PersistentType>(persistentTypeCombo, PersistentType.class);

        Label inheritanceStrateyLabel = toolkit.createLabel(detailComposite,
                Messages.PersistentTypeInfoSection_labelInheritanceStrategy);
        Combo inheritanceStrategyCombo = toolkit.createCombo(detailComposite);
        ComboField inheritanceStrategyField = new EnumField<InheritanceStrategy>(inheritanceStrategyCombo,
                InheritanceStrategy.class);
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
        ComboField descriminatorDatatypeField = new EnumField<DiscriminatorDatatype>(descriminatorDatatypeCombo,
                DiscriminatorDatatype.class);

        toolkit.createLabel(discriminatorDefComposite, Messages.PersistentTypeInfoSection_labelColumnValue);
        Text descriminatorColumnValueText = toolkit.createText(discriminatorDefComposite);

        if (ipsObject.getPersistenceTypeInfo() != null) {
            bindingContext.bindContent(persistentTypeField, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_PERSISTENT_TYPE);
            bindingContext.add(new ControlPropertyBinding(persistentTypeField.getControl(), ipsObject
                    .getPersistenceTypeInfo(), "enabled", Boolean.TYPE) { //$NON-NLS-1$
                        @Override
                        public void updateUiIfNotDisposed() {
                            IPersistentTypeInfo persTypeInfo = (IPersistentTypeInfo)getObject();
                            boolean enabled = persTypeInfo.getPersistentType() == PersistentType.ENTITY;
                            for (Control ctrl : persistentComposites) {
                                uiToolkit.setDataChangeable(ctrl, enabled);
                            }
                        }
                    });

            bindingContext.bindContent(inheritanceStrategyField, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY);
            bindingContext.bindContent(checkboxTableDefinedInSuperclass, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_USE_TABLE_DEFINED_IN_SUPERTYPE);
            bindingContext.add(new EnabledControlsBindingByProperty(checkboxTableDefinedInSuperclass, toolkit,
                    IPersistentTypeInfo.PROPERTY_USE_TABLE_DEFINED_IN_SUPERTYPE, false) {
                @Override
                public void updateUiIfNotDisposed() {
                    IPersistentTypeInfo persistenceTypeInfo = ipsObject.getPersistenceTypeInfo();
                    if (!(ipsObject.getPersistenceTypeInfo().getPersistentType() == PersistentType.ENTITY)) {
                        uiToolkit.setDataChangeable(tableNameText, false);
                        return;
                    }

                    if (persistenceTypeInfo.isUseTableDefinedInSupertype()) {
                        bindingContext.removeBindings(tableNameText);
                        try {
                            IPolicyCmptType rootEntity = persistenceTypeInfo.findRootEntity();
                            IType superType = ipsObject.findSupertype(ipsObject.getIpsProject());
                            if (superType == null) {
                                tableNameText.setText(Messages.PersistentTypeInfoSection_textSupertypeNotFound);
                            } else if (rootEntity == null) {
                                tableNameText.setText(Messages.PersistentTypeInfoSection_textRootEntityNotFound);
                            } else {
                                tableNameText.setText(rootEntity.getPersistenceTypeInfo().getTableName());
                            }
                            uiToolkit.setDataChangeable(tableNameText, false);
                        } catch (CoreException e) {
                            IpsPlugin.logAndShowErrorDialog(e);
                        }
                    } else {
                        bindingContext.bindContent(tableNameText, persistenceTypeInfo,
                                IPersistentTypeInfo.PROPERTY_TABLE_NAME);
                        uiToolkit.setDataChangeable(tableNameText, true);
                    }
                }
            });

            bindingContext.bindContent(defineDiscriminatorColumn, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN);
            bindingContext.add(new EnabledControlsBindingByProperty(descriminatorColumnNameText, toolkit,
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN, true));
            bindingContext.add(new EnabledControlsBindingByProperty(descriminatorDatatypeCombo, toolkit,
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN, true));

            bindingContext.bindContent(descriminatorColumnNameText, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_COLUMN_NAME);
            bindingContext.bindContent(descriminatorDatatypeField, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_DATATYPE);

            bindingContext.bindContent(descriminatorColumnValueText, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE);
        }
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

}

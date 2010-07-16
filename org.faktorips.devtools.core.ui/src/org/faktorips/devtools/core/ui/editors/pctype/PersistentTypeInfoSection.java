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
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.DiscriminatorDatatype;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.PersistentType;
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

    private final IPolicyCmptType ipsObject;
    private UIToolkit uiToolkit;

    private List<Control> persistentComposites = new ArrayList<Control>();

    public PersistentTypeInfoSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE, GridData.FILL_HORIZONTAL, toolkit);
        this.ipsObject = ipsObject;

        initControls();
        setText("JPA Entity Information"); //$NON-NLS-1$
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
        toolkit.createLabel(detailComposite, "Persistent type"); //$NON-NLS-1$
        Combo persistentTypeCombo = toolkit.createCombo(detailComposite);
        setComboItems(persistentTypeCombo, PersistentType.class);
        ComboField persistentTypeField = new EnumField(persistentTypeCombo, PersistentType.class);

        Label inheritanceStrateyLabel = toolkit.createLabel(detailComposite, "Inheritance Strategy"); //$NON-NLS-1$
        Combo inheritanceStrategyCombo = toolkit.createCombo(detailComposite);
        setComboItems(inheritanceStrategyCombo, InheritanceStrategy.class);
        ComboField inheritanceStrategyField = new EnumField(inheritanceStrategyCombo, InheritanceStrategy.class);
        persistentComposites.add(inheritanceStrateyLabel);
        persistentComposites.add(inheritanceStrategyCombo);

        Composite tableAndDiscrComposite = uiToolkit.createGridComposite(client, 2, true, false);

        // create table group
        Group tableGroup = toolkit.createGroup(tableAndDiscrComposite, "Table"); //$NON-NLS-1$
        persistentComposites.add(tableGroup);
        Checkbox checkboxTableDefinedInSuperclass = toolkit.createCheckbox(tableGroup);
        checkboxTableDefinedInSuperclass.setText("Use table defined in supertype"); //$NON-NLS-1$

        Composite tableNameComposite = toolkit.createLabelEditColumnComposite(tableGroup);
        toolkit.createLabel(tableNameComposite, "Table Name"); //$NON-NLS-1$
        final Text tableNameText = toolkit.createText(tableNameComposite);

        // create discriminator group
        Group discriminatorGroup = toolkit.createGroup(tableAndDiscrComposite, "Descriminator"); //$NON-NLS-1$
        persistentComposites.add(discriminatorGroup);

        Checkbox defineDiscriminatorColumn = toolkit.createCheckbox(discriminatorGroup);
        defineDiscriminatorColumn.setText("This type defines the dicriminator column"); //$NON-NLS-1$

        Composite discriminatorDefComposite = toolkit.createLabelEditColumnComposite(discriminatorGroup);

        toolkit.createLabel(discriminatorDefComposite, "Column Name"); //$NON-NLS-1$
        Text descriminatorColumnNameText = toolkit.createText(discriminatorDefComposite);

        toolkit.createLabel(discriminatorDefComposite, "Datatype"); //$NON-NLS-1$
        Combo descriminatorDatatypeCombo = toolkit.createCombo(discriminatorDefComposite);
        setComboItems(descriminatorDatatypeCombo, DiscriminatorDatatype.class);
        ComboField descriminatorDatatypeField = new EnumField(descriminatorDatatypeCombo, DiscriminatorDatatype.class);

        toolkit.createLabel(discriminatorDefComposite, "Column Value"); //$NON-NLS-1$
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
                            tableNameText.setText(rootEntity.getPersistenceTypeInfo().getTableName());
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

    /**
     * Enables or disables the expanded state on this section.
     */
    public void setExpanded(boolean expanded) {
        getSectionControl().setExpanded(expanded);
    }

    private void setComboItems(Combo combo, Class<? extends Enum> class1) {
        Enum<?>[] allEnumConstants = class1.getEnumConstants();
        String[] allEnumValues = new String[allEnumConstants.length];
        for (int i = 0; i < allEnumConstants.length; i++) {
            allEnumValues[i] = allEnumConstants[i].toString();
        }
        combo.setItems(allEnumValues);
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

}

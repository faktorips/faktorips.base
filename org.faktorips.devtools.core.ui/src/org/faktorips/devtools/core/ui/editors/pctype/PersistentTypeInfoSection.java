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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.DiscriminatorDatatype;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Section to display and edit the persistence properties specific to an {@link IPolicyCmptType}.
 * <p/>
 * The editable properties are Table Name, Inheritance Strategy amongst others.
 * 
 * @author Roman Grutza
 */
public class PersistentTypeInfoSection extends IpsSection {

    private final IPolicyCmptType ipsObject;
    private Composite composite;

    public PersistentTypeInfoSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE, GridData.FILL_HORIZONTAL, toolkit);
        this.ipsObject = ipsObject;

        initControls();
        setText("JPA Entity Information");
        setExpanded(false);
    }

    private class EnabledControlsBindingByProperty extends ControlPropertyBinding {
        private UIToolkit toolkit;

        public EnabledControlsBindingByProperty(Control control, UIToolkit toolkit, String property) {
            super(control, ipsObject.getPersistenceTypeInfo(), property, Boolean.TYPE);
            this.toolkit = toolkit;
        }

        @Override
        public void updateUiIfNotDisposed() {
            try {
                boolean enabled;
                if (!ipsObject.getPersistenceTypeInfo().isEnabled()) {
                    enabled = false;
                } else {
                    enabled = (Boolean)getProperty().getReadMethod().invoke(getObject(), new Object[0]);
                }
                toolkit.setDataChangeable(getControl(), enabled);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));

        Checkbox checkboxEnable = toolkit.createCheckbox(client);
        checkboxEnable.setText("Activate persistent for this type");

        composite = toolkit.createGridComposite(client, 1, true, false);

        Composite detailComposite = toolkit.createLabelEditColumnComposite(composite);

        toolkit.createLabel(detailComposite, "Inheritance Strategy");
        Combo inheritanceStrategyCombo = toolkit.createCombo(detailComposite);
        setComboItems(inheritanceStrategyCombo, InheritanceStrategy.class);
        ComboField inheritanceStrategyField = new EnumField(inheritanceStrategyCombo, InheritanceStrategy.class);

        Checkbox checkboxTableDefinedInSuperclass = toolkit.createCheckbox(composite);
        checkboxTableDefinedInSuperclass.setText("Use table defined in supertype");

        Composite tableNameComposite = toolkit.createLabelEditColumnComposite(composite);
        toolkit.createLabel(tableNameComposite, "Table Name");
        Text tableNameText = toolkit.createText(tableNameComposite);

        Group discriminatorGroup = toolkit.createGroup(composite, "Descriminator");

        Checkbox defineDiscriminatorColumn = toolkit.createCheckbox(discriminatorGroup);
        defineDiscriminatorColumn.setText("This type defines the dicriminator column");

        Composite discriminatorDefComposite = toolkit.createLabelEditColumnComposite(discriminatorGroup);

        toolkit.createLabel(discriminatorDefComposite, "Descriminator Column Name");
        Text descriminatorColumnNameText = toolkit.createText(discriminatorDefComposite);

        toolkit.createLabel(discriminatorDefComposite, "Descriminator Datatype");
        Combo descriminatorDatatypeCombo = toolkit.createCombo(discriminatorDefComposite);
        setComboItems(descriminatorDatatypeCombo, DiscriminatorDatatype.class);
        ComboField descriminatorDatatypeField = new EnumField(descriminatorDatatypeCombo, DiscriminatorDatatype.class);

        toolkit.createLabel(discriminatorDefComposite, "Descriminator Column Value");
        Text descriminatorColumnValueText = toolkit.createText(discriminatorDefComposite);

        if (ipsObject.getPersistenceTypeInfo() != null) {
            bindingContext.bindContent(checkboxEnable, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_ENABLED);
            bindingContext.add(new EnabledControlsBindingByProperty(composite, toolkit,
                    IPersistentTypeInfo.PROPERTY_ENABLED));

            bindingContext.bindContent(inheritanceStrategyField, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY);

            bindingContext.bindContent(checkboxTableDefinedInSuperclass, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_USE_TABLE_DEFINED_IN_SUPERTYPE);
            bindingContext.bindContent(tableNameText, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_TABLE_NAME);

            bindingContext.bindContent(defineDiscriminatorColumn, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN);
            bindingContext.add(new EnabledControlsBindingByProperty(descriminatorColumnNameText, toolkit,
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));
            bindingContext.add(new EnabledControlsBindingByProperty(descriminatorDatatypeCombo, toolkit,
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));

            bindingContext.bindContent(descriminatorColumnNameText, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_COLUMN_NAME);
            bindingContext.bindContent(descriminatorDatatypeField, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_DATATYPE);

            bindingContext.bindContent(descriminatorColumnValueText, ipsObject.getPersistenceTypeInfo(),
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE);

            // bindingContext.bindEnabled(descriminatorColumnValueText,
            // ipsObject.getPersistenceTypeInfo(),
            // IPersistentTypeInfo.PROPERTY_INHERITANCE_NOT_JOINEDSUBCLASS);
            // bindingContext.bindEnabled(descriminatorDatatypeCombo,
            // ipsObject.getPersistenceTypeInfo(),
            // IPersistentTypeInfo.PROPERTY_INHERITANCE_NOT_JOINEDSUBCLASS);
            // bindingContext.bindEnabled(descriminatorColumnNameText,
            // ipsObject.getPersistenceTypeInfo(),
            // IPersistentTypeInfo.PROPERTY_INHERITANCE_NOT_JOINEDSUBCLASS);
            // bindingContext.bindContent(secondaryTableNameText,
            // ipsObject.getPersistenceTypeInfo(),
            // IPersistentTypeInfo.PROPERTY_SECONDARY_TABLE_NAME);
        } else {
            // special handling if no persistence type info exists before
            checkboxEnable.setChecked(false);
        }
    }

    /**
     * Enables or disables the expanded state on this section.
     */
    public void setExpanded(boolean expanded) {
        getSectionControl().setExpanded(expanded);
    }

    private void setComboItems(Combo combo, Class<? extends Enum> class1) {
        Enum[] allEnumConstants = class1.getEnumConstants();
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

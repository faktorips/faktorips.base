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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.DescriminatorDatatype;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
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

    public PersistentTypeInfoSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE, GridData.FILL_HORIZONTAL, toolkit);
        this.ipsObject = ipsObject;

        initControls();
        setText("Entity Information");
        setExpanded(false);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        toolkit.createLabel(composite, "Table Name");
        Text tableNameText = toolkit.createText(composite);
        bindingContext.bindContent(tableNameText, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_TABLE_NAME);

        toolkit.createLabel(composite, "Secondary Table Name");
        Text secondaryTableNameText = toolkit.createText(composite);
        bindingContext.bindContent(secondaryTableNameText, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_SECONDARY_TABLE_NAME);

        toolkit.createLabel(composite, "Inheritance Strategy");
        Combo inheritanceStrategyCombo = toolkit.createCombo(composite);
        setComboItems(inheritanceStrategyCombo, InheritanceStrategy.class);
        ComboField inheritanceStrategyField = new EnumField(inheritanceStrategyCombo, InheritanceStrategy.class);
        bindingContext.bindContent(inheritanceStrategyField, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY);

        toolkit.createLabel(composite, "Descriminator Column Name");
        Text descriminatorColumnNameText = toolkit.createText(composite);
        bindingContext.bindContent(descriminatorColumnNameText, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_DESCRIMINATOR_COLUMN_NAME);
        // disable this control if the inheritance strategy JoinedSubclass is used
        bindingContext.bindEnabled(descriminatorColumnNameText, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_INHERITANCE_NOT_JOINEDSUBCLASS);

        toolkit.createLabel(composite, "Descriminator Datatype");
        Combo descriminatorDatatypeCombo = toolkit.createCombo(composite);
        setComboItems(descriminatorDatatypeCombo, DescriminatorDatatype.class);
        ComboField descriminatorDatatypeField = new EnumField(descriminatorDatatypeCombo, DescriminatorDatatype.class);
        bindingContext.bindContent(descriminatorDatatypeField, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_DESCRIMINATOR_DATATYPE);
        bindingContext.bindEnabled(descriminatorDatatypeCombo, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_INHERITANCE_NOT_JOINEDSUBCLASS);

        toolkit.createLabel(composite, "Descriminator Column Value");
        Text descriminatorColumnValueText = toolkit.createText(composite);
        bindingContext.bindContent(descriminatorColumnValueText, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_DESCRIMINATOR_VALUE);
        bindingContext.bindEnabled(descriminatorColumnValueText, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_INHERITANCE_NOT_JOINEDSUBCLASS);
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

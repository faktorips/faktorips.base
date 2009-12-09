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
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.DatatypeField;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;

public class PersistentTypeInfoSection extends IpsSection {

    private final IPolicyCmptType ipsObject;

    public PersistentTypeInfoSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        this.ipsObject = ipsObject;

        initControls();
        setText("Entity Information");
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
        ComboField inheritanceStrategyField = new ComboField(inheritanceStrategyCombo);
        bindingContext.bindContent(inheritanceStrategyField, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY);

        toolkit.createLabel(composite, "Descriminator Column Name");
        Text descriminatorColumnNameText = toolkit.createText(composite);
        bindingContext.bindContent(descriminatorColumnNameText, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_DESCRIMINATOR_COLUMN_NAME);

        toolkit.createLabel(composite, "Descriminator Datatype");
        DatatypeRefControl descriminatorDatatypeRefEdit = toolkit.createDatatypeRefEdit(ipsObject.getIpsProject(),
                composite);
        DatatypeField datatypeField = new DatatypeField(descriminatorDatatypeRefEdit);
        bindingContext.bindContent(datatypeField, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_DESCRIMINATOR_DATATYPE);

        toolkit.createLabel(composite, "Descriminator Column Value");
        Text descriminatorColumnValueText = toolkit.createText(composite);
        bindingContext.bindContent(descriminatorColumnValueText, ipsObject.getPersistenceTypeInfo(),
                IPersistentTypeInfo.PROPERTY_DESCRIMINATOR_VALUE);
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }
}
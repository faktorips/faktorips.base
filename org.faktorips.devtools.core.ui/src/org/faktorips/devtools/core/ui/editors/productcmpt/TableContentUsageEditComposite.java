/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.TableContentsUsageRefControl;
import org.faktorips.util.message.ObjectProperty;

/**
 * Allows the user to edit a table usage.
 * 
 * @see ITableContentUsage
 * 
 * @author Alexander Weickmann
 */
public final class TableContentUsageEditComposite extends
        EditPropertyValueComposite<ITableStructureUsage, ITableContentUsage> {

    public TableContentUsageEditComposite(ITableStructureUsage property, ITableContentUsage propertyValue,
            ProductCmptPropertySection propertySection, Composite parent, CompositeUIController uiMasterController,
            UIToolkit toolkit) {

        super(property, propertyValue, propertySection, parent, uiMasterController, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties) {
        createTableContentEditField(editFieldsToEditedProperties);
    }

    private void createTableContentEditField(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties) {
        TableContentsUsageRefControl tcuControl = new TableContentsUsageRefControl(getProperty().getIpsProject(), this,
                getToolkit(), getProperty());
        TextButtonField editField = new TextButtonField(tcuControl);

        editFieldsToEditedProperties.put(editField, new ObjectProperty(getPropertyValue(),
                ITableContentUsage.PROPERTY_TABLE_CONTENT));
    }

}

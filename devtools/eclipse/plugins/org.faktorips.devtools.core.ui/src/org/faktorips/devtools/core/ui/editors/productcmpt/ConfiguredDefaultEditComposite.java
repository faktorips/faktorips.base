/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * Provides controls that allow the user to edit the value set and the default value of an
 * {@link IConfigElement}.
 * 
 * @since 3.19
 * 
 * @see IConfigElement
 * @see IValueSet
 */
public class ConfiguredDefaultEditComposite extends AbstractConfigElementEditComposite<IConfiguredDefault> {

    public ConfiguredDefaultEditComposite(IPolicyCmptTypeAttribute property, IConfiguredDefault propertyValue,
            IpsSection parentSection, Composite parent, BindingContext bindingContext, UIToolkit toolkit) {
        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) {
        EditField<String> defaultValueEditField = createDefaultValueEditField();
        createTemplateStatusButton(defaultValueEditField);
        editFields.add(defaultValueEditField);
        createEditFieldsForExtensionProperties();
        addOverlaysToEditFields(editFields);
    }

    private EditField<String> createDefaultValueEditField() {
        createLabel(Messages.ConfigElementEditComposite_defaultValue);

        ValueDatatype datatype = getProperty() == null ? null : findDatatypeForDefaultValueEditField();
        if (datatype == null) {
            // No datatype found - use String as default
            datatype = Datatype.STRING;
        }
        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        EditField<String> editField = controlFactory.createEditField(getToolkit(), this, datatype,
                getPropertyValue().getValueSet(), getPropertyValue().getIpsProject());
        getBindingContext().bindContent(editField, getPropertyValue(), IConfiguredDefault.PROPERTY_VALUE);
        return editField;
    }

    private ValueDatatype findDatatypeForDefaultValueEditField() {
        return getProperty().findDatatype(getPropertyValue().getIpsProject());
    }

    private void addOverlaysToEditFields(List<EditField<?>> editFields) {
        for (EditField<?> editField : editFields) {
            addChangingOverTimeDecorationIfRequired(editField);
        }
    }

    @Override
    protected Function<IConfiguredDefault, String> getToolTipFormatter() {
        return PropertyValueFormatter.CONFIGURED_DEFAULT;
    }
}

/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;

import com.google.common.base.Function;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.BooleanValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.ConfiguredValueSetField;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Provides controls that allow the user to edit the value set and the default value of an
 * {@link IConfigElement}.
 * 
 * @since 3.19
 * 
 * @see IConfigElement
 * @see IValueSet
 */
public class ConfiguredValueSetEditComposite extends AbstractConfigElementEditComposite<IConfiguredValueSet> {

    private AnyValueSetControl valueSetControl;

    public ConfiguredValueSetEditComposite(IPolicyCmptTypeAttribute property, IConfiguredValueSet propertyValue,
            IpsSection parentSection, Composite parent, BindingContext bindingContext, UIToolkit toolkit) {
        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) {
        EditField<?> valueSetEditField;
        if (isBooleanDatatype()) {
            valueSetEditField = createValueSetEditFieldForBoolean();
        } else {
            valueSetEditField = createValueSetField();
        }
        editFields.add(valueSetEditField);
        createTemplateStatusButton(valueSetEditField);
        addOverlaysToEditFields(editFields);
    }

    private boolean isBooleanDatatype() {
        String datatype = getProperty() == null ? null : getProperty().getDatatype();
        return datatype != null ? datatype.equals(Datatype.PRIMITIVE_BOOLEAN.getQualifiedName())
                || datatype.equals(Datatype.BOOLEAN.getQualifiedName()) : false;
    }

    private BooleanValueSetField createValueSetEditFieldForBoolean() {
        createLabel(Messages.ConfigElementEditComposite_valueSet);
        BooleanValueSetControl booleanValueSetControl = new BooleanValueSetControl(this, getToolkit(), getProperty(),
                getPropertyValue());
        BooleanValueSetField field = new BooleanValueSetField(getPropertyValue(), booleanValueSetControl);

        getBindingContext().bindContent(field, getPropertyValue(), IConfiguredValueSet.PROPERTY_VALUE_SET);

        return field;
    }

    private ConfiguredValueSetField createValueSetField() {
        createLabel(Messages.ConfigElementEditComposite_valueSet);
        valueSetControl = new AnyValueSetControl(this, getToolkit(), getPropertyValue(), getShell());
        valueSetControl.setText(IpsUIPlugin.getDefault().getDatatypeFormatter()
                .formatValueSet(getPropertyValue().getValueSet()));
        ((GridData)valueSetControl.getLayoutData()).widthHint = UIToolkit.DEFAULT_WIDTH;

        ConfiguredValueSetField editField = new ConfiguredValueSetField(getPropertyValue(), valueSetControl);
        getBindingContext().bindContent(editField, getPropertyValue(), IConfiguredValueSet.PROPERTY_VALUE_SET);

        return editField;
    }

    private void addOverlaysToEditFields(List<EditField<?>> editFields) {
        for (EditField<?> editField : editFields) {
            addChangingOverTimeDecorationIfRequired(editField);
        }
    }

    public void setEnumValueSetProvider(IEnumValueSetProvider enumValueSetProvider) {
        // anyValueSetControl might be null in case of a Range ValueSet
        if (valueSetControl != null) {
            valueSetControl.setEnumValueSetProvider(enumValueSetProvider);
        }
    }

    @Override
    protected Function<IConfiguredValueSet, String> getToolTipFormatter() {
        return PropertyValueFormatter.CONFIGURED_VALUESET;
    }
}

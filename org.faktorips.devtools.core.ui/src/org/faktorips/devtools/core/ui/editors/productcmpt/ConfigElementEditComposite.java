/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.BooleanValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.ConfigElementField;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Provides controls that allow the user to edit the value set and the default value of an
 * {@link IConfigElement}.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see IConfigElement
 * @see IValueSet
 */
public class ConfigElementEditComposite extends EditPropertyValueComposite<IPolicyCmptTypeAttribute, IConfigElement> {

    private ExtensionPropertyControlFactory extPropControlFactory;

    private AnyValueSetControl valueSetControl;

    public ConfigElementEditComposite(IPolicyCmptTypeAttribute property, IConfigElement propertyValue,
            IpsSection parentSection, Composite parent, BindingContext bindingContext, UIToolkit toolkit) {

        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        extPropControlFactory = new ExtensionPropertyControlFactory(propertyValue);
        initControls();
    }

    @Override
    protected void setLayout() {
        super.setLayout();
        GridLayout clientLayout = (GridLayout)getLayout();
        clientLayout.numColumns = 2;
        // Space between Labels and fields. Also allows error markers to be drawn.
        clientLayout.horizontalSpacing = 7;
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) {
        if (isBooleanDatatype()) {
            createValueSetEditFieldForBoolean(editFields);
        } else {
            createValueSetField(editFields);
        }
        createDefaultValueEditField(editFields);
        createEditFieldsForExtensionProperties();
        addOverlaysToEditFields(editFields);
    }

    private EditField<String> createDefaultValueEditField(List<EditField<?>> editFields) {
        createLabel(Messages.ConfigElementEditComposite_defaultValue);

        ValueDatatype datatype = getProperty() == null ? null : findDatatypeForDefaultValueEditField();
        if (datatype == null) {
            // No datatype found - use String as default
            datatype = Datatype.STRING;
        }
        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        EditField<String> editField = controlFactory.createEditField(getToolkit(), this, datatype, getPropertyValue()
                .getValueSet(), getPropertyValue().getIpsProject());
        editFields.add(editField);
        getBindingContext().bindContent(editField, getPropertyValue(), IConfigElement.PROPERTY_VALUE);
        return editField;
    }

    private ValueDatatype findDatatypeForDefaultValueEditField() {
        ValueDatatype datatype = null;
        try {
            datatype = getProperty().findDatatype(getPropertyValue().getIpsProject());
        } catch (CoreException e) {
            // Exception while searching for datatype, log exception and use String as default
            IpsPlugin.log(e);
            datatype = Datatype.STRING;
        }
        return datatype;
    }

    private boolean isBooleanDatatype() {
        String datatype = getProperty() == null ? null : getProperty().getDatatype();
        return datatype != null ? datatype.equals(Datatype.PRIMITIVE_BOOLEAN.getQualifiedName())
                || datatype.equals(Datatype.BOOLEAN.getQualifiedName()) : false;
    }

    private void createValueSetEditFieldForBoolean(List<EditField<?>> editFields) {
        createLabel(Messages.ConfigElementEditComposite_valueSet);
        BooleanValueSetControl booleanValueSetControl = new BooleanValueSetControl(this, getToolkit(), getProperty(),
                getPropertyValue());
        booleanValueSetControl.setDataChangeable(getProductCmptPropertySection().isDataChangeable());
        BooleanValueSetField field = new BooleanValueSetField(getPropertyValue(), booleanValueSetControl);
        editFields.add(field);

        getBindingContext().bindContent(field, getPropertyValue(), IConfigElement.PROPERTY_VALUE_SET);
    }

    private void createValueSetField(List<EditField<?>> editFields) {
        createLabel(Messages.ConfigElementEditComposite_valueSet);
        valueSetControl = new AnyValueSetControl(this, getToolkit(), getPropertyValue(), getShell());
        valueSetControl.setDataChangeable(getProductCmptPropertySection().isDataChangeable());
        valueSetControl.setText(IpsUIPlugin.getDefault().getDatatypeFormatter()
                .formatValueSet(getPropertyValue().getValueSet()));
        ((GridData)valueSetControl.getLayoutData()).widthHint = UIToolkit.DEFAULT_WIDTH;

        ConfigElementField editField = new ConfigElementField(getPropertyValue(), valueSetControl);
        editFields.add(editField);
        getBindingContext().bindContent(editField, getPropertyValue(), IConfigElement.PROPERTY_VALUE_SET);
    }

    /**
     * Creates a {@link Label} whose width corresponds to the width of the broadest label of this
     * section.
     */
    private void createLabel(String text) {
        getToolkit().createLabel(this, text);
    }

    private void createEditFieldsForExtensionProperties() {
        extPropControlFactory.createControls(this, getToolkit(), getPropertyValue(),
                IExtensionPropertyDefinition.POSITION_TOP);
        extPropControlFactory.createControls(this, getToolkit(), getPropertyValue(),
                IExtensionPropertyDefinition.POSITION_BOTTOM);
        extPropControlFactory.bind(getBindingContext());
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
    protected Function<IConfigElement, String> getToolTipFormatter() {
        return new Function<IConfigElement, String>() {

            @Override
            public String apply(IConfigElement configElement) {
                return configElement != null ? configElement.getPropertyValue() : StringUtils.EMPTY;
            }
        };
    }
}

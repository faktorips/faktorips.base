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
import java.util.Locale;
import java.util.function.Function;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.LocalizedStringEditField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.InternationalStringControl;
import org.faktorips.devtools.core.ui.controls.InternationalStringDialogHandler;
import org.faktorips.devtools.core.ui.controls.MultiValueAttributeControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.values.LocalizedString;

/**
 * Provides controls that allow the user to edit the an {@link IAttributeValue}.
 * <p>
 * For attributes that do not change over time, a decoration marker is attached to the edit control.
 * 
 * @since 3.6
 * 
 * @see IAttributeValue
 */
public class AttributeValueEditComposite
        extends EditPropertyValueComposite<IProductCmptTypeAttribute, IAttributeValue> {

    public AttributeValueEditComposite(IProductCmptTypeAttribute property, IAttributeValue propertyValue,
            IpsSection parentSection, Composite parent, BindingContext bindingContext, UIToolkit toolkit) {
        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        initControls();
    }

    @Override
    protected void setLayout() {
        super.setLayout();
        GridLayout clientLayout = (GridLayout)getLayout();
        clientLayout.numColumns = 2;
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) throws CoreRuntimeException {
        createValueEditField(editFields);
        createEditFieldsForExtensionProperties();
    }

    private void createValueEditField(List<EditField<?>> editFields) {
        ValueDatatype datatype = getProperty() == null ? null
                : getProperty().findDatatype(getPropertyValue().getIpsProject());
        createEditField(datatype, editFields);
    }

    protected void createEditField(ValueDatatype datatype, List<EditField<?>> editFields) {
        EditField<?> editField;
        IValueHolder<?> valueHolder = getPropertyValue().getValueHolder();
        if (valueHolder.isMultiValue()) {
            editField = createMultiValueField(datatype);
        } else {
            editField = createSingleValueField(datatype, valueHolder);
        }
        getBindingContext().bindProblemMarker(editField, getPropertyValue(), IAttributeValue.PROPERTY_ATTRIBUTE);
        getBindingContext().bindProblemMarker(editField, getPropertyValue(), IAttributeValue.PROPERTY_VALUE_HOLDER);
        editFields.add(editField);
        createTemplateStatusButton(editField);
        addChangingOverTimeDecorationIfRequired(editField);
    }

    private EditField<?> createMultiValueField(ValueDatatype datatype) {
        MultiValueAttributeControl control = new MultiValueAttributeControl(this, getToolkit(), getProperty(),
                getPropertyValue(), datatype);
        EditField<?> editField = new TextButtonField(control);
        AttributeValueFormatter formatter = AttributeValueFormatter.createFormatterFor(getPropertyValue());
        getBindingContext().bindContent(editField, formatter, AttributeValueFormatter.PROPERTY_FORMATTED_VALUE);
        return editField;
    }

    private EditField<?> createSingleValueField(ValueDatatype datatype, IValueHolder<?> valueHolder) {
        IValueSet valueSet = getProperty() == null ? null : getProperty().getValueSet();

        if (valueHolder.getValueType() == ValueType.STRING) {
            return createSimpleField(datatype, valueSet);
        } else if (valueHolder.getValueType() == ValueType.INTERNATIONAL_STRING) {
            return createInternationalStringField();
        } else {
            throw new RuntimeException("Illegal value type in attribute " + getProperty().getName()); //$NON-NLS-1$
        }
    }

    private EditField<?> createSimpleField(ValueDatatype datatype, IValueSet valueSet) {
        ValueHolderPmo valueHolderPMO = new ValueHolderPmo(getPropertyValue());
        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        EditField<?> editField = controlFactory.createEditField(getToolkit(), this, datatype, valueSet,
                getPropertyValue().getIpsProject());
        getBindingContext().bindContent(editField, valueHolderPMO, ValueHolderPmo.PROPERTY_STRING_VALUE);
        return editField;
    }

    private EditField<?> createInternationalStringField() {
        final Locale localizationLocale = IIpsModel.get().getMultiLanguageSupport()
                .getLocalizationLocaleOrDefault(getPropertyValue().getIpsProject());
        MultilingualValueHolderPmo valueHolderPMO = new MultilingualValueHolderPmo(getPropertyValue(),
                localizationLocale);
        InternationalStringDialogHandler handler = new MyMultilingualValueAttributeHandler(getShell(),
                getPropertyValue());
        InternationalStringControl control = new InternationalStringControl(this, getToolkit(), handler);
        LocalizedStringEditField editField = new LocalizedStringEditField(control);
        getBindingContext().bindContent(editField, valueHolderPMO,
                MultilingualValueHolderPmo.PROPERTY_LOCALIZED_STRING_VALUE);
        return editField;
    }

    @Override
    protected Function<IAttributeValue, String> getToolTipFormatter() {
        return PropertyValueFormatter.ATTRIBUTE_VALUE;
    }

    public static IValue<?> getSingleValue(IAttributeValue attributeValue) {
        IValueHolder<?> valueHolder = attributeValue.getValueHolder();
        if (valueHolder.getValue() instanceof IValue) {
            return (IValue<?>)valueHolder.getValue();
        }
        throw new IllegalStateException(
                "Value " + valueHolder.getValue() + " of ValueHolder " + valueHolder + " is no single value."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private static class MyMultilingualValueAttributeHandler extends InternationalStringDialogHandler {

        private final IAttributeValue attributeValue;

        private MyMultilingualValueAttributeHandler(Shell shell, IAttributeValue part) {
            super(shell, part);
            this.attributeValue = part;
        }

        @Override
        protected IInternationalString getInternationalString() {
            IValueHolder<?> valueHolder = attributeValue.getValueHolder();
            if (valueHolder instanceof SingleValueHolder) {
                SingleValueHolder singleValueHolder = (SingleValueHolder)valueHolder;
                IValue<?> value = singleValueHolder.getValue();
                Object content = value.getContent();
                if (content instanceof IInternationalString) {
                    IInternationalString internationalString = (IInternationalString)content;
                    return internationalString;
                }
            }
            throw new IllegalArgumentException(
                    "The object provided to the InternationalStringDialog is not supported."); //$NON-NLS-1$
        }
    }

    public static class ValueHolderPmo extends IpsObjectPartPmo {

        public static final String PROPERTY_STRING_VALUE = "stringValue"; //$NON-NLS-1$

        public ValueHolderPmo(IAttributeValue attributeValue) {
            super(attributeValue);
        }

        public String getStringValue() {
            return getSingleValue(getIpsObjectPartContainer()).getContentAsString();
        }

        public IValueHolder<?> getValueHolder() {
            return getIpsObjectPartContainer().getValueHolder();
        }

        @Override
        public IAttributeValue getIpsObjectPartContainer() {
            return (IAttributeValue)super.getIpsObjectPartContainer();
        }

        public void setStringValue(String value) {
            if (getValueHolder() instanceof SingleValueHolder) {
                SingleValueHolder singleValueHolder = (SingleValueHolder)getValueHolder();
                singleValueHolder.setValue(ValueFactory.createStringValue(value));
                notifyListeners();
            } else {
                throw new IllegalStateException("Set string value is only supported for single value holders."); //$NON-NLS-1$
            }
        }

    }

    public static class MultilingualValueHolderPmo extends IpsObjectPartPmo {

        public static final String PROPERTY_LOCALIZED_STRING_VALUE = "localizedStringValue"; //$NON-NLS-1$

        private final Locale locale;

        public MultilingualValueHolderPmo(IAttributeValue attributeValue, Locale locale) {
            super(attributeValue);
            this.locale = locale;
        }

        @Override
        public IAttributeValue getIpsObjectPartContainer() {
            return (IAttributeValue)super.getIpsObjectPartContainer();
        }

        public LocalizedString getLocalizedStringValue() {
            IValue<?> value = getSingleValue(getIpsObjectPartContainer());
            return value == null || value.getContent() == null ? null
                    : ((IInternationalString)value.getContent()).get(locale);
        }

        public void setLocalizedStringValue(LocalizedString newValue) {
            IValue<?> value = getSingleValue(getIpsObjectPartContainer());
            if (value != null) {
                IInternationalString currentString = (IInternationalString)value.getContent();
                currentString.add(newValue);
            }
            notifyListeners();
        }

    }

}
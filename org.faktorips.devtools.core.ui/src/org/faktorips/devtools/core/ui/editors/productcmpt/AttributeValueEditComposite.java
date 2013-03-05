/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
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
import org.faktorips.values.LocalizedString;

/**
 * Provides controls that allow the user to edit the an {@link IAttributeValue}.
 * <p>
 * For attributes that do not change over time, a decoration marker is attached to the edit control.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann, Faktor Zehn AG
 * 
 * @see IAttributeValue
 */
public class AttributeValueEditComposite extends EditPropertyValueComposite<IProductCmptTypeAttribute, IAttributeValue> {

    public AttributeValueEditComposite(IProductCmptTypeAttribute property, IAttributeValue propertyValue,
            IpsSection parentSection, Composite parent, BindingContext bindingContext, UIToolkit toolkit) {

        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) throws CoreException {
        createValueEditField(editFields);
    }

    private void createValueEditField(List<EditField<?>> editFields) throws CoreException {
        ValueDatatype datatype = getProperty() == null ? null : getProperty().findDatatype(
                getPropertyValue().getIpsProject());
        EditField<?> editField = createEditField(datatype);
        registerAndBindEditField(editFields, editField);
    }

    protected EditField<?> createEditField(ValueDatatype datatype) {
        EditField<?> editField = null;
        IValueSet valueSet = getProperty() == null ? null : getProperty().getValueSet();

        if (getPropertyValue().getValueHolder() instanceof MultiValueHolder) {
            MultiValueAttributeControl control = new MultiValueAttributeControl(this, getToolkit(), getProperty(),
                    getPropertyValue(), datatype);
            editField = new TextButtonField(control);
            ValueHolderToFormattedStringWrapper wrapper = ValueHolderToFormattedStringWrapper
                    .createWrapperFor(getPropertyValue());
            getBindingContext().bindContent(editField, wrapper,
                    ValueHolderToFormattedStringWrapper.PROPERTY_FORMATTED_VALUE);
        } else if (getPropertyValue().getValueHolder() instanceof SingleValueHolder) {
            SingleValueHolder singleValueHolder = (SingleValueHolder)getPropertyValue().getValueHolder();
            if (singleValueHolder.getValueType() == ValueType.STRING) {
                ValueHolderPmo valueHolderPMO = new ValueHolderPmo(getPropertyValue());

                ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                        datatype);
                editField = controlFactory.createEditField(getToolkit(), this, datatype, valueSet, getPropertyValue()
                        .getIpsProject());
                getBindingContext().bindContent(editField, valueHolderPMO, ValueHolderPmo.PROPERTY_STRING_VALUE);
            } else if (singleValueHolder.getValueType() == ValueType.INTERNATIONAL_STRING) {
                final Locale localizationLocale = IpsPlugin.getMultiLanguageSupport().getLocalizationLocaleOrDefault(
                        getPropertyValue().getIpsProject());
                MultilingualValueHolderPmo valueHolderPMO = new MultilingualValueHolderPmo(getPropertyValue(),
                        localizationLocale);
                InternationalStringDialogHandler handler = new MyMultilingualValueAttributeHandler(getShell(),
                        getPropertyValue());
                InternationalStringControl control = new InternationalStringControl(this, getToolkit(), handler);
                editField = new LocalizedStringEditField(control.getTextControl());
                getBindingContext().bindContent(editField, valueHolderPMO,
                        MultilingualValueHolderPmo.PROPERTY_LOCALIZED_STRING_VALUE);
            }
        }
        if (editField != null) {
            getBindingContext().bindProblemMarker(editField, getPropertyValue(), IAttributeValue.PROPERTY_ATTRIBUTE);
            getBindingContext().bindProblemMarker(editField, getPropertyValue(), IAttributeValue.PROPERTY_VALUE_HOLDER);
            return editField;
        }
        throw new RuntimeException("Illegal value holder instance in attribute " + getProperty().getName()); //$NON-NLS-1$
    }

    protected void registerAndBindEditField(List<EditField<?>> editFields, EditField<?> editField) {
        editFields.add(editField);
        if (getProperty() != null && !getProperty().isChangingOverTime()) {
            addNotChangingOverTimeControlDecoration(editField);
        }
    }

    private void addNotChangingOverTimeControlDecoration(EditField<?> editField) {
        ControlDecoration controlDecoration = new ControlDecoration(editField.getControl(), SWT.LEFT | SWT.TOP,
                this.getParent());
        controlDecoration.setDescriptionText(NLS.bind(
                Messages.AttributeValueEditComposite_attributeNotChangingOverTimeDescription, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural()));
        controlDecoration.setImage(IpsUIPlugin.getImageHandling().getImage(OverlayIcons.NOT_CHANGEOVERTIME_OVR_DESC));
        controlDecoration.setMarginWidth(1);
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
            throw new IllegalArgumentException("The object provided to the InternationalStringDialog is not supported."); //$NON-NLS-1$
        }
    }

    public static class ValueHolderPmo extends IpsObjectPartPmo {

        public static final String PROPERTY_STRING_VALUE = "stringValue"; //$NON-NLS-1$

        public ValueHolderPmo(IAttributeValue attributeValue) {
            super(attributeValue);
        }

        public String getStringValue() {
            return ((SingleValueHolder)getValueHolder()).getValue().getContentAsString();
        }

        public IValueHolder<?> getValueHolder() {
            return getIpsObjectPartContainer().getValueHolder();
        }

        @Override
        public IAttributeValue getIpsObjectPartContainer() {
            return (IAttributeValue)super.getIpsObjectPartContainer();
        }

        public void setStringValue(String value) {
            ((SingleValueHolder)getValueHolder()).setValue(ValueFactory.createStringValue(value));
            notifyListeners();
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

        public SingleValueHolder getSingleValueHolder() {
            return (SingleValueHolder)getIpsObjectPartContainer().getValueHolder();
        }

        public LocalizedString getLocalizedStringValue() {
            IValue<?> value = getSingleValueHolder().getValue();
            return value == null || value.getContent() == null ? null : ((IInternationalString)value.getContent())
                    .get(locale);
        }

        public void setLocalizedStringValue(LocalizedString newValue) {
            IValue<?> value = getSingleValueHolder().getValue();
            if (value != null) {
                IInternationalString currentString = (IInternationalString)value.getContent();
                currentString.add(newValue);
            }
            notifyListeners();
        }

    }
}
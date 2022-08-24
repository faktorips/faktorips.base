/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.text.MessageFormat;
import java.util.Locale;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateValueFinder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The default value for a {@link IPolicyCmptTypeAttribute} as configured in a
 * {@link IPropertyValueContainer}.
 */
public class ConfiguredDefault extends ConfigElement implements IConfiguredDefault {

    public static final String LEGACY_TAG_NAME = ValueToXmlHelper.XML_TAG_VALUE;

    public static final String TAG_NAME = ValueToXmlHelper.XML_TAG_CONFIGURED_DEFAULT;

    private String value = ""; //$NON-NLS-1$

    public ConfiguredDefault(IPropertyValueContainer parent, String policyAttribute, String id) {
        super(parent, policyAttribute, id);
    }

    @Override
    public IConfiguredDefault findTemplateProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.findTemplateValue(this, IConfiguredDefault.class);
    }

    @Override
    public boolean hasTemplateForProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.hasTemplateForValue(this, IConfiguredDefault.class);
    }

    @Override
    public PropertyValueType getPropertyValueType() {
        return PropertyValueType.CONFIGURED_DEFAULT;
    }

    @Override
    public String getValue() {
        if (getTemplateValueStatus() == TemplateValueStatus.INHERITED) {
            return findTemplateValue();
        }
        if (getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return ""; //$NON-NLS-1$
        }
        return value;
    }

    private String findTemplateValue() {
        IConfiguredDefault templateConfigElement = findTemplateProperty(getIpsProject());
        if (templateConfigElement == null) {
            // Template should exist but does not. Use the "last known" value as a more or less
            // helpful fallback while some validation hopefully addresses the missing template...
            return value;
        }
        return templateConfigElement.getValue();
    }

    @Override
    public void setValue(String newValue) {
        String oldValue = value;
        value = newValue;
        valueChanged(oldValue, value);
    }

    @Override
    public String getPropertyValue() {
        return getValue();
    }

    @Override
    protected void validateContent(MessageList list, IIpsProject ipsProject, IPolicyCmptTypeAttribute attribute) {
        ValueDatatype valueDatatype = attribute.findDatatype(ipsProject);
        String valueToValidate = getValue();

        if (ValidationUtils.checkParsable(valueDatatype, valueToValidate, attribute,
                MessageFormat.format(Messages.ConfiguredDefault_caption, attribute), list)) {

            validateValueVsValueSet(valueDatatype, ipsProject, list);
        }

    }

    private void validateValueVsValueSet(ValueDatatype valueDatatype, IIpsProject ipsProject, MessageList list) {
        String valueToValidate = getValue();
        IValueSet valueSetToValidate = getValueSet();
        if (IpsStringUtils.isNotBlank(valueToValidate) && valueSetToValidate != null) {
            if (!valueSetToValidate.containsValue(valueToValidate, ipsProject)) {
                String formattedValue = IIpsModelExtensions.get().getModelPreferences().getDatatypeFormatter()
                        .formatValue(valueDatatype, valueToValidate);
                list.add(new Message(IConfiguredDefault.MSGCODE_VALUE_NOT_IN_VALUESET,
                        MessageFormat.format(Messages.ConfiguredDefault_msgValueNotInValueset, formattedValue),
                        Message.ERROR, this,
                        PROPERTY_VALUE));
            }
        }
    }

    @Override
    public IValueSet getValueSet() {
        IPropertyValueContainer parent = getPropertyValueContainer();
        IConfiguredValueSet propertyValue = parent.getPropertyValue(getPropertyName(), IConfiguredValueSet.class);
        if (propertyValue == null) {
            return null;
        } else {
            return propertyValue.getValueSet();
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        if (LEGACY_TAG_NAME.equals(element.getNodeName())) {
            super.initPropertiesFromXml(element, getNextPartId());
        } else {
            super.initPropertiesFromXml(element, id);
        }
        value = ValueToXmlHelper.getValueFromElement(element);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        ValueToXmlHelper.setValue(getValue(), element);
    }

    @Override
    protected void templateValueChanged() {
        value = getValue();
    }

    @Override
    public String getCaption(Locale locale) {
        return MessageFormat.format(Messages.ConfiguredDefault_caption, getAttributeLabel(locale));
    }

    @Override
    public String getLastResortCaption() {
        return MessageFormat.format(Messages.ConfiguredDefault_caption, getAttributeLabel(null));
    }
}

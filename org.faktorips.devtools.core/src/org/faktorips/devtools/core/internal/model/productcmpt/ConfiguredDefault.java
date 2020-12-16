/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.productcmpt.template.TemplateValueFinder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
    protected void validateContent(MessageList list, IIpsProject ipsProject, IPolicyCmptTypeAttribute attribute)
            throws CoreException {
        ValueDatatype valueDatatype = attribute.findDatatype(ipsProject);
        String valueToValidate = getValue();

        if (ValidationUtils.checkParsable(valueDatatype, valueToValidate, attribute,
                NLS.bind(Messages.ConfiguredDefault_caption, attribute), list)) {

            validateValueVsValueSet(valueDatatype, ipsProject, list);
        }

    }

    private void validateValueVsValueSet(ValueDatatype valueDatatype, IIpsProject ipsProject, MessageList list)
            throws CoreException {
        String valueToValidate = getValue();
        IValueSet valueSetToValidate = getValueSet();
        if (StringUtils.isNotEmpty(valueToValidate) && valueSetToValidate != null) {
            if (!valueSetToValidate.containsValue(valueToValidate, ipsProject)) {
                String formattedValue = IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter()
                        .formatValue(valueDatatype, valueToValidate);
                list.add(new Message(IConfiguredDefault.MSGCODE_VALUE_NOT_IN_VALUESET,
                        NLS.bind(Messages.ConfiguredDefault_msgValueNotInValueset, formattedValue), Message.ERROR, this,
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
        this.value = getValue();
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        return NLS.bind(Messages.ConfiguredDefault_caption, getAttributeLabel(locale));
    }

    @Override
    public String getLastResortCaption() {
        return NLS.bind(Messages.ConfiguredDefault_caption, getAttributeLabel(null));
    }
}

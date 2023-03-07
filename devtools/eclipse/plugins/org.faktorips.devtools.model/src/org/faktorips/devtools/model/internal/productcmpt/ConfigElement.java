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

import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;
import java.util.Locale;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateValueSettings;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Element;

public abstract class ConfigElement extends AbstractSimplePropertyValue implements IConfigElement {

    private final TemplateValueSettings templateValueSettings;

    private String policyAttribute = IpsStringUtils.EMPTY;

    public ConfigElement(IPropertyValueContainer parent, String policyAttribute, String id) {
        super(parent, id);
        this.policyAttribute = policyAttribute;
        templateValueSettings = new TemplateValueSettings(this);
    }

    @Override
    public String getPropertyName() {
        return getPolicyCmptTypeAttribute();
    }

    @Override
    public String getName() {
        return getPropertyName();
    }

    @Override
    public IProductCmptProperty findProperty(IIpsProject ipsProject) {
        return findPcTypeAttribute(ipsProject);
    }

    @Override
    public String getPolicyCmptTypeAttribute() {
        return policyAttribute;
    }

    @Override
    public void setPolicyCmptTypeAttribute(String policyCmptTypeAttribute) {
        String oldAttribute = policyAttribute;
        policyAttribute = policyCmptTypeAttribute;
        valueChanged(oldAttribute, policyAttribute, PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE);
    }

    @Override
    public IPolicyCmptTypeAttribute findPcTypeAttribute(IIpsProject ipsProject) {
        IPolicyCmptType pcType = getPropertyValueContainer().findPolicyCmptType(ipsProject);
        if (pcType == null) {
            return null;
        }
        return pcType.findPolicyCmptTypeAttribute(getPolicyCmptTypeAttribute(), ipsProject);
    }

    @Override
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) {
        IPolicyCmptTypeAttribute a = findPcTypeAttribute(ipsProject);
        if (a != null) {
            return a.findDatatype(ipsProject);
        }
        return null;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);

        IPolicyCmptTypeAttribute attribute = validateReferenceToAttribute(list, ipsProject);

        if (attribute == null) {
            return;
        }
        list.add(templateValueSettings.validate(this, ipsProject));

        boolean isDatatypeValid = validateDatatype(list, ipsProject, attribute);
        if (!isDatatypeValid) {
            return;
        }

        if (isValidateContent(attribute)) {
            validateContent(list, ipsProject, attribute);
        }
    }

    private IPolicyCmptTypeAttribute validateReferenceToAttribute(MessageList list, IIpsProject ipsProject) {

        IPolicyCmptTypeAttribute attribute = findPcTypeAttribute(ipsProject);
        if (attribute == null) {
            IPolicyCmptType policyCmptType = getPropertyValueContainer().findPolicyCmptType(ipsProject);
            if (policyCmptType == null) {
                String text = MessageFormat.format(Messages.ConfigElement_policyCmptTypeNotFound,
                        getPolicyCmptTypeAttribute());
                list.add(new Message(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE, text, Message.ERROR, this,
                        IConfigElement.PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE));
            } else {
                String policyCmptTypeLabel = IIpsModel.get().getMultiLanguageSupport()
                        .getLocalizedLabel(policyCmptType);
                String text = MessageFormat.format(Messages.ConfigElement_msgAttrNotDefined,
                        getPolicyCmptTypeAttribute(),
                        policyCmptTypeLabel);
                list.add(new Message(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE, text, Message.ERROR, this,
                        IConfiguredDefault.PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE));
            }
        }
        return attribute;
    }

    private boolean validateDatatype(MessageList list, IIpsProject ipsProject, IPolicyCmptTypeAttribute attribute) {
        ValueDatatype valueDatatype = attribute.findDatatype(ipsProject);
        Object[] params = { attribute.getDatatype(), attribute.getName(), attribute.getPolicyCmptType().getName() };

        if (valueDatatype == null) {
            String text = MessageFormat.format(Messages.ConfigElement_msgUndknownDatatype, params);
            list.add(new Message(IConfigElement.MSGCODE_UNKNOWN_DATATYPE, text, Message.WARNING, this,
                    IConfigElement.PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE));
            return false;
        }

        if (valueDatatype.checkReadyToUse().containsErrorMsg()) {
            String text = MessageFormat.format(Messages.ConfigElement_msgInvalidDatatype, params);
            list.add(new Message(IConfiguredDefault.MSGCODE_INVALID_DATATYPE, text, Message.ERROR, this,
                    IConfigElement.PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE));
            return false;
        }

        return true;
    }

    protected boolean isValidateContent(IPolicyCmptTypeAttribute attribute) {
        if (getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return false;
        }
        return attribute.getAttributeType() == AttributeType.CHANGEABLE
                || attribute.getAttributeType() == AttributeType.CONSTANT;
    }

    protected abstract void validateContent(MessageList list,
            IIpsProject ipsProject,
            IPolicyCmptTypeAttribute attribute) throws IpsException;

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        policyAttribute = element.getAttribute(ValueToXmlHelper.XML_ATTRIBUTE_ATTRIBUTE);
        if (IpsStringUtils.isEmpty(policyAttribute) && element.getParentNode() instanceof Element) {
            policyAttribute = ((Element)element.getParentNode()).getAttribute(ValueToXmlHelper.XML_ATTRIBUTE_ATTRIBUTE);
        }
        templateValueSettings.initPropertiesFromXml(element);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(ValueToXmlHelper.XML_ATTRIBUTE_ATTRIBUTE, getPolicyCmptTypeAttribute());
        templateValueSettings.propertiesToXml(element);
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        // Nothing to do
    }

    public ValueDatatype getValueDatatype() {
        return findValueDatatype(getIpsProject());
    }

    public String getAttributeLabel(Locale locale) {
        IAttribute attribute = findPcTypeAttribute(getIpsProject());
        if (attribute != null && locale != null) {
            String labelValue = attribute.getLabelValue(locale);
            if (IpsStringUtils.isNotEmpty(labelValue)) {
                return labelValue;
            }
        }
        return getPropertyName();
    }

    @Override
    public TemplateValueStatus getTemplateValueStatus() {
        return templateValueSettings.getStatus();
    }

    @Override
    public void setTemplateValueStatus(TemplateValueStatus newStatus) {
        if (newStatus == TemplateValueStatus.DEFINED) {
            templateValueChanged();
        }
        TemplateValueStatus oldValue = templateValueSettings.getStatus();
        templateValueSettings.setStatus(newStatus);
        objectHasChanged(new PropertyChangeEvent(this, PROPERTY_TEMPLATE_VALUE_STATUS, oldValue, newStatus));
    }

    @Override
    public IPropertyValueContainer getPropertyValueContainer() {
        return (IPropertyValueContainer)getParent();
    }

    protected abstract void templateValueChanged();
}

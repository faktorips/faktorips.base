/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.TemplateValueStatus;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Handles everything regarding the template information of a property value:
 * {@link TemplateValueStatus} , XML persistence and validation.
 */
public class TemplateValueSettings {

    private TemplateValueStatus status = TemplateValueStatus.DEFINED;

    public void initialize(IPropertyValueContainer parentContainer) {
        if (parentContainer.isUsingTemplate()) {
            status = TemplateValueStatus.INHERITED;
        }
    }

    public void setTemplateStatus(TemplateValueStatus status) {
        this.status = status;
    }

    public TemplateValueStatus getTemplateStatus() {
        return status;
    }

    /**
     * Validates the template status of the given property value.
     * 
     * @param propertyValue the property value to validate
     * @param ipsProject the IPS project to use for validating
     * @return a message list containing all appropriate validation messages regarding the template
     *         status of the given property value.
     */
    public MessageList validate(IAttributeValue propertyValue, IIpsProject ipsProject) {
        MessageList messageList = new MessageList();
        if (isUndefinedInProductCmpt(propertyValue)) {
            String message = NLS.bind(Messages.TemplateStatusHandler_Msg_ExcludeNotAllowedInProductCmpt,
                    TemplateValueStatus.UNDEFINED, propertyValue);
            messageList.newError(IAttributeValue.MSGCODE_INVALID_TEMPLATE_STATUS, message, propertyValue,
                    IAttributeValue.PROPERTY_TEMPLATE_VALUE_STATUS);
        }
        if (noInheritableValueFound(propertyValue, ipsProject)) {
            String message = NLS.bind(Messages.TemplateStatusHandler_Msg_noInheritableValueFound,
                    propertyValue.getPropertyName());
            messageList.newError(IAttributeValue.MSGCODE_INVALID_TEMPLATE_STATUS, message, propertyValue,
                    IAttributeValue.PROPERTY_TEMPLATE_VALUE_STATUS);
        }
        return messageList;
    }

    private boolean isUndefinedInProductCmpt(IAttributeValue propertyValue) {
        return propertyValue.getTemplateValueStatus() == TemplateValueStatus.UNDEFINED
                && !propertyValue.getPropertyValueContainer().isProductTemplate();
    }

    private boolean noInheritableValueFound(IAttributeValue propertyValue, IIpsProject ipsProject) {
        return propertyValue.getTemplateValueStatus() == TemplateValueStatus.INHERITED
                && propertyValue.findTemplateProperty(ipsProject) == null;
    }

    public void initPropertiesFromXml(Element element) {
        if (element.hasAttribute(IAttributeValue.PROPERTY_TEMPLATE_VALUE_STATUS)) {
            String statusString = element.getAttribute(IAttributeValue.PROPERTY_TEMPLATE_VALUE_STATUS);
            status = TemplateValueStatus.valueOfXml(statusString, TemplateValueStatus.DEFINED);
        } else {
            status = TemplateValueStatus.DEFINED;
        }
    }

    public void propertiesToXml(Element element) {
        element.setAttribute(IAttributeValue.PROPERTY_TEMPLATE_VALUE_STATUS, status.getXmlValue());
    }
}

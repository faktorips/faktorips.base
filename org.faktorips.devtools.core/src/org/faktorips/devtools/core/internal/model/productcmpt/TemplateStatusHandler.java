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
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue.TemplateStatus;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Handles everything regarding the template information of a property value: {@link TemplateStatus}
 * , XML persistence and validation.
 */
public class TemplateStatusHandler {

    private TemplateStatus status = TemplateStatus.DEFINED;

    public void setTemplateStatus(TemplateStatus status) {
        this.status = status;
    }

    public TemplateStatus getTemplateStatus() {
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
        if (propertyValue.getTemplateStatus() == TemplateStatus.UNDEFINED
                && !propertyValue.getPropertyValueContainer().isProductTemplate()) {
            String message = NLS.bind(Messages.TemplateStatusHandler_Msg_ExcludeNotAllowedInProductCmpt,
                    TemplateStatus.UNDEFINED, propertyValue);
            messageList.newError(IAttributeValue.MSGCODE_INVALID_TEMPLATE_STATUS, message, propertyValue,
                    IAttributeValue.PROPERTY_TEMPLATE_STATUS);
        }
        return messageList;
    }

    public void initPropertiesFromXml(Element element) {
        if (element.hasAttribute(IAttributeValue.PROPERTY_TEMPLATE_STATUS)) {
            String statusString = element.getAttribute(IAttributeValue.PROPERTY_TEMPLATE_STATUS);
            status = TemplateStatus.valueOfXml(statusString, TemplateStatus.DEFINED);
        } else {
            status = TemplateStatus.DEFINED;
        }
    }

    public void propertiesToXml(Element element) {
        element.setAttribute(IAttributeValue.PROPERTY_TEMPLATE_STATUS, status.getXmlValue());
    }
}

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

import static org.faktorips.devtools.core.model.productcmpt.IPropertyValue.MSGCODE_INVALID_TEMPLATE_STATUS;
import static org.faktorips.devtools.core.model.productcmpt.IPropertyValue.PROPERTY_TEMPLATE_VALUE_STATUS;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.TemplateValueStatus;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Handles everything regarding the template information of a property value:
 * {@link TemplateValueStatus} , XML persistence and validation.
 */
public class TemplateValueSettings {

    private final IPropertyValue parent;
    private TemplateValueStatus status = TemplateValueStatus.DEFINED;

    public TemplateValueSettings(IPropertyValue parent) {
        this.parent = parent;
        IPropertyValue templateValue = parent.findTemplateProperty(parent.getIpsProject());
        if (templateValue != null) {
            status = TemplateValueStatus.INHERITED;
        }
    }

    public void setStatus(TemplateValueStatus status) {
        this.status = status;
    }

    public TemplateValueStatus getStatus() {
        if (parent.isConfiguringTemplateValueStatus()) {
            return status;
        } else {
            return TemplateValueStatus.DEFINED;
        }
    }

    /**
     * Validates the template status of the given property value.
     * 
     * @param value the property value to validate
     * @param ipsProject the IPS project to use for validating
     * @return a message list containing all appropriate validation messages regarding the template
     *         status of the given property value.
     */
    public MessageList validate(IPropertyValue value, IIpsProject ipsProject) {
        MessageList messageList = new MessageList();
        if (isUndefinedInProductCmpt(value)) {
            String message = NLS.bind(Messages.TemplateStatusHandler_Msg_ExcludeNotAllowedInProductCmpt,
                    TemplateValueStatus.UNDEFINED, value);
            messageList.newError(MSGCODE_INVALID_TEMPLATE_STATUS, message, value, PROPERTY_TEMPLATE_VALUE_STATUS);
        }
        if (noInheritableValueFound(value, ipsProject)) {
            String message = NLS.bind(Messages.TemplateStatusHandler_Msg_noInheritableValueFound,
                    value.getPropertyName());
            messageList.newError(MSGCODE_INVALID_TEMPLATE_STATUS, message, value, PROPERTY_TEMPLATE_VALUE_STATUS);
        }
        return messageList;
    }

    private boolean isUndefinedInProductCmpt(IPropertyValue propertyValue) {
        return propertyValue.getTemplateValueStatus() == TemplateValueStatus.UNDEFINED
                && !propertyValue.getPropertyValueContainer().isProductTemplate();
    }

    private boolean noInheritableValueFound(IPropertyValue propertyValue, IIpsProject ipsProject) {
        return propertyValue.getTemplateValueStatus() == TemplateValueStatus.INHERITED
                && propertyValue.findTemplateProperty(ipsProject) == null;
    }

    public void initPropertiesFromXml(Element element) {
        if (element.hasAttribute(PROPERTY_TEMPLATE_VALUE_STATUS)) {
            String statusString = element.getAttribute(PROPERTY_TEMPLATE_VALUE_STATUS);
            status = TemplateValueStatus.valueOfXml(statusString, TemplateValueStatus.DEFINED);
        } else {
            status = TemplateValueStatus.DEFINED;
        }
    }

    public void propertiesToXml(Element element) {
        element.setAttribute(PROPERTY_TEMPLATE_VALUE_STATUS, status.getXmlValue());
    }

}

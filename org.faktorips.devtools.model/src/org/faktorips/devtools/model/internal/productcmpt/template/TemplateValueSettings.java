/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.template;

import static org.faktorips.devtools.model.productcmpt.template.ITemplatedValue.MSGCODE_INVALID_TEMPLATE_VALUE_STATUS;
import static org.faktorips.devtools.model.productcmpt.template.ITemplatedValue.PROPERTY_TEMPLATE_VALUE_STATUS;

import java.text.MessageFormat;

import org.faktorips.devtools.model.internal.productcmpt.Messages;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Element;

/**
 * Handles everything regarding the template information of a property value:
 * {@link TemplateValueStatus} , XML persistence and validation.
 */
public class TemplateValueSettings {

    private final ITemplatedValue parent;
    private TemplateValueStatus status = TemplateValueStatus.DEFINED;

    public TemplateValueSettings(ITemplatedValue parent) {
        this.parent = parent;
        ITemplatedValue templateValue = parent.findTemplateProperty(parent.getIpsProject());
        if (templateValue != null) {
            status = TemplateValueStatus.INHERITED;
        }
    }

    public void setStatus(TemplateValueStatus status) {
        this.status = status;
    }

    public TemplateValueStatus getStatus() {
        if (parent.isPartOfTemplateHierarchy()) {
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
            String message = MessageFormat.format(Messages.TemplateValueSettings_msgExcludeNotAllowedInProductCmpt,
                    TemplateValueStatus.UNDEFINED, value);
            messageList.newError(MSGCODE_INVALID_TEMPLATE_VALUE_STATUS, message, value, PROPERTY_TEMPLATE_VALUE_STATUS);
        }
        return messageList;
    }

    /**
     * Validates the template status of the given link.
     * 
     * @param link the product component link to validate
     * @param ipsProject the IPS project to use for validating
     * @return a message list containing all appropriate validation messages regarding the template
     *         status of the given property value.
     */
    public MessageList validate(IProductCmptLink link, IIpsProject ipsProject) {
        MessageList messageList = new MessageList();
        if (noInheritablePropertyFound(link, ipsProject)) {
            String message = MessageFormat.format(Messages.TemplateValueSettings_msgNoInheritableLinkFound,
                    link.getTarget());
            messageList.newError(MSGCODE_INVALID_TEMPLATE_VALUE_STATUS, message, link, PROPERTY_TEMPLATE_VALUE_STATUS);
        }
        if (noDeletableLinkFound(link, ipsProject)) {
            String message = MessageFormat.format(Messages.TemplateValueSettings_msgNoDeletableLinkFound,
                    link.getTarget());
            messageList.newError(MSGCODE_INVALID_TEMPLATE_VALUE_STATUS, message, link, PROPERTY_TEMPLATE_VALUE_STATUS);
        }
        return messageList;
    }

    private boolean isUndefinedInProductCmpt(IPropertyValue propertyValue) {
        return propertyValue.getTemplateValueStatus() == TemplateValueStatus.UNDEFINED
                && !propertyValue.getPropertyValueContainer().isProductTemplate();
    }

    private boolean noInheritablePropertyFound(ITemplatedValue property, IIpsProject ipsProject) {
        return property.getTemplateValueStatus() == TemplateValueStatus.INHERITED
                && property.findTemplateProperty(ipsProject) == null;
    }

    private boolean noDeletableLinkFound(IProductCmptLink link, IIpsProject ipsProject) {
        return link.getTemplateValueStatus() == TemplateValueStatus.UNDEFINED
                && link.findTemplateProperty(ipsProject) == null;
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

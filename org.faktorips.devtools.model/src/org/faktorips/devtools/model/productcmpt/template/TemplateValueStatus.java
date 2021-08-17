/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt.template;

import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;

/**
 * Defines the status of a property value with regard to the template hierarchy it is used in.
 */
public enum TemplateValueStatus {

    /**
     * Indicates that no property value is defined explicitly, instead the value from the parent
     * template is used or "inherited".
     */
    INHERITED("inherited") { //$NON-NLS-1$
        @Override
        public boolean isAllowedStatus(ITemplatedValue value) {
            return value.findTemplateProperty(value.getIpsProject()) != null;
        }
    },

    /**
     * Indicates that a property's value is explicitly defined. Regarding a template hierarchy it
     * might define a new value or overwrite a value from a parent template.
     */
    DEFINED("defined") { //$NON-NLS-1$
        @Override
        public boolean isAllowedStatus(ITemplatedValue value) {
            return true;
        }
    },

    /**
     * This status applies only to property values in templates and to links in both product
     * components and templates.
     * <p>
     * For a template's property the status indicates that the property will not be used by the
     * template (and its children). The property is regarded as "undefined". Thus child templates or
     * product components cannot inherit said value and must explicitly define their own values.
     * <p>
     * For links the status indicates that the link was inherited from a template but was removed
     * from the child template or the child product component.
     */
    UNDEFINED("undefined") { //$NON-NLS-1$
        @Override
        public boolean isAllowedStatus(ITemplatedValue value) {
            return isTemplatePropertyValue(value) || isLinkWithTemplate(value);
        }

        private boolean isTemplatePropertyValue(ITemplatedValue value) {
            return value instanceof IPropertyValue && value.getTemplatedValueContainer().isProductTemplate();
        }

        private boolean isLinkWithTemplate(ITemplatedValue value) {
            return value instanceof IProductCmptLink && value.findTemplateProperty(value.getIpsProject()) != null;
        }
    };

    private static final List<TemplateValueStatus> VALUES = Arrays.asList(TemplateValueStatus.values());

    private String xmlValue;

    private TemplateValueStatus(String xmlValue) {
        this.xmlValue = xmlValue;
    }

    public static TemplateValueStatus valueOfXml(String stringValue, TemplateValueStatus fallbackStatus) {
        for (TemplateValueStatus templateStatus : VALUES) {
            if (templateStatus.getXmlValue().equals(stringValue)) {
                return templateStatus;
            }
        }
        return fallbackStatus;
    }

    public String getXmlValue() {
        return xmlValue;
    }

    public TemplateValueStatus getNextStatus(ITemplatedValue value) {
        int index = (VALUES.indexOf(this) + 1) % VALUES.size();
        TemplateValueStatus nextStatus = VALUES.get(index);
        if (nextStatus.isAllowedStatus(value)) {
            return nextStatus;
        } else {
            return nextStatus.getNextStatus(value);
        }
    }

    /** Returns whether or not the status is allowed for the given {@code IPropertyValue}. */
    public abstract boolean isAllowedStatus(ITemplatedValue value);

}
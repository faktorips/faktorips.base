/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.productcmpt;

import java.util.Arrays;
import java.util.List;

/**
 * Defines the status of a property value with regard to the template hierarchy it is used in.
 */
public enum TemplateValueStatus {

    /**
     * Indicates that no property value is defined explicitly, instead the value from the parent
     * template is used or "inherited".
     */
    INHERITED("inherited"), //$NON-NLS-1$

    /**
     * Indicates that a property's value is explicitly defined. Regarding a template hierarchy it
     * might define a new value or overwrite a value from a parent template.
     */
    DEFINED("defined"), //$NON-NLS-1$

    /**
     * For template property values only. Indicates that the property will not be used by this
     * template (and its children). The property is regarded as "undefined". Thus child templates or
     * product components can no longer inherit said value and must define it explicitly.
     */
    UNDEFINED("undefined"); //$NON-NLS-1$

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

    public TemplateValueStatus getNextStatus(IAttributeValue attributeValue) {
        int index = (VALUES.indexOf(this) + 1) % VALUES.size();
        TemplateValueStatus nextStatus = VALUES.get(index);
        if (attributeValue.isAllowedTemplateValueStatus(nextStatus)) {
            return nextStatus;
        } else {
            return nextStatus.getNextStatus(attributeValue);
        }
    }

}
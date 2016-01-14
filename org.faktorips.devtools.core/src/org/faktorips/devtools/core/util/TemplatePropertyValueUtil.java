/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.util;

import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;

public class TemplatePropertyValueUtil {

    private TemplatePropertyValueUtil() {
        // utility class that should not be instantiated
    }

    /**
     * Returns {@code true} if the given property value is defined in a template, i.e. it belongs to
     * a template and is not inherited/undefined.
     * 
     * @param v a property value
     * @return whether the given property value is defined in a template
     */
    public static boolean isDefinedTemplatePropertyValue(IPropertyValue v) {
        return v != null && v.getPropertyValueContainer() != null && v.getPropertyValueContainer().isProductTemplate()
                && v.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }
}

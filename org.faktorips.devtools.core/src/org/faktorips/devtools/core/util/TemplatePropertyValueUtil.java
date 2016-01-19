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

import org.faktorips.devtools.core.internal.model.ipsproject.TemplateHierarchyFinder;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
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
        return isTemplatePropertyValue(v) && v.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

    /**
     * Returns {@code true} if the given property value belongs to a template.
     * 
     * @param v a property value
     * @return whether the given property value belongs to a template
     */
    public static boolean isTemplatePropertyValue(IPropertyValue v) {
        return v != null && v.getPropertyValueContainer() != null && v.getPropertyValueContainer().isProductTemplate();
    }

    /**
     * Returns the {@link IPropertyValue} that is the next higher in the template hierarchy. In
     * contrast to {@link TemplateHierarchyFinder} it does not search for a defined property value.
     * 
     * @param v a property value to search from
     * @return the next {@link IPropertyValue} in the template hierarchie or <code>null</code> if
     *         there is none
     */
    public static IPropertyValue findNextTemplatePropertyValue(IPropertyValue v) {
        IPropertyValueContainer propertyValueContainer = v.getPropertyValueContainer();
        IPropertyValueContainer template = propertyValueContainer.findTemplate(v.getIpsProject());
        if (template != null) {
            IPropertyValue propertyValue = template.getPropertyValue(v.getName());
            return propertyValue;
        } else {
            return null;
        }
    }

}

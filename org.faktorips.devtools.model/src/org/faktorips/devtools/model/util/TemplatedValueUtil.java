/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;

public class TemplatedValueUtil {

    private TemplatedValueUtil() {
        // utility class that should not be instantiated
    }

    /**
     * Returns {@code true} if the given templated value is defined in a template, i.e. it belongs
     * to a template and is not inherited/undefined.
     * 
     * @param v a property value
     * @return whether the given property value is defined in a template
     */
    public static boolean isDefinedTemplateValue(ITemplatedValue v) {
        return isTemplateValue(v) && v.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

    /**
     * Returns {@code true} if the given templated value belongs to a template.
     * 
     * @param v a property value
     * @return whether the given property value belongs to a template
     */
    public static boolean isTemplateValue(ITemplatedValue v) {
        return v != null && v.getTemplatedValueContainer() != null
                && v.getTemplatedValueContainer().isProductTemplate();
    }

    /**
     * Returns the {@link ITemplatedValue} that is the next higher in the template hierarchy. In
     * contrast to
     * {@link ITemplatedValue#findTemplateProperty(org.faktorips.devtools.model.ipsproject.IIpsProject)}
     * it does not search for a defined value.
     * 
     * @param v a templated value to search from
     * @return the next {@link ITemplatedValue} in the template hierarchy or {@code null} if there
     *         is none
     */
    public static ITemplatedValue findNextTemplateValue(ITemplatedValue v) {
        ITemplatedValueContainer propertyValueContainer = v.getTemplatedValueContainer();
        ITemplatedValueContainer template = propertyValueContainer.findTemplate(v.getIpsProject());
        if (template != null) {
            return v.getIdentifier().getValueFrom(template);
        } else {
            return null;
        }
    }

}

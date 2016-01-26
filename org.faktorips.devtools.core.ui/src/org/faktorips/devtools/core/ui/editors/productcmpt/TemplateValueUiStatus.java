/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.productcmpt;

import com.google.common.base.Function;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedProperty;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public enum TemplateValueUiStatus {

    INHERITED("templateInherited.png"), //$NON-NLS-1$

    OVERWRITE_EQUAL("templateDefinedEq.png"), //$NON-NLS-1$

    OVERWRITE("templateOverwrite.png"), //$NON-NLS-1$

    NEWLY_DEFINED("templateDefinedNew.png"), //$NON-NLS-1$

    UNDEFINED("templateUndefined.png"); //$NON-NLS-1$

    private final String icon;

    private TemplateValueUiStatus(String icon) {
        this.icon = icon;
    }

    public Image getIcon() {
        return IpsUIPlugin.getImageHandling().getSharedImage(icon, true);
    }

    public static <T extends IPropertyValue> TemplateValueUiStatus mapStatus(T propertyValue) {
        return mapStatusInternal(propertyValue, new Function<T, Object>() {
            @Override
            public Object apply(T p) {
                if (p == null) {
                    return null;
                }
                // We don't care about data type etc. so we can just use the string property value
                return p.getPropertyValue();
            }
        });
    }

    public static TemplateValueUiStatus mapStatus(IProductCmptLink link) {
        return mapStatusInternal(link, new Function<IProductCmptLink, Object>() {
            @Override
            public Object apply(IProductCmptLink l) {
                if (l == null) {
                    return null;
                }
                return l.getCardinality();
            }
        });
    }

    private static <T extends ITemplatedProperty> TemplateValueUiStatus mapStatusInternal(T propertyValue,
            Function<T, Object> valueFunction) {
        TemplateValueStatus templateStatus = propertyValue.getTemplateValueStatus();
        if (templateStatus == TemplateValueStatus.INHERITED) {
            return INHERITED;
        } else if (templateStatus == TemplateValueStatus.UNDEFINED) {
            return UNDEFINED;
        } else {
            return mapDefinedStatus(propertyValue, valueFunction);
        }
    }

    private static <T extends ITemplatedProperty> TemplateValueUiStatus mapDefinedStatus(T property,
            Function<T, Object> valueFunction) {
        @SuppressWarnings("unchecked")
        T templateProperty = (T)property.findTemplateProperty(property.getIpsProject());
        if (templateProperty == null) {
            return NEWLY_DEFINED;
        } else {
            Object value = valueFunction.apply(property);
            Object templateValue = valueFunction.apply(templateProperty);
            if (ObjectUtils.equals(value, templateValue)) {
                return OVERWRITE_EQUAL;
            } else {
                return OVERWRITE;
            }
        }
    }
}
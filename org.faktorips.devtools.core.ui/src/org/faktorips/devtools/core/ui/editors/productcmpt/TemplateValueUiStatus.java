/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 
 * 3. 
 *  
 * Please see LICENSE.txt for full license terms, including the additional permissions and 
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/package org.faktorips.devtools.core.ui.editors.productcmpt;

import com.google.common.base.Function;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.TemplateValueStatus;
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

    public static TemplateValueUiStatus mapStatus(IAttributeValue propertyValue,
            Function<IAttributeValue, Object> valueFunction) {
        TemplateValueStatus templateStatus = propertyValue.getTemplateValueStatus();
        if (templateStatus == TemplateValueStatus.INHERITED) {
            return INHERITED;
        } else if (templateStatus == TemplateValueStatus.UNDEFINED) {
            return UNDEFINED;
        } else {
            return mapDefinedStatus(propertyValue, valueFunction);
        }
    }

    private static TemplateValueUiStatus mapDefinedStatus(IAttributeValue propertyValue,
            Function<IAttributeValue, Object> valueFunction) {
        IAttributeValue templateProperty = propertyValue.findTemplateProperty(propertyValue.getIpsProject());
        if (templateProperty == null) {
            return NEWLY_DEFINED;
        } else {
            Object value = valueFunction.apply(propertyValue);
            Object templateValue = valueFunction.apply(templateProperty);
            if (ObjectUtils.equals(value, templateValue)) {
                return OVERWRITE_EQUAL;
            } else {
                return OVERWRITE;
            }
        }
    }
}
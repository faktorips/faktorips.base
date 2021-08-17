/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.Comparator;
import java.util.function.Function;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;

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

    public static <T extends ITemplatedValue> TemplateValueUiStatus mapStatus(T propertyValue) {
        TemplateValueStatus templateStatus = propertyValue.getTemplateValueStatus();
        if (templateStatus == TemplateValueStatus.INHERITED) {
            return INHERITED;
        } else if (templateStatus == TemplateValueStatus.UNDEFINED) {
            return UNDEFINED;
        } else {
            return mapDefinedStatus(propertyValue);
        }
    }

    private static <T extends ITemplatedValue> TemplateValueUiStatus mapDefinedStatus(T propertyValue) {
        @SuppressWarnings("unchecked")
        T templateProperty = (T)propertyValue.findTemplateProperty(propertyValue.getIpsProject());
        if (templateProperty == null) {
            return NEWLY_DEFINED;
        } else {
            Comparator<Object> comparator = propertyValue.getValueComparator();
            if (comparator.compare(getValue(propertyValue), getValue(templateProperty)) == 0) {
                return OVERWRITE_EQUAL;
            } else {
                return OVERWRITE;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends ITemplatedValue> Object getValue(T propertyValue) {
        return ((Function<T, Object>)propertyValue.getValueGetter()).apply(propertyValue);
    }

}
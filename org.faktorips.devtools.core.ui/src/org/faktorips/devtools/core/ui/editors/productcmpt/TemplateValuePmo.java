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

import java.beans.PropertyChangeEvent;

import com.google.common.base.Function;

import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

public class TemplateValuePmo extends PresentationModelObject {

    public static final String PROPERTY_TEMPLATE_VALUE_STATUS = "templateValueStatus"; //$NON-NLS-1$

    private IAttributeValue attributeValue;

    public TemplateValuePmo(IAttributeValue attributeValue) {
        this.attributeValue = attributeValue;
    }

    public TemplateValueUiStatus getTemplateValueStatus() {
        return TemplateValueUiStatus.mapStatus(attributeValue, new Function<IAttributeValue, Object>() {

            @Override
            public Object apply(IAttributeValue attributeValue) {
                return attributeValue != null ? attributeValue.getValueHolder() : null;
            }
        });
    }

    public void onClick() {
        TemplateValueUiStatus oldValue = getTemplateValueStatus();
        attributeValue.switchTemplateValueStatus();
        TemplateValueUiStatus newValue = getTemplateValueStatus();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_TEMPLATE_VALUE_STATUS, oldValue, newValue));
    }

}
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

public class TemplateValuePmo<T extends IPropertyValue> extends PresentationModelObject {

    public static final String PROPERTY_TEMPLATE_VALUE_STATUS = "templateValueStatus"; //$NON-NLS-1$

    public static final String PROPERTY_TOOL_TIP_TEXT = "toolTipText"; //$NON-NLS-1$

    private T propertyValue;

    private Function<T, String> formatter;

    public TemplateValuePmo(T propertyValue, Function<T, String> formatter) {
        this.propertyValue = propertyValue;
        this.formatter = formatter;
    }

    public TemplateValueUiStatus getTemplateValueStatus() {
        return TemplateValueUiStatus.mapStatus(propertyValue);
    }

    public String getToolTipText() {
        switch (getTemplateValueStatus()) {
            case INHERITED:
                return NLS.bind(Messages.TemplateValuePmo_Status_Inherited, getTemplateName());
            case OVERWRITE:
                return NLS.bind(Messages.TemplateValuePmo_Status_Override, getTemplateValue(), getTemplateName());
            case OVERWRITE_EQUAL:
                return NLS.bind(Messages.TemplateValuePmo_Status_OverrideEqual, getTemplateName());
            case UNDEFINED:
                if (findTemplateProperty() == null) {
                    return Messages.TemplateValuePmo_Status_Undefined_WithoutParentTemplate;
                } else {
                    return NLS.bind(Messages.TemplateValuePmo_Status_Undefined, getTemplateValue(), getTemplateName());
                }
            case NEWLY_DEFINED:
                if (isUsingTemplate()) {
                    return NLS.bind(Messages.TemplateValuePmo_Status_NewlyDefined, getTemplateName());
                } else {
                    return Messages.TemplateValuePmo_Status_NewlyDefined_withoutParentTemplate;
                }
            default:
                return StringUtils.EMPTY;
        }
    }

    private boolean isUsingTemplate() {
        return propertyValue.getPropertyValueContainer().getProductCmpt().isUsingTemplate();
    }

    /**
     * @return the formatted value from the template or the empty string, if the template value
     *         cannot be found.
     */
    protected String getTemplateValue() {
        T templateProperty = findTemplateProperty();
        if (templateProperty != null) {
            return formatPropertyValue(templateProperty);
        } else {
            return StringUtils.EMPTY;
        }
    }

    private String formatPropertyValue(T templateProperty) {
        return formatter.apply(templateProperty);
    }

    private IIpsProject getIpsProject() {
        return propertyValue.getIpsProject();
    }

    /**
     * @return the template's name. If the template cannot be found, returns the saved qualified
     *         name.
     */
    protected String getTemplateName() {
        T templateProperty = findTemplateProperty();
        if (templateProperty != null) {
            return templateProperty.getPropertyValueContainer().getProductCmpt().getName();
        } else {
            return propertyValue.getPropertyValueContainer().getTemplate();
        }
    }

    @SuppressWarnings("unchecked")
    private T findTemplateProperty() {
        return (T)propertyValue.findTemplateProperty(getIpsProject());
    }

    public void onClick() {
        TemplateValueUiStatus oldValue = getTemplateValueStatus();
        propertyValue.switchTemplateValueStatus();
        TemplateValueUiStatus newValue = getTemplateValueStatus();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_TEMPLATE_VALUE_STATUS, oldValue, newValue));
    }

}
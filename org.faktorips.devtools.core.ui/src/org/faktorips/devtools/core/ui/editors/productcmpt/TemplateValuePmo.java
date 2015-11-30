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
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

public class TemplateValuePmo extends PresentationModelObject {

    public static final String PROPERTY_TEMPLATE_VALUE_STATUS = "templateValueStatus"; //$NON-NLS-1$

    public static final String PROPERTY_TOOL_TIP_TEXT = "toolTipText"; //$NON-NLS-1$

    private IAttributeValue attributeValue;

    public TemplateValuePmo(IAttributeValue attributeValue) {
        this.attributeValue = attributeValue;
    }

    public TemplateValueUiStatus getTemplateValueStatus() {
        return TemplateValueUiStatus.mapStatus(attributeValue, new Function<IAttributeValue, Object>() {

            @Override
            public Object apply(IAttributeValue value) {
                return value != null ? value.getValueHolder() : null;
            }
        });
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
        return attributeValue.getPropertyValueContainer().getProductCmpt().isUsingTemplate();
    }

    /**
     * @return the formatted value from the template or the empty string, if the template value
     *         cannot be found.
     */
    protected String getTemplateValue() {
        IAttributeValue templateProperty = findTemplateProperty();
        if (templateProperty != null) {
            return AttributeValueFormatter.format(templateProperty);
        } else {
            return StringUtils.EMPTY;
        }
    }

    private IIpsProject getIpsProject() {
        return attributeValue.getIpsProject();
    }

    /**
     * @return the template's name. If the template cannot be found, returns the saved qualified
     *         name.
     */
    protected String getTemplateName() {
        IAttributeValue templateProperty = findTemplateProperty();
        if (templateProperty != null) {
            return templateProperty.getPropertyValueContainer().getProductCmpt().getName();
        } else {
            return attributeValue.getPropertyValueContainer().getTemplate();
        }
    }

    private IAttributeValue findTemplateProperty() {
        return attributeValue.findTemplateProperty(getIpsProject());
    }

    public void onClick() {
        TemplateValueUiStatus oldValue = getTemplateValueStatus();
        attributeValue.switchTemplateValueStatus();
        TemplateValueUiStatus newValue = getTemplateValueStatus();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_TEMPLATE_VALUE_STATUS, oldValue, newValue));
    }

}
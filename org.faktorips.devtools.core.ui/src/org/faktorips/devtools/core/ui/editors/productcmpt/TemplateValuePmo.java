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

import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;

public class TemplateValuePmo<T extends IPropertyValue> extends AbstractTemplateValuePmo<T> {

    private Function<T, String> formatter;

    public TemplateValuePmo(T propertyValue, Function<T, String> formatter) {
        super(propertyValue);
        this.formatter = formatter;
    }

    protected T getPropertyValue() {
        return getTemplatedProperty();
    }

    protected String formatPropertyValue(T templateProperty) {
        return formatter.apply(templateProperty);
    }

    @Override
    public TemplateValueUiStatus getTemplateValueStatus() {
        return TemplateValueUiStatus.mapStatus(getTemplatedProperty());
    }

    @Override
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

}
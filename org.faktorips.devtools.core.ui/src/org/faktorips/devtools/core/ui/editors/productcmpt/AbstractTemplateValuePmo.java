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

import java.beans.PropertyChangeEvent;

import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;

public abstract class AbstractTemplateValuePmo<T extends ITemplatedValue> extends PresentationModelObject {

    public static final String PROPERTY_TEMPLATE_VALUE_STATUS = "templateValueStatus"; //$NON-NLS-1$
    public static final String PROPERTY_TOOL_TIP_TEXT = "toolTipText"; //$NON-NLS-1$

    private T templatedProperty;

    public AbstractTemplateValuePmo() {
        this(null);
    }

    public AbstractTemplateValuePmo(T templatedProperty) {
        super();
        this.templatedProperty = templatedProperty;
    }

    public abstract TemplateValueUiStatus getTemplateValueStatus();

    public abstract String getToolTipText();

    protected void setTemplatedProperty(T property) {
        templatedProperty = property;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_TEMPLATE_VALUE_STATUS, null, null));
        // notifyListeners(new PropertyChangeEvent(this, PROPERTY_TOOL_TIP_TEXT, null, null));
    }

    protected boolean isUsingTemplate() {
        return getTemplatedProperty().getTemplatedValueContainer().isUsingTemplate();
    }

    private IIpsProject getIpsProject() {
        return getTemplatedProperty().getIpsProject();
    }

    /**
     * @return the template's name. If the template cannot be found, returns the saved qualified
     *         name.
     */
    protected String getTemplateName() {
        T templateProperty = findTemplateProperty();
        if (templateProperty != null) {
            return templateProperty.getTemplatedValueContainer().getProductCmpt().getName();
        } else {
            return getTemplatedProperty().getTemplatedValueContainer().getTemplate();
        }
    }

    @SuppressWarnings("unchecked")
    protected T findTemplateProperty() {
        return (T)getTemplatedProperty().findTemplateProperty(getIpsProject());
    }

    public void onClick() {
        TemplateValueUiStatus oldValue = getTemplateValueStatus();
        getTemplatedProperty().switchTemplateValueStatus();
        TemplateValueUiStatus newValue = getTemplateValueStatus();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_TEMPLATE_VALUE_STATUS, oldValue, newValue));
    }

    public T getTemplatedProperty() {
        return templatedProperty;
    }

}
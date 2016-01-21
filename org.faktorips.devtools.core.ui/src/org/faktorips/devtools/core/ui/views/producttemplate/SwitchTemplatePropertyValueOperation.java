/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;

/**
 * An operation to switch a template value. The operation is triggered with a list of
 * {@link IPropertyValue}. All these property values needs to have the same value and are based on
 * the same template. After performing this operation the property value in the template will have
 * the value of the selected {@link IPropertyValue} and the selected {@link IPropertyValue} will
 * inherit this value. All previously inherited property values will be configured with
 * {@link TemplateValueStatus#DEFINED}.
 * 
 * @author dirmeier
 */
public class SwitchTemplatePropertyValueOperation extends AbstractPropertyValueOperation {

    private final IPropertyValue templatePropertyValue;
    private final Object newValue;
    private final Collection<? extends IPropertyValue> inheritingPropertyValues;
    private final Collection<? extends IPropertyValue> definingPropertyValues;

    public SwitchTemplatePropertyValueOperation(IPropertyValue templatePropertyValue, Object newValue,
            Collection<? extends IPropertyValue> inheritingPropertyValues,
            Collection<? extends IPropertyValue> definingPropertyValues) {
        this.templatePropertyValue = templatePropertyValue;
        this.newValue = newValue;
        this.inheritingPropertyValues = inheritingPropertyValues;
        this.definingPropertyValues = definingPropertyValues;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        int count = getInheritingPropertyValues().size() + getDefiningPropertyValues().size() + 1;
        monitor.beginTask(Messages.SwitchTemplatePropertyValueOperation_progress, count + 10);
        for (IPropertyValue inheritingPropertyValue : getInheritingPropertyValues()) {
            checkForSave(inheritingPropertyValue);
            inheritingPropertyValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
            monitor.worked(1);
        }
        checkForSave(getTemplatePropertyValue());
        getTemplatePropertyValue().getPropertyValueType().getValueSetter()
                .accept(getTemplatePropertyValue(), getNewValue());
        monitor.worked(1);
        for (IPropertyValue definingPropertyValue : getDefiningPropertyValues()) {
            checkForSave(definingPropertyValue);
            definingPropertyValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
            monitor.worked(1);
        }
        save(new SubProgressMonitor(monitor, 10));
        monitor.done();
    }

    /**
     * Check the list of property values if they all have the same value and are based on the same
     * template. If the check passes this method returns a new
     * {@link SwitchTemplatePropertyValueOperation} that could be performed. Otherwise it returns
     * <code>null</code>.
     * 
     * @param selectedPropertyValues A list of property values with the same value and template
     * @return an operation that could be performed to switch the template.
     */
    public static SwitchTemplatePropertyValueOperation create(Collection<? extends IPropertyValue> selectedPropertyValues) {
        PropertyValueType propertyValueType = null;
        IPropertyValue templatePropertyValue = null;
        Object value = null;
        for (IPropertyValue propertyValue : selectedPropertyValues) {
            if (propertyValueType == null) {
                propertyValueType = propertyValue.getPropertyValueType();
                templatePropertyValue = propertyValue.findTemplateProperty(propertyValue.getIpsProject());
                value = propertyValueType.getValueGetter().apply(propertyValue);
            } else {
                if (!propertyValueType.getValueGetter().apply(propertyValue).equals(value)
                        || !propertyValue.findTemplateProperty(propertyValue.getIpsProject()).equals(
                                templatePropertyValue)) {
                    return null;
                }
            }
        }
        TemplatePropertyUsagePmo templatePropertyUsagePmo = new TemplatePropertyUsagePmo(templatePropertyValue);
        Collection<IPropertyValue> inheritingPropertyValues = templatePropertyUsagePmo.getInheritingPropertyValues();
        return new SwitchTemplatePropertyValueOperation(templatePropertyValue, value, inheritingPropertyValues,
                selectedPropertyValues);
    }

    public IPropertyValue getTemplatePropertyValue() {
        return templatePropertyValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Collection<? extends IPropertyValue> getInheritingPropertyValues() {
        return inheritingPropertyValues;
    }

    public Collection<? extends IPropertyValue> getDefiningPropertyValues() {
        return definingPropertyValues;
    }

}
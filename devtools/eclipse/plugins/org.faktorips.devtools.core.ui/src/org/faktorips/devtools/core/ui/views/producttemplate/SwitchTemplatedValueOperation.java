/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.producttemplate;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueIdentifier;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.util.TemplatedValueUtil;

/**
 * An operation to switch a templated value. The operation is triggered with a list of
 * {@link ITemplatedValue}. All these values have to have the same value and have to be based on the
 * same template. After performing this operation the value in the template will have the value of
 * the selected values and all selected values will inherit that value. All previously inherited
 * templated values will be configured with {@link TemplateValueStatus#DEFINED}.
 * 
 * @author dirmeier
 */
public class SwitchTemplatedValueOperation extends AbstractTemplatedValueOperation {

    private final ITemplatedValue templateValue;
    private final Object newValue;
    private final Collection<? extends ITemplatedValue> inheritingValues;
    private final Collection<? extends ITemplatedValue> definingValues;

    public SwitchTemplatedValueOperation(ITemplatedValue templateValue, Object newValue,
            Collection<? extends ITemplatedValue> inheritingValues,
            Collection<? extends ITemplatedValue> definingValues) {
        this.templateValue = templateValue;
        this.newValue = newValue;
        this.inheritingValues = inheritingValues;
        this.definingValues = definingValues;
    }

    @Override
    public void run(IProgressMonitor monitor) {
        int count = getInheritingPropertyValues().size() + getDefiningPropertyValues().size() + 1;
        SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.SwitchTemplatePropertyValueOperation_progress,
                count + 10);
        for (ITemplatedValue inheritingPropertyValue : getInheritingPropertyValues()) {
            checkForSave(inheritingPropertyValue);
            inheritingPropertyValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
            subMonitor.worked(1);
        }
        checkForSave(getTemplateValue());
        getTemplateValue().setTemplateValueStatus(TemplateValueStatus.DEFINED);
        setNewValueInTemplate();
        subMonitor.worked(1);
        for (ITemplatedValue definingPropertyValue : getDefiningPropertyValues()) {
            checkForSave(definingPropertyValue);
            definingPropertyValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
            subMonitor.worked(1);
        }
        save(subMonitor.split(10));
    }

    @SuppressWarnings("unchecked")
    private void setNewValueInTemplate() {
        ((BiConsumer<ITemplatedValue, Object>)getTemplateValue().getValueSetter()).accept(getTemplateValue(),
                getNewValue());
    }

    /**
     * Checks whether all property values have the same value and are based on the same template. If
     * the check passes returns <code>true</code> otherwise <code>false</code>
     * 
     * @param selectedPropertyValues A list of property values with the same value and template
     * @return <code>true</code> if you can use this selection to perform this operation
     */
    public static boolean isValidSelection(Collection<? extends ITemplatedValue> selectedPropertyValues) {
        if (selectedPropertyValues.isEmpty()) {
            return false;
        }
        ITemplatedValue templatePropertyValue = null;
        ITemplatedValueIdentifier identifier = null;
        Comparator<Object> valueComparator = null;
        Object value = null;
        for (ITemplatedValue propertyValue : selectedPropertyValues) {
            if (identifier == null) {
                identifier = propertyValue.getIdentifier();
                templatePropertyValue = getTemplateValue(propertyValue);
                if (templatePropertyValue == null) {
                    return false;
                }
                value = getActualValue(identifier, propertyValue.getTemplatedValueContainer());
                valueComparator = propertyValue.getValueComparator();
            } else {
                Object otherValue = getActualValue(identifier, propertyValue.getTemplatedValueContainer());
                if (!isMatchingPropertyValue(templatePropertyValue, valueComparator, value, propertyValue,
                        otherValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Retrieves the template value's actual value from the provided container using the provided
     * identifier.
     */
    @SuppressWarnings("unchecked")
    private static Object getActualValue(ITemplatedValueIdentifier identifier, ITemplatedValueContainer container) {
        ITemplatedValue templatedValue = identifier.getValueFrom(container);
        return ((Function<ITemplatedValue, Object>)templatedValue.getValueGetter()).apply(templatedValue);
    }

    private static boolean isMatchingPropertyValue(ITemplatedValue templateValue,
            Comparator<Object> valueComparator,
            Object value,
            ITemplatedValue otherComponent,
            Object otherValue) {
        if (valueComparator == null || valueComparator.compare(value, otherValue) != 0) {
            return false;
        }
        ITemplatedValue foundTemplateValue = getTemplateValue(otherComponent);
        return templateValue.equals(foundTemplateValue);
    }

    private static ITemplatedValue getTemplateValue(ITemplatedValue someTemplatedValue) {
        ITemplatedValue foundTemplateProperty = someTemplatedValue
                .findTemplateProperty(someTemplatedValue.getIpsProject());
        if (foundTemplateProperty == null) {
            foundTemplateProperty = TemplatedValueUtil.findNextTemplateValue(someTemplatedValue);
        }
        return foundTemplateProperty;

    }

    /**
     * Checks whether all property values have the same value and are based on the same template. If
     * the check passes this method returns a new {@link SwitchTemplatedValueOperation} that can be
     * executed, otherwise returns <code>null</code>.
     * 
     * @param selectedValues A list of property values with the same value and template
     * @return an operation that can be executed to switch template values.
     */
    public static SwitchTemplatedValueOperation create(Collection<? extends ITemplatedValue> selectedValues) {
        if (!isValidSelection(selectedValues)) {
            throw new IllegalArgumentException("Illegal selection for switch template value opertation."); //$NON-NLS-1$
        }
        ITemplatedValue firstValue = selectedValues.iterator().next();
        ITemplatedValue templateValue = getTemplateValue(firstValue);
        TemplatePropertyUsagePmo pmo = new TemplatePropertyUsagePmo(templateValue);
        Collection<ITemplatedValue> inheritingValues = pmo.getInheritingTemplatedValues();
        @SuppressWarnings("unchecked")
        Function<ITemplatedValue, Object> valueGetter = (Function<ITemplatedValue, Object>)firstValue.getValueGetter();
        return new SwitchTemplatedValueOperation(templateValue, valueGetter.apply(firstValue), inheritingValues,
                selectedValues);
    }

    public ITemplatedValue getTemplateValue() {
        return templateValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Collection<? extends ITemplatedValue> getInheritingPropertyValues() {
        return inheritingValues;
    }

    public Collection<? extends ITemplatedValue> getDefiningPropertyValues() {
        return definingValues;
    }

}

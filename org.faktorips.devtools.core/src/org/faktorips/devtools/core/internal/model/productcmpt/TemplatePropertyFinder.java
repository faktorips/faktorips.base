/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.TemplateHierarchyVisitor;
import org.faktorips.devtools.core.model.productcmpt.TemplateValueStatus;

public class TemplatePropertyFinder<T extends IPropertyValue> extends TemplateHierarchyVisitor {

    private final Class<T> propertyType;
    private final T originalPropertyValue;
    private T resultingPropertyValue;

    public TemplatePropertyFinder(T originalPropertyValue, Class<T> propertyType, IIpsProject ipsProject) {
        super(ipsProject);
        this.originalPropertyValue = originalPropertyValue;
        this.propertyType = propertyType;
    }

    @Override
    protected boolean visit(IPropertyValueContainer currentValueContainer) {
        if (originalPropertyValue.getPropertyValueContainer() == currentValueContainer) {
            /*
             * Ignore property value on which the search was started and continue searching.
             */
            return true;
        }
        T currentValue = currentValueContainer.getPropertyValue(originalPropertyValue.getPropertyName(), propertyType);
        if (currentValue == null || isInherited(currentValue)) {
            return true;
        } else if (isDefined(currentValue)) {
            this.resultingPropertyValue = currentValue;
        }
        return false;
    }

    private boolean isDefined(T currentValue) {
        return getTemplateStatus(currentValue) == TemplateValueStatus.DEFINED;
    }

    private boolean isInherited(T currentValue) {
        return getTemplateStatus(currentValue) == TemplateValueStatus.INHERITED;
    }

    private TemplateValueStatus getTemplateStatus(T value) {
        if (!(value instanceof IAttributeValue)) {
            throw new CoreRuntimeException("PropertyTemplateFinder can currently only find attribute values"); //$NON-NLS-1$
        }
        return ((IAttributeValue)value).getTemplateValueStatus();
    }

    public T getPropertyValue() {
        return resultingPropertyValue;
    }
}

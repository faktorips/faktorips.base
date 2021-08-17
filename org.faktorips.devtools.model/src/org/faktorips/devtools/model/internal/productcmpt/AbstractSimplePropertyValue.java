/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.faktorips.devtools.model.internal.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueIdentifier;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;

/**
 * Base class for all {@link IPropertyValue} subclasses except {@link Formula}. Methods with
 * identical behavior in all subclasses are implemented here.
 */
abstract class AbstractSimplePropertyValue extends BaseIpsObjectPart implements IPropertyValue {

    public AbstractSimplePropertyValue(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    public IPropertyValueContainer getPropertyValueContainer() {
        return (IPropertyValueContainer)getParent();
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return getPropertyValueType().getCorrespondingPropertyType();
    }

    @Override
    public Comparator<Object> getValueComparator() {
        return getPropertyValueType().getValueComparator();
    }

    @Override
    public Function<IPropertyValue, Object> getValueGetter() {
        return getPropertyValueType().getValueGetter();
    }

    @Override
    public BiConsumer<IPropertyValue, Object> getValueSetter() {
        return getPropertyValueType().getValueSetter();
    }

    @Override
    public ITemplatedValueIdentifier getIdentifier() {
        return new PropertyValueIdentifier(this);
    }

    @Override
    public IPropertyValueContainer getTemplatedValueContainer() {
        return getPropertyValueContainer();
    }

    @Override
    public boolean isPartOfTemplateHierarchy() {
        return (getTemplatedValueContainer().isUsingTemplate() && hasTemplateForProperty(getIpsProject()))
                || getTemplatedValueContainer().isProductTemplate();
    }

    @Override
    public void switchTemplateValueStatus() {
        setTemplateValueStatus(getTemplateValueStatus().getNextStatus(this));
    }

    @Override
    public boolean isConcreteValue() {
        return getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

}

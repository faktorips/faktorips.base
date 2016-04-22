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

import java.util.Comparator;

import com.google.common.base.Function;

import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValueIdentifier;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.util.functional.BiConsumer;

/**
 * Base class for all {@link IPropertyValue} subclasses except {@link Formula}. Methods with
 * identical behavior in all subclasses are implemented here.
 */
abstract class AbstractSimplePropertyValue extends BaseIpsObjectPart implements IPropertyValue {

    public AbstractSimplePropertyValue(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    public final IPropertyValueContainer getPropertyValueContainer() {
        return (IPropertyValueContainer)getParent();
    }

    @Override
    public ProductCmptPropertyType getPropertyType() {
        return getProductCmptPropertyType();
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
        return getTemplatedValueContainer().isPartOfTemplateHierarchy();
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

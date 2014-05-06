/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder.Factory;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.util.ArgumentCheck;

/**
 * Class realizing a delta entry concerning hidden Attributes {@link IProductCmptType}
 */
public class HiddenAttributeMismatchEntry extends AbstractDeltaEntryForProperty {

    public HiddenAttributeMismatchEntry(IAttributeValue attributeValue) {
        super(attributeValue);
        ArgumentCheck.notNull(attributeValue);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.HIDDEN_ATTRIBUTE_MISMATCH;
    }

    @Override
    public String getDescription() {
        return NLS.bind(Messages.HiddenAttributeMismatchEntry_desc, getPropertyName());
    }

    private IAttributeValue getAttributeValue() {
        return (IAttributeValue)getPropertyValue();
    }

    @Override
    public void fix() {
        IProductCmptType prodCmpT = getProdCmptType();
        IAttribute attribute = prodCmpT.getAttribute(getAttributeValue().getAttribute());
        String defaultValue = attribute.getDefaultValue();
        setAttributeValueToDefault(defaultValue);
    }

    private void setAttributeValueToDefault(String defaultValue) {
        IAttributeValue attributeValue = getAttributeValue();
        attributeValue.setValueHolder(getValueHolderFor(defaultValue));
    }

    private IValueHolder<?> getValueHolderFor(String defaultValue) {
        IAttributeValue attributeValue = getAttributeValue();
        if (attributeValue.getValueHolder() instanceof MultiValueHolder) {
            return createMultiValueHolder(defaultValue, attributeValue);
        } else {
            return new SingleValueHolder(attributeValue, defaultValue);
        }
    }

    private IValueHolder<?> createMultiValueHolder(String defaultValue, IAttributeValue attributeValue) {
        Factory factory = new MultiValueHolder.Factory();
        List<SingleValueHolder> multiDefaultValues = factory.splitMultiDefaultValues(attributeValue, new StringValue(
                defaultValue));
        return new MultiValueHolder(attributeValue, multiDefaultValues);
    }

    private IProductCmptType getProdCmptType() {
        try {
            return getAttributeValue().getPropertyValueContainer().findProductCmptType(
                    getAttributeValue().getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
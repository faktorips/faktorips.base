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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.Messages;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;

/**
 * Class realizing a delta entry concerning hidden Attributes {@link IProductCmptType}
 */
public class HiddenAttributeMismatchEntry extends AbstractDeltaEntryForProperty {

    private IAttributeValue attributeValue;

    public HiddenAttributeMismatchEntry(IPropertyValue propertyValue, IAttributeValue attributeValue) {
        super(propertyValue);
        this.attributeValue = attributeValue;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.HIDDEN_ATTRIBUTE_MISMATCH;
    }

    @Override
    public String getDescription() {
        return Messages.Hidden_Attribute_InvalidValue;
    }

    @Override
    public void fix() {
        IProductCmptType prodCmpT = getProdCmptType();
        IAttribute attribute = prodCmpT.getAttribute(attributeValue.getAttribute());
        String defaultValue = attribute.getDefaultValue();
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, defaultValue));
    }

    private IProductCmptType getProdCmptType() {
        try {
            return attributeValue.getPropertyValueContainer().findProductCmptType(attributeValue.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
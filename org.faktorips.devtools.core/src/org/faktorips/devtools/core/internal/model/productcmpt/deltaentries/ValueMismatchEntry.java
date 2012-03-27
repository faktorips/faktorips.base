/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;

/**
 * This delta entry describes a mismatch of the value between an attribute and its attribute value.
 * For example the attribute is marked as multi value attribute but the attribute value has a single
 * value.
 * 
 * @author dirmeier
 */
public class ValueMismatchEntry extends AbstractDeltaEntryForProperty {

    private final IProductCmptTypeAttribute attribute;

    public ValueMismatchEntry(IAttributeValue attributeValue, IProductCmptTypeAttribute attribute) {
        super(attributeValue);
        this.attribute = attribute;
    }

    @Override
    public IAttributeValue getPropertyValue() {
        return (IAttributeValue)super.getPropertyValue();
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.VALUE_MISMATCH;
    }

    @Override
    public String getDescription() {
        if (attribute.isMultiValueAttribute()) {
            return NLS.bind(Messages.ValueMismatchEntry_convertSingleToMultiValue, getPropertyName());
        } else {
            return NLS.bind(Messages.ValueMismatchEntry_convertMultiToSingleValue, getPropertyName());
        }
    }

    @Override
    public void fix() {
        IValueHolder<?> value = getPropertyValue().getValueHolder();
        if (!attribute.isMultiValueAttribute() && value instanceof MultiValueHolder) {
            MultiValueHolder multiValue = (MultiValueHolder)value;
            String oldValue = multiValue.getValue().get(0).getValue();
            SingleValueHolder newValue = new SingleValueHolder(getPropertyValue(), oldValue);
            getPropertyValue().setValueHolder(newValue);
        } else if (attribute.isMultiValueAttribute() && value instanceof SingleValueHolder) {
            List<SingleValueHolder> oldValueList = new ArrayList<SingleValueHolder>();
            oldValueList.add((SingleValueHolder)value);
            MultiValueHolder newValue = new MultiValueHolder(getPropertyValue(), oldValueList);
            getPropertyValue().setValueHolder(newValue);
        }
    }

}

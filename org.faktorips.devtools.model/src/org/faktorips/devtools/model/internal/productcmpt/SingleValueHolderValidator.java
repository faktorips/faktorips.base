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

import static org.faktorips.devtools.model.productcmpt.IAttributeValue.MSGCODE_INVALID_VALUE_TYPE;
import static org.faktorips.devtools.model.productcmpt.IAttributeValue.MSGCODE_VALUE_NOT_IN_SET;

import java.text.MessageFormat;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;

/** {@code IValueHolderValidator} implementation for {@link SingleValueHolder}s. */
public class SingleValueHolderValidator implements IValueHolderValidator {

    private final SingleValueHolder valueHolder;
    private final IAttributeValue parent;
    private final IIpsProject ipsProject;
    private final ObjectProperty[] invalidObjectProperties;

    public SingleValueHolderValidator(SingleValueHolder valueHolder, IAttributeValue parent, IIpsProject ipsProject) {
        super();
        this.valueHolder = valueHolder;
        this.parent = parent;
        this.ipsProject = ipsProject;
        this.invalidObjectProperties = new ObjectProperty[] {
                new ObjectProperty(parent, IAttributeValue.PROPERTY_VALUE_HOLDER),
                new ObjectProperty(valueHolder, IValueHolder.PROPERTY_VALUE) };
    }

    @Override
    public MessageList validate() throws CoreRuntimeException {

        MessageList messages = new MessageList();

        IValue<?> value = valueHolder.getValue();
        IProductCmptTypeAttribute attribute = parent.findAttribute(ipsProject);

        if (attribute == null || value == null) {
            return messages;
        }

        ValueDatatype datatype = attribute.findDatatype(ipsProject);

        value.validate(datatype, attribute.getDatatype(), ipsProject, messages, invalidObjectProperties);
        if (!messages.isEmpty()) {
            return messages;
        }

        if (ValueType.STRING.equals(valueHolder.getValueType())) {
            if (attribute.isMultilingual()) {
                String text = MessageFormat.format(Messages.AttributeValue_MultiLingual, parent.getAttribute());
                messages.newError(MSGCODE_INVALID_VALUE_TYPE, text, invalidObjectProperties);
            }
            if (!attribute.getValueSet().containsValue(((StringValue)value).getContentAsString(), ipsProject)) {
                String text;
                String formattedValue = getFormattedValue(value, datatype);
                if (attribute.getValueSet().getValueSetType() == ValueSetType.RANGE) {
                    text = MessageFormat.format(Messages.AttributeValue_AllowedValuesAre, formattedValue,
                            attribute.getValueSet().toShortString());
                } else {
                    text = MessageFormat.format(Messages.AttributeValue_ValueNotAllowed, formattedValue,
                            parent.getName());
                }
                messages.newError(MSGCODE_VALUE_NOT_IN_SET, text, invalidObjectProperties);
            }
        } else if (ValueType.INTERNATIONAL_STRING.equals(valueHolder.getValueType())) {
            if (!attribute.isMultilingual()) {
                String text = MessageFormat.format(Messages.AttributeValue_NotMultiLingual, parent.getAttribute());
                messages.newError(MSGCODE_INVALID_VALUE_TYPE, text, invalidObjectProperties);
            }
        }
        return messages;
    }

    private String getFormattedValue(IValue<?> value, ValueDatatype datatype) {
        return IIpsModelExtensions.get().getModelPreferences().getDatatypeFormatter().formatValue(datatype,
                value.getContentAsString());
    }

}

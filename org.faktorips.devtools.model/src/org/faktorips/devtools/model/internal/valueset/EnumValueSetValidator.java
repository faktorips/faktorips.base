/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.valueset;

import static org.faktorips.devtools.model.util.DatatypeUtil.isNullValue;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;

public class EnumValueSetValidator extends AbstractValueSetValidator<EnumValueSet> {

    private MessageList list;

    public EnumValueSetValidator(EnumValueSet enumValueSet, IValueSetOwner owner, ValueDatatype datatype) {
        super(enumValueSet, owner, datatype);
    }

    @Override
    public MessageList validate() {
        list = new MessageList();
        if (isValidDatatype()) {
            validateNullValue();
            int numOfValues = getValueSet().size();
            for (int i = 0; i < numOfValues; i++) {
                validateValueWithoutDuplicateCheck(i);
            }
            checkForDuplicates();
        }
        return list;
    }

    private void validateNullValue() {
        if (getDatatype().isPrimitive() && getValueSet().isContainsNull()) {
            String text = Messages.ValueSet_msgNullNotSupported;
            list.add(new Message(IEnumValueSet.MSGCODE_NULL_NOT_SUPPORTED, text, Message.ERROR, getValueSet(),
                    IEnumValueSet.PROPERTY_CONTAINS_NULL));
        }
    }

    public MessageList validateValue(int index) {
        list = new MessageList();
        if (isValidDatatype()) {
            validateValueWithoutDuplicateCheck(index);
            if (list.getSeverity() != Message.ERROR) {
                checkForDuplicate(index);
            }
        }
        return list;
    }

    /**
     * Validates the datatype and returns <code>true</code> if the datatype is valid or
     * <code>false</code> if it is invalid.
     * 
     * If the datatype is invalid a proper massage is added to the list of messages.
     * 
     */
    private boolean isValidDatatype() {
        if (getDatatype() == null) {
            String msg = MessageFormat.format(Messages.EnumValueSet_msgDatatypeUnknown, getOwner().getName());
            list.add(new Message(IEnumValueSet.MSGCODE_UNKNOWN_DATATYPE, msg, Message.WARNING, getParentOp()));
            return false;
        }
        return true;
    }

    private void validateValueWithoutDuplicateCheck(int index) {
        ObjectProperty op = new ObjectProperty(getValueSet(), IEnumValueSet.PROPERTY_VALUES, index);
        String value = getValueSet().getValue(index);
        if (isSpecialNull(value, getDatatype())) {
            String msg = MessageFormat.format(Messages.EnumValueSet_msgValueNotParsable, getNotNullValue(value),
                    getDatatype().getName());
            list.add(new Message(IEnumValueSet.MSGCODE_VALUE_NOT_PARSABLE, msg, Message.ERROR, op, getParentOp()));
        }
        ValidationUtils.checkParsable(getDatatype(), value, getValueSet(), IEnumValueSet.PROPERTY_VALUES, list);
    }

    private void checkForDuplicate(int index) {
        String value = getValueSet().getValue(index);
        if (getValuesToIndexMap().get(value).size() > 1) {
            ObjectProperty op = new ObjectProperty(getValueSet(), IEnumValueSet.PROPERTY_VALUES, index);
            list.add(createMsgForDuplicateValues(value, op));
        }
    }

    private Map<String, List<Integer>> getValuesToIndexMap() {
        return getValueSet().getValuesToIndexMap();
    }

    private void checkForDuplicates() {
        for (String value : getValuesToIndexMap().keySet()) {
            List<Integer> indexes = getValuesToIndexMap().get(value);
            if (indexes.size() <= 1) {
                continue;
            }
            List<ObjectProperty> ops = new ArrayList<>(indexes.size());
            ops.add(getParentOp());
            for (Integer index : indexes) {
                ops.add(new ObjectProperty(getValueSet(), IEnumValueSet.PROPERTY_VALUES, index));
            }
            list.add(createMsgForDuplicateValues(value, ops.toArray(new ObjectProperty[ops.size()])));
        }
    }

    private Message createMsgForDuplicateValues(String value, ObjectProperty... ops) {
        String msg = MessageFormat.format(Messages.EnumValueSet_msgDuplicateValue, getNotNullValue(value));
        return new Message(IEnumValueSet.MSGCODE_DUPLICATE_VALUE, msg, Message.ERROR, ops);
    }

    /**
     * Returns whether the given value represents the special null value for the given datatype.
     */
    private boolean isSpecialNull(String value, ValueDatatype datatype) {
        if (datatype.isPrimitive()) {
            return false;
        }

        if (value == null) {
            return false;
        }

        return isNullValue(datatype, value);
    }

    private String getNotNullValue(String value) {
        if (value == null) {
            return IIpsModelExtensions.get().getModelPreferences().getNullPresentation();
        }
        return value;
    }

    private ObjectProperty getParentOp() {
        return new ObjectProperty(getOwner(), IValueSetOwner.PROPERTY_VALUE_SET);
    }

}

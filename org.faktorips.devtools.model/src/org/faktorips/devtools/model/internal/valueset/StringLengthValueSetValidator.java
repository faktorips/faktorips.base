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

import static org.faktorips.devtools.model.valueset.IValueSet.MSGCODE_UNKNOWN_DATATYPE;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.runtime.MessageList;

public class StringLengthValueSetValidator extends AbstractValueSetValidator<StringLengthValueSet> {

    public StringLengthValueSetValidator(StringLengthValueSet valueSet, IValueSetOwner owner, ValueDatatype datatype) {
        super(valueSet, owner, datatype);
    }

    @Override
    public MessageList validate() {
        MessageList messages = new MessageList();

        String maxLengthValue = getValueSet().getMaximumLength();

        if (getDatatype() == null) {
            messages.newError(MSGCODE_UNKNOWN_DATATYPE, Messages.EnumValueSet_msgDatatypeUnknown, getValueSet(),
                    StringLengthValueSet.PROPERTY_MAXIMUMLENGTH);
            return messages;
        }

        if (ValidationUtils.checkParsable(Datatype.INTEGER, maxLengthValue, getValueSet(),
                StringLengthValueSet.PROPERTY_MAXIMUMLENGTH,
                messages)) {
            Integer maxLength = maxLengthValue == null ? null : Integer.parseInt(maxLengthValue);
            if (maxLength != null && maxLength < 0) {
                messages.newError(StringLengthValueSet.MSGCODE_NEGATIVE_VALUE,
                        Messages.StringLength_msgNegativeValue, StringLengthValueSet.PROPERTY_MAXIMUMLENGTH);
            }
        }

        return messages;
    }

}
